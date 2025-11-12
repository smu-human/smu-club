package com.example.smu_club.domain;

import com.example.smu_club.exception.custom.IllegalClubStateException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter // 테스트 용 초기 값는 넣는 용
@Builder
@NoArgsConstructor
@AllArgsConstructor
//인덱스의 효율성을 위해 "작업 범위 결정 조건", "이름" 순으로 인덱스 생성
//인덱스의 정렬은 왼쪽 컬럼 기준으로 진행하기 때문에 Unique한 값이 적을수록 인덱스 스킵 스캔 활용도가 높아진다.
//현재는 쓸 일이 없겠지만 추후에 특정 범위의 레코드를 검색할 때 좋은 효율이 나온다.
@Table(name = "club", indexes = @Index(name = "idx_club_priority_name", columnList = "recruit_priority ASC, name ASC"))
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    // 대표 메인화면에 표시되면 동아리 설명에 해당합니다.
    private String title;

    @Lob // Toast UI 에디터 내용은 길이를 예측할 수 없으므로 @Lob 추가 (TEXT 타입)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "recruiting_status", nullable = false)
    @Enumerated(EnumType.STRING) //ORDINARY 사용 금지
    private RecruitingStatus recruitingStatus = RecruitingStatus.CLOSED; //짝이 되는 priority에게 Sync를 맞추기 위해 CLOSED로 초기화

    @Column(name = "recruit_priority")
    private int recruitPriority = recruitingStatus.getPriority();

    @Column(name = "recruiting_start")
    private LocalDate recruitingStart;

    @Column(name = "recruiting_end")
    private LocalDate recruitingEnd;

    private String president;

    private String contact;

    private String clubRoom;

    @Column(name = "thumbnail_url", length = 1024)
    private String thumbnailUrl;


    /*
    recruitingStatus가 변경 될 때마다
    정렬용 'recruitPriority' 컬럼을 자동으로 동기화한다.
     */
    private void syncRecruitPriority() {
        //기본 값
        this.recruitPriority = Objects.requireNonNullElse(this.recruitingStatus, RecruitingStatus.CLOSED).getPriority();
    }

    // JPA 생명주기: 저장(INSERT) 전에 호출
    @PrePersist
    public void onPrePersist() {
        syncRecruitPriority();
    }

    // JPA 생명주기: 수정(UPDATE) 전에 호출
    @PreUpdate
    public void onPreUpdate() {
        syncRecruitPriority();
    }

    //recruitingStatus를 변경할 때 recruitPriority도 같이 변경
    public void updateRecruitment(RecruitingStatus status) {
        if (status == RecruitingStatus.OPEN) {
            if (this.recruitingStatus != RecruitingStatus.UPCOMING) {
                throw new IllegalClubStateException("모집 예정 상태인 동아리만 모집을 시작 할 수 있습니다.");
            }
            this.recruitingStart = LocalDate.now();
        }

        this.recruitingStatus = status;
        syncRecruitPriority();
    }
}

