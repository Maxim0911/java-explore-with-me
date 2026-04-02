package ru.practicum.main.dto.compilation;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {

    private List<Long> events;
    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Заголовок должен содержать от 1 до 50 символов")
    private String title;
}