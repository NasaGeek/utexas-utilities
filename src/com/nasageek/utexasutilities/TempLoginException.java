package com.nasageek.utexasutilities;

/**
 * Exception for when a method will fail with the user logged in temporarily
 */
public class TempLoginException extends RuntimeException {
    public TempLoginException(String message) {
        super(message);
    }
}
