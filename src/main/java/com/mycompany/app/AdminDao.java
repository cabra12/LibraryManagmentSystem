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
        String sql = "SELECT id, username, password, name, role, must_change_password FROM admins WHERE username = ?";
        Admin admin = null;

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, username);

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next() == true) {
                    admin = new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), rs.getString("role"), rs.getBoolean("must_change_password"));
                }
            }

        } catch(SQLException| IOException e) {
            System.out.println("Failed to load admin: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return admin;
    }

    public static Admin getAdminById(int adminId) {
        String sql = "SELECT id, name, password, name, role, must_change_password FROM admins WHERE id = ?";
        Admin admin = null;

        //Prepared Statement because it has parameters
        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, adminId);

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next() == true) {
                    admin = new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), rs.getString("role"), rs.getBoolean("must_change_password"));
                }
            }

        } catch(SQLException| IOException e) {
            System.out.println("Failed to load admin: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return admin;
    }

    public static Admin getAdminByUsername(String username) {
        String sql = "SELECT id, username, password, name, role, must_change_password FROM admins WHERE username = ?";
        Admin admin = null;
    
        try(
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, username);
    
            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next() == true) {
                    admin = new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), rs.getString("role"), rs.getBoolean("must_change_password"));
                }
            }
    
        } catch(SQLException| IOException e) {
            System.out.println("Failed to load admin: " + e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    
        return admin;
    }

    public static List<Admin> getAllAdmins() {
        String sql = "SELECT id, username, password, name, role, must_change_password FROM admins";
        List<Admin> admins = new ArrayList<Admin>();

        try(
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
                while(rs.next()) {
                    Admin admin = new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), rs.getString("role"), rs.getBoolean("must_change_password"));
                    admins.add(admin);
                }
            } catch (SQLException| IOException e) {
                System.out.println("Failed to load all admins: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }

        return admins;
    }

    public static DaoResult addAdmin(Admin admin) {
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
            return DaoResult.SUCCESS;
        } catch(SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return DaoResult.DUPLICATE_KEY;
            } else {
                System.out.println("Failed to add admin: " + e.getMessage());
                return DaoResult.DATABASE_ERROR;
            }
        } catch(IOException e) {
            System.out.println("Failed to add admin: " + e.getMessage());
            return DaoResult.DATABASE_ERROR;
        } catch(Exception e) {
            e.printStackTrace();
            return DaoResult.DATABASE_ERROR;
        }
    }

    public static DaoResult updateAdmin(Admin admin) {
        String sql = "UPDATE admins SET username = ?, password = ?, name = ?, must_change_password = ? WHERE id = ?";

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPassword());
            stmt.setString(3, admin.getName());
            stmt.setBoolean(4, admin.getPasswordChangeStatus());
            stmt.setInt(5, admin.getId());

            stmt.executeUpdate();
            System.out.println("Admin information updated successfully!");
            return DaoResult.SUCCESS;

        } catch(SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return DaoResult.DUPLICATE_KEY;
            } else {
                System.out.println("Failed to update admin: " + e.getMessage());
                return DaoResult.DATABASE_ERROR;
            }
        } catch(IOException e) {
            System.out.println("Failed to update admin: " + e.getMessage());
            return DaoResult.DATABASE_ERROR;
        } catch(Exception e) {
            e.printStackTrace();
            return DaoResult.DATABASE_ERROR;
        }
    }

    public static boolean deleteAdmin(int adminId) {
        String sql = "DELETE FROM admins WHERE id = ?";

        try(
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, adminId);

            stmt.executeUpdate();
            System.out.println("Admin deleted successfully");
            return true;

        } catch(SQLException e) {
            System.out.println("Failed to delete admin: " + e.getMessage());
            return false;
        } catch(IOException e) {
            System.out.println("Failed to delete admin: " + e.getMessage());
            return false;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
