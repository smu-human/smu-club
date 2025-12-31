package com.example.smu_club.util;

import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.RecruitingStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecruitmentService {

//    @Transactional(readOnly = true)
//    public List<ClosureTarget> findEndedClubs(LocalDateTime now) {
//        String time = now.toString();
//
//
//    }

    @Transactional
    public void closeRecruitments(List<ClosureTarget> list) {
    }

    @RequiredArgsConstructor
    @Getter
    @Builder
    public static class ClosureTarget {
        private final long clubId;
        private final RecruitingStatus previousStatus;

        public static ClosureTarget from(Club club) {
            return ClosureTarget.builder()
                    .clubId(club.getId())
                    .previousStatus(club.getRecruitingStatus())
                    .build();
        }


    }
}
