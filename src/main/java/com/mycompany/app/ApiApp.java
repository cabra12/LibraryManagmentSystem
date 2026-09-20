package com.mycompany.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;

public class ApiApp {

    //who is calling: role is "member", "admin" or "superadmin"
    private record Caller(String role, int id, String name) {}

    //JSON body for adding or updating a book
    public static class BookRequest {
        public String title;
        public String author;
        public String isbn;
        public Integer totalCopies;
        public Integer availableCopies;
    }

    //A real bcrypt hash of a throwaway password. It's checked when the user doesn't exist,
    //so "unknown user" and "wrong password" take about the same time.
    private static final String DUMMY_HASH = "$2a$12$14W6/lLpmO81.dPHGoGxQOTFiM7r4rWj0iOUY6ZCgc2zbO8pIl4Me";

    //builds the server without starting it
    public static Javalin create() {
        return Javalin.create(config -> {
            //Javalin 7: routes must be registered here, before the server starts

            //every route except /health needs a login (HTTP Basic auth)
            config.routes.beforeMatched(ctx -> {
                if (ctx.path().equals("/health")) {
                    return;
                }
                ctx.attribute("caller", authenticate(ctx));
            });

            config.routes.get("/health", ctx -> ctx.json(Map.of("status", "ok")));

            // ---------- books ----------

            //list all books, or search with ?title=... or ?author=...
            config.routes.get("/books", ctx -> {
                String title = ctx.queryParam("title");
                String author = ctx.queryParam("author");

                if (!isBlank(title)) {
                    ctx.json(BookDao.searchByTitle(title));
                } else if (!isBlank(author)) {
                    ctx.json(BookDao.searchByAuthor(author));
                } else {
                    ctx.json(BookDao.getAllBooks());
                }
            });

            config.routes.get("/books/{id}", ctx -> ctx.json(findBook(ctx)));

            //add a book: admins only
            config.routes.post("/books", ctx -> {
                requireAdmin(ctx);
                BookRequest request = ctx.bodyAsClass(BookRequest.class);

                //if availableCopies is left out, every copy starts out available
                if (request.availableCopies == null && request.totalCopies != null) {
                    request.availableCopies = request.totalCopies;
                }
                validate(request);

                Book book = new Book(request.title, request.author, request.isbn, request.totalCopies, request.availableCopies);
                checkResult(BookDao.addBook(book), "A book with that ISBN already exists");

                ctx.status(201).json(book);
            });

            //replace a book's details: admins only
            config.routes.put("/books/{id}", ctx -> {
                requireAdmin(ctx);
                Book existing = findBook(ctx);
                BookRequest request = ctx.bodyAsClass(BookRequest.class);
                validate(request);

                Book updated = new Book(existing.getId(), request.title, request.author, request.isbn, request.totalCopies, request.availableCopies);
                checkResult(BookDao.updateBook(updated), "That ISBN belongs to another book");

                ctx.json(updated);
            });

            //delete a book: admins only
            config.routes.delete("/books/{id}", ctx -> {
                requireAdmin(ctx);
                Book book = findBook(ctx);

                if (!BorrowedBookDao.getBorrowedBooksByBookId(book.getId()).isEmpty()) {
                    throw new ConflictResponse("This book is currently checked out and cannot be deleted");
                }

                BookDao.deleteBook(book.getId());

                //deleteBook doesn't say whether it worked, so look again:
                //the database refuses the delete if the book has any loan history
                if (BookDao.getBookById(book.getId()) != null) {
                    throw new ConflictResponse("This book has borrowing history and cannot be deleted");
                }
                ctx.status(204);
            });

            // ---------- loans ----------

            //borrow a book: members only
            config.routes.post("/books/{id}/borrow", ctx -> {
                Caller caller = requireMember(ctx);
                Book book = findBook(ctx);

                Set<Integer> loansBefore = loanIds(BorrowedBookDao.getBorrowedBooksByMember(caller.id()));
                BorrowedBookDao.borrowBook(book.getId(), caller.id());

                //borrowBook doesn't report what happened, so look for the new loan
                BorrowedBook created = null;
                for (BorrowedBook loan : BorrowedBookDao.getBorrowedBooksByMember(caller.id())) {
                    if (!loansBefore.contains(loan.getId())) {
                        created = loan;
                    }
                }
                if (created == null) {
                    throw new ConflictResponse("Could not borrow this book (no copies available)");
                }
                ctx.status(201).json(loanToMap(created));
            });

            //return a book: members only, and only their own active loans
            config.routes.post("/loans/{id}/return", ctx -> {
                Caller caller = requireMember(ctx);
                int loanId = pathId(ctx);

                if (!loanIds(BorrowedBookDao.getBorrowedBooksByMember(caller.id())).contains(loanId)) {
                    throw new NotFoundResponse("Active loan not found");
                }

                BorrowedBookDao.returnBook(loanId);

                if (loanIds(BorrowedBookDao.getBorrowedBooksByMember(caller.id())).contains(loanId)) {
                    throw new InternalServerErrorResponse("Could not return the book");
                }
                ctx.json(Map.of("message", "Book returned"));
            });

            //my active loans: members only
            config.routes.get("/me/loans", ctx -> {
                Caller caller = requireMember(ctx);
                ctx.json(loansToMaps(BorrowedBookDao.getBorrowedBooksByMember(caller.id())));
            });

            //all active loans: admins only
            config.routes.get("/loans", ctx -> {
                requireAdmin(ctx);
                ctx.json(loansToMaps(BorrowedBookDao.getAllBorrowedBooks()));
            });

            //overdue loans: admins only
            config.routes.get("/loans/overdue", ctx -> {
                requireAdmin(ctx);
                ctx.json(loansToMaps(BorrowedBookDao.getOverdueBooks()));
            });
        });
    }

    // ---------- login check ----------

    private static Caller authenticate(Context ctx) {
        var credentials = ctx.basicAuthCredentials();
        if (credentials == null) {
            throw unauthorized(ctx, "Log in with HTTP Basic authentication");
        }

        String identifier = credentials.getUsername();
        String password = credentials.getPassword();
        if (isBlank(identifier) || password == null) {
            throw unauthorized(ctx, "Invalid credentials");
        }

        //the console app requires member emails to contain '@', so that tells the two kinds of login apart
        if (identifier.contains("@")) {
            Member member = MemberDao.getMemberByEmail(identifier);

            if (member == null) {
                BCrypt.checkpw(password, DUMMY_HASH);
                throw unauthorized(ctx, "Invalid credentials");
            }
            if (!BCrypt.checkpw(password, member.getPassword())) {
                throw unauthorized(ctx, "Invalid credentials");
            }
            if (member.getPasswordChangeStatus()) {
                throw new ForbiddenResponse("You must set a new password first. Log in through the console app to do that.");
            }
            return new Caller("member", member.getId(), member.getName());
        }

        Admin admin = AdminDao.logInWithUsername(identifier);

        if (admin == null) {
            BCrypt.checkpw(password, DUMMY_HASH);
            throw unauthorized(ctx, "Invalid credentials");
        }
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            throw unauthorized(ctx, "Invalid credentials");
        }
        if (admin.getPasswordChangeStatus()) {
            throw new ForbiddenResponse("You must set a new password first. Log in through the console app to do that.");
        }
        //the role comes from the database: "admin" or "superadmin"
        return new Caller(admin.getRole(), admin.getId(), admin.getName());
    }

    //the header tells browsers to show a username/password prompt
    private static UnauthorizedResponse unauthorized(Context ctx, String message) {
        ctx.header("WWW-Authenticate", "Basic realm=\"Library API\"");
        return new UnauthorizedResponse(message);
    }

    // ---------- helpers ----------

    private static Caller requireMember(Context ctx) {
        Caller caller = ctx.attribute("caller");
        if (!caller.role().equals("member")) {
            throw new ForbiddenResponse("This action is for members only");
        }
        return caller;
    }

    private static Caller requireAdmin(Context ctx) {
        Caller caller = ctx.attribute("caller");
        if (caller.role().equals("member")) {
            throw new ForbiddenResponse("This action requires an admin");
        }
        return caller;
    }

    private static int pathId(Context ctx) {
        //a non-number here becomes a 400 automatically
        return ctx.pathParamAsClass("id", Integer.class).get();
    }

    private static Book findBook(Context ctx) {
        Book book = BookDao.getBookById(pathId(ctx));
        if (book == null) {
            throw new NotFoundResponse("Book not found");
        }
        return book;
    }

    private static void validate(BookRequest request) {
        if (isBlank(request.title) || isBlank(request.author) || isBlank(request.isbn)
                || request.totalCopies == null || request.availableCopies == null) {
            throw new BadRequestResponse("title, author, isbn, totalCopies and availableCopies are required");
        }
        if (request.isbn.length() > 20) {
            throw new BadRequestResponse("isbn can be at most 20 characters");
        }
        if (request.totalCopies < 1 || request.availableCopies < 0 || request.availableCopies > request.totalCopies) {
            throw new BadRequestResponse("totalCopies must be at least 1, and availableCopies must be between 0 and totalCopies");
        }
    }

    private static void checkResult(DaoResult result, String duplicateMessage) {
        if (result == DaoResult.DUPLICATE_KEY) {
            throw new ConflictResponse(duplicateMessage);
        }
        if (result == DaoResult.DATABASE_ERROR) {
            throw new InternalServerErrorResponse("Database error");
        }
    }

    private static Set<Integer> loanIds(List<BorrowedBook> loans) {
        Set<Integer> ids = new HashSet<>();
        for (BorrowedBook loan : loans) {
            ids.add(loan.getId());
        }
        return ids;
    }

    //dates become plain "yyyy-MM-dd" text, so the JSON doesn't depend on a date library
    private static Map<String, Object> loanToMap(BorrowedBook loan) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", loan.getId());
        map.put("bookId", loan.getBookId());
        map.put("memberId", loan.getMemberId());
        map.put("borrowDate", loan.getBorrowDate().toString());
        map.put("dueDate", loan.getDueDate().toString());
        map.put("returnDate", loan.getReturnDate() == null ? null : loan.getReturnDate().toString());
        return map;
    }

    private static List<Map<String, Object>> loansToMaps(List<BorrowedBook> loans) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (BorrowedBook loan : loans) {
            result.add(loanToMap(loan));
        }
        return result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static void main(String[] args) {
        //127.0.0.1 = only reachable from your own machine (safe default).
        //Docker will set API_HOST=0.0.0.0 so the port can be published.
        String host = System.getenv().getOrDefault("API_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("API_PORT", "8080"));

        create().start(host, port);
    }
}