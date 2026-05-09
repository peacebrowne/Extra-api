package com.example.extra.Exceptions.Custom;

public class BadRequest extends RuntimeException {
    public BadRequest(String message) {
        super(message);
    }
}
