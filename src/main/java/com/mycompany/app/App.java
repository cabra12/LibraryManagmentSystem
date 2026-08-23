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

    public static Member registerNewMember(Scanner scanner) {
        String nameInput = "";
        while(nameInput.equals("")) {
            System.out.print("Enter your name:");
            nameInput = scanner.nextLine();
            if(nameInput.equals("")) {
                System.out.println("You can't leave your name blank");
            }
        }

        Member newMember = useEmailToGetMember(scanner, true, nameInput);
        return newMember;
    }

    public static Member useEmailToGetMember(Scanner scanner, boolean newRegistration, String nameInput) {
        String emailInput = "";
        Member member = null;
        String choice = "";

        while(member == null) {
            choice = "";
            while(!(emailInput.contains("@")) || emailInput.equals("")){
                System.out.print("Enter your email: ");
                emailInput = scanner.nextLine();
                if(!(emailInput.contains("@")) || emailInput.equals("")) {
                    System.out.println("Please enter a valid email");
                }
            }
    
            if(newRegistration == true) {
                member = new Member(nameInput, emailInput);
                MemberDao.addMember(member);
            } else {
                member = MemberDao.getMemberByEmail(emailInput);
            }

            if(member == null) {
                System.out.println("Sorry, we're not able to find you. Would you like to try again or register as a new member?");

                while(!(choice.equalsIgnoreCase("T")) && !(choice.equalsIgnoreCase("R"))) {
                    System.out.print("Press T for 'try again' or R for 'register'");
                    choice = scanner.nextLine();
                    if(!(choice.equalsIgnoreCase("T")) && !(choice.equalsIgnoreCase("R"))) {
                        System.out.println("Invalid input");
                    }
                }

                if(choice.equalsIgnoreCase("T")) {
                    emailInput = "";
                }else if(choice.equalsIgnoreCase("R")){
                    member = registerNewMember(scanner);
                }
                
            }
        }

        return member;
    }

    public static void memberActions(int memChoice, Scanner scanner, Member member) {

        switch(memChoice) {
            case 1:
                List<Book> booksSearched = searchBooks(scanner);
                printBookResults(booksSearched);
                    
                break;
            case 2: 
                List<Book> searchBooksToBorrow = null;

                while(searchBooksToBorrow == null || searchBooksToBorrow.isEmpty()) {
                    searchBooksToBorrow = searchBooks(scanner);
                    printBookResults(searchBooksToBorrow);

                    if(searchBooksToBorrow == null || searchBooksToBorrow.isEmpty()) {
                        System.out.println("No books found, please try your search again");
                    }
                }

                int bookId = 0;
                boolean validInput = false;
                
                while(!validInput) {
                    System.out.print("Enter the ID of the book you'd like to check out: ");
                    if(scanner.hasNextInt()) {
                        bookId = scanner.nextInt();
                        scanner.nextLine();
                        for(Book book: searchBooksToBorrow) {
                            if(book.getId() == bookId) {
                                validInput = true;
                                break;
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
                BorrowedBookDao.borrowBook(bookId, member.getId());
                break;
                
        }
    }

    public static boolean runMemberSession(Scanner scanner, Member member) {
        boolean continueLoop = true;

        while(continueLoop) {
            String exitChoice = "";
            int memActionChoice = 0;

            System.out.println("Hello " + member.getName());
            System.out.println("What would you like to do?");
            System.out.println("1. Search books (by title/author");
            System.out.println("2. Check out a book");
            System.out.println("3. Return a book");
            System.out.println("4. View my borrowed books");
            System.out.println("5. Exit");

            while(memActionChoice < 1 || memActionChoice > 5) {
                System.out.print("Enter your choice (1, 2, 3, 4, or 5): ");
                if(scanner.hasNextInt()) {
                    memActionChoice = scanner.nextInt();
                    scanner.nextLine();
                } else {
                    System.out.println("Please enter a number");
                    scanner.nextLine();
                }
                
            }
            
            if(memActionChoice == 5) {
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
                memberActions(memActionChoice, scanner, member);
            }
        }

        return continueLoop;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continueLoop = true;

        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("\nWelcome to the Library Management System");
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

        while(continueLoop) {
            String userType = "";
            
            int adminActionChoice = 0;
            continueLoop = true;
            String memberChoice = "";
            Member member = null;

            while(!(userType.equalsIgnoreCase("Member")) && !(userType.equalsIgnoreCase("Admin"))) {
                System.out.print("Are you a Member or an Admin?: ");
                userType = scanner.nextLine();
                if(!(userType.equalsIgnoreCase("Member")) && !(userType.equalsIgnoreCase("Admin"))) {
                    System.out.println("Incorrect option");
                }
                
            }

            if(userType.equalsIgnoreCase("Member")) {
                System.out.println("Would you like to log in or register as a new member?");
                

                while(!(memberChoice.equalsIgnoreCase("L")) && !(memberChoice.equalsIgnoreCase("R"))) {
                    System.out.print("Press 'L' for log in and 'R' for register");
                    memberChoice = scanner.nextLine();
                    if(!(memberChoice.equalsIgnoreCase("L")) && !(memberChoice.equalsIgnoreCase("R"))) {
                        System.out.println("Incorrect option");
                    }
                }

                if(memberChoice.equalsIgnoreCase("L")){
                    member = useEmailToGetMember(scanner, false, "");
                }else if(memberChoice.equalsIgnoreCase("R")) {
                    member = registerNewMember(scanner);
                }

                continueLoop = runMemberSession(scanner, member);
                
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
