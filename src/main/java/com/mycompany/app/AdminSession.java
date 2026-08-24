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
                    changeBookDetails(book, scanner);
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
                    

                    while(!(emailInput.contains("@")) || emailInput.equals("")){
                        System.out.println("Type in the member's full email: ");
                        emailInput = scanner.nextLine();
                        if(!(emailInput.contains("@")) || emailInput.equals("")) {
                            System.out.println("Please enter a valid email");
                        }
                    }

                    Member member = new Member(name, emailInput);
                    MemberDao.addMember(member);
                }else if (memberActionChoice.equalsIgnoreCase("Update")) {
                    
                }
        }
    }

    public static String addUpdateDeleteItem(Scanner scanner, String item) {
        String choice = "";
        while(!(choice.equalsIgnoreCase("Add")) && !(choice.equalsIgnoreCase("Update")) && !(choice.equalsIgnoreCase("Delete"))) {
            choice = "";

            System.out.print("Type 'Add' to add a " + item + ", 'Update' to update a " + item + ", and 'Delete' to delete a" + item + ": ");
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

    public static void changeBookDetails(Book book, Scanner scanner) {
        String input = "";
        while(!(input.equalsIgnoreCase("T")) && !(input.equalsIgnoreCase("A")) && !(input.equalsIgnoreCase("I")) && !(input.equalsIgnoreCase("TC")) && !(input.equalsIgnoreCase("AC"))) {
            System.out.print("Enter the symbol: ");
            input = scanner.nextLine();

            if(!(input.equalsIgnoreCase("T")) && !(input.equalsIgnoreCase("A")) && !(input.equalsIgnoreCase("I")) && !(input.equalsIgnoreCase("TC")) && !(input.equalsIgnoreCase("AC"))) {
                System.out.println("You did not input the correct value. Try again.");
            }
        }

        if(input.equalsIgnoreCase("T")) {
            System.out.print("Enter what you would like to change the book title to: ");
            String newTitle = scanner.nextLine();
            book.setTitle(newTitle);
            System.out.println("Successful change. The book's title is now " + book.getTitle());
        }else if(input.equalsIgnoreCase("A")){
            System.out.print("Enter what you would like to change the book author to: ");
            String newAuthor = scanner.nextLine();
            book.setAuthor(newAuthor);
            System.out.println("Successful change. The book's author is now " + book.getAuthor());
        }else if(input.equalsIgnoreCase("I")){
            System.out.print("Enter what you would like to change the book ISBN to: ");
            String newISBN = scanner.nextLine();
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
    }
}
