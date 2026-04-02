package ru.practicum.main.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    // Admin filters - используем native query
    @Query(value = "SELECT * FROM events e " +
            "WHERE (CAST(:users AS text) IS NULL OR e.initiator_id IN (:users)) " +
            "AND (CAST(:states AS text) IS NULL OR e.state IN (:states)) " +
            "AND (CAST(:categories AS text) IS NULL OR e.category_id IN (:categories)) " +
            "AND (CAST(:rangeStart AS timestamp) IS NULL OR e.event_date >= :rangeStart) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.event_date <= :rangeEnd) " +
            "ORDER BY e.id ASC " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Event> findEventsByAdminFilters(@Param("users") List<Long> users,
                                         @Param("states") List<String> states,
                                         @Param("categories") List<Long> categories,
                                         @Param("rangeStart") LocalDateTime rangeStart,
                                         @Param("rangeEnd") LocalDateTime rangeEnd,
                                         @Param("limit") int limit,
                                         @Param("offset") int offset);

    @Query(value = "SELECT * FROM events e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (CAST(:text AS text) IS NULL OR " +
            "   (e.annotation ILIKE CONCAT('%', CAST(:text AS text), '%') OR " +
            "    e.description ILIKE CONCAT('%', CAST(:text AS text), '%'))) " +
            "AND (CAST(:categories AS text) IS NULL OR e.category_id IN (:categories)) " +
            "AND (CAST(:paid AS boolean) IS NULL OR e.paid = :paid) " +
            "AND (CAST(:rangeStart AS timestamp) IS NULL OR e.event_date >= :rangeStart) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.event_date <= :rangeEnd) " +
            "AND (:onlyAvailable = false OR e.participant_limit = 0 OR e.confirmed_requests < e.participant_limit) " +
            "ORDER BY e.event_date ASC " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Event> findEventsByPublicFilters(@Param("text") String text,
                                          @Param("categories") List<Long> categories,
                                          @Param("paid") Boolean paid,
                                          @Param("rangeStart") LocalDateTime rangeStart,
                                          @Param("rangeEnd") LocalDateTime rangeEnd,
                                          @Param("onlyAvailable") Boolean onlyAvailable,
                                          @Param("limit") int limit,
                                          @Param("offset") int offset);
}