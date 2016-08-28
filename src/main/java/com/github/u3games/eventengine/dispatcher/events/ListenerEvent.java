package com.github.u3games.eventengine.dispatcher.events;

import com.github.u3games.eventengine.enums.ListenerType;

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
