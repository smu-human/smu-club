package com.example.smu_club.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
@Component
@Slf4j
@RequiredArgsConstructor
public class RecruitmentAutoClosureScheduler {
    private final RecruitmentService recruitmentService;

    // 매일 자정(00:00)에 자동으로 모집 상태를 종료하는 스케줄러
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void closeExpiredRecruitments() {
        log.info("[스케줄러] 모집상태 자동 마감 스케줄러 시작");

        try {
            List<RecruitmentService.ClosureTarget> deadLineList = recruitmentService.findEndedClubs(LocalDate.now());

            if (deadLineList.isEmpty()) {
                log.info("마감될 동아리가 없습니다.");
                return;
            }

            log.info("[스케줄러] 총 {}개의 동아리 마감 처리 시도", deadLineList.size());

            int result = recruitmentService.closeRecruitments(deadLineList);

            if (result != deadLineList.size()) {
                log.warn("[스케줄러] 일부 동아리 처리 누락 (대상: {}, 실제 처리: {}) - 이미 삭제되었을 수 있습니다.",
                        deadLineList.size(), result);
            }
            else{
                log.info("[스케줄러] 모든 동아리 마감 처리 완료 ({}건)", result);
            }


            log.info("[스케줄러] 모집상태 자동 마감 스케줄러 종료");

        } catch (Exception e) {
            log.error("[스케줄러] 모집상태 자동 마감 스케줄러 중 오류 발생: {}", e.getMessage(), e);

        }


    }
}
