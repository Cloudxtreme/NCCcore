package com.NccUsers;

public class NccUsersException extends Exception {
    private String message;

    public NccUsersException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
