package ru.practicum.main.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewUserRequest {

    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 250, message = "Имя должно содержать от 2 до 250 символов")
    private String name;

    @NotBlank(message = "Поле Email не должно быть пустым")
    @Email(message = "Некорректный формат email")
    @Size(min = 6, max = 254, message = "Email должен содержать от 6 до 254 символов")
    private String email;
}