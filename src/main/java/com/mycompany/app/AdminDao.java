package com.mycompany.app;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminDao {
    public static Admin logInWithUsername(String username) {
        String sql = "SELECT id, username, password, name FROM admins WHERE username = ?";
        Admin admin = null;

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, username);

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next() == true) {
                    admin = new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"));
                }
            }

        } catch(SQLException| IOException e) {
            System.out.println("Failed to load admin: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return admin;
    }

    public static void addAdmin(Admin admin) {
        //RETURNING id tells Postgres "after you insert this row, hand back the id column value that was generated."
        //this is so newly created members can have their ID set
        String sql = "INSERT INTO admins (username, password, name) VALUES (?, ?, ?) RETURNING id";

        //PreparedStatement features of the JDBC API used to execute parameterized SQL queries securely and efficiently
        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPassword());
            stmt.setString(3, admin.getName());

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    admin.setId(rs.getInt("id"));
                }
            }
            System.out.println("Admin named " + admin.getName() + " was added successfully");
        } catch(SQLException | IOException e) {
            System.out.println("Failed to add admin: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
