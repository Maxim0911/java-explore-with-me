package ru.practicum.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.main.dto.event.*;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.mapper.LocationMapper;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.service.EventService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String APP_NAME = "ewm-main-service";

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Creating event for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id " + newEventDto.getCategory() + " not found"));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }

        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(locationMapper.toLocation(newEventDto.getLocation()));

        Event savedEvent = eventRepository.save(event);
        log.info("Event created with id: {}", savedEvent.getId());

        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Getting events for user: {}, from: {}, size: {}", userId, from, size);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        log.info("Getting event {} for user {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found for user " + userId));

        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Updating event {} for user {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found for user " + userId));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Event date must be at least 2 hours from now");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id " + request.getCategory() + " not found"));
            event.setCategory(category);
        }

        if (request.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(request.getLocation()));
        }

        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new ValidationException("Participant limit cannot be negative");
            }
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new ValidationException("Participant limit cannot be negative");
            }
            event.setParticipantLimit(request.getParticipantLimit());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event {} updated", eventId);

        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<EventFullDto> findEventsByAdminFilters(List<Long> users,
                                                       List<EventState> states,
                                                       List<Long> categories,
                                                       LocalDateTime rangeStart,
                                                       LocalDateTime rangeEnd,
                                                       Integer from,
                                                       Integer size) {
        log.info("Searching events by admin filters");

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findEventsByAdminFilters(
                users, states, categories, rangeStart, rangeEnd, pageable);

        return events.stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        log.info("Admin updating event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));

        if (request.getStateAction() != null) {
            if (request.getStateAction().equals("PUBLISH_EVENT")) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Event date must be at least 1 hour from now");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (request.getStateAction().equals("REJECT_EVENT")) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject already published event");
                }
                event.setState(EventState.CANCELED);
            }
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id " + request.getCategory() + " not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour from now");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event {} updated by admin", eventId);

        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> findEventsByPublicFilters(String text,
                                                         List<Long> categories,
                                                         Boolean paid,
                                                         LocalDateTime rangeStart,
                                                         LocalDateTime rangeEnd,
                                                         Boolean onlyAvailable,
                                                         String sort,
                                                         Integer from,
                                                         Integer size,
                                                         HttpServletRequest request) {
        log.info("Searching events by public filters");

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        Pageable pageable;
        if (sort != null && sort.equals("VIEWS")) {
            pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "views"));
        } else {
            pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "eventDate"));
        }

        List<Event> events = eventRepository.findEventsByPublicFilters(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable);

        saveHit(request);

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> viewsMap = getViewsForEvents(eventIds);

        List<EventShortDto> result = events.stream()
                .map(eventMapper::toEventShortDto)
                .peek(dto -> dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L)))
                .collect(Collectors.toList());

        log.info("Found {} events", result.size());
        return result;
    }

    @Override
    public EventFullDto getPublishedEventById(Long eventId, HttpServletRequest request) {
        log.info("Getting published event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }

        long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
        event.setConfirmedRequests(confirmedRequests);

        Map<Long, Long> viewsMap = getViewsForEvents(List.of(eventId));
        event.setViews(viewsMap.getOrDefault(eventId, 0L));

        saveHit(request);

        return eventMapper.toEventFullDto(event);
    }

    private void saveHit(HttpServletRequest request) {
        try {
            HitDto hit = HitDto.builder()
                    .app(APP_NAME)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();
            statsClient.saveHit(hit);
        } catch (Exception e) {
            log.error("Error saving hit to stats service: {}", e.getMessage());
        }
    }

    private Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        try {
            if (eventIds.isEmpty()) {
                return Collections.emptyMap();
            }

            LocalDateTime start = LocalDateTime.now().minusYears(10);
            LocalDateTime end = LocalDateTime.now().plusYears(10);

            List<String> uris = eventIds.stream()
                    .map(id -> "/events/" + id)
                    .collect(Collectors.toList());

            List<StatsDto> stats = statsClient.getStats(start, end, uris, true);

            return stats.stream()
                    .collect(Collectors.toMap(
                            stat -> Long.parseLong(stat.getUri().replace("/events/", "")),
                            StatsDto::getHits,
                            (a, b) -> a
                    ));
        } catch (Exception e) {
            log.error("Error getting views from stats service: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private void updateEventsViews(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> viewsMap = getViewsForEvents(eventIds);

        events.forEach(event ->
                event.setViews(viewsMap.getOrDefault(event.getId(), 0L))
        );
    }
}