package com.mycompany.app;

import java.util.List;
import java.util.Scanner;

public class AdminSession {
    public static boolean runAdminSession(Scanner scanner) {
        boolean continueLoop = true;
        int adminActionChoice = 0;

        while(continueLoop) {
            String exitChoice = "";
            adminActionChoice = 0;

            
            System.out.println("What would you like to do?");
            System.out.println("1. Add/Update/Delete Books");
            System.out.println("2. Add/Update/Delete Members");
            System.out.println("3. View all borrowed books");
            System.out.println("4. View overdue books");
            System.out.println("5. Exit");

            while(adminActionChoice < 1 || adminActionChoice > 5) {
                System.out.print("Enter your choice (1, 2, 3, 4, or 5): ");
                if(scanner.hasNextInt()) {
                    adminActionChoice = scanner.nextInt();
                    scanner.nextLine();
                } else {
                    System.out.println("Please enter a number");
                    scanner.nextLine();
                }
            }
            
            if(adminActionChoice == 5) {
                System.out.println("Are you sure you want to exit? Y/N: ");
                while(!(exitChoice.equalsIgnoreCase("Y")) && !(exitChoice.equalsIgnoreCase("N"))) {
                    exitChoice = scanner.nextLine();
                    if(!(exitChoice.equalsIgnoreCase("Y")) && !(exitChoice.equalsIgnoreCase("N"))) {
                        System.out.println("Invalid input, type in either 'y' or 'n'");
                    }else if(exitChoice.equalsIgnoreCase("Y")) {
                        System.out.println("Signing out...");
                        System.out.println("Goodbye!");
                        continueLoop = false;
                    }
                }
                
            } else {
                adminActions(adminActionChoice, scanner);
            }
        }

        return continueLoop;
    }

    public static void adminActions(int adminActionChoice, Scanner scanner) {
        switch(adminActionChoice) {
            case 1:
                String bookActionChoice = addUpdateDeleteItem(scanner, "book");

                if(bookActionChoice.equalsIgnoreCase("Add")) {
                    System.out.print("Type in the title of the book: ");
                    String title = scanner.nextLine();
                    System.out.print("Type in the author of the book: ");
                    String author = scanner.nextLine();
                    System.out.print("Type in the ISBN of the book: ");
                    String isbn = scanner.nextLine();
                    int totalCopies = verifyInputIsNum("Enter the numerical value of the total copies currently purchased of the book: ", scanner, true);
                    int availableCopies = verifyInputIsNum("Enter the numerical value of the available copies of the book in the library: ", scanner, false);
 
                    while(availableCopies > totalCopies) {
                        System.out.println("Your available copies cannot be more than your total copies");
                        System.out.println("You entered you had " + availableCopies + " available copies and " + totalCopies + " total copies. Please fix it");
                        availableCopies = verifyInputIsNum("Enter the numerical value of the available copies of the book in the library: ", scanner, false);
                    }
 

                    Book book = new Book(title, author, isbn, totalCopies, availableCopies);
                    BookDao.addBook(book);
                } else if(bookActionChoice.equalsIgnoreCase("Update")) {

                    List<Book> searchBooksToUpdate = searchForBooks(scanner);

                    Book book = findBookByIdInput(searchBooksToUpdate, scanner, "update");
                    System.out.println("What would you like to update?");
                    System.out.println("To update title, press 'T'");
                    System.out.println("To update author, press 'A'");
                    System.out.println("To update isbn, press 'I'");
                    System.out.println("To update total copies, press 'TC'");
                    System.out.println("To update available copies, press 'AC'");
                    book = changeBookDetails(book, scanner);
                    BookDao.updateBook(book);

                } else if(bookActionChoice.equalsIgnoreCase("Delete")) {
                    List<Book> searchBooksToDelete = searchForBooks(scanner);

                    Book book = findBookByIdInput(searchBooksToDelete, scanner, "delete");
                    BookDao.deleteBook(book.getId());
                }
                
                break;
            case 2:
                String memberActionChoice = addUpdateDeleteItem(scanner, "member");
                String emailInput = "";

                if(memberActionChoice.equalsIgnoreCase("Add")){
                    System.out.print("Type in the member's full name: ");
                    String name = scanner.nextLine();
                    
                    emailInput = verifyInputIsEmail(scanner);

                    Member member = new Member(name, emailInput);
                    MemberDao.addMember(member);
                }else if (memberActionChoice.equalsIgnoreCase("Update")) {
                    Member updateMember = searchMembersAndDisplay(scanner);
                    System.out.println("What member info would you like to update?");
                    System.out.println("To update name, press 'N'");
                    System.out.println("To update email, press 'E'");
                    updateMember = changeMemberDetails(updateMember, scanner);
                    MemberDao.updateMember(updateMember);
                }
                break;
            case 3:
                
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

    public static int verifyInputIsNum(String request, Scanner scanner, boolean numCannotBeZero) {
        boolean validInput = false;
        int numVar = 0;

        while(!validInput) {

            validInput = false;
            System.out.print(request);
            if(scanner.hasNextInt()) {
                numVar = scanner.nextInt();
                scanner.nextLine();
                if(numVar <= 0 && numCannotBeZero) {
                    System.out.println("This value cannot be less than 1");
                } else if(numVar < 0) {
                    System.out.println("This value cannot be negative. Please enter a number equal to or greater than 0");
                } else {
                    validInput = true;
                    break;
                }
            } else {
                System.out.println("Please enter a number");
                scanner.nextLine();
            }
        }

        return numVar;
    }

    public static String verifyInputIsEmail(Scanner scanner) {
        String emailInput = "";

        while(!(emailInput.contains("@")) || emailInput.equals("")){
            System.out.println("Type in the member's full email: ");
            emailInput = scanner.nextLine();
            if(!(emailInput.contains("@")) || emailInput.equals("")) {
                System.out.println("Please enter a valid email");
            }
        }

        return emailInput;
    }

    public static String verifyInputIsNotEmpty(String request, Scanner scanner, boolean mustBeEmail) {
        String input = "";

        while(input.equals("") || (mustBeEmail && !input.contains("@"))) {
            System.out.print(request);
            input = scanner.nextLine();

            if(input.equals("")){
                System.out.println("This cannot be empty");
            } else if(!input.contains("@") && mustBeEmail) {
                System.out.println("Enter a valid email");
            }

        }
        
        return input;
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

    public static Member searchMembersAndDisplay(Scanner scanner) {
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
                emailInput = verifyInputIsEmail(scanner);
                foundMemberByEmail= MemberDao.getMemberByEmail(emailInput);

                if(foundMemberByEmail != null) {
                    System.out.println("We found the member");
                    System.out.println("ID: " + foundMemberByEmail.getId() + " | Name: " + foundMemberByEmail.getName() + "| Email: " + foundMemberByEmail.getEmail());
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
                        System.out.print("Enter the ID of the member you'd like to update: ");
                        if(scanner.hasNextInt()) {
                            memberId = scanner.nextInt();
                            scanner.nextLine();
                            for(Member checkMember: listOfMembers) {
                                if(checkMember.getId() == memberId) {
                                    return MemberDao.getMemberId(memberId);
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
            String newTitle = verifyInputIsNotEmpty("Your title of the selected book is currently " + book.getTitle() + ". What would you like to change the book title to?: ", scanner, false);
            book.setTitle(newTitle);
            System.out.println("Successful change. The book's title is now " + book.getTitle());
        }else if(input.equalsIgnoreCase("A")){
            String newAuthor = verifyInputIsNotEmpty("Your author of the selected book is currently " + book.getAuthor() + ". What would you like to change the book author to?: ", scanner, false);
            book.setAuthor(newAuthor);
            System.out.println("Successful change. The book's author is now " + book.getAuthor());
        }else if(input.equalsIgnoreCase("I")){
            String newISBN = verifyInputIsNotEmpty("Your book ISBN is currently "  + book.getIsbn() + ". What would you like to change the book ISBN to?: ", scanner, false);
            book.setIsbn(newISBN);
            System.out.println("Successful change. The book's ISBN is now " + book.getIsbn());
        }else if(input.equalsIgnoreCase("TC")){
            int newTotalCopyValue = -1;
            while(newTotalCopyValue < book.getAvailableCopies()) {
                newTotalCopyValue = -1;

                newTotalCopyValue = verifyInputIsNum("Enter what you would like to change the book's total copies number to: ", scanner, true);
                if(newTotalCopyValue < book.getAvailableCopies()) {
                    System.out.println("Your total copies value is too low compared to the available copies in the database");
                    System.out.println("The total copies you entered was " + newTotalCopyValue + " and the available copies of the book in the database base is " + book.getAvailableCopies()); 
                    System.out.println("Try again.");                 
                }

            }
            
            book.setTotalCopies(newTotalCopyValue);
            System.out.println("Successful change. The book's total copies are now " + book.getTotalCopies());
        }else if(input.equalsIgnoreCase("AC")){
            int newAvailValue = book.getTotalCopies() + 1;
            while(newAvailValue > book.getTotalCopies()) {
                newAvailValue = -1;

                newAvailValue = verifyInputIsNum("Enter what you would like to change the book's available copies number to: ", scanner, false);
                if(newAvailValue > book.getTotalCopies()) {
                    System.out.println("Your available copies is too high when compared ");
                    System.out.println("The available copies you entered was " + newAvailValue + " and the total copies of the book in the database base is " + book.getTotalCopies()); 
                    System.out.println("Try again.");                 
                }

            }

            book.setAvailableCopies(newAvailValue);
            System.out.println("Successful change. The book's available copies are now " + book.getAvailableCopies());
        }

        return book;
    }

    public static Member changeMemberDetails(Member member, Scanner scanner) {
        String input = "";
        while(!(input.equalsIgnoreCase("N")) && !(input.equalsIgnoreCase("E"))) {
            System.out.print("Enter the symbol: ");
            input = scanner.nextLine();

            if(!(input.equalsIgnoreCase("N")) && !(input.equalsIgnoreCase("E"))) {
                System.out.println("You did not input the correct value. Try again.");
            }
        }

        if(input.equalsIgnoreCase("N")) {
            String newName = verifyInputIsNotEmpty("Your member is currently called " + member.getName() + " . What would you like to change it to?: ", scanner, false);
            member.setName(newName);
            System.out.println("Successful change. The member's name is now " + member.getName());
        }else if(input.equalsIgnoreCase("E")){
            String newEmail = verifyInputIsNotEmpty("Your member's email is currently " + member.getEmail() + " . What would you like to change it to?: ", scanner, true);
            member.setEmail(newEmail);
            System.out.println("Successful change. The member's email is now " + member.getEmail());
        }

        return member;
    }
}
