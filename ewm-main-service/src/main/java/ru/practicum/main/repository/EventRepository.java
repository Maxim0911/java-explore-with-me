package ru.practicum.main.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> findEventsByAdminFilters(@Param("users") List<Long> users,
                                         @Param("states") List<EventState> states,
                                         @Param("categories") List<Long> categories,
                                         @Param("rangeStart") LocalDateTime rangeStart,
                                         @Param("rangeEnd") LocalDateTime rangeEnd,
                                         Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR " +
            "   (CAST(e.annotation AS string) LIKE CONCAT('%', CAST(:text AS string), '%') OR " +
            "    CAST(e.description AS string) LIKE CONCAT('%', CAST(:text AS string), '%'))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND (:onlyAvailable = false OR e.participantLimit = 0 OR e.confirmedRequests < e.participantLimit)")
    List<Event> findEventsByPublicFilters(@Param("text") String text,
                                          @Param("categories") List<Long> categories,
                                          @Param("paid") Boolean paid,
                                          @Param("rangeStart") LocalDateTime rangeStart,
                                          @Param("rangeEnd") LocalDateTime rangeEnd,
                                          @Param("onlyAvailable") Boolean onlyAvailable,
                                          Pageable pageable);
}