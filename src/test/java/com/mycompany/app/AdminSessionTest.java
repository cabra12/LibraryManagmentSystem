package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class AdminSessionTest {
    @Nested
    @DisplayName("runAdminSession tests")
    class RunAdminSessionTests {

        @Test
        @DisplayName("regular admin: invalid number, then exit with Y")
        public void runAdminSession_invalidNumberThenExit() {
            Admin admin = new Admin(1, "jdoe", "hashedpw", "John Doe", "admin", false);
            Scanner fakeInput = new Scanner("99\n6\nY\n");

            boolean result = AdminSession.runAdminSession(fakeInput, admin);

            assertFalse(result);
        }

        @Test
        @DisplayName("regular admin: immediate exit with Y")
        public void runAdminSession_immediateExitWithY() {
            Admin admin = new Admin(2, "jdoe", "hashedpw", "John Doe", "admin", false);
            Scanner fakeInput = new Scanner("6\nY\n");

            boolean result = AdminSession.runAdminSession(fakeInput, admin);

            assertFalse(result);
        }

        @Test
        @DisplayName("regular admin: invalid confirmation, then Y")
        public void runAdminSession_invalidConfirmationThenY() {
            Admin admin = new Admin(3, "jdoe", "hashedpw", "John Doe", "admin", false);
            Scanner fakeInput = new Scanner("6\nmaybe\nY\n");

            boolean result = AdminSession.runAdminSession(fakeInput, admin);

            assertFalse(result);
        }

        @Test
        @DisplayName("regular admin: N cancels exit, then exits with Y")
        public void runAdminSession_nCancelsExitThenY() {
            Admin admin = new Admin(4, "jdoe", "hashedpw", "John Doe", "admin", false);
            Scanner fakeInput = new Scanner("6\nN\n6\nY\n");

            boolean result = AdminSession.runAdminSession(fakeInput, admin);

            assertFalse(result);
        }

        @Test
        @DisplayName("superadmin: exit choice is 8, not 6")
        public void runAdminSession_superAdminExitAtEight() {
            Admin superAdmin = new Admin(5, "superjane", "hashedpw", "Jane Admin", "superadmin", false);
            Scanner fakeInput = new Scanner("8\nY\n");

            boolean result = AdminSession.runAdminSession(fakeInput, superAdmin);

            assertFalse(result);
        }

        @Test
        @DisplayName("superadmin: N cancels exit at 8, then exits with Y")
        public void runAdminSession_superAdminNCancelsExitThenY() {
            Admin superAdmin = new Admin(6, "superjane", "hashedpw", "Jane Admin", "superadmin", false);
            Scanner fakeInput = new Scanner("8\nN\n8\nY\n");

            boolean result = AdminSession.runAdminSession(fakeInput, superAdmin);

            assertFalse(result);
        }
    }

@Nested
@DisplayName("addUpdateDeleteItem tests")
class AddUpdateDeleteItemTests {

    @Test
    @DisplayName("invalid input once, then valid Add")
    public void addUpdateDeleteItem_invalidThenAdd() {
        Scanner fakeInput = new Scanner("xyz\nAdd\n");
        String result = AdminSession.addUpdateDeleteItem(fakeInput, "book");
        assertEquals("Add", result);
    }
}

@Nested
@DisplayName("searchForBooks tests")
class SearchForBooksTests {

    @Test
    @DisplayName("empty result once, then populated result")
    public void searchForBooks_emptyThenPopulated() {
        Scanner fakeInput = new Scanner("Title\nMissing\nTitle\nHobbit\n");
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
            mockedBook.when(() -> BookDao.searchByTitle("Missing"))
                .thenReturn(Collections.emptyList());
            mockedBook.when(() -> BookDao.searchByTitle("Hobbit"))
                .thenReturn(Arrays.asList(book));

            List<Book> result = AdminSession.searchForBooks(fakeInput);

            assertEquals(1, result.size());
        }
    }
}

@Nested
@DisplayName("findBookByIdInput tests")
class FindBookByIdInputTests {

    @Test
    @DisplayName("non-numeric input once, then valid ID")
    public void findBookByIdInput_nonNumericThenValid() {
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Scanner fakeInput = new Scanner("abc\n5\n");

        Book result = AdminSession.findBookByIdInput(Arrays.asList(book), fakeInput, "update");

        assertEquals(5, result.getId());
    }

    @Test
    @DisplayName("valid but non-matching ID once, then a matching ID")
    public void findBookByIdInput_nonMatchingThenMatching() {
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Scanner fakeInput = new Scanner("99\n5\n");

        Book result = AdminSession.findBookByIdInput(Arrays.asList(book), fakeInput, "update");

        assertEquals(5, result.getId());
    }
}

@Nested
@DisplayName("changeBookDetails tests")
class ChangeBookDetailsTests {

    @Test
    @DisplayName("invalid symbol once, then T updates title")
    public void changeBookDetails_invalidThenTitle() {
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Scanner fakeInput = new Scanner("xyz\nT\nThe Hobbit Revised\n");

        Book result = AdminSession.changeBookDetails(book, fakeInput);

        assertEquals("The Hobbit Revised", result.getTitle());
    }

    @Test
    @DisplayName("TC: retries when new total is less than available copies")
    public void changeBookDetails_totalCopiesRetry() {
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Scanner fakeInput = new Scanner("TC\n1\n5\n");

        Book result = AdminSession.changeBookDetails(book, fakeInput);

        assertEquals(5, result.getTotalCopies());
    }

    @Test
    @DisplayName("AC: retries when new available is greater than total copies")
    public void changeBookDetails_availableCopiesRetry() {
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Scanner fakeInput = new Scanner("AC\n10\n2\n");

        Book result = AdminSession.changeBookDetails(book, fakeInput);

        assertEquals(2, result.getAvailableCopies());
    }
}

@Nested
@DisplayName("adminActions case 1 (Manage Books) tests")
class AdminActionsCase1Tests {

    @Test
    @DisplayName("Add: happy path, unique ISBN, valid copies")
    public void case1_add_happyPath() {
        Scanner fakeInput = new Scanner(
            "Add\n" +
            "The Hobbit\nTolkien\n111-111\n" +
            "3\n2\n"
        );

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
            mockedBook.when(() -> BookDao.getBookByIsbn("111-111")).thenReturn(null);
            mockedBook.when(() -> BookDao.addBook(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(1, fakeInput, false);

            mockedBook.verify(() -> BookDao.addBook(any()));
        }
    }

    @Test
    @DisplayName("Add: duplicate ISBN detected by pre-check, loops back, then succeeds with new details")
    public void case1_add_duplicateIsbnPreCheckThenSuccess() {
        Scanner fakeInput = new Scanner(
            "Add\n" +
            "The Hobbit\nTolkien\n111-111\n" +
            "The Hobbit\nTolkien\n222-222\n" +
            "3\n2\n"
        );

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
            mockedBook.when(() -> BookDao.getBookByIsbn("111-111")).thenReturn(new Book(1, "Existing", "Someone", "111-111", 1, 1));
            mockedBook.when(() -> BookDao.getBookByIsbn("222-222")).thenReturn(null);
            mockedBook.when(() -> BookDao.addBook(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(1, fakeInput, false);

            mockedBook.verify(() -> BookDao.addBook(any()), times(1));
        }
    }

    @Test
    @DisplayName("Add: available copies greater than total, retries, then succeeds")
    public void case1_add_availableGreaterThanTotalRetry() {
        Scanner fakeInput = new Scanner(
            "Add\n" +
            "The Hobbit\nTolkien\n111-111\n" +
            "3\n10\n2\n"
        );

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
            mockedBook.when(() -> BookDao.getBookByIsbn("111-111")).thenReturn(null);
            mockedBook.when(() -> BookDao.addBook(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(1, fakeInput, false);

            mockedBook.verify(() -> BookDao.addBook(any()));
        }
    }

    @Test
    @DisplayName("Add: addBook returns DUPLICATE_KEY, retries with new details, then succeeds")
    public void case1_add_addBookDuplicateKeyThenSuccess() {
        Scanner fakeInput = new Scanner(
            "Add\n" +
            "The Hobbit\nTolkien\n111-111\n" +
            "3\n2\n" +
            "The Hobbit\nTolkien\n222-222\n" +
            "3\n2\n"
        );

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
            mockedBook.when(() -> BookDao.getBookByIsbn(any())).thenReturn(null);
            mockedBook.when(() -> BookDao.addBook(any()))
                .thenReturn(DaoResult.DUPLICATE_KEY, DaoResult.SUCCESS);

            AdminSession.adminActions(1, fakeInput, false);

            mockedBook.verify(() -> BookDao.addBook(any()), times(2));
        }
    }

    @Test
    @DisplayName("Update: happy path, search, select, change title, succeeds")
    public void case1_update_happyPath() {
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Scanner fakeInput = new Scanner(
            "Update\n" +
            "Title\nHobbit\n" +
            "5\n" +
            "T\nThe Hobbit Revised\n"
        );

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
            mockedBook.when(() -> BookDao.searchByTitle("Hobbit")).thenReturn(Arrays.asList(book));
            mockedBook.when(() -> BookDao.updateBook(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(1, fakeInput, false);

            mockedBook.verify(() -> BookDao.updateBook(any()));
        }
    }

    @Test
    @DisplayName("Update: updateBook returns DATABASE_ERROR, retries, then succeeds")
    public void case1_update_databaseErrorThenSuccess() {
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Scanner fakeInput = new Scanner(
            "Update\n" +
            "Title\nHobbit\n" +
            "5\n" +
            "T\nThe Hobbit Revised\n" +
            "T\nThe Hobbit Final\n"
        );

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
            mockedBook.when(() -> BookDao.searchByTitle("Hobbit")).thenReturn(Arrays.asList(book));
            mockedBook.when(() -> BookDao.updateBook(any()))
                .thenReturn(DaoResult.DATABASE_ERROR, DaoResult.SUCCESS);

            AdminSession.adminActions(1, fakeInput, false);

            mockedBook.verify(() -> BookDao.updateBook(any()), times(2));
        }
    }

    @Test
    @DisplayName("Delete: book has no active borrows, gets deleted")
    public void case1_delete_noActiveBorrows() {
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Scanner fakeInput = new Scanner(
            "Delete\n" +
            "Title\nHobbit\n" +
            "5\n"
        );

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
             MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {

            mockedBook.when(() -> BookDao.searchByTitle("Hobbit")).thenReturn(Arrays.asList(book));
            mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByBookId(5))
                .thenReturn(Collections.emptyList());

            AdminSession.adminActions(1, fakeInput, false);

            mockedBook.verify(() -> BookDao.deleteBook(5));
        }
    }

    @Test
    @DisplayName("Delete: book has active borrows, is not deleted")
    public void case1_delete_hasActiveBorrows() {
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        BorrowedBook activeBorrow = new BorrowedBook(10, 5, 1, null, null, null);
        Scanner fakeInput = new Scanner(
            "Delete\n" +
            "Title\nHobbit\n" +
            "5\n"
        );

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
             MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {

            mockedBook.when(() -> BookDao.searchByTitle("Hobbit")).thenReturn(Arrays.asList(book));
            mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByBookId(5))
                .thenReturn(Arrays.asList(activeBorrow));

            AdminSession.adminActions(1, fakeInput, false);

            mockedBook.verify(() -> BookDao.deleteBook(anyInt()), Mockito.never());
        }
    }
}

@Nested
@DisplayName("searchMembersAndDisplay tests")
class SearchMembersAndDisplayTests {

    @Test
    @DisplayName("email search, found immediately")
    public void searchMembersAndDisplay_emailFoundImmediately() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("Email\njane@gmail.com\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
            mockedMember.when(() -> MemberDao.getMemberByEmail("jane@gmail.com")).thenReturn(member);

            Member result = AdminSession.searchMembersAndDisplay(fakeInput, "update");

            assertEquals(member, result);
        }
    }

    @Test
    @DisplayName("email search, not found once, then found")
    public void searchMembersAndDisplay_emailNotFoundThenFound() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("Email\nmissing@gmail.com\nEmail\njane@gmail.com\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
            mockedMember.when(() -> MemberDao.getMemberByEmail("missing@gmail.com")).thenReturn(null);
            mockedMember.when(() -> MemberDao.getMemberByEmail("jane@gmail.com")).thenReturn(member);

            Member result = AdminSession.searchMembersAndDisplay(fakeInput, "update");

            assertEquals(member, result);
        }
    }

    @Test
    @DisplayName("name search, list found, valid ID first try")
    public void searchMembersAndDisplay_nameFoundValidIdFirstTry() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("Name\nJane\n1\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
            mockedMember.when(() -> MemberDao.searchByName("Jane")).thenReturn(Arrays.asList(member));

            Member result = AdminSession.searchMembersAndDisplay(fakeInput, "update");

            assertEquals(member, result);
        }
    }

    @Test
    @DisplayName("name search, list found, invalid ID once, then valid")
    public void searchMembersAndDisplay_nameFoundInvalidIdThenValid() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("Name\nJane\n99\n1\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
            mockedMember.when(() -> MemberDao.searchByName("Jane")).thenReturn(Arrays.asList(member));

            Member result = AdminSession.searchMembersAndDisplay(fakeInput, "update");

            assertEquals(member, result);
        }
    }
}

@Nested
@DisplayName("changeMemberDetails tests")
class ChangeMemberDetailsTests {

    @Test
    @DisplayName("invalid symbol once, then N updates name")
    public void changeMemberDetails_invalidThenName() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("xyz\nN\nJanet Doe\n");

        Member result = AdminSession.changeMemberDetails(member, fakeInput);

        assertEquals("Janet Doe", result.getName());
    }

    @Test
    @DisplayName("E updates email")
    public void changeMemberDetails_updatesEmail() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("E\njanet@gmail.com\n");

        Member result = AdminSession.changeMemberDetails(member, fakeInput);

        assertEquals("janet@gmail.com", result.getEmail());
    }

    @Test
    @DisplayName("P resets password and sets passwordChangeStatus to true")
    public void changeMemberDetails_resetsPassword() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "oldHashedPw", false);
        Scanner fakeInput = new Scanner("P\n");

        Member result = AdminSession.changeMemberDetails(member, fakeInput);

        assertNotEquals("oldHashedPw", result.getPassword());
        assertTrue(result.getPasswordChangeStatus());
    }
}

@Nested
@DisplayName("adminActions case 2 (Manage Members) tests")
class AdminActionsCase2Tests {

    @Test
    @DisplayName("Add: happy path")
    public void case2_add_happyPath() {
        Scanner fakeInput = new Scanner("Add\nJohn Smith\njohn@gmail.com\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
            mockedMember.when(() -> MemberDao.getMemberByEmail("john@gmail.com")).thenReturn(null);
            mockedMember.when(() -> MemberDao.addMember(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(2, fakeInput, false);

            mockedMember.verify(() -> MemberDao.addMember(any()));
        }
    }

    @Test
    @DisplayName("Add: duplicate email pre-check, loops back, then succeeds")
    public void case2_add_duplicateEmailPreCheckThenSuccess() {
        Member existingMember = new Member(2, "Existing", "taken@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("Add\nJohn Smith\ntaken@gmail.com\njohn@gmail.com\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
            mockedMember.when(() -> MemberDao.getMemberByEmail("taken@gmail.com")).thenReturn(existingMember);
            mockedMember.when(() -> MemberDao.getMemberByEmail("john@gmail.com")).thenReturn(null);
            mockedMember.when(() -> MemberDao.addMember(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(2, fakeInput, false);

            mockedMember.verify(() -> MemberDao.addMember(any()), times(1));
        }
    }

    @Test
    @DisplayName("Add: addMember returns DUPLICATE_KEY, retries, then succeeds")
    public void case2_add_addMemberDuplicateKeyThenSuccess() {
        Scanner fakeInput = new Scanner("Add\nJohn Smith\njohn@gmail.com\nJohn Smith\njohn2@gmail.com\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
            mockedMember.when(() -> MemberDao.getMemberByEmail(any())).thenReturn(null);
            mockedMember.when(() -> MemberDao.addMember(any()))
                .thenReturn(DaoResult.DUPLICATE_KEY, DaoResult.SUCCESS);

            AdminSession.adminActions(2, fakeInput, false);

            mockedMember.verify(() -> MemberDao.addMember(any()), times(2));
        }
    }

    @Test
    @DisplayName("Update: happy path, email search, change name, succeeds")
    public void case2_update_happyPath() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("Update\nEmail\njane@gmail.com\nN\nJanet Doe\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
            mockedMember.when(() -> MemberDao.getMemberByEmail("jane@gmail.com")).thenReturn(member);
            mockedMember.when(() -> MemberDao.updateMember(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(2, fakeInput, false);

            mockedMember.verify(() -> MemberDao.updateMember(any()));
        }
    }

    @Test
    @DisplayName("Update: updateMember returns DATABASE_ERROR, retries, then succeeds")
    public void case2_update_databaseErrorThenSuccess() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("Update\nEmail\njane@gmail.com\nN\nJanet Doe\nN\nJanet Smith\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
            mockedMember.when(() -> MemberDao.getMemberByEmail("jane@gmail.com")).thenReturn(member);
            mockedMember.when(() -> MemberDao.updateMember(any()))
                .thenReturn(DaoResult.DATABASE_ERROR, DaoResult.SUCCESS);

            AdminSession.adminActions(2, fakeInput, false);

            mockedMember.verify(() -> MemberDao.updateMember(any()), times(2));
        }
    }

    @Test
    @DisplayName("Delete: no active borrows, member deleted")
    public void case2_delete_noActiveBorrows() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("Delete\nEmail\njane@gmail.com\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class);
             MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {

            mockedMember.when(() -> MemberDao.getMemberByEmail("jane@gmail.com")).thenReturn(member);
            mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                .thenReturn(Collections.emptyList());

            AdminSession.adminActions(2, fakeInput, false);

            mockedMember.verify(() -> MemberDao.deleteMember(1));
        }
    }

    @Test
    @DisplayName("Delete: has active borrows, member not deleted")
    public void case2_delete_hasActiveBorrows() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        BorrowedBook activeBorrow = new BorrowedBook(10, 5, 1, null, null, null);
        Scanner fakeInput = new Scanner("Delete\nEmail\njane@gmail.com\n");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class);
             MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {

            mockedMember.when(() -> MemberDao.getMemberByEmail("jane@gmail.com")).thenReturn(member);
            mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                .thenReturn(Arrays.asList(activeBorrow));

            AdminSession.adminActions(2, fakeInput, false);

            mockedMember.verify(() -> MemberDao.deleteMember(anyInt()), Mockito.never());
        }
    }
}

@Nested
@DisplayName("printBorrowedBooks tests")
class PrintBorrowedBooksTests {

    @Test
    @DisplayName("empty list produces no output and no DAO calls")
    public void printBorrowedBooks_emptyList() {
        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
             MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {

            AdminSession.printBorrowedBooks(Collections.emptyList());

            mockedBook.verify(() -> BookDao.getBookById(anyInt()), Mockito.never());
            mockedMember.verify(() -> MemberDao.getMemberId(anyInt()), Mockito.never());
        }
    }

    @Test
    @DisplayName("populated list prints book and member details")
    public void printBorrowedBooks_populatedList() {
        BorrowedBook borrowedBook = new BorrowedBook(10, 5, 1, null, null, null);
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);

        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outContent));

        try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
             MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {

            mockedBook.when(() -> BookDao.getBookById(5)).thenReturn(book);
            mockedMember.when(() -> MemberDao.getMemberId(1)).thenReturn(member);

            AdminSession.printBorrowedBooks(Arrays.asList(borrowedBook));
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("The Hobbit"));
        assertTrue(output.contains("Tolkien"));
        assertTrue(output.contains("Jane Doe"));
    }
}

@Nested
@DisplayName("printAllMembers tests")
class PrintAllMembersTests {

    @Test
    @DisplayName("empty list produces no output and no DAO calls")
    public void printAllMembers_emptyList() {
        try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {

            AdminSession.printAllMembers(Collections.emptyList());

            mockedBorrowed.verify(() -> BorrowedBookDao.getBorrowedBooksByMember(anyInt()), Mockito.never());
            mockedBorrowed.verify(() -> BorrowedBookDao.getOverdueBooksByMember(anyInt()), Mockito.never());
        }
    }

    @Test
    @DisplayName("populated list prints member details with correct counts")
    public void printAllMembers_populatedList() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        BorrowedBook borrowedBook = new BorrowedBook(10, 5, 1, null, null, null);

        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outContent));

        try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {
            mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                .thenReturn(Arrays.asList(borrowedBook));
            mockedBorrowed.when(() -> BorrowedBookDao.getOverdueBooksByMember(1))
                .thenReturn(Collections.emptyList());

            AdminSession.printAllMembers(Arrays.asList(member));
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("Jane Doe"));
        assertTrue(output.contains("Number of Books Borrowed: 1"));
        assertTrue(output.contains("Number of Overdue Books: 0"));
    }
}

@Nested
@DisplayName("adminActions cases 3, 4, 5 tests")
class AdminActionsCase345Tests {

    @Test
    @DisplayName("case 3: fetches and prints all members")
    public void case3_viewAllMembers() {
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("");

        try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class);
             MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {

            mockedMember.when(MemberDao::getAllMembers).thenReturn(Arrays.asList(member));
            mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                .thenReturn(Collections.emptyList());
            mockedBorrowed.when(() -> BorrowedBookDao.getOverdueBooksByMember(1))
                .thenReturn(Collections.emptyList());

            AdminSession.adminActions(3, fakeInput, false);

            mockedMember.verify(MemberDao::getAllMembers);
        }
    }

    @Test
    @DisplayName("case 4: fetches and prints all borrowed books")
    public void case4_viewAllBorrowedBooks() {
        BorrowedBook borrowedBook = new BorrowedBook(10, 5, 1, null, null, null);
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("");

        try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class);
             MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
             MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {

            mockedBorrowed.when(BorrowedBookDao::getAllBorrowedBooks).thenReturn(Arrays.asList(borrowedBook));
            mockedBook.when(() -> BookDao.getBookById(5)).thenReturn(book);
            mockedMember.when(() -> MemberDao.getMemberId(1)).thenReturn(member);

            AdminSession.adminActions(4, fakeInput, false);

            mockedBorrowed.verify(BorrowedBookDao::getAllBorrowedBooks);
        }
    }

    @Test
    @DisplayName("case 5: fetches and prints overdue books")
    public void case5_viewOverdueBooks() {
        BorrowedBook borrowedBook = new BorrowedBook(10, 5, 1, null, null, null);
        Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        Member member = new Member(1, "Jane Doe", "jane@gmail.com", "hashedpw", false);
        Scanner fakeInput = new Scanner("");

        try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class);
             MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
             MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {

            mockedBorrowed.when(BorrowedBookDao::getOverdueBooks).thenReturn(Arrays.asList(borrowedBook));
            mockedBook.when(() -> BookDao.getBookById(5)).thenReturn(book);
            mockedMember.when(() -> MemberDao.getMemberId(1)).thenReturn(member);

            AdminSession.adminActions(5, fakeInput, false);

            mockedBorrowed.verify(BorrowedBookDao::getOverdueBooks);
        }
    }
}

@Nested
@DisplayName("searchAdminsAndDisplayOne tests")
class SearchAdminsAndDisplayOneTests {

    @Test
    @DisplayName("non-numeric ID once, then valid ID, yes confirms")
    public void searchAdminsAndDisplayOne_nonNumericThenValidYes() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("abc\n2\nyes\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(AdminDao::getAllAdmins).thenReturn(Arrays.asList(admin));
            mockedAdmin.when(() -> AdminDao.getAdminById(2)).thenReturn(admin);

            Admin result = AdminSession.searchAdminsAndDisplayOne(fakeInput, "update");

            assertEquals(admin, result);
        }
    }

    @Test
    @DisplayName("ID not found once, then found, yes confirms")
    public void searchAdminsAndDisplayOne_notFoundThenFoundYes() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("99\n2\nyes\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(AdminDao::getAllAdmins).thenReturn(Arrays.asList(admin));
            mockedAdmin.when(() -> AdminDao.getAdminById(99)).thenReturn(null);
            mockedAdmin.when(() -> AdminDao.getAdminById(2)).thenReturn(admin);

            Admin result = AdminSession.searchAdminsAndDisplayOne(fakeInput, "update");

            assertEquals(admin, result);
        }
    }

    @Test
    @DisplayName("found, no rejects, then found again, yes confirms")
    public void searchAdminsAndDisplayOne_foundNoThenYes() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("2\nno\n2\nyes\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(AdminDao::getAllAdmins).thenReturn(Arrays.asList(admin));
            mockedAdmin.when(() -> AdminDao.getAdminById(2)).thenReturn(admin);

            Admin result = AdminSession.searchAdminsAndDisplayOne(fakeInput, "update");

            assertEquals(admin, result);
        }
    }

    @Test
    @DisplayName("invalid confirmation once, then yes")
    public void searchAdminsAndDisplayOne_invalidConfirmationThenYes() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("2\nmaybe\n2\nyes\n");
    
        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(AdminDao::getAllAdmins).thenReturn(Arrays.asList(admin));
            mockedAdmin.when(() -> AdminDao.getAdminById(2)).thenReturn(admin);
    
            Admin result = AdminSession.searchAdminsAndDisplayOne(fakeInput, "update");
    
            assertEquals(admin, result);
        }
    }
}

@Nested
@DisplayName("changeAdminDetails tests")
class ChangeAdminDetailsTests {

    @Test
    @DisplayName("invalid symbol once, then N updates name")
    public void changeAdminDetails_invalidThenName() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("xyz\nN\nJohnny Smith\n");

        Admin result = AdminSession.changeAdminDetails(admin, fakeInput);

        assertEquals("Johnny Smith", result.getName());
    }

    @Test
    @DisplayName("U updates username")
    public void changeAdminDetails_updatesUsername() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("U\njsmith2\n");

        Admin result = AdminSession.changeAdminDetails(admin, fakeInput);

        assertEquals("jsmith2", result.getUsername());
    }

    @Test
    @DisplayName("P resets password and sets passwordChangeStatus to true")
    public void changeAdminDetails_resetsPassword() {
        Admin admin = new Admin(2, "jsmith", "oldHashedPw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("P\n");

        Admin result = AdminSession.changeAdminDetails(admin, fakeInput);

        assertNotEquals("oldHashedPw", result.getPassword());
        assertTrue(result.getPasswordChangeStatus());
    }
}

@Nested
@DisplayName("adminActions case 6 (Manage Admin) tests")
class AdminActionsCase6Tests {

    @Test
    @DisplayName("access denied for regular admin")
    public void case6_accessDeniedForRegularAdmin() {
        Scanner fakeInput = new Scanner("");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            AdminSession.adminActions(6, fakeInput, false);

            mockedAdmin.verify(AdminDao::getAllAdmins, Mockito.never());
            mockedAdmin.verify(() -> AdminDao.addAdmin(any()), Mockito.never());
        }
    }

    @Test
    @DisplayName("Add: happy path")
    public void case6_add_happyPath() {
        Scanner fakeInput = new Scanner("Add\nnewadmin\nNew Admin\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(() -> AdminDao.getAdminByUsername("newadmin")).thenReturn(null);
            mockedAdmin.when(() -> AdminDao.addAdmin(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(6, fakeInput, true);

            mockedAdmin.verify(() -> AdminDao.addAdmin(any()));
        }
    }

    @Test
    @DisplayName("Add: duplicate username pre-check, loops back, then succeeds")
    public void case6_add_duplicateUsernamePreCheckThenSuccess() {
        Admin existingAdmin = new Admin(3, "taken", "hashedpw", "Existing", "admin", false);
        Scanner fakeInput = new Scanner("Add\ntaken\nnewadmin\nNew Admin\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(() -> AdminDao.getAdminByUsername("taken")).thenReturn(existingAdmin);
            mockedAdmin.when(() -> AdminDao.getAdminByUsername("newadmin")).thenReturn(null);
            mockedAdmin.when(() -> AdminDao.addAdmin(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(6, fakeInput, true);

            mockedAdmin.verify(() -> AdminDao.addAdmin(any()), times(1));
        }
    }

    @Test
    @DisplayName("Add: addAdmin returns DUPLICATE_KEY, retries, then succeeds")
    public void case6_add_addAdminDuplicateKeyThenSuccess() {
        Scanner fakeInput = new Scanner("Add\nnewadmin\nNew Admin\nnewadmin2\nNew Admin\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(() -> AdminDao.getAdminByUsername(any())).thenReturn(null);
            mockedAdmin.when(() -> AdminDao.addAdmin(any()))
                .thenReturn(DaoResult.DUPLICATE_KEY, DaoResult.SUCCESS);

            AdminSession.adminActions(6, fakeInput, true);

            mockedAdmin.verify(() -> AdminDao.addAdmin(any()), times(2));
        }
    }

    @Test
    @DisplayName("Update: happy path")
    public void case6_update_happyPath() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("Update\n2\nyes\nN\nJohnny Smith\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(AdminDao::getAllAdmins).thenReturn(Arrays.asList(admin));
            mockedAdmin.when(() -> AdminDao.getAdminById(2)).thenReturn(admin);
            mockedAdmin.when(() -> AdminDao.updateAdmin(any())).thenReturn(DaoResult.SUCCESS);

            AdminSession.adminActions(6, fakeInput, true);

            mockedAdmin.verify(() -> AdminDao.updateAdmin(any()));
        }
    }

    @Test
    @DisplayName("Update: updateAdmin returns DATABASE_ERROR, retries, then succeeds")
    public void case6_update_databaseErrorThenSuccess() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("Update\n2\nyes\nN\nJohnny Smith\nN\nJohn S.\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(AdminDao::getAllAdmins).thenReturn(Arrays.asList(admin));
            mockedAdmin.when(() -> AdminDao.getAdminById(2)).thenReturn(admin);
            mockedAdmin.when(() -> AdminDao.updateAdmin(any()))
                .thenReturn(DaoResult.DATABASE_ERROR, DaoResult.SUCCESS);

            AdminSession.adminActions(6, fakeInput, true);

            mockedAdmin.verify(() -> AdminDao.updateAdmin(any()), times(2));
        }
    }

    @Test
    @DisplayName("Delete: regular admin found, deleted")
    public void case6_delete_regularAdmin() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("Delete\n2\nyes\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(AdminDao::getAllAdmins).thenReturn(Arrays.asList(admin));
            mockedAdmin.when(() -> AdminDao.getAdminById(2)).thenReturn(admin);

            AdminSession.adminActions(6, fakeInput, true);

            mockedAdmin.verify(() -> AdminDao.deleteAdmin(2));
        }
    }

    @Test
    @DisplayName("Delete: superadmin role found, blocked from deletion")
    public void case6_delete_superAdminBlocked() {
        Admin superAdmin = new Admin(1, "superjane", "hashedpw", "Jane Admin", "superadmin", false);
        Scanner fakeInput = new Scanner("Delete\n1\nyes\n");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(AdminDao::getAllAdmins).thenReturn(Arrays.asList(superAdmin));
            mockedAdmin.when(() -> AdminDao.getAdminById(1)).thenReturn(superAdmin);

            AdminSession.adminActions(6, fakeInput, true);

            mockedAdmin.verify(() -> AdminDao.deleteAdmin(anyInt()), Mockito.never());
        }
    }
}

@Nested
@DisplayName("adminActions case 7 (View all Admin) tests")
class AdminActionsCase7Tests {

    @Test
    @DisplayName("access denied for regular admin")
    public void case7_accessDeniedForRegularAdmin() {
        Scanner fakeInput = new Scanner("");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            AdminSession.adminActions(7, fakeInput, false);

            mockedAdmin.verify(AdminDao::getAllAdmins, Mockito.never());
        }
    }

    @Test
    @DisplayName("superadmin: fetches and prints all admins")
    public void case7_viewAllAdmins() {
        Admin admin = new Admin(2, "jsmith", "hashedpw", "John Smith", "admin", false);
        Scanner fakeInput = new Scanner("");

        try (MockedStatic<AdminDao> mockedAdmin = Mockito.mockStatic(AdminDao.class)) {
            mockedAdmin.when(AdminDao::getAllAdmins).thenReturn(Arrays.asList(admin));

            AdminSession.adminActions(7, fakeInput, true);

            mockedAdmin.verify(AdminDao::getAllAdmins);
        }
    }
}

}
