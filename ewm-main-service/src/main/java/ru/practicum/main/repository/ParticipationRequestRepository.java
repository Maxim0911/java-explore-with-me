package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.event.id = :eventId AND pr.status = 'CONFIRMED'")
    long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.event.id = :eventId AND pr.id IN :requestIds")
    List<ParticipationRequest> findAllByEventIdAndRequestIds(@Param("eventId") Long eventId,
                                                             @Param("requestIds") List<Long> requestIds);

    @Modifying
    @Query("UPDATE ParticipationRequest pr SET pr.status = :status WHERE pr.event.id = :eventId AND pr.status = 'PENDING'")
    void rejectAllPendingRequestsByEventId(@Param("eventId") Long eventId, @Param("status") RequestStatus status);
}