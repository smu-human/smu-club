package com.example.smu_club.util;

import com.example.smu_club.answer.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OracleStorageCleanupScheduler {
    private final OciStorageService ociStorageService;
    private final AnswerRepository answerRepository;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시에 실행
    public void cleanUpOrphanFiles() {
        log.info("==고아 파일 삭제 스케줄러 시작==");

        //List<String> storedFileUrls = ociStorageService.getOldFileKeys();

        if(storedFileUrls .isEmpty()) {
            log.info("==삭제할 파일이 없습니다==");
            return;
        }

        //List<String> dbFileUrls = answerRepository.findAllFileKeys();
        //Set<String> validKeys = new HashSet<>(dbFileUrls);

        int deleteCount = 0;
        List<String> deleteKeyList = new ArrayList<>();
        for(String key : storedFileUrls) {
            if(!validKeys.contains(key)) {
                deleteKeyList.add(key);
                deleteCount++;
            }
        }

        if(!deleteKeyList.isEmpty()) {
            ociStorageService.deleteUrls(deleteKeyList);
            log.info("==고아 파일 삭제 완료: {}개 파일 삭제==", deleteCount);
        } else {
            log.info("==삭제할 파일이 없습니다==");
        }
    }
}
