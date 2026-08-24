package com.mycompany.app;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private int totalCopies;
    private int availableCopies;

    //use this when you're reading a book out of the DB
    public Book(int id, String title, String author, String isbn, int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    //creating a new book to insert, postgres will generate an ID once you insert it
    public Book(String title, String author, String isbn, int totalCopies, int availableCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public int getId() {
        return id; 
    }

    // public void setId(int idInput) {
    //     this.id = idInput;
    // }

    public String getTitle() {
        return title;
    }

    public void setTitle(String titleInput) {
        this.title = titleInput;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String authorInput) {
        this.author = authorInput;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbnInput) {
        this.isbn = isbnInput;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopiesInput) {
        this.totalCopies = totalCopiesInput;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopiesInput) {
        this.availableCopies = availableCopiesInput;
    }
}