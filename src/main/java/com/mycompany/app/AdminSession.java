package com.mycompany.app;

import java.util.List;
import java.util.Scanner;

import org.mindrot.jbcrypt.BCrypt;

public class AdminSession {
    public static boolean runAdminSession(Scanner scanner, Admin admin) {
        boolean continueLoop = true;
        boolean isSuperAdmin = admin.getRole().equalsIgnoreCase("superadmin");

        while(continueLoop) {
            int adminActionChoice = 0;
            int maxChoice = isSuperAdmin ? 8 : 6;
            String exitChoice = "";

            
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Manage Books (Add/Update/Delete)");
            System.out.println("2. Manage Members (Add/Update/Delete)");
            System.out.println("3. View all members");
            System.out.println("4. View all borrowed books");
            System.out.println("5. View overdue books");

            if(isSuperAdmin) {
                System.out.println("6. Manage Admin (Add/Update/Delete)");
                System.out.println("7. View all Admin");
                System.out.println("8. Exit");
            } else {
                System.out.println("6. Exit");
            }

            while(adminActionChoice < 1 || adminActionChoice > maxChoice) {
                System.out.print("Enter your choice (1-" + maxChoice + "): ");
                if(scanner.hasNextInt()) {
                    adminActionChoice = scanner.nextInt();
                    scanner.nextLine();
                } else {
                    System.out.println("Please enter a number");
                    scanner.nextLine();
                }
            }
            
            if(adminActionChoice == maxChoice) {
                System.out.print("Are you sure you want to exit? Y/N: ");
                while(!(exitChoice.equalsIgnoreCase("Y")) && !(exitChoice.equalsIgnoreCase("N"))) {
                    exitChoice = scanner.nextLine();
                    if(!(exitChoice.equalsIgnoreCase("Y")) && !(exitChoice.equalsIgnoreCase("N"))) {
                        System.out.println("Invalid input, type in either 'y' or 'n'");
                    } else if(exitChoice.equalsIgnoreCase("Y")) {
                        System.out.println("Signing out...");
                        System.out.println("Goodbye!");
                        continueLoop = false;
                    } else if(exitChoice.equalsIgnoreCase("N")) {
                        System.out.println("Okay, continuing your session...");
                    }
                }
            } else {
                adminActions(adminActionChoice, scanner, isSuperAdmin);
            }
        }

        return continueLoop;
    }

    public static void adminActions(int adminActionChoice, Scanner scanner, boolean isSuperAdmin) {
        switch(adminActionChoice) {
            case 1:
                String bookActionChoice = addUpdateDeleteItem(scanner, "book");

                if(bookActionChoice.equalsIgnoreCase("Add")) {

                    boolean bookAdded = false;

                    while(!bookAdded) {
                        String title = InputValidator.verifyInputIsNotEmpty("Type in the title of the book: ", scanner);
                        String author = InputValidator.verifyInputIsNotEmpty("Type in the author of the book: ", scanner);
                        String isbn = InputValidator.verifyInputIsNotEmpty("Type in the ISBN of the book: ", scanner);

                        if(BookDao.getBookByIsbn(isbn) != null) {
                            System.out.println("That ISBN is already in the system. Please check your book and try again.");
                            continue;
                        }

                        int totalCopies = InputValidator.verifyInputIsNum("Enter the numerical value of the total copies currently purchased of the book: ", scanner, true);
                        int availableCopies = InputValidator.verifyInputIsNum("Enter the numerical value of the available copies of the book in the library: ", scanner, false);
     
                        while(availableCopies > totalCopies) {
                            System.out.println("Your available copies cannot be more than your total copies");
                            System.out.println("You entered you had " + availableCopies + " available copies and " + totalCopies + " total copies. Please fix it");
                            availableCopies = InputValidator.verifyInputIsNum("Enter the numerical value of the available copies of the book in the library: ", scanner, false);
                        }

                        Book book = new Book(title, author, isbn, totalCopies, availableCopies);
                        DaoResult addBookResult = BookDao.addBook(book);

                        switch(addBookResult) {
                            case SUCCESS -> bookAdded = true;
                            case DUPLICATE_KEY -> {
                                System.out.println("That ISBN is already in the system. Please check your book and try again.");    
                                System.out.println("Let's start over with the book's details."); 
                            }
                            case DATABASE_ERROR -> System.out.println("Something went wrong");
                        }
                    }
                } else if(bookActionChoice.equalsIgnoreCase("Update")) {

                    List<Book> searchBooksToUpdate = searchForBooks(scanner);

                    Book updateBook = findBookByIdInput(searchBooksToUpdate, scanner, "update");

                    boolean bookUpdateSucceeded = false;
                    while(!bookUpdateSucceeded) {
                        System.out.println("What would you like to update?");
                        System.out.println("To update title, press 'T'");
                        System.out.println("To update author, press 'A'");
                        System.out.println("To update isbn, press 'I'");
                        System.out.println("To update total copies, press 'TC'");
                        System.out.println("To update available copies, press 'AC'");
                        updateBook = changeBookDetails(updateBook, scanner);
                        DaoResult updateResult = BookDao.updateBook(updateBook);
    
                        switch(updateResult) {
                            case SUCCESS -> {
                                bookUpdateSucceeded = true;
                                System.out.println("Successful update.");              
                            }
                            case DUPLICATE_KEY -> System.out.println("That ISBN is already taken. Please try a different one.");
                            case DATABASE_ERROR -> System.out.println("Something went wrong updating the book. Please try again.");
                        }
                    }
                } else if(bookActionChoice.equalsIgnoreCase("Delete")) {
                    List<Book> searchBooksToDelete = searchForBooks(scanner);

                    Book book = findBookByIdInput(searchBooksToDelete, scanner, "delete");
                    List<BorrowedBook> activeBorrowsOfBook = BorrowedBookDao.getBorrowedBooksByBookId(book.getId());
                    
                    if(!activeBorrowsOfBook.isEmpty()) {
                        System.out.println("This book is currently checked out and cannot be deleted.");
                    } else {
                        BookDao.deleteBook(book.getId());
                    }
                }
                break;
            case 2:
                String memberActionChoice = addUpdateDeleteItem(scanner, "member");
                String emailInput = "";

                if(memberActionChoice.equalsIgnoreCase("Add")){

                    String name = InputValidator.verifyInputIsNotEmpty("Type in the member's full name: ", scanner);
                    boolean memberAdded = false;
                    while(!memberAdded) {
                        emailInput = InputValidator.verifyInputIsEmail(scanner);

                        if(MemberDao.getMemberByEmail(emailInput) != null) {
                            System.out.println("That email is already taken. Please choose a different one.");
                            continue;
                        } 

                        String[] result = gettingHashedTempPassword();
                        String hashedPassword = result[0];
                        String tempPassword = result[1];
                        Member member = new Member(name, emailInput, hashedPassword, true);
                        DaoResult addMemberResult = MemberDao.addMember(member);

                        switch(addMemberResult) {
                            case SUCCESS -> {
                                memberAdded = true;
                                System.out.println("Temporary password: " + tempPassword);
                                System.out.println("Give this to the new member directly. They'll be required to change it on first login.");
                            }
                            case DUPLICATE_KEY -> System.out.println("That email is already taken. Please choose a different one.");
                            case DATABASE_ERROR -> System.out.println("Something went wrong adding the member. Please try again.");
                        } 
                    }
                }else if (memberActionChoice.equalsIgnoreCase("Update")) {
                    System.out.println("Let's search the member you'd like to update: ");
                    Member updateMember = searchMembersAndDisplay(scanner, "update");

                    boolean updatingMemberSucceeded = false;
                    while(!updatingMemberSucceeded) {
                        System.out.println("What member info would you like to update?");
                        System.out.println("To update name, press 'N'");
                        System.out.println("To update email, press 'E'");
                        System.out.println("To reset password, press 'P'");
                        updateMember = changeMemberDetails(updateMember, scanner);
                        DaoResult updateMemberResult = MemberDao.updateMember(updateMember);

                        switch(updateMemberResult) {
                            case SUCCESS -> {
                                updatingMemberSucceeded = true;
                                System.out.println("Successful update");
                            }
                            case DUPLICATE_KEY -> System.out.println("The email is already taken. Please try a different one");
                            case DATABASE_ERROR -> System.out.println("Something went wrong updating the member. Please try again.");
                        }
                    }

                }else if(memberActionChoice.equalsIgnoreCase("Delete")) {
                    System.out.println("Let's search the member you'd like to delete: ");
                    Member deleteMember = searchMembersAndDisplay(scanner, "delete");
                    List<BorrowedBook> listOfBorrowedBooks = BorrowedBookDao.getBorrowedBooksByMember(deleteMember.getId());

                    if(!listOfBorrowedBooks.isEmpty()) {
                        System.out.println("This member has active borrowed books and cannot be deleted");
                    } else {
                        MemberDao.deleteMember(deleteMember.getId());
                    }
                    
                }
                break;
            case 3: 
                System.out.println("Here are all the members in the Library Database:");
                List<Member> allMembers = MemberDao.getAllMembers();
                printAllMembers(allMembers);
                break;
            case 4:
                System.out.println("Here are all the books that are checked out: ");
                List<BorrowedBook> allBorrowedBooks = BorrowedBookDao.getAllBorrowedBooks();
                printBorrowedBooks(allBorrowedBooks);
                break;
            case 5:
                System.out.println("Here are all the overdue books: ");
                List<BorrowedBook> allOverdueBooks = BorrowedBookDao.getOverdueBooks();
                printBorrowedBooks(allOverdueBooks);
                break;
            case 6: 
                if(isSuperAdmin) {
                    String changingAdminActionChoice = addUpdateDeleteItem(scanner, "admin");
                    
                    if(changingAdminActionChoice.equalsIgnoreCase(("Add"))) {
                        boolean adminAdded = false;

                        while(!adminAdded) {
                            String username = InputValidator.verifyInputIsNotEmpty("Enter admin's username: ", scanner);
    
                            if(AdminDao.getAdminByUsername(username) != null) {
                                System.out.println("That username is already taken. Please choose a different one.");
                                continue;
                            } 
    
                            String name = InputValidator.verifyInputIsNotEmpty("Enter admin's full name: ", scanner);
        
                            String[] result = gettingHashedTempPassword();
                            String hashedPassword = result[0];
                            String tempPassword = result[1];
        
                            Admin newAdmin = new Admin(username, hashedPassword, name);
                            DaoResult addResult = AdminDao.addAdmin(newAdmin);
                            
                            switch(addResult) {
                                case SUCCESS -> {
                                    adminAdded = true;
                                    System.out.println("Temporary password: " + tempPassword);
                                    System.out.println("Give this to the new admin directly. They'll be required to change it on first login.");
                                }
                                case DUPLICATE_KEY -> System.out.println("That username is already taken. Please choose a different one.");
                                case DATABASE_ERROR ->System.out.println("Something went wrong adding the admin. Please try again.");

                            }
                        }

                    }else if(changingAdminActionChoice.equalsIgnoreCase("Update")) {
                        System.out.println("Let's search the admin you'd like to update: ");
                        Admin updateAdmin = searchAdminsAndDisplayOne(scanner, "update");

                        boolean updateSucceeded = false;
                        while(!updateSucceeded) {
                            System.out.println("What admin info would you like to update?");
                            System.out.println("To update name, press 'N'");
                            System.out.println("To update username, press 'U'");
                            System.out.println("To update password, press 'P'");
                            updateAdmin = changeAdminDetails(updateAdmin, scanner);
                            DaoResult updateResult = AdminDao.updateAdmin(updateAdmin);

                            switch(updateResult) {
                                case SUCCESS -> {
                                    updateSucceeded = true;
                                    System.out.println("Successful update.");                              
                                }
                                case DUPLICATE_KEY -> System.out.println("That username is already taken. Please try a different one.");
                                case DATABASE_ERROR -> System.out.println("Something went wrong updating the admin. Please try again.");
                            }
                        }
                    }else if(changingAdminActionChoice.equalsIgnoreCase("Delete")) {
                        System.out.println("Let's search the admin you'd like to delete.");
                        Admin deleteAdmin = searchAdminsAndDisplayOne(scanner, "delete");
                        
                        if (deleteAdmin.getRole().equalsIgnoreCase("superadmin")) {
                            System.out.println("The superadmin account cannot be deleted.");
                        } else {
                            AdminDao.deleteAdmin(deleteAdmin.getId());
                        }
                    }
                } else {
                    System.out.println("Access denied: this action requires superadmin privileges.");
                }
                break;
            case 7:
                if (isSuperAdmin) {
                    System.out.println("Here are all the admins of the Library Database:");
                    List<Admin> allAdmins = AdminDao.getAllAdmins();
                    printAllAdmins(allAdmins);
                } else {
                    System.out.println("Access denied: this action requires superadmin privileges.");
                }
                break;
            default:
                System.out.println("Invalid option");
        }
    }

    public static String addUpdateDeleteItem(Scanner scanner, String item) {
        String choice = "";
        while(!(choice.equalsIgnoreCase("Add")) && !(choice.equalsIgnoreCase("Update")) && !(choice.equalsIgnoreCase("Delete"))) {
            choice = "";

            System.out.print("Type 'Add' to add a " + item + ", 'Update' to update a " + item + ", and 'Delete' to delete a " + item + ": ");
            choice = scanner.nextLine();

            if(!(choice.equalsIgnoreCase("Add")) && !(choice.equalsIgnoreCase("Update")) && !(choice.equalsIgnoreCase("Delete"))) {
                System.out.println("Sorry, we didn't recognize what you wrote, try again");
            }
        }

        return choice;
    }

    public static List<Book> searchForBooks(Scanner scanner) {
        System.out.println("We need to first search for the book you're looking for...");
        List<Book> searchBooksToUpdate = null;

        while(searchBooksToUpdate == null || searchBooksToUpdate.isEmpty()) {
            searchBooksToUpdate = MemberSession.searchBooks(scanner);
            MemberSession.printBookResults(searchBooksToUpdate);

            if(searchBooksToUpdate == null || searchBooksToUpdate.isEmpty()) {
                System.out.println("No books found, please try your search again");
            }
        }

        return searchBooksToUpdate;
    }

    public static Member searchMembersAndDisplay(Scanner scanner, String action) {
        String searchOption = "";
        String emailInput = "";
        String nameInput = "";
        Member foundMemberByEmail = null;
        List<Member> listOfMembers = null;
        boolean validInput = false;

        while(true) {
            searchOption = "";
            emailInput = "";
            nameInput = "";
            foundMemberByEmail = null;
            listOfMembers = null;
            int memberId = 0;
            
            while(!(searchOption.equalsIgnoreCase("Name")) && !(searchOption.equalsIgnoreCase("Email"))) {
                System.out.print("Would you like to search for members by name or email?: ");
                searchOption = scanner.nextLine();
                if(!(searchOption.equalsIgnoreCase("Name")) && !(searchOption.equalsIgnoreCase("Email"))) {
                    System.out.println("Please enter either 'name' or 'email': ");
                }
            }

            if(searchOption.equalsIgnoreCase("Email")) {
                emailInput = InputValidator.verifyInputIsEmail(scanner);
                foundMemberByEmail= MemberDao.getMemberByEmail(emailInput);

                if(foundMemberByEmail != null) {
                    System.out.println("We found the member");
                    System.out.println("ID: " + foundMemberByEmail.getId() + " | Name: " + foundMemberByEmail.getName() + " | Email: " + foundMemberByEmail.getEmail());
                    return foundMemberByEmail;
                } else {
                    System.out.println("We cannot find any members by the email you entered. Try again.");
                }
    
    
            } else if(searchOption.equalsIgnoreCase("Name")) {
                while(nameInput.equals("")){
                    System.out.print("Enter the name of the member (or part of their full name): ");
                    nameInput = scanner.nextLine();
                    if(nameInput.equals("")) {
                        System.out.println("Cannot be empty");
                    }
                }
    
                listOfMembers = MemberDao.searchByName(nameInput);

                if(listOfMembers != null && !listOfMembers.isEmpty()) {
                    System.out.println("Here's a list of members:");
                    for(Member oneMember : listOfMembers) {
                        System.out.println("ID: " + oneMember.getId() + " | Name: " + oneMember.getName() + "| Email: " + oneMember.getEmail());
                    }
                    
                    while(!validInput) {
                        validInput = false;
                        System.out.print("Enter the ID of the member you'd like to " + action + ": ");
                        if(scanner.hasNextInt()) {
                            memberId = scanner.nextInt();
                            scanner.nextLine();
                            for(Member checkMember: listOfMembers) {
                                if(checkMember.getId() == memberId) {
                                    return checkMember;
                                }
                            }
    
                            if(!validInput) {
                                System.out.println("That ID wasn't in the search results, try again");
                            }
                        } else {
                            System.out.println("Please enter a number");
                            scanner.nextLine();
                        }
                    }
                }else {
                    System.out.println("We cannot find any members by the name you entered. Try again.");
                }
            }

        }
    }

    public static Admin searchAdminsAndDisplayOne(Scanner scanner, String action) {

        List<Admin> listOfAdmins = AdminDao.getAllAdmins();
        System.out.println("Here are all the admins in the system:");
        for(Admin oneAdmin : listOfAdmins) {
            System.out.println("ID: " + oneAdmin.getId() + " | Name: " + oneAdmin.getName() + "| Username: " + oneAdmin.getUsername() + "| Role: " + oneAdmin.getRole() + "| Need to Change Temp Password?: " + oneAdmin.getPasswordChangeStatus());
        }

        boolean continueLoop = true;
        Admin admin = null;
        while(continueLoop) {
            continueLoop = true;
            admin = null;

            System.out.print("Enter the id of the admin you'd like to " + action + ": ");
            int idUpdateAdmin = 0;
            if (scanner.hasNextInt()) {
                idUpdateAdmin = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Please enter a number");
                scanner.nextLine();
                continue;
            }
            admin = AdminDao.getAdminById(idUpdateAdmin);

            if(admin == null) {
                System.out.println("An incorrect id for an admin was entered. Look at the list of admin and try again.");
            }else {
                System.out.println("Here's the admin you choose: ");
                System.out.println("ID: " + admin.getId() + "| Name: " + admin.getName());
                System.out.print("Is this the admin whose information needs to be " + action + "d? Type yes or no: ");
                String confirmation = scanner.nextLine();
    
                if(confirmation.equalsIgnoreCase("yes")) {
                    continueLoop = false;
                }else if(confirmation.equalsIgnoreCase("no")) {
                    continueLoop = true;
                    System.out.println("Let's try again...");
                } else {
                    System.out.println("Please type yes or no.");
                }
            }
        }
        return admin;
    }

    public static Book findBookByIdInput(List<Book> listOfBooksSearched, Scanner scanner, String action) {
        int bookId = 0;
    
        while(true) {
            bookId = 0;

            System.out.print("Enter the ID of the book you'd like to " + action + ": ");
            if(scanner.hasNextInt()) {
                bookId = scanner.nextInt();
                scanner.nextLine();
                for(Book book: listOfBooksSearched) {
                    if(book.getId() == bookId) {
                        return book;
                    }
                }

            } else {
                System.out.println("Please enter a number");
                scanner.nextLine();
            }

            System.out.println("That ID wasn't in the search results, try again");
        }
    }

    public static Book changeBookDetails(Book book, Scanner scanner) {
        String input = "";
        while(!(input.equalsIgnoreCase("T")) && !(input.equalsIgnoreCase("A")) && !(input.equalsIgnoreCase("I")) && !(input.equalsIgnoreCase("TC")) && !(input.equalsIgnoreCase("AC"))) {
            System.out.print("Enter the symbol: ");
            input = scanner.nextLine();

            if(!(input.equalsIgnoreCase("T")) && !(input.equalsIgnoreCase("A")) && !(input.equalsIgnoreCase("I")) && !(input.equalsIgnoreCase("TC")) && !(input.equalsIgnoreCase("AC"))) {
                System.out.println("You did not input the correct value. Try again.");
            }
        }

        if(input.equalsIgnoreCase("T")) {
            String newTitle = InputValidator.verifyInputIsNotEmpty("Your title of the selected book is currently " + book.getTitle() + ". What would you like to change the book title to?: ", scanner);
            book.setTitle(newTitle);
            System.out.println("Title is now set to " + book.getTitle() + ". Saving...");
        }else if(input.equalsIgnoreCase("A")){
            String newAuthor = InputValidator.verifyInputIsNotEmpty("Your author of the selected book is currently " + book.getAuthor() + ". What would you like to change the book author to?: ", scanner);
            book.setAuthor(newAuthor);
            System.out.println("Author is now set to " + book.getAuthor() + ". Saving...");
        }else if(input.equalsIgnoreCase("I")){
            String newISBN = InputValidator.verifyInputIsNotEmpty("Your book ISBN is currently "  + book.getIsbn() + ". What would you like to change the book ISBN to?: ", scanner);
            book.setIsbn(newISBN);
            System.out.println("ISBN is now set to " + book.getIsbn() + ". Saving...");
        }else if(input.equalsIgnoreCase("TC")){
            int newTotalCopyValue = -1;
            while(newTotalCopyValue < book.getAvailableCopies()) {
                newTotalCopyValue = -1;

                newTotalCopyValue = InputValidator.verifyInputIsNum("Enter what you would like to change the book's total copies number to: ", scanner, true);
                if(newTotalCopyValue < book.getAvailableCopies()) {
                    System.out.println("Your total copies value is too low compared to the available copies in the database");
                    System.out.println("The total copies you entered was " + newTotalCopyValue + " and the available copies of the book in the database base is " + book.getAvailableCopies()); 
                    System.out.println("Try again.");                 
                }

            }
            
            book.setTotalCopies(newTotalCopyValue);
            System.out.println("Total copies are now set to " + book.getTotalCopies() + ". Saving...");
        }else if(input.equalsIgnoreCase("AC")){
            int newAvailValue = book.getTotalCopies() + 1;
            while(newAvailValue > book.getTotalCopies()) {
                newAvailValue = -1;

                newAvailValue = InputValidator.verifyInputIsNum("Enter what you would like to change the book's available copies number to: ", scanner, false);
                if(newAvailValue > book.getTotalCopies()) {
                    System.out.println("Your available copies is too high when compared ");
                    System.out.println("The available copies you entered was " + newAvailValue + " and the total copies of the book in the database base is " + book.getTotalCopies()); 
                    System.out.println("Try again.");                 
                }

            }
            book.setAvailableCopies(newAvailValue);
            System.out.println("Available copies are now set to " + book.getAvailableCopies() + ". Saving...");
        }

        return book;
    }

    public static Member changeMemberDetails(Member member, Scanner scanner) {
        String input = "";
        while(!(input.equalsIgnoreCase("N")) && !(input.equalsIgnoreCase("E")) && !(input.equalsIgnoreCase("P"))) {
            System.out.print("Enter the symbol: ");
            input = scanner.nextLine();

            if(!(input.equalsIgnoreCase("N")) && !(input.equalsIgnoreCase("E")) && !(input.equalsIgnoreCase("P"))) {
                System.out.println("You did not input the correct value. Try again.");
            }
        }

        if(input.equalsIgnoreCase("N")) {
            String newName = InputValidator.verifyInputIsNotEmpty("Your member is currently called " + member.getName() + " . What would you like to change it to?: ", scanner);
            member.setName(newName);
            System.out.println("Name set to " + member.getName() + ". Saving...");
        }else if(input.equalsIgnoreCase("E")){
            System.out.println("Member's email is now set to " + member.getEmail() + " . What would you like to change it to?");
            String newEmail = InputValidator.verifyInputIsEmail(scanner);
            member.setEmail(newEmail);
            System.out.println("Email is now set to " + member.getEmail() + ". Saving...");
        } else if(input.equalsIgnoreCase("P")) {
            String[] result = gettingHashedTempPassword();
            String hashedPassword = result[0];
            String tempPassword = result[1];
    
            member.setPassword(hashedPassword);
            member.setPasswordChangeStatus(true);
            System.out.println("Password reset. Temporary password: " + tempPassword);
            System.out.println("Give this to the member directly. They'll be required to change it on next login.");
        }

        return member;
    }

    public static Admin changeAdminDetails(Admin admin, Scanner scanner) {
        String input = "";
        while(!(input.equalsIgnoreCase("N")) && !(input.equalsIgnoreCase("U")) && !(input.equalsIgnoreCase("P"))) {
            System.out.print("Enter the symbol: ");
            input = scanner.nextLine();

            if(!(input.equalsIgnoreCase("N")) && !(input.equalsIgnoreCase("U")) && !(input.equalsIgnoreCase("P"))) {
                System.out.println("You did not input the correct value. Try again.");
            }
        }

        if(input.equalsIgnoreCase("N")) {
            String newName = InputValidator.verifyInputIsNotEmpty("The admin is currently called " + admin.getName() + ". What would you like to change it to?: ", scanner);
            admin.setName(newName);
            System.out.println("Name set to " + admin.getName() + ". Saving...");
        }else if(input.equalsIgnoreCase("U")){
            String newUsername = InputValidator.verifyInputIsNotEmpty("The admin's username is currently " + admin.getUsername() + ". What would you like to change it to?: ", scanner);
            admin.setUsername(newUsername);
            System.out.println("Username set to " + admin.getUsername() + ". Saving...");
        }else if(input.equalsIgnoreCase("P")) {
            String[] result = gettingHashedTempPassword();
            String hashedPassword = result[0];
            String tempPassword = result[1];

            admin.setPassword(hashedPassword);
            admin.setPasswordChangeStatus(true);
            System.out.println("Password reset. Temporary password: " + tempPassword);
            System.out.println("Give this to the admin directly. They'll be required to change it on next login.");
        }

        return admin;
    }

    public static void printBorrowedBooks(List<BorrowedBook> listOfBooksObject) {
        for(BorrowedBook borrowedBook: listOfBooksObject) {
            Book book = BookDao.getBookById(borrowedBook.getBookId());
            Member member = MemberDao.getMemberId(borrowedBook.getMemberId());
            System.out.println("Book ID: " + borrowedBook.getBookId() + " | Title: " + book.getTitle() + " | Author: " + book.getAuthor() + " | Due Date: " + borrowedBook.getDueDate() + " | Member's ID: " + borrowedBook.getMemberId() + " | Member Name: " + member.getName());
        }
    }

    public static void printAllMembers(List<Member> listOfMemberObjects) {
        for(Member member: listOfMemberObjects) {
            List<BorrowedBook> booksBorrowedByMember = BorrowedBookDao.getBorrowedBooksByMember(member.getId());
            List<BorrowedBook> overdueBooksByMember = BorrowedBookDao.getOverdueBooksByMember(member.getId());
            System.out.println("Member ID: " + member.getId() + " | Name: " + member.getName() + " | Email: " + member.getEmail() + " | Number of Books Borrowed: " + booksBorrowedByMember.size() + " | Number of Overdue Books: " + overdueBooksByMember.size());
        }
    }

    public static void printAllAdmins(List<Admin> listOfAdminObjects) {
        for(Admin admin: listOfAdminObjects) {
            System.out.println("Admin ID: " + admin.getId() + " | Name: " + admin.getName() + " | Username: " + admin.getUsername() + " | Role: " + admin.getRole() + " | Password Change Needed?: " + admin.getPasswordChangeStatus());
        }
    }

    public static String[] gettingHashedTempPassword() {
        String tempPassword = PasswordUtil.generateTempPassword();
        String hashedPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt());
        return new String[]{hashedPassword, tempPassword};
    }
}
