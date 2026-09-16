package service.impl;

import model.Book;
import model.Borrower;
import model.Borrowing;
import model.BorrowingStatus;

import dto.BorrowBookRequest;
import dto.ReturnBookRequest;

import repository.BookRepository;
import repository.BorrowerRepository;
import repository.BorrowingRepository;
import service.BorrowService;
import exception.BookNotFoundException;
import exception.BorrowerNotFoundException;
import exception.BookNotAvailableException;
import exception.BookNotBorrowedException;
import exception.BorrowLimitExceededException;
import exception.BookAlreadyBorrowedException;
import exception.BorrowingNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.Clock;
import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Validated 
@Transactional(readOnly = true)
public class BorrowServiceImpl implements BorrowService{
    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;
    private final BorrowingRepository borrowingRepository;
    static final int MAX_BORROW_LIMIT = 6; // Maximum number of books a borrower can borrow at once
    private final Clock clock;
    private static final BigDecimal DAILY_LATE_FEE = new BigDecimal("1.50");
    private static final List<BorrowingStatus> OPEN_STATUSES = 
        List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE
    );

    public BorrowServiceImpl(BookRepository bookRepository, BorrowerRepository borrowerRepository,
        BorrowingRepository borrowingRepository, Clock clock) {
            this.bookRepository = bookRepository;
            this.borrowerRepository = borrowerRepository;
            this.borrowingRepository = borrowingRepository;
            this.clock = clock;
            
    }

    // Methed for calculating latefee
    private BigDecimal calculateLateFee(Borrowing borrowing, LocalDate returnDate){
        LocalDate dueDate = borrowing.getDueDate();
      
        if(dueDate == null || returnDate == null){
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if(!returnDate.isAfter(dueDate)){
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);

        return DAILY_LATE_FEE
            .multiply(BigDecimal.valueOf(overdueDays))
            .setScale(2, RoundingMode.HALF_UP);
    }

    // Method to borrow a book
    @Transactional
    public Borrowing borrowBook( @Valid @NotNull BorrowBookRequest request){ 
        // check if book and borrower exist
        Book existingBook = bookRepository.findById(request.bookId())
            .orElseThrow(() -> new BookNotFoundException(
                "Book with ID " + request.bookId() + " not found."
            ));

        Borrower existingBorrower = borrowerRepository.findById(request.borrowerId())
            .orElseThrow(() -> new BorrowerNotFoundException(
                "Borrower with ID " + request.borrowerId() + " not found."
            ));
        
            // Check if book is available
        if(!existingBook.isAvailable()){
            throw new BookNotAvailableException(
                "Book with ID " + request.bookId() + " is not available for borrowing."
            );
        }

        // check if borrower has reached the borrowing limit
        long borrowedCount = borrowingRepository.countByBorrowerIdAndStatusIn(
            existingBorrower.getId(),
            OPEN_STATUSES);
        
        if(borrowedCount >= MAX_BORROW_LIMIT){
            throw new BorrowLimitExceededException(
                "Borrower with ID " + request.borrowerId() + " has reached the borrowing limit."
            );
        }

        // Check if borrower already has the book borrowed and not returned
        boolean alreadyBorrowed = borrowingRepository.existsByBookIdAndBorrowerIdAndStatusIn(
            existingBook.getId(),
            existingBorrower.getId(),
            OPEN_STATUSES
        );

        if(alreadyBorrowed){
            throw new BookAlreadyBorrowedException(
                "Borrower with ID " + request.borrowerId() + " has already borrowed the book with ID " + request.bookId() + " and has not returned it yet."
            );
        }

        // Create a new borrowing record
        Borrowing newBorrowing = new Borrowing(existingBook, existingBorrower, LocalDate.now(clock));

        // Update book availability
        existingBook.setIsAvailable(false);

        // increment timesBorrowed
        existingBook.incrementTimesBorrowed();

        // Save the borrowing record and update the book
        bookRepository.save(existingBook);
        return borrowingRepository.save(newBorrowing);
    }


    @Transactional
    public Borrowing returnBook( @Valid @NotNull @Positive ReturnBookRequest request){
        // Check if book and borrower exist
        Book existingBook = bookRepository.findById(request.bookId())
            .orElseThrow(() -> new BookNotFoundException(
                "Book with ID " + request.bookId() + " not found."
            ));

        Borrower existingBorrower = borrowerRepository.findById(request.borrowerId())
            .orElseThrow(() -> new BorrowerNotFoundException(
                "Borrower with ID " + request.borrowerId() + " not found."
            ));

        // Find active borrowing
        Optional<Borrowing> openBorrowing = borrowingRepository.findByBookIdAndBorrowerIdAndStatusIn(
            existingBook.getId(),
            existingBorrower.getId(),
            OPEN_STATUSES
        );

        Borrowing borrowing = openBorrowing.orElseThrow(() -> 
            new BookNotBorrowedException(
                "Borrower with ID " + request.borrowerId() + " does not currently have this book borrowed."
            )
        );


        // Calculate late fee
        BigDecimal lateFee = calculateLateFee(borrowing, LocalDate.now(clock));

        //set late fee
        borrowing.setLateFee(lateFee);

        // Mark status as returned 
        borrowing.setStatus(BorrowingStatus.RETURNED);
        
        // Set return date
        borrowing.setReturnDate(LocalDate.now(clock));
        
        // // Update book availability
        // borrowing.getBook().setIsAvailable(true);

        // always explicitly update the book entity
        existingBook.setIsAvailable(true);
        bookRepository.save(existingBook);

        return borrowingRepository.save(borrowing);
    }

    // Finds one recode of a borrowing transaction
    @Transactional(readOnly = true)
    public Borrowing getBorrowingById(Long borrowingId){
        
        return borrowingRepository.findById(borrowingId)
            .orElseThrow(()-> new BorrowingNotFoundException("Borrowing with ID " + borrowingId + " not found.")
        );
    }

    // Finds only the active borrowings of a specific borrower
    @Transactional(readOnly = true)
    public List<Borrowing> getActiveBorrowingsForBorrower(Long borrowerId){
        if(!borrowerRepository.existsById(borrowerId)){
            throw new BorrowerNotFoundException("Borrower with ID " + borrowerId + " not found.");
        }

        return borrowingRepository.findByBorrowerIdAndStatusIn(
            borrowerId, 
            OPEN_STATUSES
        );
    }

    // Finds all the borrowings of a specific borrower
    @Transactional(readOnly = true)
    public List<Borrowing> getBorrowingHistoryForBorrower(Long borrowerId){
        if(!borrowerRepository.existsById(borrowerId)){
            throw new BorrowerNotFoundException("Borrower with ID " + borrowerId + " not found.");
        }

        return borrowingRepository.findByBorrowerId(borrowerId);
    }

    // Finds if a book is actively borrowed and returns the borrowing record
    // If book isn't borrowed, it returns an empty result
    @Transactional(readOnly = true)
    public Optional<Borrowing> getCurrentBorrowingForBook(Long bookId){
        if(!bookRepository.existsById(bookId)){
            throw new BookNotFoundException("Book with ID " + bookId + " not found.");
        }

        return borrowingRepository.findByBookIdAndStatusIn(
            bookId,
            OPEN_STATUSES
        );
    }

    // Finds all active borrowings in the system. ADMIN function
    @Transactional(readOnly = true)
    public List<Borrowing> getAllBorrowings(){
        return borrowingRepository.findByStatusIn(
            OPEN_STATUSES
        );
    }
    
    // Finds all borrowings with status ACTIVE
    @Transactional(readOnly = true)
    public List<Borrowing> getAllActiveBorrowings(){
        return borrowingRepository.findByStatus(BorrowingStatus.ACTIVE);
    }

    // Finds all borrowings with status OVERDUE
    @Transactional(readOnly = true)
    public List<Borrowing> getOverdueBorrowings(){
        return borrowingRepository.findByStatus(BorrowingStatus.OVERDUE);
    }

}