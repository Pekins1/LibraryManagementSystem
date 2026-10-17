package com.prahkel.library.repository;

import com.prahkel.library.model.LibraryMember;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryMemberRepository extends JpaRepository<LibraryMember, Long> {
    Optional<LibraryMember> findByEmail(String email);
    
    Optional<LibraryMember> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);
    
    boolean existsByEmailIgnoreCase(String email);
}
