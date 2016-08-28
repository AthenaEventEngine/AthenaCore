package com.github.u3games.eventengine.dispatcher.events;

import com.github.u3games.eventengine.enums.ListenerType;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.skills.Skill;

public class OnUseSkillEvent extends ListenerEvent {

    private final L2Playable mCaster;
    private final Skill mSkill;
    private final L2Character mTarget;

    public OnUseSkillEvent(L2Playable caster, Skill skill, L2Character target) {
        mCaster = caster;
        mSkill = skill;
        mTarget = target;
    }

    public L2Playable getCaster() {
        return mCaster;
    }

    public Skill getSkill() {
        return mSkill;
    }

    public L2Character getTarget() {
        return mTarget;
    }

    public ListenerType getType() {
        return ListenerType.ON_USE_SKILL;
    }
}
