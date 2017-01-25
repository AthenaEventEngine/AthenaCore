package com.github.athenaengine.core.helper;

import com.github.athenaengine.core.enums.ScoreType;
import com.github.athenaengine.core.interfaces.IParticipant;
import com.github.athenaengine.core.model.holder.EItemHolder;
import com.github.athenaengine.core.util.SortUtils;

import java.util.*;

public class RewardHelper {

    private final Collection<IParticipant> mParticipants = new LinkedList<>();
    private final Map<Integer, List<EItemHolder>> mRewards = new HashMap<>();
    private final List<ScoreType> mScoreTypes = new LinkedList<>();

    public static RewardHelper newInstance() {
        return new RewardHelper();
    }

    private RewardHelper() {}

    public void setParticipants(Collection<IParticipant> participants) {
        mParticipants.addAll(participants);
    }

    public void addReward(int position, EItemHolder... itemHolders) {
        mRewards.put(position, new LinkedList<>());

        for (EItemHolder itemHolder : itemHolders) {
            mRewards.get(position).add(itemHolder);
        }
    }

    public void addGeneralReward(EItemHolder... itemHolders) {
        addReward(-1, itemHolders);
    }

    public void setmScoreTypes(Collection<ScoreType> scoreTypes) {
        mScoreTypes.addAll(scoreTypes);
    }

    public void distribute() {
        // Order participants by score types

        Collection<IParticipant> participants;

        for (ScoreType scoreType : mScoreTypes) {
            SortUtils.getOrdered()
        }


        // Give rewards
    }
}
