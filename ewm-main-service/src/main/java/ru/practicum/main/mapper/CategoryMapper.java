package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.category.NewCategoryDto;
import ru.practicum.main.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toCategory(NewCategoryDto newCategoryDto);

    CategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toCategoryFromDto(CategoryDto categoryDto);

    @Named("mapIdToCategory")
    default Category mapIdToCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return Category.builder().id(categoryId).build();
    }

    @Named("mapCategoryToId")
    default Long mapCategoryToId(Category category) {
        return category != null ? category.getId() : null;
    }
}