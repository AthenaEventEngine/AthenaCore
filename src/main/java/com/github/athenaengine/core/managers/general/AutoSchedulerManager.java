package com.github.athenaengine.core.managers.general;

import com.github.athenaengine.core.config.BaseConfigLoader;
import com.l2jserver.gameserver.ThreadPoolManager;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class AutoSchedulerManager {

    private ScheduledFuture mScheduler;

    public boolean start() {
        if (mScheduler != null) {
            mScheduler = ThreadPoolManager.getInstance().scheduleEvent(
                    new NextEventTask(),
                    BaseConfigLoader.getInstance().getMainConfig().getInterval(),
                    TimeUnit.MINUTES);
        }

        return false;
    }

    public void cancel() {
        if (mScheduler != null) mScheduler.cancel(true);
        mScheduler = null;
    }

    public long getTimeForNextEvent() {
        return mScheduler != null ? mScheduler.getDelay(TimeUnit.SECONDS) : -1;
    }

    public static AutoSchedulerManager getInstance() {
        return SingletonHolder.mInstance;
    }

    private static class SingletonHolder {
        private static final AutoSchedulerManager mInstance = new AutoSchedulerManager();
    }

    private final class NextEventTask implements Runnable {

        @Override
        public void run() {
            // Start an Event Assistant
        }
    }
}
