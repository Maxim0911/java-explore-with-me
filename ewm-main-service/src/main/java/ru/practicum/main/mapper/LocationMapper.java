package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    Location toLocation(LocationDto locationDto);

    LocationDto toLocationDto(Location location);
}