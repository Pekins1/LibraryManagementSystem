package model;

import java.io.Serializable;
import java.util.Objects;

public class Book implements Serializable {

    private String title;
    private String author;
    private String genre;
    private String isbn;
    private int publishedYear;
    private boolean isAvailable;

    public Book(String title, String author, String genre, String isbn, int year){
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isbn = isbn;
        this.publishedYear = year;
        this.isAvailable = true;
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
    public void setTitle(String title){
        this.title = title;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public void setGenre(String genre){
        this.genre = genre;
    }

    public void setISBN(String isbn){
        this.isbn = isbn;
    }

    public void setPublishedYear(int year){
        this.publishedYear = year;
    }

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
