package com.example.extra.Success;

import lombok.Data;

@Data
public class SuccessDetails<T> {
  private String message;
  private int status;
  private T data;

    public SuccessDetails(String message, int status, T data) {
        this.message = message;
        this.status = status;
        this.data = data;
    }

    public SuccessDetails(String message, int status) {
        this.status = status;
        this.message = message;
    }
}
