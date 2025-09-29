package com.example.smu_club.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class EditMyInfoResponseDto {
    private int memberId;//내정보 수정 할때 식별용
    private String name;
    private int mail;
    private String phone;
}
