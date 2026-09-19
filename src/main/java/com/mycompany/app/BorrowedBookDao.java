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
            System.out.println("That book could not be found.");
            return;
        }

        Date twoWeeksFromToday = Date.valueOf(LocalDate.now().plusDays(14));

        //"available_copies > 0" makes the availability check part of the UPDATE itself,
        //so two people can't both take the last copy
        String sql1 = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0";
        String sql2 = "INSERT INTO borrowed_books (book_id, member_id, due_date) VALUES (?, ?, ?)";

        try {
            conn = DBConnection.getConnection();

            conn.setAutoCommit(false);
            //normally, SQL statements that are run are treated as a mini-transaction and executes and saved permanently to the DB
            //adding false argument turns this off, statements run but they aren't saved yet (pending)


            // statement 1: take one copy off the shelf (only if one is available)
            int copiesUpdated;
            try(PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
                pstmt1.setInt(1, bookId);
                copiesUpdated = pstmt1.executeUpdate();
            }

            if(copiesUpdated == 0) {
                conn.rollback();
                System.out.println("Your book is not available!");
                return;
            }

            try(PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setInt(1, bookId);
                pstmt2.setInt(2, memberId);
                pstmt2.setDate(3, twoWeeksFromToday);
                pstmt2.executeUpdate();
            }

            conn.commit();
            //tells database to save everything at this point, not yet permanent

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            System.out.println(book.getTitle() + " by " + book.getAuthor() + " is checked out and yours. Your due date is " + sdf.format(twoWeeksFromToday));
        } catch (SQLException | IOException e) {
            rollbackQuietly(conn);
            System.out.println("Failed: " + e.getMessage());
        } catch (Exception e) {
            rollbackQuietly(conn);
            e.printStackTrace();
        } finally {
            closeQuietly(conn);
        }
    }

    public static void returnBook(int borrowId) {
        String sql1 = "SELECT book_id, return_date FROM borrowed_books WHERE id = ?";
        String sql2 = "UPDATE borrowed_books SET return_date = CURRENT_DATE WHERE id = ?";
        String sql3 = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?";
        Connection conn = null;
    
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
    
            int foundBookId;
    
            try(PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
                pstmt1.setInt(1, borrowId);
    
                try(ResultSet rs = pstmt1.executeQuery()) {
                    if(!rs.next()) {
                        System.out.println("Book not found");
                        return;
                    }
    
                    if(rs.getDate("return_date") != null) {
                        System.out.println("The book has already been returned");
                        return;
                    }
    
                    foundBookId = rs.getInt("book_id");
                }
            }
    
            try(PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setInt(1, borrowId);
                pstmt2.executeUpdate();
            }
    
            try(PreparedStatement pstmt3 = conn.prepareStatement(sql3)) {
                pstmt3.setInt(1, foundBookId);
                pstmt3.executeUpdate();
            }
    
            conn.commit();
            System.out.println("Book was returned!");
        } catch (SQLException | IOException e) {
            rollbackQuietly(conn);
            System.out.println("Failed: " + e.getMessage());
        } catch (Exception e) {
            rollbackQuietly(conn);
            e.printStackTrace();
        } finally {
            closeQuietly(conn);
        }
    }

    //safe even if the connection was never opened (conn == null)
    private static void rollbackQuietly(Connection conn) {
        if(conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.out.println("Failed to roll back: " + e.getMessage());
        }
    }

    private static void closeQuietly(Connection conn) {
        if(conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println("Failed to close connection: " + e.getMessage());
        }
    }

    public static List<BorrowedBook> getAllBorrowedBooks(){
        String sql = "SELECT * FROM borrowed_books WHERE return_date IS NULL";
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
        String sql = "SELECT * FROM borrowed_books WHERE member_id = ? AND return_date is NULL";
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

    public static List<BorrowedBook> getBorrowedBooksByBookId(int bookId) {
        String sql = "SELECT * FROM borrowed_books WHERE book_id = ? AND return_date IS NULL";
        List<BorrowedBook> borrowedBooks = new ArrayList<BorrowedBook>();

        try ( 
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, bookId);

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

    public static List<BorrowedBook> getOverdueBooksByMember(int memberId) {
        String sql = "SELECT * FROM borrowed_books WHERE member_id = ? AND due_date < CURRENT_DATE AND return_date is NULL";
        List<BorrowedBook> overdueBooks = new ArrayList<BorrowedBook>();

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, memberId);

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    BorrowedBook bb = new BorrowedBook(rs.getInt("id"), rs.getInt("book_id"), rs.getInt("member_id"), rs.getDate("borrow_date"), rs.getDate("due_date"), rs.getDate("return_date"));
                    overdueBooks.add(bb);
                }
            }

        } catch(SQLException| IOException e) {
            System.out.println("Failed to load overdue books by member: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return overdueBooks;
    }
}
