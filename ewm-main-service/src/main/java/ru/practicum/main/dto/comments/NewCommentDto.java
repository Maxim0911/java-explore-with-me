package ru.practicum.main.dto.comments;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    @NotBlank(message = "Text cannot be empty")
    @Size(min = 3, max = 2000, message = "Text must be between 3 and 2000 characters")
    private String text;
}
