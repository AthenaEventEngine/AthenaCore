package com.github.athenaengine.core.model.config;

import com.google.gson.annotations.SerializedName;

public class DualboxEventConfig {

    @SerializedName("enabled") private boolean mEnabled;
    @SerializedName("maxAllowed") private int mMaxAllowed;

    public boolean isEnabled() {
        return mEnabled;
    }

    public int getMaxAllowed() {
        return mMaxAllowed;
    }
}
