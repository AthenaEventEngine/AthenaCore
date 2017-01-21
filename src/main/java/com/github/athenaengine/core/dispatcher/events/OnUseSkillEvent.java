package com.github.athenaengine.core.dispatcher.events;

import com.github.athenaengine.core.enums.ListenerType;
import com.github.athenaengine.core.model.entity.Character;
import com.github.athenaengine.core.model.entity.Playable;
import com.l2jserver.gameserver.model.skills.Skill;

public class OnUseSkillEvent extends ListenerEvent {

    private final Playable mCaster;
    private final Skill mSkill;
    private final Character mTarget;

    public OnUseSkillEvent(Playable caster, Skill skill, Character target) {
        mCaster = caster;
        mSkill = skill;
        mTarget = target;
    }

    public Playable getCaster() {
        return mCaster;
    }

    public Skill getSkill() {
        return mSkill;
    }

    public Character getTarget() {
        return mTarget;
    }

    public ListenerType getType() {
        return ListenerType.ON_USE_SKILL;
    }
}
