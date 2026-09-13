package com.mycompany.app;

public class Admin {
    private int id;
    private String username;
    private String password;
    private String name;
    private String role;
    private boolean mustChangePassword;

    public Admin(String username, String password, String name, boolean mustChangePassword) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.mustChangePassword = mustChangePassword;
    }

    //for Insert
    public Admin(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public Admin(int id, String username, String password, String name, String role, boolean mustChangePassword) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
    }

    public int getId() {
        return id;
    }

    public void setId(int idInput) {
        this.id = idInput;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String usernameInput) {
        this.username = usernameInput;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwordInput) {
        this.password = passwordInput;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameInput) {
        this.name = nameInput;
    }

    public String getRole() {
        return role;
    }

    public boolean getPasswordChangeStatus() {
        return mustChangePassword;
    }

    public void setPasswordChangeStatus(boolean passwordChangeStatusInput) {
        this.mustChangePassword = passwordChangeStatusInput;
    }
}
