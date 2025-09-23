package com.example.smu_club.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
public class ApiResponseDto<T> {

    private final String status;
    private final String message;
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorCode;

    // private 로 new 를 통한 생성자 방식을 막는다
    private ApiResponseDto(String status, String message, T data, String errorCode) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    // 성공 응답 (데이터 포함)
    public static <T> ApiResponseDto<T> success(T data, String message) {
        return new ApiResponseDto<>("SUCCESS", message, data, null);
    }

    // 성공 응답 (데이터 미포함)
    public static <T> ApiResponseDto<T> success(String message) {
        return new ApiResponseDto<>("SUCCESS", message, null, null);
    }

    // 실패 응답
    public static <T> ApiResponseDto<T> fail(String errorCode, String message) {
        return new ApiResponseDto<>("FAIL",message, null, errorCode);
    }


}
