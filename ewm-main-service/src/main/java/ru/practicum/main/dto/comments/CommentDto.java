package ru.practicum.main.dto.comments;

import lombok.*;
import ru.practicum.main.dto.user.UserShortDto;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private Long eventId;
    private String eventTitle;
    private UserShortDto author;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String status;
    private LocalDateTime publishedDate;
}