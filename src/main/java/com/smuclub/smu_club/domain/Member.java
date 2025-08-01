package com.smuclub.smu_club.domain;


import javax.persistence.*;
import javax.persistence.Entity;

import lombok.*;
import org.hibernate.annotations.*;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private long id;

    @OneToMany(mappedBy = "member")
    private List<ClubMember> clubMembers = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Answer> answers = new ArrayList<>();

    @NaturalId
    @Column(unique = true, nullable = false, name = "student_id")
    private String studentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String department;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Member() {}
}
