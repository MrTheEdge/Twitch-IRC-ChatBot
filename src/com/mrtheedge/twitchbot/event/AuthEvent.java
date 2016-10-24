package com.mrtheedge.twitchbot.event;

import java.util.EventObject;

/**
 * Created by E.J. Schroeder on 10/23/2016.
 */
public class AuthEvent extends EventObject {

    private boolean successful;
    private String token;
    private String user;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public AuthEvent(Object source, boolean successful, String user, String token) {
        super(source);
        this.successful = successful;
        this.token = token;
        this.user = user;
    }

    public String getUsername(){
        return user;
    }

    public String getToken(){
        return token;
    }

    public boolean isSuccessful(){
        return successful;
    }
}
