package ru.practicum.main.dto.compilation;

import lombok.*;
import ru.practicum.main.dto.event.EventShortDto;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {

    private Long id;
    private List<EventShortDto> events;
    private Boolean pinned;
    private String title;
}