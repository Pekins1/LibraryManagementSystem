package com.prahkel.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

import com.prahkel.library.model.MemberType;

public record CreateMemberRequest (
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotNull(message = "Member type is required")
    MemberType type

){
    
}
