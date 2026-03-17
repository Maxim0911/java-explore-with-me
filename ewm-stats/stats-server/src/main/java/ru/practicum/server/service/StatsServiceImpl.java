package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.server.mapper.StatsMapper;
import ru.practicum.server.repository.StatsRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    public void saveHit(HitDto hitDto) {
        log.info("Saving hit: {}", hitDto);
        statsRepository.save(statsMapper.toEntity(hitDto));
    }

    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                   List<String> uris, Boolean unique) {
        log.info("Getting stats from {} to {}, uris: {}, unique: {}", start, end, uris, unique);

        List<Object[]> results;

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                results = statsRepository.getUniqueStats(start, end);
            } else {
                results = statsRepository.getStats(start, end);
            }
        } else {
            if (unique) {
                results = statsRepository.getUniqueStatsByUris(start, end, uris);
            } else {
                results = statsRepository.getStatsByUris(start, end, uris);
            }
        }

        return results.stream()
                .map(statsMapper::toStatsDto)
                .collect(Collectors.toList());
    }
}