package com.example.extra.Exceptions;

import com.example.extra.Exceptions.Custom.BadRequest;
import com.example.extra.Exceptions.Custom.Conflict;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Exceptions.Custom.NotFound;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.security.core.AuthenticationException;


@RestControllerAdvice
public class Errors extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<?> BAD_REQUEST(BadRequest exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                exception.getMessage(), request.getDescription(false), HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFound.class)
    public ResponseEntity<?> NOT_FOUND(NotFound exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                exception.getMessage(), request.getDescription(false), HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Conflict.class)
    public ResponseEntity<?> CONFLICT(Conflict exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                exception.getMessage(), request.getDescription(false), HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InternalServerError.class)
    public ResponseEntity<?> INTERNAL_SERVER_ERROR(InternalServerError exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                exception.getMessage(), request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> UNAUTHORIZED(AuthenticationException exception, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                exception.getMessage(), request.getDescription(false), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String errors = ex.getAllErrors().get(0).getDefaultMessage();

        ErrorDetails errorDetails = new ErrorDetails(
                errors,
                
                request.getDescription(false),
                status.value()
        );

        return new ResponseEntity<>(errorDetails, headers, status);
    }

}
