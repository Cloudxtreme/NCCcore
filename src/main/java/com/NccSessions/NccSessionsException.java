package com.NccSessions;

public class NccSessionsException extends Exception {
    private String message;

    public NccSessionsException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
