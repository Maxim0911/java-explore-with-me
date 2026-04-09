package ru.practicum.main.service;

import org.springframework.data.domain.Page;
import ru.practicum.main.dto.comments.CommentDto;
import ru.practicum.main.dto.comments.NewCommentDto;
import ru.practicum.main.dto.comments.UpdateCommentDto;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

    void deleteComment(Long userId, Long commentId);

    CommentDto moderateCommentByAdmin(Long commentId, boolean publish);

    void hardDeleteCommentByAdmin(Long commentId);

    CommentDto updateCommentByAdmin(Long commentId, UpdateCommentDto dto);

    Page<CommentDto> getUserComments(Long userId, Integer from, Integer size);

    Page<CommentDto> getEventComments(Long eventId, Integer from, Integer size);

    Page<CommentDto> getPendingComments(Integer from, Integer size);

    CommentDto getCommentById(Long commentId);
}