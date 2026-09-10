package com.mycompany.app;

import java.util.Scanner;

import org.mindrot.jbcrypt.BCrypt;

public class AuthHelper {
    public static Member registerNewMember(Scanner scanner) {

        String name = retrieveName(scanner);
        String email = getEmailInput(scanner);
        String password = makeNewPassword(scanner);

        Member newMember = new Member(name, email, password);
        MemberDao.addMember(newMember);
        return newMember;
    }

    public static Member logInExistingMember(Scanner scanner) {
        boolean successfulLogin = false;
        Member member = null;
        

        while(true) {
            String email = getEmailInput(scanner);
            member = MemberDao.getMemberByEmail(email);

            if(member == null) {
                String choice = "";
                while(!(choice.equalsIgnoreCase("T")) && !(choice.equalsIgnoreCase("R"))) {
                    System.out.println("Your email '" + email + "' is not registered. Would you like to try again or register as a new member?");
                    System.out.print("Press 'T' to try again or 'R' to register as a new member: ");
                    choice = scanner.nextLine();
    
                    if(!(choice.equalsIgnoreCase("T")) && !(choice.equalsIgnoreCase("R"))) {
                        System.out.println("Please enter either 'T' or 'R'");
                    }
                }
    
                if(choice.equalsIgnoreCase("T")) {
                    continue;
                } else if (choice.equalsIgnoreCase("R")) {
                    member = registerNewMember(scanner);
                    break;
                }
            }else {
                String password = retrievePassword(scanner);

                //BCrypt checks (plainTextCandidate, storedHash)
                successfulLogin = checkPassword(password, member.getPassword());

                if(successfulLogin == true) {
                    break;
                }else {
                    continue;
                }
            }

        }


        System.out.println("Hello " + member.getName());
        return member;
    }

    public static String retrieveName(Scanner scanner) {
        String nameInput = "";
        while(nameInput.equals("")) {
            System.out.print("Enter your full name: ");
            nameInput = scanner.nextLine();
            if(nameInput.equals("")) {
                System.out.println("You can't leave your name blank");
            }
        }

        return nameInput;
    }

    public static String getEmailInput(Scanner scanner) {
        String emailInput = "";

        while(!(emailInput.contains("@")) || emailInput.equals("")){
            System.out.print("Enter your email: ");
            emailInput = scanner.nextLine();
            if(!(emailInput.contains("@")) || emailInput.equals("")) {
                System.out.println("Please enter a valid email");
            }
        }

        return emailInput;
    }

    public static String makeNewPassword(Scanner scanner) {
        String passwordInput = "";

        while(passwordInput.equals("") || passwordInput.length() < 10) {
            System.out.print("Enter a strong password that's at least 10 characters: ");
            passwordInput = scanner.nextLine();

            if((passwordInput.equals(""))) {
                System.out.println("Password cannot be empty. Try again.");
            }else if(passwordInput.length() < 10) {
                System.out.println("Password must be at least 10 characters. Try again.");
            }
        }

        String hashedPw = BCrypt.hashpw(passwordInput, BCrypt.gensalt(12));

        return hashedPw;
    }

    public static String retrievePassword(Scanner scanner) {
        String passwordInput = "";
        while(passwordInput.equals("")) {
            System.out.print("Enter your pasword: ");
            passwordInput = scanner.nextLine();
            
            if(passwordInput.equals("")) {
                System.out.println("Password cannot be empty. Try again");
            }
        }

        return passwordInput;
    }

    public static boolean checkPassword(String passwordInput, String passwordDB) {
        //must be BCrypt((plaintextCandidate, storedHash))

        if(BCrypt.checkpw(passwordInput, passwordDB)) {
            System.out.println("You are now logged in.");
            return true;
        } else {
            System.out.println("Your email or password is incorrect. Try to log in again");
            return false;
        }
    }

}
