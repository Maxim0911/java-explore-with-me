package ru.practicum.main.controllers.privateapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.comments.CommentDto;
import ru.practicum.main.dto.comments.NewCommentDto;
import ru.practicum.main.dto.comments.UpdateCommentDto;
import ru.practicum.main.service.CommentService;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto dto) {
        log.info("POST /users/{}/events/{}/comments", userId, eventId);
        return commentService.createComment(userId, eventId, dto);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto dto) {
        log.info("PATCH /users/{}/comments/{}", userId, commentId);
        return commentService.updateComment(userId, commentId, dto);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {
        log.info("DELETE /users/{}/comments/{}", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/comments")
    public Page<CommentDto> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GET /users/{}/comments", userId);
        return commentService.getUserComments(userId, from, size);
    }
}