package com.github.athenaengine.core.model.instance;

public class ItemInstance {

    private final int mObjectId;

    public static ItemInstance newInstance(int objectId) {
        return new ItemInstance(objectId);
    }

    private ItemInstance(int objectId) {
        mObjectId = objectId;
    }

    public int getObjectId() {
        return mObjectId;
    }
}
