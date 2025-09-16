package com.example.smu_club.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UnivUserInfoResponse {
    private String username;
    private String name;
    private String email;
    private String department;
    private String secondDepartment;
}
