package com.example.extra.Exceptions.Custom;

public class InternalServerError extends RuntimeException {
    public InternalServerError(String message)
    {
        super(message);
    }
}

