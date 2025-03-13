package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.Book;
import model.Library;


public final class FileUtils {
    //private constructor to prevent instantiation
    private FileUtils(){
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /*
     * saves library to a file using java serialization
     */
    public static void saveLibraryToFile(Library library, String fileName) throws IOException{
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))){
            out.writeObject(library);
        }
    }

    /*
     * loads library data from a file using java serialization
     */
    public static Library loadLibraryFromFile(String filename) throws IOException,ClassNotFoundException{
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))){
            return (Library) in.readObject();
        }
    }

    /*
     * Exports library books to a CSV file
     */
    public static void exportBooksToCSV(List<Book> books, String filename) throws IOException{
        try(PrintWriter writer = new PrintWriter(new FileWriter(filename))){
            //write header
            writer.println("ISBN,Title,Author,Genre,PublishedYear,Available");

            // Write book data
            for(Book book : books){
                writer.println(book.getISBN() + "," + 
                book.getTitle() + "," +
                book.getAuthor() + "," +
                book.getGenre() + "," +
                book.getPublishedYear() + "," +
                book.isAvailable());
            }
        }
    }

    /*
     * Imports books from a CSV file into the library
     */
    public static List<Book> importBooksFromCSV(String filename) throws IOException {
        List<Book> books = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader( new FileReader(filename))){
            // Skip header
            String line = reader.readLine(); 

            // Read book data
            while((line = reader.readLine()) != null){
                String[] data = line.split(",");
                if(data.length >= 5){
                    Book book = new Book(
                        data[1],//title
                        data[2],//author
                        data[3],//genre
                        data[0],//isbn
                        Integer.parseInt(data[4])//published year
                    );

                    // Set availability if present
                    if(data.length > 5){
                        book.setIsAvailable(Boolean.parseBoolean(data[5]));
                    }

                    // add book to list
                    books.add(book);
                }
            }
        }
        return books;
    }
}
