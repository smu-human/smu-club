package com.example.smu_club.exception.custom;

public class ApplicationNotFoundException extends RuntimeException{
    public ApplicationNotFoundException(Long clubId) {
        super("해당 동아리(ID: " + clubId + ")에 대한 지원 이력을 찾을 수 없습니다.");
    }
}
