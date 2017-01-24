package com.github.athenaengine.core.model.template;

public class SkillTemplate {

    private int mId;
    private int mLevel;
    private boolean mIsDebuff;
    private boolean mIsDamage;

    public int getId() {
        return mId;
    }

    private void setId(int id) {
        mId = id;
    }

    public int getLevel() {
        return mLevel;
    }

    private void setLevel(int level) {
        mLevel = level;
    }

    public boolean isDebuff() {
        return mIsDebuff;
    }

    private void setIsDebuff(boolean isDebuff) {
        mIsDebuff = isDebuff;
    }

    public boolean isDamage() {
        return mIsDamage;
    }

    private void setIsDamage(boolean isDamage) {
        mIsDamage = isDamage;
    }

    public static class Builder {

        private final SkillTemplate mSkill = new SkillTemplate();

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder setId(int id) {
            mSkill.setId(id);
            return this;
        }

        public Builder setLevel(int level) {
            mSkill.setLevel(level);
            return this;
        }

        public Builder setIsDamage(boolean value) {
            mSkill.setIsDamage(value);
            return this;
        }

        public Builder setIsDebuff(boolean value) {
            mSkill.setIsDebuff(value);
            return this;
        }

        public SkillTemplate build() {
            return mSkill;
        }
    }
}
