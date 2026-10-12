package com.prahkel.library.model;

import jakarta.persistence.*;


@Entity
@Table(name = "library_members")
public class LibraryMember {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType type;

    protected LibraryMember(){

    }

    public LibraryMember(String name, String email, MemberType type){
        this.name = name;
        this.email = email;
        this.type = type;
    }


    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public MemberType getType(){
        return type;
    }

    public void setType(MemberType type){
        this.type = type;
    }

}
