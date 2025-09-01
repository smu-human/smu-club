package com.example.smu_club.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerId implements Serializable {


    private Long memberId;

    private Long clubId;

    private Long questionId;

    @Override
    public boolean equals(Object o){
        //1. 둘이 같은지 확인하는 로직
        if(this == o) return true;

        //2. 대상(인자 값)이 없거나, 비교하는 객체와 다르면 false
        if(o == null || getClass() != o.getClass()) return false;

        //복합키를 위해서 같은지 비교해주는 로직 (서로 다른 인스턴스 일수 있지만(1), null도 아니고 동일한 타입일 경우(2)

        //3. 인자로 받은 o를 ClubMemberId 타입으로 캐스팅 해주고 각 필드를 개별적으로 비교해준다.
        AnswerId that = (AnswerId) o;

        //4. 그런다음 같으면 true, 다르면 false를 반환하는 걸로 복합키 역할을 해준다..
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(clubId, that.clubId) &&
                Objects.equals(questionId, that.questionId);
    }

    @Override
    public int hashCode(){
        //복합키를 구성하는 모든 필드를 해시 처리
        return Objects.hash(memberId, clubId, questionId);
    }
}
