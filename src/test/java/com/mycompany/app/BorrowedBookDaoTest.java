package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BorrowedBookDaoTest extends AbstractDaoTest {

    // ---------- small local helpers ----------

    private static int newMember(String email) {
        return insertMember("Member " + email, email, "hash", false);
    }

    /** A book with plenty of copies; the isbn just has to be unique within a test. */
    private static int newBook(String isbn) {
        return insertBook("Title " + isbn, "Some Author", isbn, 5, 5);
    }

    private static int availableCopies(int bookId) {
        Map<String, Object> row = selectRow("SELECT available_copies FROM books WHERE id = ?", bookId);
        return ((Number) row.get("available_copies")).intValue();
    }

    private static Set<Integer> idsOf(List<BorrowedBook> loans) {
        return loans.stream().map(BorrowedBook::getId).collect(Collectors.toSet());
    }

    // ---------- borrowBook ----------

    @Nested
    class BorrowBookTests {

        @Test
        void availableBookCreatesLoanDueInTwoWeeksAndDecrementsCopies() {
            int memberId = newMember("jane@example.com");
            int bookId = insertBook("Dune", "Frank Herbert", "978-0441013593", 2, 2);
            LocalDate expectedDue = LocalDate.now().plusDays(14);

            BorrowedBookDao.borrowBook(bookId, memberId);

            assertEquals(1, countRows("borrowed_books"));
            Map<String, Object> loan = selectRow("SELECT * FROM borrowed_books WHERE book_id = ?", bookId);
            assertEquals(memberId, loan.get("member_id"));
            assertNull(loan.get("return_date"));
            // DAO computes the due date with Java's clock, so compare against Java's date
            assertEquals(expectedDue, ((java.sql.Date) loan.get("due_date")).toLocalDate());
            assertEquals(1, availableCopies(bookId));
        }

        @Test
        void bookWithNoAvailableCopiesCreatesNoLoan() {
            int memberId = newMember("jane@example.com");
            int bookId = insertBook("Dune", "Frank Herbert", "978-0441013593", 1, 0);

            BorrowedBookDao.borrowBook(bookId, memberId);

            assertEquals(0, countRows("borrowed_books"));
            assertEquals(0, availableCopies(bookId));
        }

        @Test
        void nonexistentMemberFailsAndRollsBackTheCopyCount() {
            int bookId = insertBook("Dune", "Frank Herbert", "978-0441013593", 1, 1);

            BorrowedBookDao.borrowBook(bookId, 9999);

            // The foreign key rejects the loan, and the earlier decrement must be undone
            assertEquals(0, countRows("borrowed_books"));
            assertEquals(1, availableCopies(bookId));
        }

        @Test
        void nonexistentBookThrowsNothingAndCreatesNoLoan() {
            int memberId = newMember("jane@example.com");

            assertDoesNotThrow(() -> BorrowedBookDao.borrowBook(9999, memberId));

            assertEquals(0, countRows("borrowed_books"));
        }
    }

    // ---------- returnBook ----------

    @Nested
    class ReturnBookTests {

        @Test
        void activeLoanGetsReturnDateAndCopyIsRestored() {
            int memberId = newMember("jane@example.com");
            int bookId = insertBook("Dune", "Frank Herbert", "978-0441013593", 1, 0);
            int loanId = insertLoan(bookId, memberId, 14, false);

            BorrowedBookDao.returnBook(loanId);

            Map<String, Object> loan = selectRow("SELECT * FROM borrowed_books WHERE id = ?", loanId);
            assertNotNull(loan.get("return_date"));
            assertEquals(1, availableCopies(bookId));
        }

        @Test
        void alreadyReturnedLoanDoesNotIncrementCopiesAgain() {
            int memberId = newMember("jane@example.com");
            int bookId = insertBook("Dune", "Frank Herbert", "978-0441013593", 1, 1);
            int loanId = insertLoan(bookId, memberId, 14, true);

            BorrowedBookDao.returnBook(loanId);

            assertEquals(1, availableCopies(bookId));
        }

        @Test
        void unknownLoanIdThrowsNothingAndChangesNothing() {
            int bookId = insertBook("Dune", "Frank Herbert", "978-0441013593", 1, 1);

            assertDoesNotThrow(() -> BorrowedBookDao.returnBook(9999));

            assertEquals(1, availableCopies(bookId));
            assertEquals(0, countRows("borrowed_books"));
        }
    }

    // ---------- getAllBorrowedBooks ----------

    @Nested
    class GetAllBorrowedBooksTests {

        @Test
        void emptyTableReturnsEmptyList() {
            List<BorrowedBook> loans = BorrowedBookDao.getAllBorrowedBooks();

            assertNotNull(loans);
            assertTrue(loans.isEmpty());
        }

        @Test
        void returnsOnlyUnreturnedLoansWithCorrectFields() {
            int memberId = newMember("jane@example.com");
            int book1 = newBook("111-0000000001");
            int book2 = newBook("111-0000000002");
            int activeId = insertLoan(book1, memberId, 14, false);
            insertLoan(book2, memberId, 14, true);

            List<BorrowedBook> loans = BorrowedBookDao.getAllBorrowedBooks();

            assertEquals(1, loans.size());
            BorrowedBook loan = loans.get(0);
            assertEquals(activeId, loan.getId());
            assertEquals(book1, loan.getBookId());
            assertEquals(memberId, loan.getMemberId());
            assertNull(loan.getReturnDate());
            assertNotNull(loan.getBorrowDate());
            assertNotNull(loan.getDueDate());
        }
    }

    // ---------- getBorrowedBooksByMember ----------

    @Nested
    class GetBorrowedBooksByMemberTests {

        @Test
        void returnsOnlyThatMembersUnreturnedLoans() {
            int memberA = newMember("a@example.com");
            int memberB = newMember("b@example.com");
            int book1 = newBook("222-0000000001");
            int book2 = newBook("222-0000000002");
            int book3 = newBook("222-0000000003");
            int aActive = insertLoan(book1, memberA, 14, false);
            insertLoan(book2, memberA, 14, true);   // A's returned loan: excluded
            insertLoan(book3, memberB, 14, false);  // B's loan: excluded

            List<BorrowedBook> loans = BorrowedBookDao.getBorrowedBooksByMember(memberA);

            assertEquals(Set.of(aActive), idsOf(loans));
        }
    }

    // ---------- getBorrowedBooksByBookId ----------

    @Nested
    class GetBorrowedBooksByBookIdTests {

        @Test
        void returnsOnlyThatBooksUnreturnedLoans() {
            int memberA = newMember("a@example.com");
            int memberB = newMember("b@example.com");
            int book1 = newBook("333-0000000001");
            int book2 = newBook("333-0000000002");
            int activeOnBook1 = insertLoan(book1, memberA, 14, false);
            insertLoan(book1, memberB, 14, true);   // returned loan of book1: excluded
            insertLoan(book2, memberB, 14, false);  // loan of a different book: excluded

            List<BorrowedBook> loans = BorrowedBookDao.getBorrowedBooksByBookId(book1);

            assertEquals(Set.of(activeOnBook1), idsOf(loans));
        }
    }

    // ---------- overdue ----------

    @Nested
    class GetOverdueBooksTests {

        @Test
        void returnsOnlyUnreturnedLoansWithAPastDueDate() {
            int memberId = newMember("jane@example.com");
            int book1 = newBook("444-0000000001");
            int book2 = newBook("444-0000000002");
            int book3 = newBook("444-0000000003");
            int overdueActive = insertLoan(book1, memberId, -3, false);
            insertLoan(book2, memberId, 5, false);   // due in the future: excluded
            insertLoan(book3, memberId, -3, true);   // late but returned: excluded

            List<BorrowedBook> loans = BorrowedBookDao.getOverdueBooks();

            assertEquals(Set.of(overdueActive), idsOf(loans));
        }
    }

    @Nested
    class GetOverdueBooksByMemberTests {

        @Test
        void appliesTheOverdueFilterAndOnlyReturnsThatMembersLoans() {
            int memberA = newMember("a@example.com");
            int memberB = newMember("b@example.com");
            int book1 = newBook("555-0000000001");
            int book2 = newBook("555-0000000002");
            int book3 = newBook("555-0000000003");
            int aOverdue = insertLoan(book1, memberA, -3, false);
            insertLoan(book2, memberA, 5, false);    // A's loan, not overdue: excluded
            insertLoan(book3, memberB, -3, false);   // B's overdue loan: excluded

            List<BorrowedBook> loans = BorrowedBookDao.getOverdueBooksByMember(memberA);

            assertEquals(Set.of(aOverdue), idsOf(loans));
        }
    }
}
