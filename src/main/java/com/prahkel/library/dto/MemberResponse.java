package com.prahkel.library.dto;

import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.model.MemberType;

public record MemberResponse(
    Long id,
    String name,
    String email,
    MemberType typy
){
    public static MemberResponse from(LibraryMember member){
        return new MemberResponse(
            member.getId(),
            member.getName(),
            member.getEmail(),
            member.getType()
        );
    }
    
}
