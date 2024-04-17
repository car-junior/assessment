package com.senior.assessment.infrastructure;

import com.senior.assessment.infrastructure.exception.CustomException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException customException) {
        return ResponseEntity.status(customException.getHttpStatus())
                .body(customException.getHandleCustomException());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                                FieldError::getField,
                                Collectors.mapping(
                                        fieldError -> Optional.ofNullable(fieldError.getDefaultMessage()).orElse(""),
                                        Collectors.toList()
                                )
                        )
                );
        return ResponseEntity.badRequest().body(paramsNotValid(errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public  ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        exception.printStackTrace();
        var customException = CustomException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(exception.getCause().getCause().getMessage())
                .build();
        return ResponseEntity.status(customException.getHttpStatus())
                .body(customException.getHandleCustomException());
    }
    private Map<String, Object> paramsNotValid(Map<String, List<String>> errors) {
        var params = new LinkedHashMap<String, Object>();
        params.put("status", HttpStatus.BAD_REQUEST.value());
        params.put("errors", errors);
        return params;
    }
}