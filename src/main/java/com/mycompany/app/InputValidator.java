package com.mycompany.app;

import java.util.Scanner;

public class InputValidator {
    public static int verifyInputIsNum(String request, Scanner scanner, boolean numCannotBeZero) {
        boolean validInput = false;
        int numVar = 0;
    
        while(!validInput) {
    
            validInput = false;
            System.out.print(request);
            if(scanner.hasNextInt()) {
                numVar = scanner.nextInt();
                scanner.nextLine();
                if(numVar < 0) {
                    System.out.println("This value cannot be negative. Please enter a number equal to or greater than 0");
                } else if(numVar == 0 && numCannotBeZero) {
                    System.out.println("This value cannot be less than 1");
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

    public static String verifyInputIsEmail(Scanner scanner) {
        String emailInput = "";

        while(!(emailInput.contains("@")) || emailInput.equals("")){
            System.out.print("Type in the member's email: ");
            emailInput = scanner.nextLine();
            if(!(emailInput.contains("@")) || emailInput.equals("")) {
                System.out.println("Please enter a valid email");
            }
        }

        return emailInput;
    }

    public static String verifyInputIsNotEmpty(String request, Scanner scanner) {
        String input = "";
    
        while(input.equals("")) {
            System.out.print(request);
            input = scanner.nextLine();
    
            if(input.equals("")){
                System.out.println("This cannot be empty");
            }
        }
        return input;
    }
}
