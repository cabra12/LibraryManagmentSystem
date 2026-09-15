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

}
