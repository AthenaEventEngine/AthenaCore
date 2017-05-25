package com.github.athenaengine.core.managers.general;

import com.github.athenaengine.core.datatables.EventLoader;
import com.github.athenaengine.core.interfaces.IEventContainer;
import com.github.athenaengine.core.model.entity.Player;
import com.l2jserver.util.Rnd;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class VoteManager {

    // Id's list of characters who voted
    private final Set<Integer> mPlayersAlreadyVoted = ConcurrentHashMap.newKeySet();
    // Map of the Id's of the characters who voted
    private final Map<String, Set<Integer>> mCurrentEventVotes = new HashMap<>();

    public void initVotes() {
        for (IEventContainer container : EventLoader.getInstance().getEnabledEvents()) {
            mCurrentEventVotes.put(container.getSimpleEventName(), ConcurrentHashMap.newKeySet());
        }
    }

    public void clearVotes() {
        // The map is restarted
        for (String eventName : mCurrentEventVotes.keySet()) {
            mCurrentEventVotes.get(eventName).clear();
        }

        // The list of players who voted cleaned
        mPlayersAlreadyVoted.clear();
    }

    /**
     * Increase by 1, the number of votes.
     * @param player The character who is voting.
     * @param eventName Event voting.
     */
    public void increaseVote(Player player, String eventName) {
        if (mPlayersAlreadyVoted.add(player.getObjectId())) {
            mCurrentEventVotes.get(eventName).add(player.getObjectId());
        }
    }

    /**
     * Decrease the number of votes.
     * @param player Character that are voting.
     */
    public void removeVote(Player player) {
        // Deletes it from the list of players who voted
        if (mPlayersAlreadyVoted.remove(player.getObjectId())) {
            // If he was on the list, start looking for which event voted
            for (String eventName : mCurrentEventVotes.keySet()) {
                mCurrentEventVotes.get(eventName).remove(player.getObjectId());
            }
        }
    }

    /**
     * Get the number of votes it has a certain event.
     * @param eventName AVA, TVT, CFT.
     * @return int
     */
    public int getCurrentVotesInEvent(String eventName) {
        return mCurrentEventVotes.get(eventName).size();
    }

    /**
     * Get the amount of total votes.
     * @return int
     */
    public int getAllCurrentVotesInEvents() {
        int count = 0;

        for (Set<Integer> set : mCurrentEventVotes.values()) {
            count += set.size();
        }

        return count;
    }

    /**
     * Get the event with more votes. In case all have the same amount of votes, it will make a random among those most votes have.
     * @return IEventContainer
     */
    public IEventContainer getEventMoreVotes() {
        int maxVotes = 0;
        List<String> topEvents = new ArrayList<>();

        for (String eventName : mCurrentEventVotes.keySet()) {
            int eventVotes = mCurrentEventVotes.get(eventName).size();

            if (eventVotes > maxVotes) {
                topEvents.clear();
                topEvents.add(eventName);
                maxVotes = eventVotes;
            } else if (eventVotes == maxVotes) {
                topEvents.add(eventName);
            }
        }

        int topEventsSize = topEvents.size();
        String topEventName;
        topEventName = topEventsSize > 1 ? topEvents.get(Rnd.get(0, topEventsSize - 1)) : topEvents.get(0);

        return EventLoader.getInstance().getEvent(topEventName);
    }

    public static VoteManager getInstance() {
        return VoteManager.SingletonHolder.mInstance;
    }

    private static class SingletonHolder {
        private static final VoteManager mInstance = new VoteManager();
    }
}
