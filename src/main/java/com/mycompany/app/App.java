package com.mycompany.app;

import java.util.Scanner;

public class App {

    public static void memberActions(int memChoice, Scanner scanner) {
        String nameInput = "";
        String emailInput = "";
        String searchOption = "";
        String authorInput = "";

        switch(memChoice) {
            case 1:
                while(nameInput.equals("")) {
                    System.out.print("Enter your name:");
                    nameInput = scanner.nextLine();
                    System.out.println("You can't leave your name blank");
                }

                while(!(emailInput.contains("@")) || !(emailInput.equals(""))){
                    System.out.print("Enter your email: ");
                    emailInput = scanner.nextLine();
                    System.out.println("Please enter a valid email");
                }

                Member member = new Member(nameInput, emailInput);
                MemberDao.addMember(member);
                break;
            case 2:
                while(!(searchOption.equalsIgnoreCase("Author")) && !(searchOption.equalsIgnoreCase("Title"))) {
                    System.out.print("Would you like to search for books by author or title?: ");
                    searchOption = scanner.nextLine();
                    System.out.println("Invalid option!");
                }

                if(searchOption.equalsIgnoreCase("Author")) {
                    while(authorInput.equals("")){
                        System.out.print("Enter the author's name (or part of it): ");
                        authorInput = scanner.nextLine();
                        System.out.println("Invalid input");
                    }
                    
                    // Prompt for the search term, call BookDao.searchByAuthor(term), store the result in a List<Book>.
                    // Check if it's empty — if so, print something like "No books found."
                    // If not empty, loop through the list (enhanced for-loop works well: for (Book b : results)) and print each book's relevant fields (title, author, availability, etc.) using your Book getters.
                }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continueLoop = true;
        String userType = "";
        int memActionChoice = 0;
        int AdminActionChoice = 0;

        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("\nWelcome to the Library Management System");
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

        while(continueLoop) {

            while(!(userType.equalsIgnoreCase("Member")) && !(userType.equalsIgnoreCase("Admin"))) {
                System.out.print("Are you a Member or an Admin?: ");
                userType = scanner.nextLine();
                System.out.println("Incorrect option");
            }

            if(userType.equalsIgnoreCase("Member")) {
                System.out.println("What would you like to do?");
                System.out.println("1. Register as a new member");
                System.out.println("2. Search books (by title/author");
                System.out.println("3. Check out a book");
                System.out.println("4. Return a book");
                System.out.println("5. View my borrowed books");
                System.out.println("6. Exit");

                while(memActionChoice != 1 && memActionChoice != 2 && memActionChoice != 3 && memActionChoice != 4 && memActionChoice != 5) {
                    System.out.print("Enter your choice (1, 2, 3, 4, 5, or 6): ");
                    memActionChoice = scanner.nextInt();
                    scanner.nextLine();
                }

                memberActions(memActionChoice, scanner);
                
            }else if (userType.equalsIgnoreCase("Admin")) {
                System.out.println("What would you like to do?");
                System.out.println("1. Add/Update/Delete Books");
                System.out.println("2. Add/Update/Delete Members");
                System.out.println("3. View all borrowed books");
                System.out.println("4. View overdue books");
                System.out.println("5. Exit");
                System.out.print("Enter your choice (1, 2, 3, 4, or 5): ");

                while(AdminActionChoice != 1 && AdminActionChoice != 2 && AdminActionChoice != 3 && AdminActionChoice != 4) {
                    System.out.print("Enter your choice (1, 2, 3, or 4): ");
                    AdminActionChoice = scanner.nextInt();
                    scanner.nextLine();
                }
            }
            
        }

        scanner.close();
    }
}
