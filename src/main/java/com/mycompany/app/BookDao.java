package com.mycompany.app;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
    public static void addBook(Book book) {
        String sql = "INSERT INTO books (title, author, isbn, total_copies, available_copies) VALUES (?, ?, ?, ?, ?)";

        //PreparedStatement features of the JDBC API used to execute parameterized SQL queries securely and efficiently
        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setInt(4, book.getTotalCopies());
            stmt.setInt(5, book.getAvailableCopies());

            stmt.executeUpdate();
            System.out.println(book.getTitle() + " by " + book.getAuthor() +  " was inserted successfully");
        } catch(SQLException | IOException e) {
            System.out.println("Failed to add book: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Book> getAllBooks() {
        String sql = "SELECT * FROM books";
        List<Book> books = new ArrayList<Book>();

        //ResultSet in Java is an object that holds the data returned from a database after executing a SQL query (SELECT)
        //.next() method advances cursor through first row and subsequent rows, method returns true if a row exists and false otherwise, ideal for while loop
        //use getter to extract column data by using column name
        try(
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) 
            {
                while(rs.next()) {
                    Book b = new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("total_copies"), rs.getInt("available_copies"));
                    books.add(b);
                }
            } catch (SQLException| IOException e) {
                System.out.println("Failed to load all books: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }

        return books;
    }

    public static Book getBookById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        Book b = null;

        //Prepared Statement because it has parameters
        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, id);

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next() == true) {
                    b = new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("total_copies"), rs.getInt("available_copies"));
                }
            }

        } catch(SQLException| IOException e) {
            System.out.println("Failed to load book: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return b;
    }

    public static List<Book> searchByTitle(String title) {
        String sql = "SELECT * FROM books WHERE title ILIKE ?";
        List<Book> searchedBooks = new ArrayList<Book>();

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, "%" + title + "%");

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Book b = new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("total_copies"), rs.getInt("available_copies"));
                    searchedBooks.add(b);
                }
            }
        } catch(SQLException| IOException e) {
            System.out.println("Failed to search: " + e.getMessage());

        } catch(Exception e){
            e.printStackTrace();
        }

        return searchedBooks;
    }

    public static List<Book> searchByAuthor(String author) {
        String sql = "SELECT * FROM books WHERE author ILIKE ?";
        List<Book> searchedBooks = new ArrayList<Book>();

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, "%" + author + "%");

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Book b = new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("total_copies"), rs.getInt("available_copies"));
                    searchedBooks.add(b);
                }
            }
        } catch(SQLException| IOException e) {
            System.out.println("Failed to search: " + e.getMessage());

        } catch(Exception e){
            e.printStackTrace();
        }

        return searchedBooks;
    }

    public static void updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, total_copies = ?, available_copies = ? WHERE id = ?";

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setInt(4, book.getTotalCopies());
            stmt.setInt(5, book.getAvailableCopies());
            stmt.setInt(6, book.getId());

            stmt.executeUpdate();
            System.out.println("Updated Book: ");
            System.out.println(book.getTitle() + " by " + book.getAuthor() + " with ISBN " + book.getIsbn() + " and " + book.getAvailableCopies() + " available copies out of " + book.getTotalCopies() + " total copies.");
        } catch(SQLException | IOException e) {
            System.out.println("Failed to update book: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteBook(int id) {
        String sql = "DELETE FROM books WHERE id = ?";

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, id);

            stmt.executeUpdate();
            System.out.println("Book was deleted successfully!");

        } catch(SQLException | IOException e) {
            System.out.println("Failed to delete book: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
