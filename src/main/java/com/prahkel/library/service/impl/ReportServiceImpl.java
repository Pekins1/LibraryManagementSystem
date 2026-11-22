package com.prahkel.library.service.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.BookLifecycleStatus;
import com.prahkel.library.model.Borrowing;
import com.prahkel.library.model.BorrowingStatus;
import com.prahkel.library.repository.BookCopyRepository;
import com.prahkel.library.repository.BookRepository;
import com.prahkel.library.repository.BorrowingRepository;
import com.prahkel.library.service.BookService;
import com.prahkel.library.service.BorrowService;
import com.prahkel.library.service.ReportService;

@Service
@Validated
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowingRepository borrowingRepository;
    private final BookService bookService;
    private final BorrowService borrowService;
    private final Clock clock;

    public ReportServiceImpl(BookRepository bookRepository,
                         BookCopyRepository bookCopyRepository,
                         BorrowingRepository borrowingRepository,
                         BookService bookService,
                         BorrowService borrowService,
                         Clock clock) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.borrowingRepository = borrowingRepository;
        this.bookService = bookService;
        this.borrowService = borrowService;
        this.clock = clock;
    }
    // 1. Direct delegate - the service already does it
    @Override
    public List<BookCopy> getCopiesForBook(Long bookId){
        return bookService.getCopiesForBook(bookId);
    } 

     @Override
    public List<Borrowing> getAllActiveBorrowings() {
        return borrowService.getAllActiveBorrowings();
    }

    @Override 
    public List<Borrowing> getBorrowingHistoryForBorrower(Long borrowerId){
        return borrowService.getBorrowingHistoryForBorrower(borrowerId);
    }

    @Override 
    public List<Borrowing> getCurrentBorrowingsForBorrower(Long borrowerId){
        return borrowService.getActiveBorrowingsForBorrower(borrowerId);
    }

    @Override 
    public List<BookCopy> getAvailableCopiesForBook(Long bookId) {
        return bookService.getAvailableCopiesForBook(bookId);
    }

    // 2. Repository count - the queary already exists
    @Override 
    public long getTotalBookCopyCount() {
        return bookCopyRepository.count();
    }

      @Override 
    public long getTotalBookCount(){
        return bookRepository.count();
    }

    @Override 
    public long getTotalArchivedBookCount(){
        return bookRepository.countByLifecycle(BookLifecycleStatus.ARCHIVED);
    }

    @Override 
    public long getAvailableBookCopyCount(){
        return bookCopyRepository.countByIsAvailableTrue();
    }

    @Override 
    public long getUnavailableBookCopyCount(){
        return bookCopyRepository.countByIsAvailableFalse();
    }

    @Override 
    public long getActiveBorrowingCount() {
        return borrowingRepository.countByStatus(BorrowingStatus.ACTIVE);
    }

    @Override 
    public long getOverdueBorrowingCount(){
        return getAllOverdueBorrowings().size();
    }
    
    @Override 
    public long getCopyCountForBook(Long bookId){
        return bookCopyRepository.countByBookId(bookId);
    }
    
    @Override 
    public long getAvailableCopyCountForBook(Long bookId){
        return bookCopyRepository.countByBookIdAndIsAvailableTrue(bookId);
    }


    // 3. Overdue, computed — because OVERDUE is never set, don't delegate
    //      to BorrowService.getOverdueBorrowings() (it returns empty in practice)
    @Override 
    public List<Borrowing> getAllOverdueBorrowings() {
        LocalDate today = LocalDate.now(clock);
        return borrowingRepository.findByStatusIn(
                    List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
                .stream()
                .filter(b -> b.getDueDate() != null && b.getDueDate().isBefore(today))
                .toList();
    }

   
    
    // 4. Late-fee sum - Borrowing DOES carry a lateFee field (set at return time),
    //    so this is a sum over stored values, not a recalculation
    @Override 
    public BigDecimal getTotalLateFeesAssessed() {
        return borrowingRepository.sumLateFees();
    }

 

}


