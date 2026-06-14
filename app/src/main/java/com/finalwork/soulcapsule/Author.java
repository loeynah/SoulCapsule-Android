package com.finalwork.soulcapsule;

import com.stfalcon.chatkit.commons.models.IUser;

public class Author implements IUser {

    private final String id;
    private final String name;
    private final String avatar;

    public Author(String id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar != null ? avatar : "";
    }
}
