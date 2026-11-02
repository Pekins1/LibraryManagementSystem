package com.prahkel.library.controller.exceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.prahkel.library.exception.BookAlreadyBorrowedException;
import com.prahkel.library.exception.BookAlreadyExistsException;
import com.prahkel.library.exception.BookCopyAlreadyExistException;
import com.prahkel.library.exception.BookCopyDeletionNotAllowedException;
import com.prahkel.library.exception.BookCopyNotFoundException;
import com.prahkel.library.exception.BookDeletionNotAllowedException;
import com.prahkel.library.exception.BookHasHistoryException;
import com.prahkel.library.exception.BookNotAvailableException;
import com.prahkel.library.exception.BookNotBorrowedException;
import com.prahkel.library.exception.BookNotFoundException;
import com.prahkel.library.exception.BookWithActiveLoanCannotBeArchivedException;
import com.prahkel.library.exception.BorrowLimitExceededException;
import com.prahkel.library.exception.BorrowerNotFoundException;
import com.prahkel.library.exception.BorrowingNotFoundException;
import com.prahkel.library.exception.InvalidCopyStateException;
import com.prahkel.library.exception.MemberAlreadyExistsException;
import com.prahkel.library.exception.MemberNotFoundException;

@RestControllerAdvice   // applies to every controller, automatically
public class GlobalExceptionHandler {

    // 1. Initialize the logger
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(
        LocalDateTime timestamp, int status, String error, String message, String path) {
    }

    @ExceptionHandler({
        BookNotFoundException.class,
        BookCopyNotFoundException.class,
        BorrowerNotFoundException.class,
        BorrowingNotFoundException.class,
        MemberNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(
            RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()));
    }

    @ExceptionHandler({
        BookAlreadyExistsException.class,
        BookCopyAlreadyExistException.class,
        MemberAlreadyExistsException.class,
        BookAlreadyBorrowedException.class,
        BookNotAvailableException.class,
        BorrowLimitExceededException.class
    })
    public ResponseEntity<ErrorResponse>handleConflict(
        RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            request.getRequestURI()));
    }

    

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),          // use the field name as the key
                        error -> error.getDefaultMessage(), // use the failure reason as the value
                        (first, second) -> first));     // if a field failed twice, keep the first reason
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
            LocalDateTime.now(),            // when it happened
            HttpStatus.BAD_REQUEST.value(), // 400
            "Bad Request",            // the label for 400
            "Validation failed: " + fields, // the map, turned into readable text
            request.getRequestURI()));      // which URL was called

    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> fields = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (first, second) -> first));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed: " + fields,
            request.getRequestURI()));
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleSpringBadRequest(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()));
    }

    @ExceptionHandler({
        BookNotBorrowedException.class,
        BookHasHistoryException.class,
        BookDeletionNotAllowedException.class,
        BookCopyDeletionNotAllowedException.class,
        BookWithActiveLoanCannotBeArchivedException.class,
        InvalidCopyStateException.class
    })
    public ResponseEntity<ErrorResponse>handleBadRequest(
        RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()));
    }


    

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>handleInternalServerError(
        Exception ex, HttpServletRequest request){

            logger.error("Unhandled exception caught by global handler: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",   // <-- generic for the client
            request.getRequestURI()));
    }

}
