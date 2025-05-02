package model;

import java.io.Serializable;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Book implements Serializable {

    @JsonProperty("title")
    private final String title;
    
    @JsonProperty("author")
    private final String author;
    
    @JsonProperty("genre")
    private final String genre;
    
    @JsonProperty("isbn")
    private final String isbn;
    
    @JsonProperty("publishedYear")
    private final int publishedYear;
    
    @JsonProperty("available")
    private boolean isAvailable;

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
    }

    // Getters for retrieving the values of the attributes
    public String getTitle(){
        return this.title;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getGenre(){
        return this.genre;
    }

    public String getISBN(){
        return this.isbn;
    }

    public int getPublishedYear(){
        return this.publishedYear;
    }


    // Setters for updating the values of the attributes
    public void setIsAvailable(boolean isAvailable){
        this.isAvailable = isAvailable;
    }

    // Method to check if the book is available
      public boolean isAvailable(){
        return this.isAvailable;
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
