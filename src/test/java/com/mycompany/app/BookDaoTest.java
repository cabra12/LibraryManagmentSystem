package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BookDaoTest extends AbstractDaoTest {

    private static final String DUNE_ISBN = "978-0441013593";
    private static final String NEUROMANCER_ISBN = "978-0441569595";

    @Nested
    class AddBookTests {

        @Test
        void insertsRowAndSetsGeneratedId() {
            Book book = new Book("Dune", "Frank Herbert", DUNE_ISBN, 5, 3);

            DaoResult result = BookDao.addBook(book);

            assertEquals(DaoResult.SUCCESS, result);
            assertTrue(book.getId() > 0);
            Map<String, Object> row = selectRow("SELECT * FROM books WHERE id = ?", book.getId());
            assertNotNull(row);
            assertEquals("Dune", row.get("title"));
            assertEquals("Frank Herbert", row.get("author"));
            assertEquals(DUNE_ISBN, row.get("isbn"));
            assertEquals(5, row.get("total_copies"));
            assertEquals(3, row.get("available_copies"));
        }

        @Test
        void duplicateIsbnReturnsDuplicateKey() {
            insertBook("Dune", "Frank Herbert", DUNE_ISBN, 1, 1);

            DaoResult result = BookDao.addBook(new Book("Another Dune", "Someone Else", DUNE_ISBN, 2, 2));

            assertEquals(DaoResult.DUPLICATE_KEY, result);
            assertEquals(1, countRows("books"));
        }
    }

    @Nested
    class GetAllBooksTests {

        @Test
        void emptyTableReturnsEmptyList() {
            List<Book> books = BookDao.getAllBooks();

            assertNotNull(books);
            assertTrue(books.isEmpty());
        }

        @Test
        void returnsEveryBookWithAllFields() {
            int duneId = insertBook("Dune", "Frank Herbert", DUNE_ISBN, 5, 3);
            insertBook("Neuromancer", "William Gibson", NEUROMANCER_ISBN, 2, 2);

            List<Book> books = BookDao.getAllBooks();

            Map<String, Book> byIsbn = books.stream().collect(Collectors.toMap(Book::getIsbn, b -> b));
            assertEquals(2, byIsbn.size());
            Book dune = byIsbn.get(DUNE_ISBN);
            assertNotNull(dune);
            assertEquals(duneId, dune.getId());
            assertEquals("Dune", dune.getTitle());
            assertEquals("Frank Herbert", dune.getAuthor());
            assertEquals(5, dune.getTotalCopies());
            assertEquals(3, dune.getAvailableCopies());
        }
    }

    @Nested
    class GetBookByIdTests {

        @Test
        void existingIdReturnsBookWithAllFields() {
            int id = insertBook("Dune", "Frank Herbert", DUNE_ISBN, 5, 3);

            Book book = BookDao.getBookById(id);

            assertNotNull(book);
            assertEquals(id, book.getId());
            assertEquals("Dune", book.getTitle());
            assertEquals("Frank Herbert", book.getAuthor());
            assertEquals(DUNE_ISBN, book.getIsbn());
            assertEquals(5, book.getTotalCopies());
            assertEquals(3, book.getAvailableCopies());
        }

        @Test
        void unknownIdReturnsNull() {
            assertNull(BookDao.getBookById(9999));
        }
    }

    @Nested
    class GetBookByIsbnTests {

        @Test
        void existingIsbnReturnsBook() {
            int id = insertBook("Dune", "Frank Herbert", DUNE_ISBN, 5, 3);

            Book book = BookDao.getBookByIsbn(DUNE_ISBN);

            assertNotNull(book);
            assertEquals(id, book.getId());
            assertEquals("Dune", book.getTitle());
        }

        @Test
        void unknownIsbnReturnsNull() {
            assertNull(BookDao.getBookByIsbn("000-0000000000"));
        }
    }

    @Nested
    class SearchByTitleTests {

        @Test
        void partialCaseInsensitiveMatchOnTitleOnly() {
            insertBook("Dune", "Frank Herbert", DUNE_ISBN, 1, 1);
            insertBook("DUNE Messiah", "Frank Herbert", "978-0441172696", 1, 1);
            insertBook("Neuromancer", "William Gibson", NEUROMANCER_ISBN, 1, 1);
            // "dun" appears in this author's name, not in the title, so it must not match
            insertBook("Other Book", "Dunn Smith", "111-1111111111", 1, 1);

            List<Book> results = BookDao.searchByTitle("dun");

            Set<String> titles = results.stream().map(Book::getTitle).collect(Collectors.toSet());
            assertEquals(Set.of("Dune", "DUNE Messiah"), titles);
        }

        @Test
        void noMatchReturnsEmptyList() {
            insertBook("Dune", "Frank Herbert", DUNE_ISBN, 1, 1);

            List<Book> results = BookDao.searchByTitle("zzz");

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    class SearchByAuthorTests {

        @Test
        void partialCaseInsensitiveMatchOnAuthorOnly() {
            insertBook("Neuromancer", "William Gibson", NEUROMANCER_ISBN, 1, 1);
            insertBook("Dune", "Frank Herbert", DUNE_ISBN, 1, 1);
            // "gib" appears in this title, not in the author, so it must not match
            insertBook("Gibson Guitars", "Someone Else", "222-2222222222", 1, 1);

            List<Book> results = BookDao.searchByAuthor("GIB");

            Set<String> titles = results.stream().map(Book::getTitle).collect(Collectors.toSet());
            assertEquals(Set.of("Neuromancer"), titles);
        }
    }

    @Nested
    class UpdateBookTests {

        @Test
        void allFieldsArePersisted() {
            int id = insertBook("Old Title", "Old Author", "333-3333333333", 5, 5);

            DaoResult result = BookDao.updateBook(new Book(id, "New Title", "New Author", "444-4444444444", 10, 7));

            assertEquals(DaoResult.SUCCESS, result);
            Map<String, Object> row = selectRow("SELECT * FROM books WHERE id = ?", id);
            assertEquals("New Title", row.get("title"));
            assertEquals("New Author", row.get("author"));
            assertEquals("444-4444444444", row.get("isbn"));
            assertEquals(10, row.get("total_copies"));
            assertEquals(7, row.get("available_copies"));
        }

        @Test
        void isbnOwnedByAnotherBookReturnsDuplicateKeyAndLeavesRowUnchanged() {
            insertBook("Dune", "Frank Herbert", DUNE_ISBN, 1, 1);
            int otherId = insertBook("Neuromancer", "William Gibson", NEUROMANCER_ISBN, 1, 1);

            DaoResult result = BookDao.updateBook(new Book(otherId, "Neuromancer", "William Gibson", DUNE_ISBN, 1, 1));

            assertEquals(DaoResult.DUPLICATE_KEY, result);
            Map<String, Object> row = selectRow("SELECT * FROM books WHERE id = ?", otherId);
            assertEquals(NEUROMANCER_ISBN, row.get("isbn"));
        }
    }

    @Nested
    class DeleteBookTests {

        @Test
        void bookWithNoLoansIsDeleted() {
            int id = insertBook("Dune", "Frank Herbert", DUNE_ISBN, 1, 1);

            BookDao.deleteBook(id);

            assertEquals(0, countRows("books"));
        }

        @Test
        void bookWithALoanIsBlockedByForeignKey() {
            int memberId = insertMember("Jane Doe", "jane@example.com", "hash", false);
            int bookId = insertBook("Dune", "Frank Herbert", DUNE_ISBN, 1, 0);
            insertBorrowedBook(bookId, memberId);

            BookDao.deleteBook(bookId);

            assertEquals(1, countRows("books"));
            assertEquals(1, countRows("borrowed_books"));
        }
    }
}
