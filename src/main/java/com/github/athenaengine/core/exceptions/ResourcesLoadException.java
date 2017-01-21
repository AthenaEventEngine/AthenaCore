package com.github.athenaengine.core.exceptions;

public class ResourcesLoadException extends Exception {

    private final String mMessage;

    public ResourcesLoadException(String message) {
        super();
        mMessage = message;
    }

    @Override
    public final String getMessage() {
        return getClass().getSimpleName() + mMessage;
    }
}
