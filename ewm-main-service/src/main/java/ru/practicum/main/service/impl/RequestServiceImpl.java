package ru.practicum.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.service.RequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting requests for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);

        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.info("Getting participants for event: {} by user: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found for user " + userId));

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.info("Adding request for user: {} to event: {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot add request to own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Request already exists");
        }

        long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit has been reached");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(determineInitialStatus(event))
                .build();

        ParticipationRequest savedRequest = requestRepository.save(request);

        event.setConfirmedRequests(requestRepository.countConfirmedRequestsByEventId(eventId));
        eventRepository.save(event);

        log.info("Request created with id: {}", savedRequest.getId());

        return requestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancelling request: {} for user: {}", requestId, userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id " + requestId + " not found for user " + userId);
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest cancelledRequest = requestRepository.save(request);

        Event event = cancelledRequest.getEvent();
        event.setConfirmedRequests(requestRepository.countConfirmedRequestsByEventId(event.getId()));
        eventRepository.save(event);

        log.info("Request {} cancelled", requestId);
        return requestMapper.toParticipationRequestDto(cancelledRequest);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        log.info("Updating request status for event: {} by user: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found for user " + userId));

        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndRequestIds(
                eventId, request.getRequestIds());

        if (requests.size() != request.getRequestIds().size()) {
            throw new NotFoundException("Some requests not found");
        }

        for (ParticipationRequest req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        long confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);

        if (request.getStatus().equals("CONFIRMED")) {
            for (ParticipationRequest req : requests) {
                if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toParticipationRequestDto(req));
                } else {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(requestMapper.toParticipationRequestDto(req));
                    confirmedCount++;
                }
            }

            if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                requestRepository.rejectAllPendingRequestsByEventId(eventId, RequestStatus.REJECTED);
            }
        } else if (request.getStatus().equals("REJECTED")) {
            for (ParticipationRequest req : requests) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toParticipationRequestDto(req));
            }
        }

        requestRepository.saveAll(requests);

        event.setConfirmedRequests(requestRepository.countConfirmedRequestsByEventId(eventId));
        eventRepository.save(event);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private RequestStatus determineInitialStatus(Event event) {
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return RequestStatus.CONFIRMED;
        }
        return RequestStatus.PENDING;
    }
}