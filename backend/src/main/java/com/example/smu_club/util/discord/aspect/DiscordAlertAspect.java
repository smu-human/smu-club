package com.example.smu_club.util.discord.aspect;

import com.example.smu_club.util.discord.annotation.DiscordAlert;
import com.example.smu_club.util.discord.service.DiscordAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordAlertAspect {

    private final DiscordAlertService discordAlertService;

    @AfterThrowing(pointcut = "@annotation(discordAlert)", throwing = "ex")
    public void handleException(JoinPoint jp, DiscordAlert discordAlert, Exception ex) {
        //디스코드 알림 서비스 호출
        String methodName = jp.getSignature().getName();
        String className = jp.getSignature().getName();
        String alertTitle = discordAlert.value();

        String message = String.format(
                """
                        **[%s]**
                        - 클래스: %s
                        - 메서드: %s
                        - 메시지: %s""",
                alertTitle, className, methodName, ex.getMessage()
        );

        log.info("Discord Alert AOP가 에러를 감지했습니다.\n디스코드로 알람을 전송합니다.");
        discordAlertService.sendErrorAlert(message);

    }

}
