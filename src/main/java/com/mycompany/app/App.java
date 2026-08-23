package com.mycompany.app;

import java.util.Scanner;

public class App {
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
                    member = AuthHelper.useEmailToGetMember(scanner, false, "");
                }else if(memberChoice.equalsIgnoreCase("R")) {
                    member = AuthHelper.registerNewMember(scanner);
                }

                System.out.println("Hello " + member.getName());
                continueLoop = MemberSession.runMemberSession(scanner, member);
                
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
