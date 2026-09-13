package com.mycompany.app;

public class Member {
    private int id;
    private String name;
    private String email;
    private String password;
    private boolean mustChangePassword;

    public Member(int id, String name, String email, boolean mustChangePassword) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mustChangePassword = mustChangePassword;
    }

    //creating a new member to insert, postgres will generate an ID once you insert it
    public Member(String name, String email, String password, boolean mustChangePassword) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.mustChangePassword = mustChangePassword;
    }

    public Member(int id, String name, String email, String password, boolean mustChangePassword) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.mustChangePassword = mustChangePassword;
    }

    public int getId() {
        return id; 
    }

    public void setId(int idInput) {
        this.id = idInput;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameInput) {
        this.name = nameInput;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String emailInput) {
        this.email = emailInput;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwordInput) {
        this.password = passwordInput;
    }

    public boolean getPasswordChangeStatus() {
        return mustChangePassword;
    }

    public void setPasswordChangeStatus(boolean passwordChangeStatusInput) {
        this.mustChangePassword = passwordChangeStatusInput;
    }
}
