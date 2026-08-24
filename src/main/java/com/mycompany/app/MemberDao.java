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
        String sql = "INSERT INTO members (name, email) VALUES (?, ?)";

        //PreparedStatement features of the JDBC API used to execute parameterized SQL queries securely and efficiently
        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());

            stmt.executeUpdate();
            System.out.println("Member named " + member.getName() + " inserted successfully");
        } catch(SQLException | IOException e) {
            System.out.println("Failed to add member: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Member> getAllMembers() {
        String sql = "SELECT * FROM members";
        List<Member> members = new ArrayList<Member>();

        //ResultSet in Java is an object that holds the data returned from a database after executing a SQL query (SELECT)
        //.next() method advances cursor through first row and subsequent rows, method returns true if a row exists and false otherwise, ideal for while loop
        //use getter to extract column data by using column name
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
        String sql = "SELECT * FROM members WHERE id = ?";
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

    public static Member getMemberByEmail(String email) {
        String sql = "SELECT * FROM members WHERE email = ?";
        Member m = null;

        try (
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, email);

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

    public static List<Member> searchByName(String name) {
        String sql = "SELECT * FROM members WHERE name ILIKE ?";
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

            int rows = stmt.executeUpdate();
            System.out.println(rows + " member(s) updated successfully");
        } catch(SQLException | IOException e) {
            System.out.println("Failed to update member(s): " + e.getMessage());
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

            int rows = stmt.executeUpdate();
            System.out.println(rows + " memeber(s) deleted successfully!");

        } catch(SQLException | IOException e) {
            System.out.println("Failed to delete member: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
