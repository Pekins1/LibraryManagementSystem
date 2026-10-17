package com.prahkel.library.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;


import com.prahkel.library.dto.CreateMemberRequest;
import com.prahkel.library.model.LibraryMember;

public interface LibraryMemberService {

    LibraryMember createMember(@Valid @NotNull CreateMemberRequest request);

    LibraryMember getMemberById(@NotNull @Positive Long id);

    LibraryMember getMemberByEmail(@NotBlank String email);


    
}
