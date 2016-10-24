package com.mrtheedge.twitchbot;

import java.util.Comparator;

/**
 * Created by E.J. Schroeder on 3/14/2016.
 */
public class User implements Comparable<User> {

    private final String username;
    private String displayName;
    private String nameColor;
    private boolean isMod;
    private boolean isTurbo;
    private boolean isSubscriber;

    public User(
            String username,
            String displayName,
            String nameColor,
            boolean isMod,
            boolean isTurbo,
            boolean isSubscriber )
    {
        this.username = username;
        this.displayName = displayName;
        this.nameColor = nameColor;
        this.isMod = isMod;
        this.isTurbo = isTurbo;
        this.isSubscriber = isSubscriber;
    }

    public User(String username){
        this.username = username;
        displayName = username;
        nameColor = "";
        isMod = false;
        isTurbo = false;
        isSubscriber = false;
    }

    public String getUsername(){
        return username;
    }

    public String getDisplayName(){
        return displayName;
    }

    public String getNameColor(){
        return nameColor;
    }

    public void setModStatus(boolean isMod){
        this.isMod = isMod;
    }

    public boolean isMod(){
        return isMod;
    }

    public boolean isTurbo(){
        return isTurbo;
    }

    public boolean isSubscriber(){
        return isSubscriber;
    }

    public boolean equals(Object o){
        if (o == null) return false;

        if (o instanceof User){
            return username.equals(((User) o).getUsername());
        }

        return false;
    }

    @Override
    public int compareTo(User o2) {
        return this.username.compareTo(o2.getUsername());
    }
}
