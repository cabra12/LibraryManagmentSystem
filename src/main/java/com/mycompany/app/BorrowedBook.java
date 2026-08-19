package com.mycompany.app;


import java.sql.Date;

public class BorrowedBook {
    private int id;
    private int bookId;
    private int memberId;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;

    public BorrowedBook(int id, int bookId, int memberId, Date borrowDate, Date dueDate, Date returnDate) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    //new borrow, id is auto generated, borrow date defaults to CURRENT_DATE, returnDate can return null
    public BorrowedBook(int bookId, int memberId, Date dueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int idInput) {
        this.id = idInput;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookIdInput) {
        this.bookId = bookIdInput;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberIdInput) {
        this.memberId = memberIdInput;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(Date borrowDateInput) {
        this.borrowDate = borrowDateInput;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDateInput) {
        this.dueDate = dueDateInput;
    }

    //could be null
    public Date getReturnDate() {
        return returnDate;
    }

    //most necessary setter
    public void setReturnDate(Date returnDateInput) {
        this.returnDate = returnDateInput;
    }
}
