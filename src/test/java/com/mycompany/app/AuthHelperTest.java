package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Scanner;

public class AuthHelperTest {

    @Nested
    @DisplayName("registers a new member and saves it, registerNewMember tests")
    class RegisterNewMemberTests {

        @Test
        @DisplayName("happy path for registerNewMember")
        public void registerNewMember_success() {
            Scanner fakeInput = new Scanner("Hanna Roberts\nhanna@gmail.com\nerr56!pass\nerr56!pass\n");
    
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.addMember(any())).thenReturn(DaoResult.SUCCESS);
                //when() is for static methods, it tells Mockito, whenthis specific call happens, do this instead of the actual thing
                //lambda expression is how Mockito captures which staticcall to intercept
                //any() says "whatever Member object gets constructed insie registerNewMember" -- don't know what the method exactly produces so you use this
                Member result = AuthHelper.registerNewMember(fakeInput);
                mocked.verify(() -> MemberDao.addMember(any()));

                assertEquals("Hanna Roberts", result.getName());
                assertEquals("hanna@gmail.com", result.getEmail());
            }
        }    

        @Test
        @DisplayName("retries with a new email after a duplicate key, then succeeds")
        public void registerNewMember_duplicateEmailThenSuccess() {
            Scanner fakeInput = new Scanner(
                "George Wallace\n" +      
                "taken@gmail.com\n" +        
                "pasttheCorner23\npasttheCorner23\n" + 
                "george@gmail.com\n" +    
                "pasttheCorner23\npasttheCorner23\n"  
            );

            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.addMember(any()))
                    .thenReturn(DaoResult.DUPLICATE_KEY, DaoResult.SUCCESS);
                //first call to addMember returns DUPLICATE_KEY, second call returns SUCCESS
                //DUPLICATE_KEY makes the loop run again
                //Scaner supplies the second round
                Member result = AuthHelper.registerNewMember(fakeInput);

                assertEquals("george@gmail.com", result.getEmail());
                mocked.verify(() -> MemberDao.addMember(any()), times(2));
                //times(2) confirms addMember was called twice
            }
        }

        @Test
        @DisplayName("retries after a database error, then succeeds")
        public void registerNewMember_databaseErrorThenSuccess() {
            Scanner fakeInput = new Scanner(
                "Marcus Lee\n" +
                "marcus@gmail.com\n" +
                "safepass123\nsafepass123\n" +
                "marcus@gmail.com\n" +
                "safepass123\nsafepass123\n"
            );
        
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.addMember(any()))
                    .thenReturn(DaoResult.DATABASE_ERROR, DaoResult.SUCCESS);
                Member result = AuthHelper.registerNewMember(fakeInput);
        
                assertEquals("marcus@gmail.com", result.getEmail());
                mocked.verify(() -> MemberDao.addMember(any()), times(2));
            }
        }
    }

    @Nested
    @DisplayName("logInExistingMember tests")
    class LogInExistingMemberTests {
    
        @Test
        @DisplayName("happy path: valid email, correct password, no password change needed")
        public void logInExistingMember_happyPath() {
            String rawPassword = "correctPass123";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
            Member existingMember = new Member(1, "Jane Doe", "jane@gmail.com", hashedPassword, false);
    
            Scanner fakeInput = new Scanner("jane@gmail.com\n" + rawPassword + "\n");
    
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.getMemberByEmail("jane@gmail.com")).thenReturn(existingMember);
    
                Member result = AuthHelper.logInExistingMember(fakeInput);
    
                assertEquals(existingMember, result);
                mocked.verify(() -> MemberDao.getMemberByEmail("jane@gmail.com"), times(1));
                mocked.verify(() -> MemberDao.updateMember(any()), Mockito.never());
            }
        }
    
        @Test
        @DisplayName("wrong password once, then correct password")
        public void logInExistingMember_wrongPasswordThenCorrect() {
            String rawPassword = "realPassword1";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
            Member existingMember = new Member(2, "Tom Baker", "tom@gmail.com", hashedPassword, false);
    
            Scanner fakeInput = new Scanner(
                "tom@gmail.com\n" +
                "wrongPassword\n" +
                "tom@gmail.com\n" +
                rawPassword + "\n"
            );
    
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.getMemberByEmail("tom@gmail.com")).thenReturn(existingMember);
    
                Member result = AuthHelper.logInExistingMember(fakeInput);
    
                assertEquals(existingMember, result);
                mocked.verify(() -> MemberDao.getMemberByEmail("tom@gmail.com"), times(2));
            }
        }
    
        @Test
        @DisplayName("email not found, user chooses to try again, then valid email/password")
        public void logInExistingMember_emailNotFoundThenTryAgain() {
            String rawPassword = "goodPassword1";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
            Member existingMember = new Member(3, "Alice Kim", "alice@gmail.com", hashedPassword, false);
    
            Scanner fakeInput = new Scanner(
                "missing@gmail.com\n" +
                "T\n" +
                "alice@gmail.com\n" +
                rawPassword + "\n"
            );
    
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.getMemberByEmail("missing@gmail.com")).thenReturn(null);
                mocked.when(() -> MemberDao.getMemberByEmail("alice@gmail.com")).thenReturn(existingMember);
    
                Member result = AuthHelper.logInExistingMember(fakeInput);
    
                assertEquals(existingMember, result);
                mocked.verify(() -> MemberDao.getMemberByEmail("missing@gmail.com"));
                mocked.verify(() -> MemberDao.getMemberByEmail("alice@gmail.com"));
            }
        }
    
        @Test
        @DisplayName("email not found, user chooses to register, delegates to registerNewMember")
        public void logInExistingMember_emailNotFoundThenRegister() {
            Scanner fakeInput = new Scanner(
                "missing@gmail.com\n" +
                "R\n" +
                "New Person\n" +
                "newperson@gmail.com\n" +
                "brandNewPass1\n" +
                "brandNewPass1\n"
            );
    
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.getMemberByEmail("missing@gmail.com")).thenReturn(null);
                mocked.when(() -> MemberDao.addMember(any())).thenReturn(DaoResult.SUCCESS);
    
                Member result = AuthHelper.logInExistingMember(fakeInput);
    
                assertEquals("New Person", result.getName());
                assertEquals("newperson@gmail.com", result.getEmail());
                assertFalse(result.getPasswordChangeStatus());
                mocked.verify(() -> MemberDao.getMemberByEmail("missing@gmail.com"));
                mocked.verify(() -> MemberDao.addMember(any()));
            }
        }
    
        @Test
        @DisplayName("password change required, triggers second loop and updates member")
        public void logInExistingMember_passwordChangeRequired() {
            String tempPassword = "tempPass123";
            String hashedTempPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt(12));
            Member existingMember = new Member(4, "Carl White", "carl@gmail.com", hashedTempPassword, true);
    
            String newPassword = "brandNewSecurePass1";
    
            Scanner fakeInput = new Scanner(
                "carl@gmail.com\n" +
                tempPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n"
            );
    
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.getMemberByEmail("carl@gmail.com")).thenReturn(existingMember);
                mocked.when(() -> MemberDao.updateMember(any())).thenReturn(DaoResult.SUCCESS);
    
                Member result = AuthHelper.logInExistingMember(fakeInput);
    
                assertFalse(result.getPasswordChangeStatus());
                assertTrue(BCrypt.checkpw(newPassword, result.getPassword()));
                mocked.verify(() -> MemberDao.updateMember(existingMember));
            }
        }

        @Test
        @DisplayName("multiple consecutive wrong passwords, then correct password")
        public void logInExistingMember_multipleWrongPasswordsThenCorrect() {
            String rawPassword = "finallyCorrect1";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
            Member existingMember = new Member(5, "Nina Ortiz", "nina@gmail.com", hashedPassword, false);
        
            Scanner fakeInput = new Scanner(
                "nina@gmail.com\n" +
                "wrongOne\n" +
                "nina@gmail.com\n" +
                "wrongTwo\n" +
                "nina@gmail.com\n" +
                rawPassword + "\n"
            );
        
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.getMemberByEmail("nina@gmail.com")).thenReturn(existingMember);
        
                Member result = AuthHelper.logInExistingMember(fakeInput);
        
                assertEquals(existingMember, result);
                mocked.verify(() -> MemberDao.getMemberByEmail("nina@gmail.com"), times(3));
            }
        }
        
        @Test
        @DisplayName("password change needed: DATABASE_ERROR on first update attempt, then succeeds")
        public void logInExistingMember_passwordChangeDatabaseErrorThenSuccess() {
            String tempPassword = "tempPass456";
            String hashedTempPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt(12));
            Member existingMember = new Member(6, "Derek Hall", "derek@gmail.com", hashedTempPassword, true);
        
            String newPassword = "brandNewSecurePass2";
        
            Scanner fakeInput = new Scanner(
                "derek@gmail.com\n" +
                tempPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n"
            );
            //first call is a Database error, so password change doesn't happen, which is why new Password needs to be entered again (that's why newPassword is inputted four times)
            //on second call, makeNewPassword is called again, reprompting from scratch
        
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.getMemberByEmail("derek@gmail.com")).thenReturn(existingMember);
                mocked.when(() -> MemberDao.updateMember(any()))
                    .thenReturn(DaoResult.DATABASE_ERROR, DaoResult.SUCCESS);
        
                Member result = AuthHelper.logInExistingMember(fakeInput);
        
                assertFalse(result.getPasswordChangeStatus());
                assertTrue(BCrypt.checkpw(newPassword, result.getPassword()));
                mocked.verify(() -> MemberDao.updateMember(existingMember), times(2));
            }
        }
        
        @Test
        @DisplayName("password change needed: DUPLICATE_KEY on first update attempt, then succeeds")
        public void logInExistingMember_passwordChangeDuplicateKeyThenSuccess() {
            String tempPassword = "tempPass789";
            String hashedTempPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt(12));
            Member existingMember = new Member(7, "Priya Nair", "priya@gmail.com", hashedTempPassword, true);
        
            String newPassword = "brandNewSecurePass3";
        
            Scanner fakeInput = new Scanner(
                "priya@gmail.com\n" +
                tempPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n"
            );
        
            try (MockedStatic<MemberDao> mocked = Mockito.mockStatic(MemberDao.class)) {
                mocked.when(() -> MemberDao.getMemberByEmail("priya@gmail.com")).thenReturn(existingMember);
                mocked.when(() -> MemberDao.updateMember(any()))
                    .thenReturn(DaoResult.DUPLICATE_KEY, DaoResult.SUCCESS);
        
                Member result = AuthHelper.logInExistingMember(fakeInput);
        
                assertFalse(result.getPasswordChangeStatus());
                assertTrue(BCrypt.checkpw(newPassword, result.getPassword()));
                mocked.verify(() -> MemberDao.updateMember(existingMember), times(2));
            }
        }
    }

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
            Scanner fakeInput = new Scanner("sara\nsara@gmail.com\n");
            String result = AuthHelper.getEmailInput(fakeInput);
            assertEquals("sara@gmail.com", result);
        }

        @Test
        @DisplayName("invalid empty email input and then valid email given")
        public void getEmailInput_emptyInput() {
            Scanner fakeInput = new Scanner("\nkatherine@gmail.com\n");
            String result = AuthHelper.getEmailInput(fakeInput);
            assertEquals("katherine@gmail.com", result);
        }

        @Test
        @DisplayName("invalid email with no @ symbol, invalid empty email input,then valid email given")
        public void getEmailInput_emptyAndNoAtSymbolInput() {
            Scanner fakeInput = new Scanner("rachel\n\nrachel@gmail.com\n");
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
            Scanner fakeInput = new Scanner("\nsave_the_sharks\nsave_the_sharks\n");
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
            Scanner fakeInput = new Scanner("greenflowers4me\ngreen\ngreenflower\ngreenflowers4\ngreenflowers4me\n");
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
    @DisplayName("logInAsAdmin tests")
    class LogInAsAdminTests {
    
        @Test
        @DisplayName("happy path: valid username, correct password, no password change needed")
        public void logInAsAdmin_happyPath() {
            String rawPassword = "adminPass123";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
            Admin existingAdmin = new Admin(1, "jsmith", hashedPassword, "Jane Smith", "admin", false);
    
            Scanner fakeInput = new Scanner("jsmith\n" + rawPassword + "\n");
    
            try (MockedStatic<AdminDao> mocked = Mockito.mockStatic(AdminDao.class)) {
                mocked.when(() -> AdminDao.logInWithUsername("jsmith")).thenReturn(existingAdmin);
    
                Admin result = AuthHelper.logInAsAdmin(fakeInput);
    
                assertEquals(existingAdmin, result);
                mocked.verify(() -> AdminDao.logInWithUsername("jsmith"), times(1));
                mocked.verify(() -> AdminDao.updateAdmin(any()), Mockito.never());
                //Mockito.never() makes sure the updateAdmin method was not called
            }
        }
    
        @Test
        @DisplayName("wrong password once, then correct password")
        public void logInAsAdmin_wrongPasswordThenCorrect() {
            String rawPassword = "realAdminPass1";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
            Admin existingAdmin = new Admin(2, "tbaker", hashedPassword, "Tom Baker", "admin", false);
    
            Scanner fakeInput = new Scanner(
                "tbaker\n" +
                "wrongPassword\n" +
                "tbaker\n" +
                rawPassword + "\n"
            );
    
            try (MockedStatic<AdminDao> mocked = Mockito.mockStatic(AdminDao.class)) {
                mocked.when(() -> AdminDao.logInWithUsername("tbaker")).thenReturn(existingAdmin);
    
                Admin result = AuthHelper.logInAsAdmin(fakeInput);
    
                assertEquals(existingAdmin, result);
                mocked.verify(() -> AdminDao.logInWithUsername("tbaker"), times(2));
            }
        }
    
        @Test
        @DisplayName("username not found once, then a valid username/password")
        public void logInAsAdmin_usernameNotFoundThenValid() {
            String rawPassword = "goodAdminPass1";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
            Admin existingAdmin = new Admin(3, "akim", hashedPassword, "Alice Kim", "admin", false);
    
            Scanner fakeInput = new Scanner(
                "missingUser\n" +
                "akim\n" +
                rawPassword + "\n"
            );
    
            try (MockedStatic<AdminDao> mocked = Mockito.mockStatic(AdminDao.class)) {
                mocked.when(() -> AdminDao.logInWithUsername("missingUser")).thenReturn(null);
                mocked.when(() -> AdminDao.logInWithUsername("akim")).thenReturn(existingAdmin);
    
                Admin result = AuthHelper.logInAsAdmin(fakeInput);
    
                assertEquals(existingAdmin, result);
                mocked.verify(() -> AdminDao.logInWithUsername("missingUser"));
                mocked.verify(() -> AdminDao.logInWithUsername("akim"));
            }
        }
    
        @Test
        @DisplayName("password change required, triggers second loop and updates admin")
        public void logInAsAdmin_passwordChangeRequired() {
            String tempPassword = "tempAdminPass1";
            String hashedTempPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt(12));
            Admin existingAdmin = new Admin(4, "cwhite", hashedTempPassword, "Carl White", "admin", true);
    
            String newPassword = "brandNewSecureAdminPass1";
    
            Scanner fakeInput = new Scanner(
                "cwhite\n" +
                tempPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n"
            );
    
            try (MockedStatic<AdminDao> mocked = Mockito.mockStatic(AdminDao.class)) {
                mocked.when(() -> AdminDao.logInWithUsername("cwhite")).thenReturn(existingAdmin);
                mocked.when(() -> AdminDao.updateAdmin(any())).thenReturn(DaoResult.SUCCESS);
    
                Admin result = AuthHelper.logInAsAdmin(fakeInput);
    
                assertFalse(result.getPasswordChangeStatus());
                assertTrue(BCrypt.checkpw(newPassword, result.getPassword()));
                mocked.verify(() -> AdminDao.updateAdmin(existingAdmin));
            }
        }

        @Test
        @DisplayName("multiple consecutive wrong passwords, then correct password")
        public void logInAsAdmin_multipleWrongPasswordsThenCorrect() {
            String rawPassword = "finallyCorrectAdmin1";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
            Admin existingAdmin = new Admin(5, "nortiz", hashedPassword, "Nina Ortiz", "admin", false);
        
            Scanner fakeInput = new Scanner(
                "nortiz\n" +
                "wrongOne\n" +
                "nortiz\n" +
                "wrongTwo\n" +
                "nortiz\n" +
                rawPassword + "\n"
            );
        
            try (MockedStatic<AdminDao> mocked = Mockito.mockStatic(AdminDao.class)) {
                mocked.when(() -> AdminDao.logInWithUsername("nortiz")).thenReturn(existingAdmin);
        
                Admin result = AuthHelper.logInAsAdmin(fakeInput);
        
                assertEquals(existingAdmin, result);
                mocked.verify(() -> AdminDao.logInWithUsername("nortiz"), times(3));
            }
        }
        
        @Test
        @DisplayName("password change needed: DATABASE_ERROR on first update attempt, then succeeds")
        public void logInAsAdmin_passwordChangeDatabaseErrorThenSuccess() {
            String tempPassword = "tempAdminPass456";
            String hashedTempPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt(12));
            Admin existingAdmin = new Admin(6, "dhall", hashedTempPassword, "Derek Hall", "admin", true);
        
            String newPassword = "brandNewSecureAdminPass2";
        
            Scanner fakeInput = new Scanner(
                "dhall\n" +
                tempPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n"
            );
        
            try (MockedStatic<AdminDao> mocked = Mockito.mockStatic(AdminDao.class)) {
                mocked.when(() -> AdminDao.logInWithUsername("dhall")).thenReturn(existingAdmin);
                mocked.when(() -> AdminDao.updateAdmin(any()))
                    .thenReturn(DaoResult.DATABASE_ERROR, DaoResult.SUCCESS);
        
                Admin result = AuthHelper.logInAsAdmin(fakeInput);
        
                assertFalse(result.getPasswordChangeStatus());
                assertTrue(BCrypt.checkpw(newPassword, result.getPassword()));
                mocked.verify(() -> AdminDao.updateAdmin(existingAdmin), times(2));
            }
        }
        
        @Test
        @DisplayName("password change needed: DUPLICATE_KEY on first update attempt, then succeeds")
        public void logInAsAdmin_passwordChangeDuplicateKeyThenSuccess() {
            String tempPassword = "tempAdminPass789";
            String hashedTempPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt(12));
            Admin existingAdmin = new Admin(7, "pnair", hashedTempPassword, "Priya Nair", "admin", true);
        
            String newPassword = "brandNewSecureAdminPass3";
        
            Scanner fakeInput = new Scanner(
                "pnair\n" +
                tempPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n" +
                newPassword + "\n"
            );
        
            try (MockedStatic<AdminDao> mocked = Mockito.mockStatic(AdminDao.class)) {
                mocked.when(() -> AdminDao.logInWithUsername("pnair")).thenReturn(existingAdmin);
                mocked.when(() -> AdminDao.updateAdmin(any()))
                    .thenReturn(DaoResult.DUPLICATE_KEY, DaoResult.SUCCESS);
        
                Admin result = AuthHelper.logInAsAdmin(fakeInput);
        
                assertFalse(result.getPasswordChangeStatus());
                assertTrue(BCrypt.checkpw(newPassword, result.getPassword()));
                mocked.verify(() -> AdminDao.updateAdmin(existingAdmin), times(2));
            }
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
