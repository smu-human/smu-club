package com.example.smu_club.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String studentId;
    private String password;
}
