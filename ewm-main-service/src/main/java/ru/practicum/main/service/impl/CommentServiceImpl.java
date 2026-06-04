package ru.practicum.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.comments.CommentDto;
import ru.practicum.main.dto.comments.NewCommentDto;
import ru.practicum.main.dto.comments.UpdateCommentDto;
import ru.practicum.main.dto.user.UserShortDto;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.CommentRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.service.CommentService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        log.info("Creating comment for user {} on event {}", userId, eventId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        // Check if event is published
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot comment on unpublished event");
        }

        // Check if user is not the event initiator
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Event initiator cannot comment on own event");
        }

        // Check if user already commented (optional business rule)
        if (commentRepository.existsByAuthorIdAndEventIdAndStatusNot(userId, eventId, CommentStatus.DELETED)) {
            throw new ConflictException("User already commented on this event");
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .event(event)
                .author(author)
                .created(LocalDateTime.now())
                .status(CommentStatus.PUBLISHED)
                .build();

        return toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        log.info("Updating comment {} by user {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("User can only update their own comments");
        }

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new ConflictException("Cannot update deleted comment");
        }

        comment.setText(dto.getText());
        comment.setUpdated(LocalDateTime.now());

        return toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("Soft deleting comment {} by user {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("User can only delete their own comments");
        }

        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public CommentDto moderateCommentByAdmin(Long commentId, boolean publish) {
        log.info("Admin moderating comment {} to status: {}", commentId, publish ? "PUBLISHED" : "REJECTED");

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Comment is not pending moderation");
        }

        comment.setStatus(publish ? CommentStatus.PUBLISHED : CommentStatus.REJECTED);
        if (publish) {
            comment.setPublishedDate(LocalDateTime.now());
        }

        return toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void hardDeleteCommentByAdmin(Long commentId) {
        log.info("Admin hard deleting comment {}", commentId);

        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found with id: " + commentId);
        }

        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAdmin(Long commentId, UpdateCommentDto dto) {
        log.info("Admin updating comment {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        comment.setText(dto.getText());
        comment.setUpdated(LocalDateTime.now());

        return toDto(commentRepository.save(comment));
    }

    @Override
    public Page<CommentDto> getUserComments(Long userId, Integer from, Integer size) {
        log.info("Getting comments for user {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());

        return commentRepository.findAllByAuthorIdAndStatusNot(userId, CommentStatus.DELETED, pageable)
                .map(this::toDto);
    }

    @Override
    public Page<CommentDto> getEventComments(Long eventId, Integer from, Integer size) {
        log.info("Getting comments for event {}", eventId);

        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());

        return commentRepository.findAllByEventIdAndStatus(eventId, CommentStatus.PUBLISHED, pageable)
                .map(this::toDto);
    }

    @Override
    public Page<CommentDto> getPendingComments(Integer from, Integer size) {
        log.info("Getting pending comments");

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").ascending());

        return commentRepository.findAllByStatus(CommentStatus.PENDING, pageable)
                .map(this::toDto);
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        log.info("Getting comment by id: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new NotFoundException("Comment not available");
        }

        return toDto(comment);
    }

    private CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEvent().getId())
                .eventTitle(comment.getEvent().getTitle())
                .author(UserShortDto.builder()
                        .id(comment.getAuthor().getId())
                        .name(comment.getAuthor().getName())
                        .build())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .status(comment.getStatus().name())
                .publishedDate(comment.getPublishedDate())
                .build();
    }
}