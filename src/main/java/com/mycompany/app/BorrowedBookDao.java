package com.mycompany.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;

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
                    }
    
                }else {
                    System.out.println("Your book is not available!");
                }
            }


            conn.commit();
            //tells database to save everything at this point, not yet permanent
        } catch (SQLException e) {
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
}
