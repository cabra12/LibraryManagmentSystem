package com.mycompany.app;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MemberDao {
    public static void addMember(Member member) {
        //RETURNING id tells Postgres "after you insert this row, hand back the id column value that was generated."
        //this is so newly created members can have their ID set
        String sql = "INSERT INTO members (name, email, password) VALUES (?, ?, ?) RETURNING id";

        //PreparedStatement features of the JDBC API used to execute parameterized SQL queries securely and efficiently
        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPassword());

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    member.setId(rs.getInt("id"));
                }
            }
            System.out.println("Member named " + member.getName() + " was added successfully");
        } catch(SQLException | IOException e) {
            System.out.println("Failed to add member: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Member> getAllMembers() {
        String sql = "SELECT id, name, email FROM members";
        List<Member> members = new ArrayList<Member>();

        //ResultSet in Java is an object that holds the data returned from a database after executing a SQL query (SELECT)
        //.next() method advances cursor through first row and subsequent rows, method returns true if a row exists and false otherwise, ideal for while loop
        //use getter to extract column data by using column name
        //using Statement here because there's nothing from the user being inserted, so it's fine to use this (no ?), otherwise you'd use PreparedStatement
        try(
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
                while(rs.next()) {
                    Member m = new Member(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                    members.add(m);
                }
            } catch (SQLException| IOException e) {
                System.out.println("Failed to load all books: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }

        return members;
    }

    public static Member getMemberId(int id) {
        String sql = "SELECT id, name, email FROM members WHERE id = ?";
        Member m = null;

        //Prepared Statement because it has parameters
        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, id);

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next() == true) {
                    m = new Member(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                }
            }

        } catch(SQLException| IOException e) {
            System.out.println("Failed to load member: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return m;
    }

    //specifying columns instead of using * to make sure programmer knows this method uses password, something sensitive
    public static Member getMemberByEmail(String email) {
        String sql = "SELECT id, name, email, password FROM members WHERE email = ?";
        Member m = null;

        try (
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, email);

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next() == true) {
                    m = new Member(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("password"));
                }
            }

        } catch(SQLException| IOException e) {
            System.out.println("Failed to load member: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return m;
    }

    public static List<Member> searchByName(String name) {
        String sql = "SELECT id, name, email FROM members WHERE name ILIKE ?";
        List<Member> searchedMembers = new ArrayList<Member>();

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, "%" + name + "%");

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Member m = new Member(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                    searchedMembers.add(m);
                }
            }
        } catch(SQLException| IOException e) {
            System.out.println("Failed to search: " + e.getMessage());

        } catch(Exception e){
            e.printStackTrace();
        }

        return searchedMembers;
    }

    public static void updateMember(Member member) {
        String sql = "UPDATE members SET name = ?, email = ? WHERE id = ?";

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setInt(3, member.getId());

            stmt.executeUpdate();
            System.out.println("Member information updated successfully!");
        } catch(SQLException | IOException e) {
            System.out.println("Failed to update member: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteMember(int id) {
        String sql = "DELETE FROM members WHERE id = ?";

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, id);

            stmt.executeUpdate();
            System.out.println("Member deleted successfully");

        } catch(SQLException | IOException e) {
            System.out.println("Failed to delete member: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
