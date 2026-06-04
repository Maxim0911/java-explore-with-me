package ru.practicum.main.controllers.publicapi;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.comments.CommentDto;
import ru.practicum.main.service.CommentService;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/{eventId}/comments")
    public Page<CommentDto> getEventComments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GET /events/{}/comments", eventId);
        return commentService.getEventComments(eventId, from, size);
    }

    @GetMapping("/{eventId}/comments/{commentId}")
    public CommentDto getCommentById(
            @PathVariable Long eventId,
            @PathVariable Long commentId) {
        log.info("GET /events/{}/comments/{}", eventId, commentId);
        return commentService.getCommentById(commentId);
    }
}