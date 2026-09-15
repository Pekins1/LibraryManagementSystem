package model;

import java.io.Serializable;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;


@Entity
@Table(name = "books")
public class Book implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false)
    private String genre;
    
    @Column(nullable = false, unique = true)
    private String isbn;
    
    @Column(name = "published_year", nullable = false)
    private int publishedYear;
    
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    @Column(name = "times_borrowed", nullable = false)
    private int timesBorrowed;

    protected Book() {
        // Required by JPA.
    }

    @JsonCreator
    public Book(@JsonProperty("title") String title,
                @JsonProperty("author") String author,
                @JsonProperty("genre") String genre,
                @JsonProperty("isbn") String isbn,
                @JsonProperty("publishedYear") int publishedYear) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isbn = isbn;
        this.publishedYear = publishedYear;
        this.isAvailable = false;
        this.timesBorrowed = 0;
    }
    

    // Getters for retrieving the values of the attributes
    public Long getId() {
        return id;
    }

    public String getTitle(){
        return this.title;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getGenre(){
        return this.genre;
    }

    public String getIsbn(){
        return this.isbn;
    }

    public int getPublishedYear(){
        return this.publishedYear;
    }

    public int getTimesBorrowed() {
        return this.timesBorrowed;
    }


    // Setters for updating the values of the attributes
    public void setId(Long id){
        this.id = id;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public void setGenre(String genre){
        this.genre = genre;
    }

    public void setIsbn(String isbn){
        this.isbn = isbn;
    }

    public void setPublishedYear(int publishedYear){
        this.publishedYear = publishedYear;
    }

    public void setIsAvailable(boolean isAvailable){
        this.isAvailable = isAvailable;
    }

    // Method to check if the book is available
      public boolean isAvailable(){
        return this.isAvailable;
    }

    // Method to increment the timesBorrowed attribute
    public void incrementTimesBorrowed() {
        this.timesBorrowed++;
    }

    // Equals method to compare two books
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;
        return isbn.equals(book.isbn);
    }

    // Hashcode method to generate a unique hash value for the book
    @Override
    public int hashCode(){
        return Objects.hash(isbn);
    }

    // toString method to display the book details
    @Override
    public String toString(){
        return "Book{" + "title=" + title + ", author=" + author + ", genre=" + genre
        + ", ISBN=" + isbn + ", publishedYear=" + publishedYear
        + ", isAvailable=" + isAvailable + "}";
    }
} 
