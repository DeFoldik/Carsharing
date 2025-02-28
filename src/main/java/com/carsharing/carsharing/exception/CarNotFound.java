package com.carsharing.carsharing.exception;

public class CarNotFound extends RuntimeException {
    public CarNotFound(String message) {
        super(message);
    }
}
