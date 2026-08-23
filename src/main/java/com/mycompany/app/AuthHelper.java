package com.mycompany.app;

import java.util.Scanner;

public class AuthHelper {
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

}
