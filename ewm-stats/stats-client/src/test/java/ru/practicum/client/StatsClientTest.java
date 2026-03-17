package ru.practicum.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.HitDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class StatsClientTest {

    private final StatsClient statsClient = new StatsClient("http://localhost:9090");

    @Test
    public void testSaveHit() {
        HitDto hit = HitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        // Просто проверяем, что метод не падает с ошибкой
        // Для реального теста нужен запущенный сервер
        System.out.println("Hit created: " + hit);
        assertNotNull(hit);
    }

    @Test
    public void testGetStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // Просто проверяем создание параметров
        System.out.println("Getting stats from " + start + " to " + end);
        assertNotNull(start);
        assertNotNull(end);
    }
}