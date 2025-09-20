package com.example.smu_club.auth.security;


import com.example.smu_club.auth.repository.MemberRepository;
import com.example.smu_club.auth.token.JwtTokenProvider;
import com.example.smu_club.domain.Member;
import com.example.smu_club.exception.custom.MemberNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;


import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1. 토큰 가져오기
        String token = resolveToken(request);

        //2. 토큰 유효성 검증
        if(token != null && jwtTokenProvider.validateToken(token)){

            //3. 유효하다면 토큰에서 학번 가져오기
            String studentId = jwtTokenProvider.getStudentIdFromToken(token);

            //4. 학번으로 Member 엔티티 조회(db)
            Member member =
                    memberRepository.findByStudentId(studentId).orElseThrow(() -> new MemberNotFoundException(studentId));

            //5. 조회한 Member 정보로 CustomUserDetails 객체 생성
            CustomUserDetails userDetails = new CustomUserDetails(member);

            // 6. Authentication 객체를 만들어 SecurityContext에 저장합니다.
            //    이것으로 이제 이 요청은 '인증된' 요청이 되며, @AuthenticationPrincipal을 사용할 수 있게 됩니다.
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        //요청 전달
        filterChain.doFilter(request, response);

    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


}
