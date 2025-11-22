package com.example.smu_club.club.service;

import com.example.smu_club.club.dto.ClubInfoRequest;
import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.domain.*;

import com.example.smu_club.club.repository.ClubImageRepository;

import com.example.smu_club.member.repository.MemberRepository;
import com.example.smu_club.util.OciStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile; // Mockito가 아닌 Spring의 mock 파일
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 사용 설정
class OwnerClubServiceTest {

    @InjectMocks // 테스트 대상: Mock 객체들을 주입받습니다.
    private OwnerClubService ownerClubService;

    // --- 가짜(Mock)로 만들 의존성들 ---
    @Mock
    private OciStorageService ociStorageService;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ClubImageRepository clubImageRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ClubMemberRepository clubMemberRepository;

    @Test
    @DisplayName("동아리 등록 시 모든 정보(클럽, 이미지, 멤버관계)가 정상 저장된다")
    void register_Success() {
        // --- 1. Given (준비) ---

        // 가짜 파일 2개 생성
        MockMultipartFile image1 = new MockMultipartFile(
                "images", "image1.jpg", "image/jpeg", "image1_data".getBytes());
        MockMultipartFile image2 = new MockMultipartFile(
                "images", "image2.png", "image/png", "image2_data".getBytes());

        // 가짜 ClubInfoRequest 생성
        ClubInfoRequest request = new ClubInfoRequest();
        request.setName("테스트 동아리");
        request.setTitle("테스트용입니다");
        request.setDescription("설명입니다.");
        request.setPresident("회장");
        request.setContact("010-1234-5678");
        request.setClubRoom("K123");
        request.setRecruitingEnd(LocalDate.now().plusDays(10));
        request.setClubImages(List.of(image1, image2));

        String studentId = "123456";

        // 가짜 Member 객체
        Member mockOwner = Member.builder().studentId(studentId).name("테스트유저").build();

        // Mockito가 OCI와 DB의 동작을 흉내 내도록 설정

        // 1-1. OCI 업로드 흉내: image1, image2가 들어오면 각 URL을 반환
        when(ociStorageService.upload(image1)).thenReturn("http://oci.com/image1.jpg");
        when(ociStorageService.upload(image2)).thenReturn("http://oci.com/image2.png");

        // 1-2. MemberRepository 흉내: studentId로 찾으면 mockOwner 반환
        when(memberRepository.findByStudentId(studentId)).thenReturn(Optional.of(mockOwner));

        // 1-3. save() 메서드에 어떤 객체가 들어왔는지 "캡처"할 준비
        ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        ArgumentCaptor<List<ClubImage>> imageListCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<ClubMember> clubMemberCaptor = ArgumentCaptor.forClass(ClubMember.class);

        // --- 2. When (실행) ---
        ownerClubService.register(studentId, request);

        // --- 3. Then (검증) ---

        // 3-1. OCI 업로드가 2번 호출되었는지 검증
        verify(ociStorageService, times(2)).upload(any(MultipartFile.class));

        // 3-2. Club 저장이 1번 호출되었는지 검증 (그리고 저장된 Club 객체 캡처)
        verify(clubRepository, times(1)).save(clubCaptor.capture());
        Club savedClub = clubCaptor.getValue(); // 캡처한 Club 객체

        // 3-3. ClubImage 저장이 1번 호출되었는지 검증 (그리고 저장된 List 캡처)
        verify(clubImageRepository, times(1)).saveAll(imageListCaptor.capture());
        List<ClubImage> savedImages = imageListCaptor.getValue(); // 캡처한 List<ClubImage>

        // 3-4. ClubMember 저장이 1번 호출되었는지 검증 (그리고 저장된 ClubMember 객체 캡처)
        verify(clubMemberRepository, times(1)).save(clubMemberCaptor.capture());
        ClubMember savedClubMember = clubMemberCaptor.getValue();

        // --- 4. Assert (주장) ---

        // 4-1. 저장된 Club 객체가 올바른가?
        assertThat(savedClub.getName()).isEqualTo("테스트 동아리");
        assertThat(savedClub.getPresident()).isEqualTo("회장");
        assertThat(savedClub.getThumbnailUrl()).isEqualTo("http://oci.com/image1.jpg"); // 첫 번째 이미지 URL

        // 4-2. 저장된 ClubImage 리스트가 올바른가?
        assertThat(savedImages).hasSize(2); // 2개가 저장되었는가
        assertThat(savedImages.get(0).getImageUrl()).isEqualTo("http://oci.com/image1.jpg");
        assertThat(savedImages.get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(savedImages.get(0).getClub()).isEqualTo(savedClub); // Club과 연결되었는가
        assertThat(savedImages.get(1).getImageUrl()).isEqualTo("http://oci.com/image2.png");
        assertThat(savedImages.get(1).getDisplayOrder()).isEqualTo(1);

        // 4-3. 저장된 ClubMember 관계가 올바른가?
        assertThat(savedClubMember.getMember()).isEqualTo(mockOwner); // Member와 연결
        assertThat(savedClubMember.getClub()).isEqualTo(savedClub); // Club과 연결
        assertThat(savedClubMember.getClubRole()).isEqualTo(ClubRole.OWNER); // 역할이 OWNER인가
    }

    // (필요시) 이미지가 없을 때의 테스트, 멤버를 못 찾았을 때의 테스트 등을 추가
}