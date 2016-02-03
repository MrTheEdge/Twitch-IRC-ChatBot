package com.mrtheedge.twitchbot;

import java.util.EventObject;

/**
 * Created by E.J. Schroeder on 2/2/2016.
 */
public interface ChatEventListener {

    public void handleChatEvent(ChatEvent e);
}
