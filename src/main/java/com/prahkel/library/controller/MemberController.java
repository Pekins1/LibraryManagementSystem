package com.prahkel.library.controller;



import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.prahkel.library.dto.CreateMemberRequest;
import com.prahkel.library.dto.MemberResponse;
import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.service.LibraryMemberService;



@RestController                          // 1: this class serves HTTP, and return values become JSON
@RequestMapping("/api/v1/members")       // 2: every method below starts with this path
@Validated                              // 3: turns on validation for @PathVariable/@RequestParam
public class MemberController {
    private final LibraryMemberService memberService;

    public MemberController(LibraryMemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(
            @Valid @RequestBody CreateMemberRequest request){
        LibraryMember saved = memberService.createMember(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MemberResponse.from(saved)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember(
            @Positive @PathVariable Long id) {
        LibraryMember libMember = memberService.getMemberById(id);
        return ResponseEntity.ok(MemberResponse.from(libMember));
    }

    @GetMapping
    public ResponseEntity<MemberResponse> getMemberByEmail(
            @NotBlank @RequestParam String email){
        LibraryMember libMember = memberService.getMemberByEmail(email);
        return ResponseEntity.ok(MemberResponse.from(libMember));
    }

}
