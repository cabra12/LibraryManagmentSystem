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
                System.out.println("\nWould you like to log in or register as a new member?");
                
                while(!(memberChoice.equalsIgnoreCase("L")) && !(memberChoice.equalsIgnoreCase("R"))) {
                    System.out.print("Press 'L' for log in and 'R' for register: ");
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
                
            } else if (userType.equalsIgnoreCase("Admin")) {

                System.out.println("Hello Admin");
                continueLoop = AdminSession.runAdminSession(scanner);

            }
            
        }

        scanner.close();
    }
}
