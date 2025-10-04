package com.example.smu_club.exception;


import com.example.smu_club.common.ApiResponseDto;
import com.example.smu_club.exception.custom.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
    Auth 예외 관련
     */
    // [로그인 실패] 예외 처리 -> 401 Unauthorized 응답
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleLoginFailedException(LoginFailedException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("LOGIN_FAILED", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // [미가입 회원] 예외 처리 -> 404 Not Found 응답
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMemberNotFoundException(MemberNotFoundException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("MEMBER_NOT_FOUND", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // [유효하지 않은 토큰 예외처리] RefreshToken 관련 -> 401 Unauthorized 응답
    // 예시 상황 : JWT 토큰이 없거나, 서명이 잘못되었거나 , 권한정보가 없거나
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleInvalidTokenException(InvalidRefreshTokenException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("INVALID_REFRESH_TOKEN", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // [이미 가입된 회원이 다시 인증시도] -> 409 Conflict 응답
    @ExceptionHandler(MemberAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMemberAlreadyExistsException(MemberAlreadyExistsException e) {

        ApiResponseDto<Object> response = ApiResponseDto.fail("MEMBER_ALREADY_EXISTS", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // [학교 API 인증에 실패했을 때] -> 401 Unauthorized 응답
    @ExceptionHandler(UnivAuthenticationFailedException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleUnivAuthenticationFailedException(UnivAuthenticationFailedException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("UNIV_AUTH_FAILED", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    /*
    Club 예외 관련
     */

    // [클럽 정보를 찾을 수 없음] -> 404 Not Found 반환
    @ExceptionHandler(ClubNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleClubNotFoundException(ClubNotFoundException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("CLUB_NOT_FOUND", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // [클럽 정보들을 찾을 수 없음] -> 404 Not Found 반환
    @ExceptionHandler(ClubsNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleClubsNotFoundException(ClubsNotFoundException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("CLUBS_NOT_FOUND", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // [UPCOMING 이 아닌 동아리에서 모집예정 버튼을 누르면] -> 409 CONFLICT 반환 (시나리오 상 일어날 수 없음)
    @ExceptionHandler(IllegalClubStateException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleIllegalClubStateException(IllegalClubStateException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("ILLEGAL_CLUB", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /*
    ClubMember 예외 관련
     */

    // [ClubMember 테이블에서 정보를 찾을 수 없다면] -> 404 NOT FOUND 반환
    @ExceptionHandler(ClubMemberNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleClubMemberNotFoundException(ClubMemberNotFoundException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("CLUB_MEMBER_NOT_FOUND", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // [ClubMember 에는 있지만 권한이 OWNER 이 아니라면] ->  403 Forbidden 반환
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleAuthorizationException(AuthorizationException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("AUTHORIZATION", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /*
    Question 예외 관련
     */
    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleQuestionNotFoundException(QuestionNotFoundException e) {
        ApiResponseDto<Object> response = ApiResponseDto.fail("QUESTION_NOT_FOUND", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

}
