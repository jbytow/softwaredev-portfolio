package com.portfolio.config;

import com.portfolio.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex,
                                                                 HttpServletRequest request) {
        return clientError(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the maximum allowed limit", request);
    }

    /**
     * Malformed input on mapped endpoints (bad enum in a path or query, invalid sort property,
     * unreadable JSON, missing parameter, missing or non-multipart upload). Scanners trigger these routinely, so one WARN line
     * with no stack trace; the message is generic so parser details do not reach the client.
     */
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            PropertyReferenceException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MultipartException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequest(HttpServletRequest request) {
        return clientError(HttpStatus.BAD_REQUEST, "Invalid request", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                      HttpServletRequest request) {
        return clientError(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", request);
    }

    /**
     * Crawlers send Accept: text/html to JSON endpoints. No JSON error body can be written for
     * that Accept (it would fail again), so the answer is body-less.
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Void> handleMediaTypeNotAcceptable(HttpServletRequest request) {
        logClientError(HttpStatus.NOT_ACCEPTABLE, request);
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                         HttpServletRequest request) {
        return clientError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type", request);
    }

    /**
     * Backstop for unique and length constraint violations. The log names only the most specific
     * cause class: the SQL message carries column values.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                          HttpServletRequest request) {
        log.warn("Data integrity violation ({}): {} {} from {}", ex.getMostSpecificCause().getClass().getName(),
                request.getMethod(), request.getRequestURI(), clientAddress(request));
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("The data conflicts with an existing record or exceeds a field limit"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex,
                                                                  HttpServletRequest request) {
        HttpStatusCode status = ex.getStatusCode();
        String message = ex.getReason() != null ? ex.getReason() : reasonPhrase(status);
        if (status.is5xxServerError()) {
            log.error("{} {} from {} failed with {}", request.getMethod(), request.getRequestURI(),
                    clientAddress(request), status.value(), ex);
            return ResponseEntity.status(status).body(ApiResponse.error(message));
        }
        return clientError(status, message, request);
    }

    /**
     * Unknown paths (typically scanners probing /api/.env and similar) are routine noise,
     * so they get a one-line WARN instead of the generic ERROR with a stack trace.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFound(NoResourceFoundException ex,
                                                                     HttpServletRequest request) {
        log.warn("No resource: {} {} from {}", request.getMethod(), request.getRequestURI(), clientAddress(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "not found"));
    }

    private ResponseEntity<ApiResponse<Void>> clientError(HttpStatusCode status, String message,
                                                          HttpServletRequest request) {
        logClientError(status, request);
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }

    private void logClientError(HttpStatusCode status, HttpServletRequest request) {
        log.warn("{} {}: {} {} from {}", status.value(), reasonPhrase(status), request.getMethod(),
                request.getRequestURI(), clientAddress(request));
    }

    private static String reasonPhrase(HttpStatusCode status) {
        HttpStatus known = HttpStatus.resolve(status.value());
        return known != null ? known.getReasonPhrase() : "Error";
    }

    /**
     * CF-Connecting-IP is set (not appended) by Cloudflare, so a client cannot choose it;
     * the handler itself does not read X-Forwarded-For, which a client can pre-fill. Without
     * the header the remote address is used; with forward-headers-strategy=framework that
     * address is derived from X-Forwarded-For, so for direct-to-origin traffic it depends on
     * the proxy in front overwriting that header.
     */
    private static String clientAddress(HttpServletRequest request) {
        String cloudflareAddress = request.getHeader("CF-Connecting-IP");
        if (cloudflareAddress != null && !cloudflareAddress.isBlank()) {
            return cloudflareAddress.trim();
        }
        return request.getRemoteAddr();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        // Spring's own 4xx exceptions without an explicit handler above (missing header, handler
        // method validation, ...) are client errors, not server faults.
        if (ex instanceof ErrorResponse errorResponse && errorResponse.getStatusCode().is4xxClientError()) {
            return clientError(errorResponse.getStatusCode(), reasonPhrase(errorResponse.getStatusCode()), request);
        }
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}
