package com.prahkel.library.exception;

public class BookCopyDeletionNotAllowedException extends RuntimeException {
    public BookCopyDeletionNotAllowedException(String message){
        super(message);
    }
    
}
