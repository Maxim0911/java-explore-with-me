package ru.practicum.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.server.controller.StatsController;
import ru.practicum.server.service.StatsService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatsService statsService;

    @Test
    void saveHit_shouldReturn201() throws Exception {
        HitDto hit = HitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hit)))
                .andExpect(status().isCreated());
    }

    @Test
    void getStats_shouldReturnStatsList() throws Exception {
        List<StatsDto> stats = Arrays.asList(
                StatsDto.builder().app("test-app").uri("/test").hits(5L).build(),
                StatsDto.builder().app("test-app").uri("/test2").hits(3L).build()
        );

        when(statsService.getStats(any(), any(), any(), anyBoolean()))
                .thenReturn(stats);

        mockMvc.perform(get("/stats")
                        .param("start", "2026-03-01 00:00:00")
                        .param("end", "2026-12-31 23:59:59")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].app").value("test-app"))
                .andExpect(jsonPath("$[0].hits").value(5));
    }
}