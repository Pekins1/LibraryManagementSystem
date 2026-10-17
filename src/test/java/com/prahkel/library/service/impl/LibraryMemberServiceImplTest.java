package com.prahkel.library.service.impl;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


import com.prahkel.library.dto.CreateMemberRequest;
import com.prahkel.library.exception.MemberAlreadyExistsException;
import com.prahkel.library.exception.MemberNotFoundException;
import com.prahkel.library.model.MemberType;
import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.repository.LibraryMemberRepository;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LibraryMemberServiceImplTest {
    @Mock 
    private LibraryMemberRepository libraryMemberRepository;

    @InjectMocks 
    private LibraryMemberServiceImpl libraryMemberService;


    @Test 
    void createMember_shouldSucceedWhenEmailDoesNotExist(){
        CreateMemberRequest request = new CreateMemberRequest(
            "Rahkel Pekins",
            "rahkel@Example.com", 
            MemberType.INDIVIDUAL
        );

        LibraryMember member = new LibraryMember(
            "Rahkel Pekins",
            "rahkel@Example.com",
            MemberType.INDIVIDUAL
        );

        when(libraryMemberRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(libraryMemberRepository.saveAndFlush(any(LibraryMember.class))).thenReturn(member);

        LibraryMember result = libraryMemberService.createMember(request);

        assertNotNull(result);
        assertEquals(member.getName(),result.getName());
        assertEquals(member.getEmail(), result.getEmail());
        assertEquals(member.getType(), result.getType());

        verify(libraryMemberRepository).existsByEmailIgnoreCase(request.email());
        verify(libraryMemberRepository).saveAndFlush(any(LibraryMember.class));
    }

    @Test 
    void createMember_shouldThrowWhenEmailAlreadyExists(){
        CreateMemberRequest request = new CreateMemberRequest(
            "Rahkel Pekins",
            "rahkel@Example.com", 
            MemberType.INDIVIDUAL
        );

        when(libraryMemberRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThrows(MemberAlreadyExistsException.class,
            () -> libraryMemberService.createMember(request)
        );

        verify(libraryMemberRepository).existsByEmailIgnoreCase(request.email());
        verify(libraryMemberRepository, never()).saveAndFlush(any(LibraryMember.class));
    }

    @Test 
    void getMemberById_shouldSucceedWhenMemberWithIdExists(){
        Long id = 1L;

        LibraryMember member = new LibraryMember(
            "Rahkel Pekins",
            "rahkel@Example.com",
            MemberType.INDIVIDUAL
        );

        member.setId(id);

        when(libraryMemberRepository.findById(id)).thenReturn(Optional.of(member));

        LibraryMember result = libraryMemberService.getMemberById(id);

        assertNotNull(result);
        assertEquals(member.getId(), result.getId());
        assertEquals(member.getName(), result.getName());
        assertEquals(member.getEmail(), result.getEmail());
        assertEquals(member.getType(), result.getType());

        verify(libraryMemberRepository).findById(id);
    }

    @Test 
    void getMemberById_shouldThrowWhenMemberWithIdDoesNotExists(){
        Long id = 1L;

        when(libraryMemberRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class,
            () -> libraryMemberService.getMemberById(id)
        );

        verify(libraryMemberRepository).findById(id);
    }

    @Test 
    void getMemberById_shouldSucceedWhenMemberWithEmailExists(){

        String email = "rahkel@Example.com";
        
        LibraryMember member = new LibraryMember(
            "Rahkel Pekins",
            "rahkel@Example.com",
            MemberType.INDIVIDUAL
        );


        when(libraryMemberRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(member));

        LibraryMember result = libraryMemberService.getMemberByEmail(email);

        assertNotNull(result);
        assertEquals(member.getId(), result.getId());
        assertEquals(member.getName(), result.getName());
        assertEquals(member.getEmail(), result.getEmail());
        assertEquals(member.getType(), result.getType());


        verify(libraryMemberRepository).findByEmailIgnoreCase(email);
    }
    
     @Test 
    void getMemberById_shouldThrowWhenMemberWithEmailDoesNotExists(){
        String email = "rahkel@Example.com";

        when(libraryMemberRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class,
            () -> libraryMemberService.getMemberByEmail(email)
        );

        verify(libraryMemberRepository).findByEmailIgnoreCase(email);
    }
}
