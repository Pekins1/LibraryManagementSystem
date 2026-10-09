package com.prahkel.library.exception;

public class BookDeletionNotAllowedException extends RuntimeException{

    public BookDeletionNotAllowedException(String message){
        super(message);
    }
    
}
