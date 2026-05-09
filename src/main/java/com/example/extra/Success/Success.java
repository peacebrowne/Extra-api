package com.example.extra.Success;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public class Success {
    static public <T> ResponseEntity<?> CREATED(String message, T data) {

        HttpStatus status = HttpStatus.CREATED;

        Optional<T> safeData = Optional.ofNullable(data);

        SuccessDetails<T> successDetails = safeData.isEmpty() ? new SuccessDetails<>(message,status.value())
                : new SuccessDetails<>(message, status.value(), data);

        return new ResponseEntity<>(successDetails, status);

    }

    static public <T> ResponseEntity<?> OK(String message, T data) {

        HttpStatus status = HttpStatus.OK;

        Optional<T> safeData = Optional.ofNullable(data);

        SuccessDetails<T> successDetails = safeData.isEmpty() ? new SuccessDetails<>(message,status.value())
                : new SuccessDetails<>(message, status.value(), data);

        return new ResponseEntity<>(successDetails, status);

    }
}
