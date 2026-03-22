package ru.practicum.main.mapper;

import org.mapstruct.*;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.event.*;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;

@Mapper(componentModel = "spring", uses = {UserMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "confirmedRequests", constant = "0L")
    @Mapping(target = "views", constant = "0L")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "category", expression = "java(categoryIdToCategory(newEventDto.getCategory()))")
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "category", expression = "java(categoryToCategoryDto(event.getCategory()))")
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "category", expression = "java(categoryToCategoryDto(event.getCategory()))")
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", expression = "java(categoryIdToCategory(request.getCategory()))")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromUserRequest(UpdateEventUserRequest request, @MappingTarget Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", expression = "java(categoryIdToCategory(request.getCategory()))")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromAdminRequest(UpdateEventAdminRequest request, @MappingTarget Event event);

    default Category categoryIdToCategory(Long categoryId) {
        if (categoryId == null) return null;
        return Category.builder().id(categoryId).build();
    }

    default CategoryDto categoryToCategoryDto(Category category) {
        if (category == null) return null;
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    default Long categoryToCategoryId(Category category) {
        return category != null ? category.getId() : null;
    }
}