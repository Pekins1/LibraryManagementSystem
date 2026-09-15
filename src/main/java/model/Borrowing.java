package model;

import jakarta.persistence.*;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Entity
@Table(name = "borrowings")
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BorrowingStatus status;

    @Column(name = "late_fee", precision = 10, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;


    // ==================== Constructors ====================

    protected Borrowing() {
        // Required by JPA.
    }


    public Borrowing( Book book, 
             Borrower borrower,
            LocalDate borrowDate) {
        this.book = book;
        this.borrower = borrower;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(14);     // default 14-day loan
        this.status = BorrowingStatus.ACTIVE;
        this.lateFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.returnDate = null;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public void setBorrower(Borrower borrower) {
        this.borrower = borrower;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Transient
    public boolean isReturned() {
        return status == BorrowingStatus.RETURNED;
    }
    @Transient
    public boolean isOpen() {
        return status == BorrowingStatus.ACTIVE || status == BorrowingStatus.OVERDUE;
    }

    // @Transient
    // public Boolean IsOverdue() {
    //     return status == BorrowingStatus.OVERDUE;
    // }

    public BorrowingStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowingStatus status) {
        this.status = status;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee == null
            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : lateFee.setScale(2, RoundingMode.HALF_UP);
    }
}