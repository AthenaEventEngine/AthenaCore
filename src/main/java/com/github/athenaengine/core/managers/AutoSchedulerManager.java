package com.github.athenaengine.core.managers;

import com.github.athenaengine.core.task.EventEngineTask;
import com.l2jserver.gameserver.ThreadPoolManager;

import java.util.concurrent.ScheduledFuture;

public class AutoSchedulerManager {

    private ScheduledFuture mScheduler;
    private Runnable mTask;

    public boolean isRunning() {
        return mScheduler != null;
    }

    public boolean start() {
        if (mScheduler == null) {
            mTask = new EventEngineTask();
            mScheduler = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(mTask, 10 * 1000, 1000);
            return true;
        }

        return false;
    }

    public void stop() {
        if (mScheduler != null) mScheduler.cancel(true);
    }

    public static AutoSchedulerManager getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final AutoSchedulerManager _instance = new AutoSchedulerManager();
    }
}
