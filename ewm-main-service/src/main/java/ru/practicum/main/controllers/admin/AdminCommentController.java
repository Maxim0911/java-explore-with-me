package ru.practicum.main.controllers.admin;

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
import ru.practicum.main.dto.comments.UpdateCommentDto;
import ru.practicum.main.service.CommentService;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public Page<CommentDto> getPendingComments(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GET /admin/comments");
        return commentService.getPendingComments(from, size);
    }

    @PatchMapping("/{commentId}/publish")
    public CommentDto publishComment(@PathVariable Long commentId) {
        log.info("PATCH /admin/comments/{}/publish", commentId);
        return commentService.moderateCommentByAdmin(commentId, true);
    }

    @PatchMapping("/{commentId}/reject")
    public CommentDto rejectComment(@PathVariable Long commentId) {
        log.info("PATCH /admin/comments/{}/reject", commentId);
        return commentService.moderateCommentByAdmin(commentId, false);
    }

    @PutMapping("/{commentId}")
    public CommentDto updateCommentByAdmin(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto dto) {
        log.info("PUT /admin/comments/{}", commentId);
        return commentService.updateCommentByAdmin(commentId, dto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDeleteComment(@PathVariable Long commentId) {
        log.info("DELETE /admin/comments/{}", commentId);
        commentService.hardDeleteCommentByAdmin(commentId);
    }
}