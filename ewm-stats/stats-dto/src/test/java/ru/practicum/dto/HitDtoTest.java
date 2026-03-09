package ru.practicum.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class HitDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidHitDto_thenNoViolations() {
        HitDto hit = HitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<HitDto>> violations = validator.validate(hit);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenAppIsBlank_thenViolations() {
        HitDto hit = HitDto.builder()
                .app("")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<HitDto>> violations = validator.validate(hit);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void whenIpIsInvalid_thenViolations() {
        HitDto hit = HitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("invalid-ip")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<HitDto>> violations = validator.validate(hit);
        assertFalse(violations.isEmpty());
    }
}