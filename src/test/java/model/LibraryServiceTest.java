package model;

import util.FileUtils;
import java.util.List;
import java.util.ArrayList;
import exception.BookAlreadyExistsException;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;



class LibraryServiceTest {

    @Test
    void shouldPreventDuplicateIsbn() throws Exception{
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
        assertThrows(BookAlreadyExistsException.class,() -> lib.addBooks(bookList),
            "Adding a duplicate ISBN should throw");
    }
    @Test
    void newBookStartsUnavailableUntilAdded() {
        Book b = new Book("Brave New World", "A. Huxley", "Fiction", "123", 1932);
        assertFalse(b.isAvailable(), "Fresh book should be unavailable on creation");
    }

    @Test
    void checkIfBorrowUpdatesAvailability() throws Exception{
        Library newLib = new Library();

        Book first = new Book("Clean Code", "R. C. Martin", "Software", "978-0132350884", 2008);
        Book second = new Book("Brave New World", "A. Huxley", "Fiction", "123", 1932);

        List<Book> bookList =new ArrayList<>();
        bookList.add(first);
        bookList.add(second);

        newLib.addBooks(bookList);
        // newLib.addBook(first);

        // Borrowing book should succeed
        newLib.borrowBook("Clean Code","Rahkel");

        // should pass
        assertFalse(first.isAvailable(),"Book should be set to unavailabl on borrow");

    }

    @Test
    void roundTripPersistsData(@TempDir Path tmp) throws Exception {
        Library lib = new Library();
        Path file = tmp.resolve("lib.json");
        Book book = new Book(
            "The Pragmatic Programmer",
            "Andrew Hunt and David Thomas",
            "Software Development",
            "978-0201616224",
            1999
        );

        lib.addBook(book);
        assertTrue(lib.getBooks().contains(book), "Book should be in library after adding");

        // Save to file (Serializes)
        FileUtils.saveLibraryToFile(lib, file.toString());
        
        // Load from file(Deserializes)
        Library loadedLib = FileUtils.loadLibraryFromFile(file.toString());
        
        // Verify the loaded library contains the same book
        assertTrue(loadedLib.getBooks().contains(book), "Loaded library should contain the same book");
        assertEquals(1, loadedLib.getBooks().size(), "Loaded library should have one book");
    }

    
}
