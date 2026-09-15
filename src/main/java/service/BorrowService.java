package service;

import dto.BorrowBookRequest;
import dto.ReturnBookRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import model.Borrowing;
import java.util.*;


public interface BorrowService {

    Borrowing borrowBook(@Valid @NotNull BorrowBookRequest borrow);

    Borrowing returnBook(@Valid @NotNull ReturnBookRequest request);

    Borrowing getBorrowingById(Long borrowingId);

    List<Borrowing> getActiveBorrowingsForBorrower(Long borrowerId);

    List<Borrowing> getBorrowingHistoryForBorrower(Long borrowerId);

    Optional<Borrowing> getCurrentBorrowingForBook(Long bookId);

    List<Borrowing> getAllActiveBorrowings();

    List<Borrowing> getOverdueBorrowings();
    
}
