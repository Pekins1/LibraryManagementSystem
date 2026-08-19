package model;

import jakarta.persistence.*;

import java.lang.annotation.Inherited;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowings")
public class Borrowing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long id;

    @ManyToOne(optional = false)
    private Book book;

    @ManyToOne(optional = false)
    private Borrower borrower;

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(nullable = false)
    private boolean returned;
}
