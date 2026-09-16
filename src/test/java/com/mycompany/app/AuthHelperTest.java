package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Scanner;

public class AuthHelperTest {
    
    @Nested
    @DisplayName("retrieveName Tests")
    class RetrieveNameTests {

        @Test 
        @DisplayName("valid name entered on first try")
        public void retrieveName_validName() {
            Scanner fakeInput = new Scanner("Charlotte Abraham\n");
            String result = AuthHelper.retrieveName(fakeInput);
            assertEquals("Charlotte Abraham", result);
        }

        @Test
        @DisplayName("empty input first, then a valid name input")
        public void retrieveName_emptyName() {
            Scanner fakeInput = new Scanner("\nSara Smith\n");
            String result = AuthHelper.retrieveName(fakeInput);
            assertEquals("Sara Smith", result);
        }
    }

    @Nested
    @DisplayName("getEmailInput tests")
    class GetEmailInputTests {

        @Test
        @DisplayName("valid email entered on first try")
        public void getEmailInput_validEmail() {
            Scanner fakeInput = new Scanner("charlotte@gmail.com\n");
            String result = AuthHelper.getEmailInput(fakeInput);
            assertEquals("charlotte@gmail.com", result);
        }

        @Test
        @DisplayName("invalid email input with no @ symbol and then valid email given")
        public void getEmailInput_noAtSymbol() {
            Scanner fakeInput = new Scanner("sara\nsara@gmail.com");
            String result = AuthHelper.getEmailInput(fakeInput);
            assertEquals("sara@gmail.com", result);
        }

        @Test
        @DisplayName("invalid empty email input and then valid email given")
        public void getEmailInput_emptyInput() {
            Scanner fakeInput = new Scanner("\nkatherine@gmail.com");
            String result = AuthHelper.getEmailInput(fakeInput);
            assertEquals("katherine@gmail.com", result);
        }

        @Test
        @DisplayName("invalid email with no @ symbol, invalid empty email input,then valid email given")
        public void getEmailInput_emptyAndNoAtSymbolInput() {
            Scanner fakeInput = new Scanner("rachel\n\nrachel@gmail.com");
            String result = AuthHelper.getEmailInput(fakeInput);
            assertEquals("rachel@gmail.com", result);
        }
    }

    @Nested
    @DisplayName("makeNewPassword tests")
    class MakeNewPasswordTests {
        //two inputs because there's two loops
        @Test
        @DisplayName("accepts a matching password on the first try")
        public void makeNewPassword_matchingIdealPassword() {
            Scanner fakeInput = new Scanner("password4u\npassword4u\n");
            String result = AuthHelper.makeNewPassword(fakeInput);
            assertTrue(BCrypt.checkpw("password4u", result));
        }

        //too short password, then valid one
        @Test
        @DisplayName("accepts a short password first, then takes in a password of correct length")
        public void makeNewPassword_tooShortPassword() {
            Scanner fakeInput = new Scanner("password\nnewpassword898!\nnewpassword898!\n");
            String result = AuthHelper.makeNewPassword(fakeInput);
            assertTrue(BCrypt.checkpw("newpassword898!", result));
        }

        @Test 
        @DisplayName("accepts an empty password, then takes a valid password")
        public void makeNewPassword_emptyPassword() {
            Scanner fakeInput = new Scanner("\nsave_the_sharks\nsave_the_sharks");
            String result = AuthHelper.makeNewPassword(fakeInput);
            assertTrue(BCrypt.checkpw("save_the_sharks", result));
        }

        @Test
        @DisplayName("checks for mismatched password, catches it, then accepts a matched password")
        public void makeNewPassword_mismatchedPassword() {
            Scanner fakeInput = new Scanner("ohNoNewPassword1\nohNoNewPasswor\nohNoNewPassword1\n");
            String result = AuthHelper.makeNewPassword(fakeInput);
            assertTrue(BCrypt.checkpw("ohNoNewPassword1", result));
        }

        //multiple mismatched Passwords
        @Test
        @DisplayName("takes in multiple mismatchec passwords, then finally accepts a matched password")
        public void makeNewPassword_multipleMismatchedPassword() {
            Scanner fakeInput = new Scanner("greenflowers4me\ngreen\ngreenflower\ngreenflowers4\ngreenflowers4me");
            String result = AuthHelper.makeNewPassword(fakeInput);
            assertTrue(BCrypt.checkpw("greenflowers4me", result));
        }
    }

    @Nested
    @DisplayName("checkPassword tests")
    class CheckPasswordTests {
        @Test 
        @DisplayName("matched passwords checked, should return true")
        public void checkPassword_matchedPasswords() {
            String password = "pass123!$p";
            String hashedPw = BCrypt.hashpw(password, BCrypt.gensalt(12));
            boolean output = AuthHelper.checkPassword(password, hashedPw);
            assertTrue(output);
        }

        @Test
        @DisplayName("mismatched passwords checked, should return false")
        public void checkPassword_mismatchedPasswords() {
            String actualPassword = "fortune150";
            String mistypedPassword = "for150";

            String hashedPw = BCrypt.hashpw(actualPassword, BCrypt.gensalt(12));
            boolean output = AuthHelper.checkPassword(mistypedPassword, hashedPw);
            assertFalse(output);
        }
    }


    @Nested
    @DisplayName("retrievePassword tests")
    class RetrievePasswordTests {
        @Test 
        @DisplayName("valid password entered on first try")
        public void retrievePassword_nonEmptyPassword() {
            Scanner fakeInput = new Scanner("password123\n");
            String result = AuthHelper.retrievePassword(fakeInput);
            assertEquals("password123", result);
        }

        @Test
        @DisplayName("empty input first, then a valid password input")
        public void retrievePassword_emptyPassword() {
            Scanner fakeInput = new Scanner("\nredhot&dangerous\n");
            String result = AuthHelper.retrievePassword(fakeInput);
            assertEquals("redhot&dangerous", result);
        }
    }

    @Nested
    @DisplayName("getUsernameInput tests")
    class GetUsernameInputTests {
        @Test 
        @DisplayName("valid username entered on first try")
        public void getUsernameInput_nonEmptyUsername() {
            Scanner fakeInput = new Scanner("char12\n");
            String result = AuthHelper.getUsernameInput(fakeInput);
            assertEquals("char12", result);
        }

        @Test
        @DisplayName("empty input first, then a valid username input")
        public void getUsernameInput_emptyUsername() {
            Scanner fakeInput = new Scanner("\nwillow1\n");
            String result = AuthHelper.getUsernameInput(fakeInput);
            assertEquals("willow1", result);
        }
    }

}
