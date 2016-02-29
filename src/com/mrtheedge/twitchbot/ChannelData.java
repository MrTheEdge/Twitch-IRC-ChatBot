package com.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 2/26/2016.
 */
public class ChannelData {

    private boolean mature;
    private String status;
    private String game;
    private String name;
    private String created_at;
    private boolean partner;
    private long views;
    private long followers;

    public boolean isMature() {
        return mature;
    }

    public String getStatus() {
        return status;
    }

    public String getGame() {
        return game;
    }

    public String getName() {
        return name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public boolean isPartner() {
        return partner;
    }

    public long getViews() {
        return views;
    }

    public long getFollowers() {
        return followers;
    }

}
