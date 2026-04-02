package ru.practicum.main.dto.user;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShortDto {

    private Long id;
    private String name;
}