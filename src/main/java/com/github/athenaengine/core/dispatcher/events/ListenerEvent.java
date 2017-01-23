package com.github.athenaengine.core.dispatcher.events;

import com.github.athenaengine.core.enums.ListenerType;

public abstract class ListenerEvent {

    private boolean mCanceled;

    public abstract ListenerType getType();

    public boolean isCanceled() {
        return mCanceled;
    }

    public void setCancel(boolean value) {
        mCanceled = value;
    }
}
