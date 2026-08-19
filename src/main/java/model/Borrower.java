package model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowers")
public class Borrower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "borrowed_book_id")
    private Long borrowedBookId;

    @Column(name = "borrow_date")
    private LocalDateTime borrowDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    protected Borrower() {
        // Required by JPA.
    }

    public Borrower(String name, String email) {
        this.name = name;
        this.email = email;
        this.borrowedBookId = null;
        this.borrowDate = null;
        this.returnDate = null;
    }
}