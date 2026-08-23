package com.mycompany.app;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class MemberSession {
    public static boolean runMemberSession(Scanner scanner, Member member) {
        boolean continueLoop = true;

        while(continueLoop) {
            String exitChoice = "";
            int memActionChoice = 0;

            
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

    public static void memberActions(int memChoice, Scanner scanner, Member member) {
        boolean validInput = false;

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
                
                while(!validInput) {
                    validInput = false;
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
            case 3:
                int borrowedBookId = 0;

                List<BorrowedBook> borrowedBooks = getAllBorrowedBooksByMember(member);

                while(!validInput) {
                    borrowedBookId = 0;
                    validInput = false;

                    System.out.print("Which would you like to return? Type the ID: ");

                    if(scanner.hasNextInt()) {
                        borrowedBookId = scanner.nextInt();
                        scanner.nextLine();

                        for(BorrowedBook borrowedBook : borrowedBooks) {
                            if(borrowedBook.getId() == borrowedBookId) {
                                validInput = true;
                                break;
                            }
                        }

                        if(!validInput) {
                            System.out.println("That ID wasn't in the search results, try again");
                        }

                    } else {
                        System.out.println("Please only enter numbers");
                        scanner.nextLine();
                    }
                }

                BorrowedBookDao.returnBook(borrowedBookId);
                break;

            case 4:
                getAllBorrowedBooksByMember(member);
                break;
                
        }
    }

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

    public static List<BorrowedBook> getAllBorrowedBooksByMember(Member member) {
        System.out.println("Here are all the books you borrowed:");
        List<BorrowedBook> borrowedBooks = BorrowedBookDao.getBorrowedBooksByMember(member.getId());
         
        for(BorrowedBook borrowedBook: borrowedBooks) {
            Book book = BookDao.getBookById(borrowedBook.getBookId());
            System.out.println("ID: " + borrowedBook.getId() + " | " + book.getTitle() + " | " + book.getAuthor() + " | " + borrowedBook.getDueDate());
        }

        return borrowedBooks;
    }


}
