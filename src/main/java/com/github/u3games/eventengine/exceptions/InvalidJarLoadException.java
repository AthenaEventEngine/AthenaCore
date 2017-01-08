package com.github.u3games.eventengine.exceptions;

public class InvalidJarLoadException extends Exception {

    private final String mMessage;

    public InvalidJarLoadException(String message) {
        super();
        mMessage = message;
    }

    @Override
    public final String getMessage() {
        return getClass().getSimpleName() + mMessage;
    }
}
