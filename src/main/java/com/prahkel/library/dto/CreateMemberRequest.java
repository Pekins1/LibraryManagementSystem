package com.prahkel.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.prahkel.library.model.MemberType;

public record CreateMemberRequest (
    @NotBlank 
    String name,

    @NotBlank 
    String email,

    @NotNull 
    MemberType type

){
    
}
