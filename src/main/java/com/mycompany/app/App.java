package com.mycompany.app;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class App {

    public static List<Book> searchBooks(Scanner scanner) {
        String searchOption = "";
        String authorInput = "";
        String titleInput = "";

        while(!(searchOption.equalsIgnoreCase("Author")) && !(searchOption.equalsIgnoreCase("Title"))) {
            System.out.print("Would you like to search for books by author or title?: ");
            searchOption = scanner.nextLine();
            if(!(searchOption.equalsIgnoreCase("Author")) && !(searchOption.equalsIgnoreCase("Title"))) {
                System.out.println("Please enter either 'author' or 'title': ");
            }
        }

        if(searchOption.equalsIgnoreCase("Author")) {
            while(authorInput.equals("")){
                System.out.print("Enter the author's name (or part of it): ");
                authorInput = scanner.nextLine();
                if(authorInput.equals("")) {
                    System.out.println("Cannot be empty");
                }
            }
            
            return BookDao.searchByAuthor(authorInput);


        } else if(searchOption.equalsIgnoreCase("Title")) {
            while(titleInput.equals("")){
                System.out.print("Enter the title of a book (or part of it): ");
                titleInput = scanner.nextLine();
                if(titleInput.equals("")) {
                    System.out.println("Cannot be empty");
                }
            }

            return BookDao.searchByTitle(titleInput);
        } else {
            return Collections.emptyList();
        }
    }

    public static void printBookResults(List<Book> booksSearched) {
        if(booksSearched == null || booksSearched.isEmpty()) {
            System.out.println("No books found");
        } else {
            for (Book book: booksSearched) {
                System.out.println(book.getTitle() + " by " + book.getAuthor() + " has " + book.getAvailableCopies() + " available copies out of " + book.getTotalCopies() + " total copies.");
                System.out.println("     ISBN: " + book.getIsbn());
                System.out.println("     Book ID: " + book.getId());
            }
        }
    }

    public static void memberActions(int memChoice, Scanner scanner) {
        String nameInput = "";
        String emailInput = "";


        switch(memChoice) {
            case 1:
                while(nameInput.equals("")) {
                    System.out.print("Enter your name:");
                    nameInput = scanner.nextLine();
                    if(nameInput.equals("")) {
                        System.out.println("You can't leave your name blank");
                    }
                }

                while(!(emailInput.contains("@")) || emailInput.equals("")){
                    System.out.print("Enter your email: ");
                    emailInput = scanner.nextLine();
                    if(!(emailInput.contains("@")) || emailInput.equals("")) {
                        System.out.println("Please enter a valid email");
                    }
                    
                }

                Member member = new Member(nameInput, emailInput);
                MemberDao.addMember(member);
                break;
            case 2:
                List<Book> booksSearched = searchBooks(scanner);
                printBookResults(booksSearched);
                    
                break;
            case 3: 
                
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continueLoop = true;

        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("\nWelcome to the Library Management System");
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

        while(continueLoop) {
            String userType = "";
            int memActionChoice = 0;
            int adminActionChoice = 0;
            String exitChoice = "";
            continueLoop = true;

            while(!(userType.equalsIgnoreCase("Member")) && !(userType.equalsIgnoreCase("Admin"))) {
                System.out.print("Are you a Member or an Admin?: ");
                userType = scanner.nextLine();
                if(!(userType.equalsIgnoreCase("Member")) && !(userType.equalsIgnoreCase("Admin"))) {
                    System.out.println("Incorrect option");
                }
                
            }

            if(userType.equalsIgnoreCase("Member")) {
                System.out.println("What would you like to do?");
                System.out.println("1. Register as a new member");
                System.out.println("2. Search books (by title/author");
                System.out.println("3. Check out a book");
                System.out.println("4. Return a book");
                System.out.println("5. View my borrowed books");
                System.out.println("6. Exit");

                while(memActionChoice < 1 || memActionChoice > 6) {
                    System.out.print("Enter your choice (1, 2, 3, 4, 5, or 6): ");
                    if(scanner.hasNextInt()) {
                        memActionChoice = scanner.nextInt();
                        scanner.nextLine();
                    } else {
                        System.out.println("Please enter a number");
                        scanner.nextLine();
                    }
                    
                }
                if(memActionChoice == 6) {
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
                    memberActions(memActionChoice, scanner);
                }

                
                
            }else if (userType.equalsIgnoreCase("Admin")) {
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
            }
            
        }

        scanner.close();
    }
}
