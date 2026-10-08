package exception;

public class BookCopyAlreadyExistException extends RuntimeException {
    public BookCopyAlreadyExistException(String message){
        super(message);
    }
    
}
