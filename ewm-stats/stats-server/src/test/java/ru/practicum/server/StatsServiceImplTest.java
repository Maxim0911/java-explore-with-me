package ru.practicum.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.HitDto;

import ru.practicum.server.mapper.StatsMapper;
import ru.practicum.server.model.EndpointHit;
import ru.practicum.server.repository.StatsRepository;
import ru.practicum.server.service.StatsServiceImpl;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @Mock
    private StatsMapper statsMapper;

    @InjectMocks
    private StatsServiceImpl statsService;

    private HitDto hitDto;
    private EndpointHit endpointHit;

    @BeforeEach
    void setUp() {
        hitDto = HitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        endpointHit = EndpointHit.builder()
                .id(1L)
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void saveHit_shouldCallRepository() {
        when(statsMapper.toEntity(any(HitDto.class))).thenReturn(endpointHit);
        when(statsRepository.save(any(EndpointHit.class))).thenReturn(endpointHit);

        statsService.saveHit(hitDto);

        verify(statsRepository, times(1)).save(any(EndpointHit.class));
    }

    @Test
    void getStats_withoutUris_shouldCallCorrectRepositoryMethod() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        statsService.getStats(start, end, null, false);

        verify(statsRepository, times(1)).getStats(start, end);
        verify(statsRepository, never()).getUniqueStats(any(), any());
        verify(statsRepository, never()).getStatsByUris(any(), any(), any());
    }

    @Test
    void getStats_withUnique_shouldCallUniqueMethod() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        statsService.getStats(start, end, null, true);

        verify(statsRepository, times(1)).getUniqueStats(start, end);
    }
}