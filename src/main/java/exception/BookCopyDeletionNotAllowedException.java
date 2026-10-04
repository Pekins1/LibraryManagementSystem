package exception;

public class BookCopyDeletionNotAllowedException extends RuntimeException {
    public BookCopyDeletionNotAllowedException(String message){
        super(message);
    }
    
}
