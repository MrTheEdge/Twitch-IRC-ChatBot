package com.mrtheedge.twitchbot.event;

import java.util.EventObject;

/**
 * Created by E.J. Schroeder on 2/3/2016.
 */


public class UserEvent extends EventObject {

    private String user;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public UserEvent(Object source, String user) {
        // TODO Edit this to make it compile. Don't require a user event type
        super(source);
        this.user = user;
    }

    public String getUser(){
        return user;
    }
}

enum UserEventType {
    addition, deletion
}
