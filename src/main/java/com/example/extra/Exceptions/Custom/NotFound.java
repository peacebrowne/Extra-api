package com.example.extra.Exceptions.Custom;

public class NotFound extends RuntimeException {
    public NotFound(String message) {
        super(message);
    }
}
