package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Scanner;

public class InputValidatorTest {
    @Nested
    @DisplayName("verifyInputIsNum tests")
    class VerifyInputIsNumTests {

        @Test
        @DisplayName("valid positive number on first try")
        public void verifyInputIsNum_validPositiveNumber() {
            Scanner fakeInput = new Scanner("5\n");
            int result = InputValidator.verifyInputIsNum("Enter a number: ", fakeInput, true);
            assertEquals(5, result);
        }

        @Test
        @DisplayName("non-numeric input, then a valid number")
        public void verifyInputIsNum_nonNumericThenValid() {
            Scanner fakeInput = new Scanner("abc\n7\n");
            int result = InputValidator.verifyInputIsNum("Enter a number: ", fakeInput, true);
            assertEquals(7, result);
        }

        @Test
        @DisplayName("zero entered when numCannotBeZero is true, rejected, then valid number")
        public void verifyInputIsNum_zeroRejectedWhenCannotBeZero() {
            Scanner fakeInput = new Scanner("0\n3\n");
            int result = InputValidator.verifyInputIsNum("Enter a number: ", fakeInput, true);
            assertEquals(3, result);
        }

        @Test
        @DisplayName("zero entered when numCannotBeZero is false, accepted immediately")
        public void verifyInputIsNum_zeroAcceptedWhenCanBeZero() {
            Scanner fakeInput = new Scanner("0\n");
            int result = InputValidator.verifyInputIsNum("Enter a number: ", fakeInput, false);
            assertEquals(0, result);
        }

        @Test
        @DisplayName("negative number when numCannotBeZero is false, rejected, then valid number")
        public void verifyInputIsNum_negativeRejectedWhenCanBeZero() {
            Scanner fakeInput = new Scanner("-4\n6\n");
            int result = InputValidator.verifyInputIsNum("Enter a number: ", fakeInput, false);
            assertEquals(6, result);
        }

        @Test
        @DisplayName("negative number when numCannotBeZero is true, rejected, then valid number")
        public void verifyInputIsNum_negativeRejectedWhenCannotBeZero() {
            Scanner fakeInput = new Scanner("-2\n9\n");
            int result = InputValidator.verifyInputIsNum("Enter a number: ", fakeInput, true);
            assertEquals(9, result);
        }
    }

    @Nested
    @DisplayName("verifyInputIsEmail tests")
    class VerifyInputIsEmailTests {

        @Test
        @DisplayName("valid email on first try")
        public void verifyInputIsEmail_validEmail() {
            Scanner fakeInput = new Scanner("test@gmail.com\n");
            String result = InputValidator.verifyInputIsEmail(fakeInput);
            assertEquals("test@gmail.com", result);
        }

        @Test
        @DisplayName("input without @ symbol, then a valid email")
        public void verifyInputIsEmail_noAtSymbolThenValid() {
            Scanner fakeInput = new Scanner("nope\nreal@gmail.com\n");
            String result = InputValidator.verifyInputIsEmail(fakeInput);
            assertEquals("real@gmail.com", result);
        }

        @Test
        @DisplayName("empty input, then a valid email")
        public void verifyInputIsEmail_emptyThenValid() {
            Scanner fakeInput = new Scanner("\nvalid@gmail.com\n");
            String result = InputValidator.verifyInputIsEmail(fakeInput);
            assertEquals("valid@gmail.com", result);
        }
    }

    @Nested
    @DisplayName("verifyInputIsNotEmpty tests")
    class VerifyInputIsNotEmptyTests {

        @Test
        @DisplayName("non-empty input on first try")
        public void verifyInputIsNotEmpty_nonEmptyFirstTry() {
            Scanner fakeInput = new Scanner("Some Name\n");
            String result = InputValidator.verifyInputIsNotEmpty("Enter something: ", fakeInput);
            assertEquals("Some Name", result);
        }
    
        @Test
        @DisplayName("empty input, then a valid input")
        public void verifyInputIsNotEmpty_emptyThenValid() {
            Scanner fakeInput = new Scanner("\nSome Name\n");
            String result = InputValidator.verifyInputIsNotEmpty("Enter something: ", fakeInput);
            assertEquals("Some Name", result);
        }
    }
}
