package com.prahkel.library.exception;

public class BookWithActiveLoanCannotBeArchivedException extends RuntimeException {
    public BookWithActiveLoanCannotBeArchivedException(String message){
        super(message);
    }
    
}
