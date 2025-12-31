//package com.example.smu_club.util;
//
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.List;
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class RecruitmentAutoClosureScheduler {
//    private final RecruitmentService recruitmentService;
//
//    // 매일 자정(00:00)에 자동으로 모집 상태를 종료하는 스케줄러
//    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 0 * * *")
//    public void closeExpiredRecruitments() {
//        log.info("모집상태 자동 마감 스케줄러 시작");
//
//        //service 계층보다 scheduler 계층이 더 높아, service 계층에를 inner class를 추가하는 것이 맞음
//        List<RecruitmentService.ClosureTarget> list = recruitmentService.findEndedClubs(LocalDateTime.now());
//
//        if(list.isEmpty()) {
//            log.info("마감될 동아리가 없습니다.");
//            return;
//        }
//
//        recruitmentService.closeRecruitments(list);
//
//        log.info("마감된 동아리 수: {}", list.size());
//        log.info("모집상태 자동 마감 스케줄러 종료");
//
//    }
//
//
//}
