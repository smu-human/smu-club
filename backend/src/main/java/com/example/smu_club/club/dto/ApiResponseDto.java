package com.example.smu_club.club.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public class ApiResponseDto {

    @Data
    @AllArgsConstructor
    public static class ApiResponseToList<T>{
        private List<T> data;
    }
    @Data
    @AllArgsConstructor
    public static class ApiResponse<T>{
        private T data;
    }


}
