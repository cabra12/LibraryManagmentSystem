package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

public class PasswordUtilTest {
    private static final String ALLOWED_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";

    @Test
    @DisplayName("generated password is exactly 12 characters long")
    public void generateTempPassword_correctLength() {
        String result = PasswordUtil.generateTempPassword();
        assertEquals(12, result.length());
    }

    //checks every character of the password to confirm it's in the allowed set, runs 3 times total
    //x is placeholder value, like in algebra, not the character
    //if there is a "not allowed" character, then the warning runs, otherwise it's fine
    @Test
    @DisplayName("every character in the generated password is from the allowed character set")
    public void generateTempPassword_onlyAllowedCharacters() {
        for (int i = 0; i < 3; i++) {
            String result = PasswordUtil.generateTempPassword();
            for (char x : result.toCharArray()) {
                assertTrue(ALLOWED_CHARS.indexOf(x) >= 0,
                    "Character '" + x + "' is not in the allowed character set");
            }
        }
    }

    @Test
    @DisplayName("two consecutive calls produce different passwords")
    public void generateTempPassword_differentOnEachCall() {
        String first = PasswordUtil.generateTempPassword();
        String second = PasswordUtil.generateTempPassword();
        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("generated password is never null or empty")
    public void generateTempPassword_notNullOrEmpty() {
        String result = PasswordUtil.generateTempPassword();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
