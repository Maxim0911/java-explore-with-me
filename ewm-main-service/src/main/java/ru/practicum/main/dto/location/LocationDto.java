package ru.practicum.main.dto.location;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {

    @NotNull(message = "Широта не может быть пустой")
    private Float lat;

    @NotNull(message = "Долгота не может быть пустой")
    private Float lon;
}