package com.example.smu_club.club.service;

import com.example.smu_club.club.dto.ClubResponseDto;
import com.example.smu_club.club.dto.ClubsResponseDto;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.RecruitingStatus;
import com.example.smu_club.exception.custom.ClubNotFoundException;
import com.example.smu_club.exception.custom.ClubsNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuestClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private GuestClubService guestClubService;


    @Test
    @DisplayName("성공")
    public void 메인페이지_동아리목록_단위테스트_성공(){
        //Given
        List<Club> mockClubs = new ArrayList<>();
        for(int i = 0 ; i < 10 ; i++){
            mockClubs.add(new Club(
                    (long)i+1,
                    "동아리 "+i,
                    "test",
                    "test",
                    LocalDateTime.now(),
                    RecruitingStatus.OPEN,
                    LocalDate.of(2025,10,1),
                    LocalDate.of(2025,10,7),
                    "유승준",
                    "010-4130-1904",
                    "G308",
                    "www.github.com/fluanceifi"
            ));
        }


        //When

        //실제 DB를 거치지 않고 반환
        when(clubRepository.findAll()).thenReturn(mockClubs);
        List<ClubsResponseDto> result = guestClubService.findAllClubs();

        //Then
        assertThat(result).hasSize(10);
        assertThat(result.get(5).getName()).isEqualTo("동아리 5");

        verify(clubRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("ClubsNotFoundException 예외 실행")
    public void 메인페이지_동아리목록_단위테스트_실패(){
        //Given
        List<Club> mockClubs = new ArrayList<>();

        //When
        when(clubRepository.findAll()).thenReturn(mockClubs);


        //Then
        ClubsNotFoundException e =
                Assertions.assertThrows(ClubsNotFoundException.class,
                        () -> guestClubService.findAllClubs());

        assertThat(e.getMessage()).isEqualTo("등록된 클럽이 하나도 없습니다.");
    }


    @Test
    public void 메인페이지_동아리상세페이지_단위테스트_성공(){
        //Given
        Club mockClub = new Club(
                3L,
                "동아리 "+3,
                "test",
                "test",
                LocalDateTime.now(),
                RecruitingStatus.OPEN,
                LocalDate.of(2025,10,1),
                LocalDate.of(2025,10,7),
                "유승준",
                "010-4130-1904",
                "G308",
                "www.github.com/fluanceifi"
        );
        Long clubId = 3L;
        Long noClubId = 4L;

        //When

        //성공
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(mockClub));
        ClubResponseDto result1 = guestClubService.findClubById(clubId);

        //Then
        assertThat(result1).isNotNull();
        assertThat(result1.getName()).isEqualTo("동아리 3");


    }

    @Test
    public void 메인페이지_동아리상세페이지_단위테스트_실패(){
        //Given

        //When&Then
        when(clubRepository.findById(3L)).thenReturn(Optional.empty());
        ClubNotFoundException e = Assertions.assertThrows(ClubNotFoundException.class, () -> guestClubService.findClubById(3L));



    }
}
