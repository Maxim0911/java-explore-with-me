package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.CommentStatus;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    Page<Comment> findAllByAuthorIdAndStatusNot(Long authorId, CommentStatus status, Pageable pageable);

    Page<Comment> findAllByStatus(CommentStatus status, Pageable pageable);

    boolean existsByAuthorIdAndEventIdAndStatusNot(Long authorId, Long eventId, CommentStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.status = :status WHERE c.id = :commentId")
    void updateStatus(Long commentId, CommentStatus status);

    long countByEventIdAndStatus(Long eventId, CommentStatus status);
}