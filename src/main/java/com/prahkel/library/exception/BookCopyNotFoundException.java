package com.prahkel.library.exception;

public class BookCopyNotFoundException extends RuntimeException {
    public BookCopyNotFoundException(String message){
        super(message);
    }
    
}
