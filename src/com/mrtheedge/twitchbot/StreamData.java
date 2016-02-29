package com.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 2/26/2016.
 */
public class StreamData {

    // Variable names for GSON
    private String game;
    private int viewers;
    private int video_height;
    private String created_at;
    private ChannelData channel;

    public String getGame() {
        return game;
    }

    public int getViewers() {
        return viewers;
    }

    public int getVideoHeight() {
        return video_height;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public ChannelData getChannel() {
        return channel;
    }

}
