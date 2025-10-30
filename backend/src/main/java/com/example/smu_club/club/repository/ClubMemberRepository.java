package com.example.smu_club.club.repository;

import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.ClubMember;
import com.example.smu_club.domain.ClubRole;
import com.example.smu_club.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Long countByClubId(Long clubId);

    @Query("SELECT cm FROM ClubMember cm JOIN FETCH cm.club WHERE cm.member.id = :memberId AND cm.clubRole = :clubRole")
    List<ClubMember> findByMemberIdAndClubRoleWithClub(long memberId, ClubRole clubRole);
  
    @Query( "SELECT cm FROM ClubMember cm JOIN FETCH cm.member m JOIN FETCH cm.club c WHERE m.studentId = :studentId")
    List<ClubMember> findAllWithMemberAndClubByStudentId(@Param("studentId") String studentId);

    @Query( "SELECT cm FROM ClubMember cm JOIN FETCH cm.member m JOIN FETCH cm.club c WHERE m.studentId = :studentId AND c.id = :clubId")
    ClubMember findAllWithMemberAndClubByStudentIdAndClubId(@Param("studentId") String studentId, @Param("clubId") Long clubId);

    Optional<ClubMember> findByClubAndMember(Club club, Member member);


    /**
     *     {@code @Modifying(clearAutomatically} = true) 를 쓰는 이유는 이러하다.
     *     {@code @Modifying} 쿼리가 실행되면 실행되면 1차 캐시를 무시하고 DB로 먼저 전송되어 DB에 데이터가 바뀌게 된다.
     *     이 때 1차 캐시를 무시한 대가는 이전에 @Transactional 메소드 로직에 남아 있던 객체와 상태 불일치가 되는 것이다.
     *     때문에 clearAutomatically = true로 1차 캐시를 싹 비워 나중에 다시 객체를 사용할 일 이 있다면 DB에서 새로 가져와 상태를 일치시킨다.
     *
     *     그렇다면 그냥 @Modifying 쿼리를 사용안하면 되는거 아닌가? 싶지만 그건 아니다.
     *     왜냐하면 ClubMember 레코드를 삭제하는 과정에서 조회 -> 삭제 로 2번의 쿼리가 발생하는데 이 때 삭제가 1차 캐시에 객체가 저장되고 삭제하는 것이다.
     *     따라서 조회 -> 1차 캐시 저장 -> 저장된 캐시 내용을 바탕으로 삭제 라는 비효율적인 3단계를 거친다.
     *     따라서 @Modifying를 사용하면 1차캐시를 무시하고 DB에서 직접 삭제 후 1차 캐시도 초기화 시켜서 한번에 일을 하는 것이다.
     *
     *     결론:
     *     1. trade off로 1차 캐시를 무시하여 빠르지만 위험한 Modifying
     *     2. 느리지만 안전한 2번의 쿼리
     *     3. 이 둘을 모두 해결 할 수 있는 삭제 후 캐시 초기화 clearAutomatically = true
     */

    //Modifying이 필요없어짐.
    void deleteByClubIdAndMemberId(Long clubId, Long memberId);
}
