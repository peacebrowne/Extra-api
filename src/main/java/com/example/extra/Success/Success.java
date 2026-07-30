package com.example.extra.Success;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public class Success {
    static public <T> ResponseEntity<?> CREATED(String message, T data) {

        HttpStatus status = HttpStatus.CREATED;

        return getResponseEntity(message, data, status);

    }

    static public <T> ResponseEntity<?> OK(String message, T data) {

        HttpStatus status = HttpStatus.OK;

        return getResponseEntity(message, data, status);

    }

    @NonNull
    private static <T> ResponseEntity<?> getResponseEntity(String message, T data, HttpStatus status) {
        Optional<T> safeData = Optional.ofNullable(data);

        SuccessDetails<T> successDetails = safeData.isEmpty() ? new SuccessDetails<>(message,status.value())
                : new SuccessDetails<>(message, status.value(), data);

        return new ResponseEntity<>(successDetails, status);
    }
}
