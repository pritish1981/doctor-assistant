package com.superclinic.doctorassistant.api.advice;

import com.superclinic.doctorassistant.ai.exception.OpenAiServiceException;
import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import com.superclinic.doctorassistant.common.exception.ConflictException;
import com.superclinic.doctorassistant.common.exception.DoctorAssistantException;
import com.superclinic.doctorassistant.common.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI NOT_FOUND_TYPE = URI.create("https://superclinic.com/errors/not-found");
    private static final URI VALIDATION_TYPE = URI.create("https://superclinic.com/errors/validation");
    private static final URI CONFLICT_TYPE = URI.create("https://superclinic.com/errors/conflict");
    private static final URI INTERNAL_TYPE = URI.create("https://superclinic.com/errors/internal");

    private static final URI OPENAI_TYPE = URI.create("https://superclinic.com/errors/openai");

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(NOT_FOUND_TYPE);
        problem.setTitle("Resource Not Found");
        problem.setProperty("resourceType", ex.getResourceType());
        problem.setProperty("resourceId", ex.getResourceId());
        return problem;
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ProblemDetail handleBusinessValidation(BusinessValidationException ex) {
        log.warn("Business validation failed: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(VALIDATION_TYPE);
        problem.setTitle("Validation Error");
        return problem;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(CONFLICT_TYPE);
        problem.setTitle("Conflict");
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("Constraint validation failed: {}", detail);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(VALIDATION_TYPE);
        problem.setTitle("Validation Error");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("Request validation failed: {}", detail);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(VALIDATION_TYPE);
        problem.setTitle("Validation Error");
        return problem;
    }

    @ExceptionHandler(OpenAiServiceException.class)
    public ProblemDetail handleOpenAi(OpenAiServiceException ex) {
        log.error("OpenAI service error: {}", ex.getMessage(), ex);
        HttpStatus status = ex.getStatusCode() != null && ex.getStatusCode().is4xxClientError()
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.SERVICE_UNAVAILABLE;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "AI service temporarily unavailable");
        problem.setType(OPENAI_TYPE);
        problem.setTitle("OpenAI Service Error");
        if (ex.getStatusCode() != null) {
            problem.setProperty("upstreamStatus", ex.getStatusCode().value());
        }
        return problem;
    }

    @ExceptionHandler(DoctorAssistantException.class)
    public ProblemDetail handleDoctorAssistant(DoctorAssistantException ex) {
        log.error("Application error: {}", ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setType(INTERNAL_TYPE);
        problem.setTitle("Internal Server Error");
        return problem;
    }

    private String formatFieldError(FieldError error) {
        return "%s: %s".formatted(error.getField(), error.getDefaultMessage());
    }
}
