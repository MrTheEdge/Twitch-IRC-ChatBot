package com.mrtheedge.twitchbot;

import java.util.EventObject;

/**
 * Created by E.J. Schroeder on 2/2/2016.
 */
public class ChatEvent extends EventObject {

    private String message;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ChatEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
