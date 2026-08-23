package com.mycompany.app;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowedBookDao {
    
    public static void borrowBook(int bookId, int memberId) {
        //doing two separate writes (insert a borrow record, decrement available copies)

        //can't use try-with-resources, since you need the conn alive across both statements and need to explicitly call commit()/rollback() before it closes
        Connection conn = null;
        
        Book book = BookDao.getBookById(bookId);

        if(book == null) {
            throw new NullPointerException();
        }

        Date twoWeeksFromToday = Date.valueOf(LocalDate.now().plusDays(14));

        String sql1 = "INSERT into borrowed_books (book_id, member_id, due_date) VALUES (?, ?, ?)";
        String sql2 = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ?";

        try {
            conn = DBConnection.getConnection();

            if(conn == null) {
                throw new NullPointerException();
            }

            conn.setAutoCommit(false);
            //normally, SQL statements that are run are treated as a mini-transaction and executes and saved permanently to the DB
            //adding false argument turns this off, statements run but they aren't saved yet (pending)


            // statement 1: insert into borrowed_books

            try(PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
                if(book.getAvailableCopies() > 0) {
                    pstmt1.setInt(1, bookId);
                    pstmt1.setInt(2, memberId);
                    pstmt1.setDate(3, twoWeeksFromToday);
    
                    pstmt1.executeUpdate();

                    // statement 2: update books set available_copies = available_copies - 1

                    try(PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                        pstmt2.setInt(1, bookId);
                        pstmt2.executeUpdate();
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        System.out.println(book.getTitle() + " by " + book.getAuthor() + " is check out and yours. Your due date is " + sdf.format(twoWeeksFromToday));
                    }
    
                }else {
                    System.out.println("Your book is not available!");
                }
            }


            conn.commit();
            //tells database to save everything at this point, not yet permanent
        } catch (SQLException | IOException e) {
            try {
                conn.rollback();
                //tells DB "throw away everything I've done since the last commit", undoes any pending, uncommited changes
            } catch (SQLException eRoll) {
                System.out.println("Failed: " + eRoll.getMessage());
            }
            
            System.out.println("Failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                System.out.println("Failed: " + e.getMessage());
            }
            
        }
    }

    public static void returnBook(int borrowId) {
        String sql1 = "SELECT book_id FROM borrowed_books WHERE id = ?";
        String sql2 = "UPDATE borrowed_books SET return_date = CURRENT_DATE WHERE id = ?";
        String sql3 = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?";
        Connection conn = null;
        ResultSet rs = null;
        int foundBookId = 0;

        try {
            conn = DBConnection.getConnection();

            if(conn == null) {
                throw new NullPointerException();
            }

            conn.setAutoCommit(false);

            try(PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
                pstmt1.setInt(1, borrowId);
                rs = pstmt1.executeQuery();
                
                if(rs.next() == true) {
                    foundBookId = rs.getInt("book_id");

                    try(PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                        pstmt2.setInt(1, borrowId);
                        pstmt2.executeUpdate();
    
                        try(PreparedStatement pstmt3 = conn.prepareStatement(sql3)) {
                            pstmt3.setInt(1, foundBookId);
                            pstmt3.executeUpdate();
                        }
                    }
                } else {
                    System.out.println("Book not found");
                }

            }
            conn.commit();

        } catch (SQLException | IOException e) {
            try {
                conn.rollback();
            } catch (SQLException eRoll) {
                System.out.println("Failed to update book: " + e.getMessage());
            }
            
            System.out.println("Failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) try { rs.close(); } catch (SQLException e) {}
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                System.out.println("Failed: " + e.getMessage());
            }
            
        }
    }

    public static List<BorrowedBook> getAllBorrowedBooks(){
        String sql = "SELECT * FROM borrowed_books";
        List<BorrowedBook> borrowedBooks = new ArrayList<BorrowedBook>();

        try(
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) 
            {
                while(rs.next()) {
                    BorrowedBook bb = new BorrowedBook(rs.getInt("id"), rs.getInt("book_id"), rs.getInt("member_id"), rs.getDate("borrow_date"), rs.getDate("due_date"), rs.getDate("return_date"));
                    borrowedBooks.add(bb);
                }
            } catch (SQLException| IOException e) {
                System.out.println("Failed to load all borrowed books: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }

        return borrowedBooks;
    }

    public static List<BorrowedBook> getBorrowedBooksByMember(int memberId) {
        String sql = "SELECT * FROM borrowed_books WHERE member_id = ?";
        List<BorrowedBook> borrowedBooks = new ArrayList<BorrowedBook>();

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, memberId);

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    BorrowedBook bb = new BorrowedBook(rs.getInt("id"), rs.getInt("book_id"), rs.getInt("member_id"), rs.getDate("borrow_date"), rs.getDate("due_date"), rs.getDate("return_date"));
                    borrowedBooks.add(bb);
                }
            }

        } catch(SQLException| IOException e) {
            System.out.println("Failed to load borrowed books: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return borrowedBooks;
    }

    public static List<BorrowedBook> getOverdueBooks() {
        String sql = "SELECT * FROM borrowed_books WHERE due_date < CURRENT_DATE AND return_date IS NULL";

        List<BorrowedBook> overdueBooks = new ArrayList<BorrowedBook>();

        try(
            Connection conn = DBConnection.getConnection(); 
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) 
        {
            while(rs.next()) {
                BorrowedBook bb = new BorrowedBook(rs.getInt("id"), rs.getInt("book_id"), rs.getInt("member_id"), rs.getDate("borrow_date"), rs.getDate("due_date"), rs.getDate("return_date"));
                overdueBooks.add(bb);
            }
        } catch (SQLException| IOException e) {
            System.out.println("Failed to load all overdue books: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return overdueBooks;
    }
}
