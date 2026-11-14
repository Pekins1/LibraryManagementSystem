package com.prahkel.library.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.prahkel.library.dto.CreateMemberRequest;
import com.prahkel.library.exception.MemberAlreadyExistsException;
import com.prahkel.library.exception.MemberNotFoundException;
import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.model.MemberType;
import com.prahkel.library.service.LibraryMemberService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MemberController.class)
public class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryMemberService memberService;

    @Autowired 
    private ObjectMapper objectMapper;
    @Test
    void createMember_shouldReturn201WhenRequestIsValid() throws Exception {
        CreateMemberRequest request = new CreateMemberRequest(
            "rahkel-pekins", "example@gmail.com", MemberType.ORGANIZATION);

        LibraryMember member = new LibraryMember(
            "rahkel-pekins", "example@gmail.com", MemberType.ORGANIZATION);
        member.setId(1L);

        when(memberService.createMember(any())).thenReturn(member);

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                    )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("rahkel-pekins"))
                .andExpect(jsonPath("$.email").value("example@gmail.com"));
    }

    @Test
    void createMember_shouldReturn409WhenEmailAlreadyExists() throws Exception{
        CreateMemberRequest request = new CreateMemberRequest(
            "rahkel-pekins", "example@gmail.com", MemberType.ORGANIZATION);

        when(memberService.createMember(any())).thenThrow(
            new MemberAlreadyExistsException("Member with email: " + request.email() + " already exists.")
        );

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("409"));
    }

    @Test
    void getMember_shouldReturn200WhenMemberExists() throws Exception{
        LibraryMember member = new LibraryMember(
            "membersName", "example@gmail.com", MemberType.INDIVIDUAL);
        member.setId(1L);
        when(memberService.getMemberById(1L)).thenReturn(member);

        mockMvc.perform(get("/api/v1/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("membersName"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getMember_shouldReturn404WhenMemberDoesNotExist()throws Exception{
        when(memberService.getMemberById(1L)).thenThrow(
            new MemberNotFoundException("Member with ID: 1L not found")
        );

        mockMvc.perform(get("/api/v1/members/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getMemberByEmail_shouldReturn200WhenEmailExists()throws Exception{
        LibraryMember member = new LibraryMember(
            "membersName", "example@gmail.com", MemberType.INDIVIDUAL);
        member.setId(1L);
        when(memberService.getMemberByEmail("example@gmail.com")).thenReturn(member);

        mockMvc.perform(get("/api/v1/members")
                        .param("email", "example@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("membersName"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getMemberByEmail_shouldReturn404WhenEmailDoesNotExist()throws Exception{
        when(memberService.getMemberByEmail("example@gmail.com")).thenThrow(
            new MemberNotFoundException("Member with Email:example@gmail.com does not exist")
        );

        mockMvc.perform(get("/api/v1/members")
                        .param("email","example@gmail.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getMemberByEmail_shouldReturn400WhenEmailIsBlank()throws Exception{
        mockMvc.perform(get("/api/v1/members")
                        .param("email"," "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(memberService, never()).getMemberByEmail(any());
    }

    
}
