package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
// import java.util.List;
import java.util.Scanner;

public class MemberSessionTest {

    @Nested
    @DisplayName("runMemberSession tests")
    class RunMemberSessionTests {

        @Test
        @DisplayName("invalid menu number, then valid action, then exit with Y")
        public void runMemberSession_invalidNumberThenValidActionThenExit() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("99\n4\n6\nY\n");
            BorrowedBook borrowedBook = new BorrowedBook(10, 5, 1, null, null, null);
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        
            try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class);
                 MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
        
                mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                    .thenReturn(Arrays.asList(borrowedBook));
                mockedBook.when(() -> BookDao.getBookById(5)).thenReturn(book);
        
                boolean result = MemberSession.runMemberSession(fakeInput, member);
        
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("choice 6 immediately, confirms exit with Y")
        public void runMemberSession_immediateExitWithY() {
            Member member = new Member(2, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("6\nY\n");

            boolean result = MemberSession.runMemberSession(fakeInput, member);

            assertFalse(result);
        }

        @Test
        @DisplayName("choice 6, invalid confirmation input, then Y")
        public void runMemberSession_invalidConfirmationThenY() {
            Member member = new Member(3, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("6\nmaybe\nY\n");

            boolean result = MemberSession.runMemberSession(fakeInput, member);

            assertFalse(result);
        }

        @Test
        @DisplayName("choice 6, N cancels exit, session continues, then exits with Y")
        public void runMemberSession_nCancelsExitThenY() {
            Member member = new Member(4, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("6\nN\n6\nY\n");

            boolean result = MemberSession.runMemberSession(fakeInput, member);

            assertFalse(result);
        }

        @Test
        @DisplayName("choice 1 dispatches to search books")
        public void runMemberSession_dispatchesToCase1() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("1\nAuthor\nTolkien\n6\nY\n");
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
    
            try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
                mockedBook.when(() -> BookDao.searchByAuthor("Tolkien"))
                    .thenReturn(Arrays.asList(book));
    
                boolean result = MemberSession.runMemberSession(fakeInput, member);
    
                mockedBook.verify(() -> BookDao.searchByAuthor("Tolkien"));
                assertFalse(result);
            }
        }
    
        @Test
        @DisplayName("choice 2 dispatches to checkout")
        public void runMemberSession_dispatchesToCase2() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("2\nAuthor\nTolkien\n5\n6\nY\n");
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
    
            try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
                 MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {
    
                mockedBook.when(() -> BookDao.searchByAuthor("Tolkien"))
                    .thenReturn(Arrays.asList(book));
    
                boolean result = MemberSession.runMemberSession(fakeInput, member);
    
                mockedBorrowed.verify(() -> BorrowedBookDao.borrowBook(5, 1));
                assertFalse(result);
            }
        }
    
        @Test
        @DisplayName("choice 3 dispatches to return book")
        public void runMemberSession_dispatchesToCase3() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("3\n10\n6\nY\n");
            BorrowedBook borrowedBook = new BorrowedBook(10, 5, 1, null, null, null);
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
    
            try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class);
                 MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
    
                mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                    .thenReturn(Arrays.asList(borrowedBook));
                mockedBook.when(() -> BookDao.getBookById(5)).thenReturn(book);
    
                boolean result = MemberSession.runMemberSession(fakeInput, member);
    
                mockedBorrowed.verify(() -> BorrowedBookDao.returnBook(10));
                assertFalse(result);
            }
        }
    
        @Test
        @DisplayName("choice 4 dispatches to view borrowed books")
        public void runMemberSession_dispatchesToCase4() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("4\n6\nY\n");
            BorrowedBook borrowedBook = new BorrowedBook(10, 5, 1, null, null, null);
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
    
            try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class);
                 MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
    
                mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                    .thenReturn(Arrays.asList(borrowedBook));
                mockedBook.when(() -> BookDao.getBookById(5)).thenReturn(book);
    
                boolean result = MemberSession.runMemberSession(fakeInput, member);
    
                mockedBorrowed.verify(() -> BorrowedBookDao.getBorrowedBooksByMember(1));
                mockedBook.verify(() -> BookDao.getBookById(5));
                assertFalse(result);
            }
        }
    
        @Test
        @DisplayName("choice 5 dispatches to change password")
        public void runMemberSession_dispatchesToCase5() {
            Member member = new Member(1, "Test User", "test@gmail.com", "oldHashedPw", false);
            Scanner fakeInput = new Scanner("5\nnewPass1\nnewPass1\n6\nY\n");
    
            try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
                mockedMember.when(() -> MemberDao.updateMember(any())).thenReturn(DaoResult.SUCCESS);
    
                boolean result = MemberSession.runMemberSession(fakeInput, member);
    
                mockedMember.verify(() -> MemberDao.updateMember(member));
                assertTrue(BCrypt.checkpw("newPass1", member.getPassword()));
                assertFalse(result);
            }
        }
    }

    @Nested
    @DisplayName("memberActions tests")
    class MemberActionsTests {

        @Test
        @DisplayName("case 1: search books by author")
        public void memberActions_case1_searchBooks() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("Author\nTolkien\n");
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);

            try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
                mockedBook.when(() -> BookDao.searchByAuthor("Tolkien"))
                    .thenReturn(Arrays.asList(book));

                MemberSession.memberActions(1, fakeInput, member);

                mockedBook.verify(() -> BookDao.searchByAuthor("Tolkien"));
            }
        }

        @Test
        @DisplayName("case 1: empty input first, then Author, valid author name")
        public void memberActions_case1_emptyThenAuthor() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("\nAuthor\nTolkien\n");
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        
            try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
                mockedBook.when(() -> BookDao.searchByAuthor("Tolkien"))
                    .thenReturn(Arrays.asList(book));
        
                MemberSession.memberActions(1, fakeInput, member);
        
                mockedBook.verify(() -> BookDao.searchByAuthor("Tolkien"));
            }
        }
        
        @Test
        @DisplayName("case 1: search books by title")
        public void memberActions_case1_searchBooksByTitle() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("Title\nHobbit\n");
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        
            try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {
                mockedBook.when(() -> BookDao.searchByTitle("Hobbit"))
                    .thenReturn(Arrays.asList(book));
        
                MemberSession.memberActions(1, fakeInput, member);
        
                mockedBook.verify(() -> BookDao.searchByTitle("Hobbit"));
            }
        }

        @Test
        @DisplayName("case 2: checkout, empty search then valid results, valid book ID")
        public void memberActions_case2_checkoutHappyPath() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("Author\nUnknown\nAuthor\nTolkien\n5\n");
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);

            try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
                 MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {

                mockedBook.when(() -> BookDao.searchByAuthor("Unknown"))
                    .thenReturn(Collections.emptyList());
                mockedBook.when(() -> BookDao.searchByAuthor("Tolkien"))
                    .thenReturn(Arrays.asList(book));

                MemberSession.memberActions(2, fakeInput, member);

                mockedBorrowed.verify(() -> BorrowedBookDao.borrowBook(5, 1));
            }
        }

        @Test
        @DisplayName("case 2: checkout, invalid book ID once, then valid")
        public void memberActions_case2_invalidIdThenValid() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("Title\nHobbit\n3\n5\n");
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);

            try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
                 MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {

                mockedBook.when(() -> BookDao.searchByTitle("Hobbit"))
                    .thenReturn(Arrays.asList(book));

                MemberSession.memberActions(2, fakeInput, member);

                mockedBorrowed.verify(() -> BorrowedBookDao.borrowBook(5, 1));
            }
        }


        @Test
        @DisplayName("case 2: checkout, non-numeric book ID once, then valid")
        public void memberActions_case2_nonNumericIdThenValid() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("Title\nHobbit\nabc\n5\n");
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);
        
            try (MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class);
                 MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {
        
                mockedBook.when(() -> BookDao.searchByTitle("Hobbit"))
                    .thenReturn(Arrays.asList(book));
        
                MemberSession.memberActions(2, fakeInput, member);
        
                mockedBorrowed.verify(() -> BorrowedBookDao.borrowBook(5, 1));
            }
        }

        @Test
        @DisplayName("case 3: return book, invalid ID once, then valid")
        public void memberActions_case3_returnBook() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("99\n10\n");
            BorrowedBook borrowedBook = new BorrowedBook(10, 5, 1, null, null, null);
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);

            try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class);
                 MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {

                mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                    .thenReturn(Arrays.asList(borrowedBook));
                mockedBook.when(() -> BookDao.getBookById(5)).thenReturn(book);

                MemberSession.memberActions(3, fakeInput, member);

                mockedBorrowed.verify(() -> BorrowedBookDao.returnBook(10));
            }
        }

        @Test
        @DisplayName("case 3: no borrowed books, returns early without prompting")
        public void memberActions_case3_noBorrowedBooks() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("");

            try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class)) {
                mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                    .thenReturn(Collections.emptyList());

                assertDoesNotThrow(() -> MemberSession.memberActions(3, fakeInput, member));

                mockedBorrowed.verify(() -> BorrowedBookDao.returnBook(anyInt()), Mockito.never());
            }
        }

        @Test
        @DisplayName("case 4: view borrowed books")
        public void memberActions_case4_viewBorrowedBooks() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("");
            BorrowedBook borrowedBook = new BorrowedBook(10, 5, 1, null, null, null);
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);

            try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class);
                 MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {

                mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                    .thenReturn(Arrays.asList(borrowedBook));
                mockedBook.when(() -> BookDao.getBookById(5)).thenReturn(book);

                MemberSession.memberActions(4, fakeInput, member);

                mockedBorrowed.verify(() -> BorrowedBookDao.getBorrowedBooksByMember(1));
                mockedBook.verify(() -> BookDao.getBookById(5));
            }
        }

        @Test
        @DisplayName("case 5: change password, mismatched once, then matched")
        public void memberActions_case5_mismatchedThenMatched() {
            Member member = new Member(1, "Test User", "test@gmail.com", "oldHashedPw", false);
            Scanner fakeInput = new Scanner("newPass1\nnewPass2\nnewPass1\nnewPass1\n");

            try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
                mockedMember.when(() -> MemberDao.updateMember(any())).thenReturn(DaoResult.SUCCESS);

                MemberSession.memberActions(5, fakeInput, member);

                assertTrue(BCrypt.checkpw("newPass1", member.getPassword()));
                mockedMember.verify(() -> MemberDao.updateMember(member), times(1));
            }
        }

        @Test
        @DisplayName("case 5: change password, DATABASE_ERROR then SUCCESS")
        public void memberActions_case5_databaseErrorThenSuccess() {
            Member member = new Member(1, "Test User", "test@gmail.com", "oldHashedPw", false);
            Scanner fakeInput = new Scanner("newPass1\nnewPass1\nnewPass1\nnewPass1\n");

            try (MockedStatic<MemberDao> mockedMember = Mockito.mockStatic(MemberDao.class)) {
                mockedMember.when(() -> MemberDao.updateMember(any()))
                    .thenReturn(DaoResult.DATABASE_ERROR, DaoResult.SUCCESS);

                MemberSession.memberActions(5, fakeInput, member);

                assertTrue(BCrypt.checkpw("newPass1", member.getPassword()));
                mockedMember.verify(() -> MemberDao.updateMember(member), times(2));
            }
        }

        @Test
        @DisplayName("default case: invalid option, no exception, no DAO calls")
        public void memberActions_defaultCase_invalidOption() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);
            Scanner fakeInput = new Scanner("");

            assertDoesNotThrow(() -> MemberSession.memberActions(99, fakeInput, member));
        }
    }

    @Nested
    @DisplayName("printBookResults tests")
    class PrintBookResultsTests {

        @Test
        @DisplayName("empty list prints not-found message")
        public void printBookResults_emptyList() {
            java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
            java.io.PrintStream originalOut = System.out;
            System.setOut(new java.io.PrintStream(outContent));

            try {
                MemberSession.printBookResults(Collections.emptyList());
            } finally {
                System.setOut(originalOut);
            }

            assertTrue(outContent.toString().contains("No books found. Please try your search again."));
        }

        @Test
        @DisplayName("null list prints not-found message")
        public void printBookResults_nullList() {
            java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
            java.io.PrintStream originalOut = System.out;
            System.setOut(new java.io.PrintStream(outContent));

            try {
                MemberSession.printBookResults(null);
            } finally {
                System.setOut(originalOut);
            }

            assertTrue(outContent.toString().contains("No books found. Please try your search again."));
        }

        @Test
        @DisplayName("populated list prints each book's details")
        public void printBookResults_populatedList() {
            Book book = new Book(5, "The Hobbit", "Tolkien", "111-111", 3, 2);

            java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
            java.io.PrintStream originalOut = System.out;
            System.setOut(new java.io.PrintStream(outContent));

            try {
                MemberSession.printBookResults(Arrays.asList(book));
            } finally {
                System.setOut(originalOut);
            }

            String output = outContent.toString();
            assertTrue(output.contains("The Hobbit"));
            assertTrue(output.contains("Tolkien"));
            assertTrue(output.contains("111-111"));
            assertTrue(output.contains("2 available copies out of 3 total copies"));
        }
    }

    @Nested
    @DisplayName("getAllBorrowedBooksByMember tests")
    class GetAllBorrowedBooksByMemberTests {

        @Test
        @DisplayName("member has no borrowed books")
        public void getAllBorrowedBooksByMember_emptyList() {
            Member member = new Member(1, "Test User", "test@gmail.com", "hashedpw", false);

            try (MockedStatic<BorrowedBookDao> mockedBorrowed = Mockito.mockStatic(BorrowedBookDao.class);
                MockedStatic<BookDao> mockedBook = Mockito.mockStatic(BookDao.class)) {

                mockedBorrowed.when(() -> BorrowedBookDao.getBorrowedBooksByMember(1))
                    .thenReturn(Collections.emptyList());

                List<BorrowedBook> result = MemberSession.getAllBorrowedBooksByMember(member);

                assertTrue(result.isEmpty());
                mockedBook.verify(() -> BookDao.getBookById(anyInt()), Mockito.never());
            }
        }
    }
}