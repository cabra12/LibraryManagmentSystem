package com.mycompany.app;

import java.util.Scanner;

import org.mindrot.jbcrypt.BCrypt;

public class AuthHelper {
    public static Member registerNewMember(Scanner scanner) {

        String name = retrieveName(scanner);

        Member newMember = null;
        boolean memberAdded = false;

        while(!memberAdded) {
            String email = getEmailInput(scanner);
            String password = makeNewPassword(scanner);
    
            newMember = new Member(name, email, password, false);
            DaoResult addResult = MemberDao.addMember(newMember);

            switch(addResult) {
                case SUCCESS -> memberAdded = true;
                case DUPLICATE_KEY -> System.out.println("That email is already taken. Please choose a different one.");
                case DATABASE_ERROR -> System.out.println("Something went wrong adding you as a member. Please try again.");
            }
        }

        return newMember;
    }

    public static Member logInExistingMember(Scanner scanner) {
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
            } else {
                if(checkPassword(retrievePassword(scanner), member.getPassword())) {
                    break;
                } else {
                    continue;
                }
            }

        }

        if(member.getPasswordChangeStatus() == true) {
            boolean passwordSet = false;
        
            while (!passwordSet) {
                System.out.println("It's time to change your temporary password to your own password");
                String hashedPassword = makeNewPassword(scanner);
                member.setPassword(hashedPassword);
                member.setPasswordChangeStatus(false);
                DaoResult changeResult = MemberDao.updateMember(member);
        
                switch (changeResult) {
                    case SUCCESS -> {
                        passwordSet = true;
                        System.out.println("Your password has been changed successfully.");
                    }
                    case DUPLICATE_KEY -> System.out.println("Something went wrong — please try again.");
                    case DATABASE_ERROR -> System.out.println("Something went wrong changing your password. Please try again.");
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
        String firstPasswordInput = "";

        while(firstPasswordInput.equals("") || firstPasswordInput.length() < 10) {
            System.out.print("Enter a strong password that's at least 10 characters: ");
            firstPasswordInput = scanner.nextLine();

            if((firstPasswordInput.equals(""))) {
                System.out.println("Password cannot be empty. Try again.");
            }else if(firstPasswordInput.length() < 10) {
                System.out.println("Password must be at least 10 characters. Try again.");
            }
        }

        String confirmPassword = "";
        while(!(firstPasswordInput.equals(confirmPassword))) {
            System.out.print("Confirm your new password: ");
            confirmPassword = scanner.nextLine();

            if(!(firstPasswordInput.equals(confirmPassword))) {
                System.out.println("Passwords didn't match. Please try again.");
            }
        }

        String hashedPw = BCrypt.hashpw(confirmPassword, BCrypt.gensalt(12));
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

    public static Admin logInAsAdmin(Scanner scanner) {
        Admin admin = null;

        while(true) {
            String username = getUsernameInput(scanner);
            admin = AdminDao.logInWithUsername(username);

            if(admin == null) {
                System.out.println("Username '" + username + "' not found. Try again.");
                continue;
            } else {
                if(checkPassword(retrievePassword(scanner), admin.getPassword())) {
                    break;
                } else {
                    continue;
                }
            }
        }

        if(admin.getPasswordChangeStatus() == true) {
            boolean passwordSet = false;
        
            while (!passwordSet) {
                System.out.println("It's time to change your temporary password to your own password");
                String hashedPassword = makeNewPassword(scanner);
                admin.setPassword(hashedPassword);
                admin.setPasswordChangeStatus(false);
                DaoResult changeResult = AdminDao.updateAdmin(admin);
        
                switch (changeResult) {
                    case SUCCESS -> {
                        passwordSet = true;
                        System.out.println("Your password has been changed successfully.");
                    }
                    case DUPLICATE_KEY -> System.out.println("Something went wrong — please try again.");
                    case DATABASE_ERROR -> System.out.println("Something went wrong changing your password. Please try again.");
                }
            }
        }

        System.out.println("Hello " + admin.getName() + " (Admin)");
        return admin;
    }

    public static String getUsernameInput(Scanner scanner) {
        String usernameInput = "";
    
        while(usernameInput.equals("")){
            System.out.print("Enter your username: ");
            usernameInput = scanner.nextLine();
            if(usernameInput.equals("")) {
                System.out.println("Please enter a valid username");
            }
        }
    
        return usernameInput;
    }

}
