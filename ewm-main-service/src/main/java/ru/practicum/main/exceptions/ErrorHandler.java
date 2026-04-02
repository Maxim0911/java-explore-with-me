package ru.practicum.main.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.error("404 Not Found: {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Искомый объект не был найден.")
                .status(HttpStatus.NOT_FOUND.toString())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.error("409 Conflict: {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Было нарушено ограничение целостности.")
                .status(HttpStatus.CONFLICT.toString())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("409 Conflict: {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getClass().getName()))
                .message("Integrity constraint has been violated.")
                .reason("For the requested operation the conditions are not met.")
                .status(HttpStatus.CONFLICT.toString())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final Exception e) {
        log.error("400 Bad Request: {}", e.getMessage());

        String message;
        if (e instanceof MethodArgumentNotValidException) {
            message = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors().stream()
                    .map(error -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                            error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                    .collect(Collectors.joining("; "));
        } else if (e instanceof ConstraintViolationException) {
            message = ((ConstraintViolationException) e).getConstraintViolations().stream()
                    .map(violation -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                            violation.getPropertyPath(), violation.getMessage(), violation.getInvalidValue()))
                    .collect(Collectors.joining("; "));
        } else {
            message = e.getMessage();
        }

        return ApiError.builder()
                .errors(List.of(e.getClass().getName()))
                .message(message)
                .reason("Неправильно составленный запрос.")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.error("500 Internal Server Error: {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.error("400 Bad Request: {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Неправильно составленный запрос.")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }
}