package com.github.athenaengine.core.dispatcher.events;

import com.github.athenaengine.core.enums.ListenerType;
import com.github.athenaengine.core.model.template.SkillTemplate;
import com.github.athenaengine.core.model.entity.Character;
import com.github.athenaengine.core.model.entity.Playable;

public class OnUseSkillEvent extends ListenerEvent {

    private final Playable mCaster;
    private final SkillTemplate mSkill;
    private final Character mTarget;

    public OnUseSkillEvent(Playable caster, SkillTemplate skill, Character target) {
        mCaster = caster;
        mSkill = skill;
        mTarget = target;
    }

    public Playable getCaster() {
        return mCaster;
    }

    public SkillTemplate getSkill() {
        return mSkill;
    }

    public Character getTarget() {
        return mTarget;
    }

    public ListenerType getType() {
        return ListenerType.ON_USE_SKILL;
    }
}
