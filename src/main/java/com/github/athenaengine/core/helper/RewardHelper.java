package com.github.athenaengine.core.helper;

import com.github.athenaengine.core.enums.CollectionTarget;
import com.github.athenaengine.core.enums.MessageType;
import com.github.athenaengine.core.enums.ScoreType;
import com.github.athenaengine.core.interfaces.IParticipant;
import com.github.athenaengine.core.model.holder.EItemHolder;
import com.github.athenaengine.core.util.EventUtil;
import com.github.athenaengine.core.util.SortUtils;

import java.util.*;
import java.util.logging.Logger;

public class RewardHelper {

    // Logger
    private static final Logger LOGGER = Logger.getLogger(RewardHelper.class.getName());

    private final Collection<IParticipant> mParticipants = new LinkedList<>();
    private final Map<Integer, Collection<EItemHolder>> mRewards = new HashMap<>();
    private final Map<Integer, Collection<EItemHolder>> mTieRewards = new HashMap<>();
    private ScoreType mScoreType = ScoreType.POINT;

    public static RewardHelper newInstance() {
        return new RewardHelper();
    }

    private RewardHelper() {}

    public RewardHelper setParticipants(Collection<? extends IParticipant> participants) {
        mParticipants.addAll(participants);
        return this;
    }

    public RewardHelper addReward(int position, List<EItemHolder> itemHolders) {
        mRewards.putIfAbsent(position, new LinkedList<>());
        mRewards.get(position).addAll(itemHolders);
        return this;
    }

    public RewardHelper addGeneralReward(List<EItemHolder> itemHolders) {
        addReward(-1, itemHolders);
        return this;
    }

    public RewardHelper addTieReward(int position, List<EItemHolder> itemHolders) {
        mTieRewards.putIfAbsent(position, new LinkedList<>());
        mTieRewards.get(position).addAll(itemHolders);
        return this;
    }

    public RewardHelper setScoreType(ScoreType scoreType) {
        mScoreType = scoreType;
        return this;
    }

    public void distribute(boolean announce) {
        try {
            int index = 0;

            for (List<IParticipant> participants : SortUtils.getOrdered(mParticipants, mScoreType)) {
                index++;
                Collection<EItemHolder> reward = null;

                if (participants.size() > 1 && mTieRewards.containsKey(index)) {
                    reward = mTieRewards.get(index);
                } else if (mRewards.containsKey(index)) {
                    reward = mRewards.get(index);
                } else if (mRewards.containsKey(-1)) {
                    reward = mRewards.get(-1);
                }

                if (reward != null) {
                    for (IParticipant participant : participants) {
                        participant.giveItems(reward);
                    }
                }

                if (index == 1) {
                    if (participants.size() == 1) {
                        EventUtil.announceTo(MessageType.BATTLEFIELD, "team_winner", "%holder%", participants.get(0).getName(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
                    } else {
                        EventUtil.announceTo(MessageType.BATTLEFIELD, "teams_tie", CollectionTarget.ALL_PLAYERS_IN_EVENT);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Something went wrong on distribute");
        }
    }
}
