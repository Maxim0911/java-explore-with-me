package ru.practicum.server.mapper;

import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.server.model.EndpointHit;
import org.springframework.stereotype.Component;

@Component
public class StatsMapper {

    public EndpointHit toEntity(HitDto hitDto) {
        return EndpointHit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }

    public StatsDto toStatsDto(Object[] result) {
        return StatsDto.builder()
                .app((String) result[0])
                .uri((String) result[1])
                .hits((Long) result[2])
                .build();
    }
}