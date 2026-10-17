package com.prahkel.library.service.impl;


import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import com.prahkel.library.dto.CreateMemberRequest;
import com.prahkel.library.exception.MemberAlreadyExistsException;
import com.prahkel.library.exception.MemberNotFoundException;
import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.service.LibraryMemberService;
import com.prahkel.library.repository.LibraryMemberRepository;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Service
@Validated 
@Transactional(readOnly = true)
public class LibraryMemberServiceImpl implements LibraryMemberService {

    private final LibraryMemberRepository libraryMemberRepository;

    public LibraryMemberServiceImpl(LibraryMemberRepository libraryMemberRepository){
        this.libraryMemberRepository = libraryMemberRepository;
    }

    @Override 
    @Transactional 
    public LibraryMember createMember(@Valid @NotBlank @NotNull CreateMemberRequest request){

        if(libraryMemberRepository.existsByEmailIgnoreCase(request.email())){
            throw new MemberAlreadyExistsException(
                "Member with Email: " + request.email() + " already exists."
            );
        }

        LibraryMember member = toMember(request);

        try{
            return libraryMemberRepository.saveAndFlush(member);
        }catch(DataIntegrityViolationException exception){
            if(isEmailConstraintViolation(exception)){
                throw new MemberAlreadyExistsException(
                    "A member with Email: " + request.email() + " already exists."
                );
            }
            throw exception;
        }   
    };

    @Transactional(readOnly = true)
    public LibraryMember getMemberById(@NotNull @Positive Long id){
        return libraryMemberRepository.findById(id)
            .orElseThrow(() -> new MemberNotFoundException("Member with ID: " + id + " not found.")
        );
    
    };

    public LibraryMember getMemberByEmail(@NotNull String email){
        return libraryMemberRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new MemberNotFoundException("Member with Email: " + email + " not found.")
        );

    };
    
    private LibraryMember toMember(CreateMemberRequest request){
        return new LibraryMember(
            request.name(),
            request.email(),
            request.type()
        );
    }

    private boolean isEmailConstraintViolation(Throwable exception) {
        for( Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if(cause instanceof ConstraintViolationException violation) {
                return "library_member_email".equalsIgnoreCase(violation.getConstraintName());
            }
        }
        return false;
    }
}
