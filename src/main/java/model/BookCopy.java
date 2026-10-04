package model;

import java.time.LocalDate;

import jakarta.persistence.*;


@Entity 
@Table(name = "book_copies", uniqueConstraints = {
    @UniqueConstraint(name = "book_copy_barcode", columnNames = "barcode")
})
public class BookCopy {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "barcode", nullable = false)
    private String barcode;
    
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    @Column(name = "times_borrowed", nullable = false)
    private int timesBorrowed = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false)
    private BookCondition condition;
    
    @Column(name = "acquired_date", nullable = false)
    private LocalDate acquiredDate;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    protected BookCopy(){
        // Requied by jpa
    }
    
    public BookCopy (
            String barcode,
            BookCondition condition, 
            LocalDate acquiredDate) {

        this.barcode = barcode;
        this.isAvailable = true;
        this.timesBorrowed = 0;
        this.condition = condition;
        this.acquiredDate = acquiredDate;
    }

    public void incrementTimesBorrowed() {
        this.timesBorrowed ++;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    // Getters 
    public Book getBook(){
        return book;
    }

    public Long getId() {
        return id;
    }

    public String getBarcode(){
        return barcode;
    }

    public int getTimesBorrowed(){
        return timesBorrowed;
    }

    public boolean isAvailable(){
        return isAvailable;
    }

    public BookCondition getCondition(){
        return condition;
    }

    public LocalDate getAcquiredDate(){
        return acquiredDate;
    }

    // Setters

    public void setId( Long id){
        this.id = id;
    }

    public void setBarcode(String barcode){
        this.barcode = barcode;
    }

    public void setIsAvailable(boolean available){
        this.isAvailable = available;
    }
    
    public void setCondition(BookCondition condition){
        this.condition = condition;
    }

    public void setAcquiredDate(LocalDate acquiredDate){
        this.acquiredDate = acquiredDate;
    }

    public void setTimesBorrowed(int timesBorrowed){
        this.timesBorrowed = timesBorrowed;
    }
}
