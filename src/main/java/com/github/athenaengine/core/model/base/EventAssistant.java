package com.github.athenaengine.core.model.base;

import com.github.athenaengine.core.EventEngineManager;
import com.github.athenaengine.core.config.BaseConfigLoader;
import com.github.athenaengine.core.datatables.EventLoader;
import com.github.athenaengine.core.enums.CollectionTarget;
import com.github.athenaengine.core.enums.EventEngineState;
import com.github.athenaengine.core.enums.MessageType;
import com.github.athenaengine.core.interfaces.IEventContainer;
import com.github.athenaengine.core.managers.general.VoteManager;
import com.github.athenaengine.core.model.config.MainEventConfig;
import com.github.athenaengine.core.util.EventUtil;
import com.l2jserver.gameserver.ThreadPoolManager;

import java.util.concurrent.ScheduledFuture;

public class EventAssistant {

    private final boolean mVoteEnabled;

    private IEventContainer mEventContainer;
    private BaseEvent mEvent;
    private EventEngineState mState;
    private ScheduledFuture mTask;
    private int mTime = 0;

    private final CollectionTarget mAnnounceType = getConfig().getGlobalMessage() ? CollectionTarget.ALL_PLAYERS : CollectionTarget.ALL_NEAR_PLAYERS;

    private static MainEventConfig getConfig() {
        return BaseConfigLoader.getInstance().getMainConfig();
    }

    public EventAssistant(boolean voteEnabled) {
        mVoteEnabled = voteEnabled;
    }

    public void start() {
        mTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new EventFlowTask(), 10 * 1000, 1000);
    }

    public void cancel() {
        mTask.cancel(true);
        // Cancel the rest
    }

    private class EventFlowTask implements Runnable {

        @Override
        public void run() {
            boolean decreaseTime = true;

            switch (mState) {
                // Maybe it's not necessary
                case WAITING:
                    waiting();
                    break;
                case VOTING:
                    voting();
                    break;
                case REGISTER:
                    register();
                    break;
                case RUN_EVENT:
                    runEvent();
                    break;
                case RUNNING_EVENT:
                    decreaseTime = false;
                    break;
                case EVENT_ENDED:
                    eventEnded();
                    break;
            }

            if (decreaseTime) mTime--;
        }

        private void waiting() {
            if (mTime <= 0) {
                if (mVoteEnabled) {
                    EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "event_voting_started", mAnnounceType);
                    mTime = getConfig().getVotingTime() * 60;
                    mState = EventEngineState.VOTING;
                } else {
                    EventEngineManager.getInstance().setNextEvent(EventLoader.getInstance().getRandomEventType());
                    String eventName = EventEngineManager.getInstance().getNextEvent().getEventName();
                    EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "event_register_started", "%event%", eventName, mAnnounceType);
                    mTime = getConfig().getRegisterTime() * 60;
                    mState = EventEngineState.REGISTER;
                }
            }
        }

        private void voting() {
            if (mTime > 0) EventUtil.announceTime(mTime, "event_voting_state", MessageType.CRITICAL_ANNOUNCE, mAnnounceType);
            else {
                IEventContainer nextEvent = VoteManager.getInstance().getEventMoreVotes();
                EventEngineManager.getInstance().setNextEvent(nextEvent);
                EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "event_voting_ended", mAnnounceType);
                EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "event_register_started", "%event%", nextEvent.getEventName(), mAnnounceType);
                mTime = getConfig().getRegisterTime() * 60;
                mState = EventEngineState.REGISTER;
            }
        }

        private void register() {
            if (EventEngineManager.getInstance().getTime() > 0) {
                int time = EventEngineManager.getInstance().getTime();
                String eventName = EventEngineManager.getInstance().getNextEvent().getEventName();
                EventUtil.announceTime(time, "event_register_state", MessageType.CRITICAL_ANNOUNCE, "%event%", eventName, mAnnounceType);
            } else {
                if (EventEngineManager.getInstance().getAllRegisteredPlayers().size() < getConfig().getMinPlayers()) {
                    EventEngineManager.getInstance().cleanUp();
                    mTime = getConfig().getInterval() * 60;
                    EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "event_aborted", mAnnounceType);
                    EventUtil.announceTime(EventEngineManager.getInstance().getTime(), "event_next", MessageType.CRITICAL_ANNOUNCE, mAnnounceType);
                    mState = EventEngineState.WAITING;
                } else {
                    EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "event_register_ended", mAnnounceType);
                    mState = EventEngineState.RUN_EVENT;
                }
            }
        }

        private void runEvent() {
            IEventContainer container = EventEngineManager.getInstance().getNextEvent();
            BaseEvent event = EventEngineManager.getInstance().getNextEvent().newEventInstance();

            if (event == null) {
                EventEngineManager.getInstance().cleanUp();
                EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "wrong_run", mAnnounceType);
                EventUtil.announceTime(EventEngineManager.getInstance().getTime(), "event_next", MessageType.CRITICAL_ANNOUNCE, mAnnounceType);
                mState = EventEngineState.WAITING;
                return;
            }

            EventEngineManager.getInstance().setCurrentEvent(container, event);
            EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "event_started", mAnnounceType);
            mState = EventEngineState.RUNNING_EVENT;
        }

        private void eventEnded() {
            EventEngineManager.getInstance().cleanUp();
            EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "event_end", mAnnounceType);
            EventUtil.announceTime(EventEngineManager.getInstance().getTime(), "event_next", MessageType.CRITICAL_ANNOUNCE, mAnnounceType);
            mTask.cancel(true);
        }
    }
}
