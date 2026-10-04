package exception;

public class BookHasHistoryException extends RuntimeException {
    public BookHasHistoryException(String message){
        super(message);
    }
    
}
