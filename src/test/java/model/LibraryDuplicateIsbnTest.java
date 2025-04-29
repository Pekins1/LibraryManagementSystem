package model;

import java.util.List;
import java.util.ArrayList;
import exception.BookAlreadyExistsException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



class LibraryDuplicateIsbnTest {

    @Test
    void addingTheSameIsbnTwiceThrows() throws Exception{
        Library lib = new Library();

        Book first = new Book("Clean Code", "R. C. Martin", "Software", "978-0132350884", 2008);
        Book second = new Book("Clean Code", "R. C. Martin", "Software", "978-0132350884", 2008);

        lib.addBook(first); // should succeed

        //should fail: same ISBN already present
        assertThrows(BookAlreadyExistsException.class, () -> lib.addBook(second));

    }

    @Test
    void addingAListOfBooksWithTheSameIsbnThrows() throws Exception{
        Library lib = new Library();

        Book first = new Book("Clean Code", "R. C. Martin", "Software", "978-0132350884", 2008);
        Book second = new Book("Clean Code", "R. C. Martin", "Software", "978-0132350884", 2008);
        Book third = new Book("Brave New World", "A. Huxley", "Fiction", "123", 1932);
        
        List<Book> bookList = new ArrayList<Book>();
        bookList.add(first);
        bookList.add(second);
        bookList.add(third);

        // this should fail
        assertThrows(BookAlreadyExistsException.class,() -> lib.addBooks(bookList));
    }
    @Test
    void newBookStartsUnavailableUntilAdded() {
        Book b = new Book("Brave New World", "A. Huxley", "Fiction", "123", 1932);
        assertFalse(b.isAvailable(), "Fresh book should be unavailable on creation");
    }

    
}
