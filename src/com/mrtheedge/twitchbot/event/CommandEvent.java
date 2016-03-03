package com.mrtheedge.twitchbot.event;

import com.mrtheedge.twitchbot.CustomCommand;

import java.util.EventObject;

/**
 * Created by E.J. Schroeder on 2/3/2016.
 */
public class CommandEvent extends EventObject {

    public static final int ADD = 0;
    public static final int DELETE = 1;

    private CustomCommand c;
    private int type;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public CommandEvent(Object source, CustomCommand c, int type) {
        super(source);
        this.c = c;
        this.type = type;
    }

    public int getType(){
        return type;
    }

    public CustomCommand getCommand(){
        return c;
    }
}
