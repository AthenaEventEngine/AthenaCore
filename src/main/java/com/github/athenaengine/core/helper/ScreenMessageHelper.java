package com.github.athenaengine.core.helper;

import com.github.athenaengine.core.interfaces.IParticipant;
import com.github.athenaengine.core.util.EventUtil;

import java.util.Collection;
import java.util.logging.Logger;

public class ScreenMessageHelper {

    // Logger
    private static final Logger LOGGER = Logger.getLogger(ScreenMessageHelper.class.getName());

    private String mMessage;
    private int mTime;

    public static ScreenMessageHelper newInstance() {
        return new ScreenMessageHelper();
    }

    private ScreenMessageHelper() {}

    public ScreenMessageHelper setMessage(String message) {
        mMessage = message;
        return this;
    }

    public ScreenMessageHelper setTime(int time) {
        mTime = time;
        return this;
    }

    public ScreenMessageHelper replaceHolder(String key, String text) {
        mMessage = mMessage.replace(key, text);
        return this;
    }

    public void show(Collection<? extends IParticipant> recipents) {
        try {
            for (IParticipant recipent : recipents) {
                EventUtil.sendEventScreenMessage(recipent, mMessage, mTime);
            }
        } catch (Exception e) {
            LOGGER.warning("Something went wrong when it tried to show a screen message");
        }
    }
}
