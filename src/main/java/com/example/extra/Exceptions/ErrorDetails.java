package com.example.extra.Exceptions;

import lombok.Data;

import java.util.Date;

@Data
public class ErrorDetails {
    private String message;
    private Date timestamp;
    private String details;
    private int status;

    public ErrorDetails(String message, String details, int status) {
        this.message = message;
        this.timestamp = new Date();
        this.details = details;
        this.status = status;
    }

}
