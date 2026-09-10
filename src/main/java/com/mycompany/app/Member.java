package com.mycompany.app;

public class Member {
    private int id;
    private String name;
    private String email;
    private String password;

    public Member(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    //creating a new member to insert, postgres will generate an ID once you insert it
    public Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Member(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
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

    public void setPassword(String password) {
        this.password = password;
    }
}
