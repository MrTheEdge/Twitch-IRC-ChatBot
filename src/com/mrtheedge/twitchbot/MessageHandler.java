package com.mrtheedge.twitchbot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.SortedList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by E.J. Schroeder on 1/7/2016.
 */
public class MessageHandler {

    private CommandHandler commandHandler;
    private SpamHandler spamHandler;
    private MainController parentController;
//    private ObservableList<String> activeUsers;
//    private SortedList<String> sortedActiveUsers;
    private HashSet<String> uniqueUsers; // Dumb way to make lookups fast and enforce uniqueness
    private ArrayList<UserEventListener> _LISTENERS = new ArrayList<>();

    public MessageHandler(CommandHandler ch, SpamHandler sh){
        this.commandHandler = ch;
        this.spamHandler = sh;

        this.commandHandler.setParentMessageHandler(this);
//        activeUsers = FXCollections.observableArrayList();
//        sortedActiveUsers = new SortedList<String>(activeUsers);
        uniqueUsers = new HashSet<>();


    }

    public ChatMessage handle(ChatMessage c){

        if (!uniqueUsers.contains(c.getSender())){
            uniqueUsers.add(c.getSender());
            fireUserEvent(c.getSender());
        }

        if (c.getMessage().startsWith("!")){
            return commandHandler.parse(c);
        } else if (spamHandler.checkMessage(c.getMessage())) {
            return new ChatMessage(c.getChannel(), c.getSender(), null, null, null);
        }
        return null; // A null instead of message means nothing was wrong.
    }

    public void addListener(UserEventListener evListener){
        _LISTENERS.add(evListener);
    }

    public void removeListener(UserEventListener eventListener){
        _LISTENERS.remove(eventListener);
    }

    public void fireUserEvent(String user){
        for (UserEventListener ev : _LISTENERS){
            ev.handle(new UserEvent(this, user));
        }
    }

    public void shutdown(){
        commandHandler.stopAllCommands();
    }

    public void sendMessage(ChatMessage cm){
        parentController.addOutboundMessage(cm);
    }

    public void setParentController(MainController mc){
        this.parentController = mc;
    }

    public void addMod(String user){
        commandHandler.addMod(user);
    }

    public void removeMod(String user){
        commandHandler.removeMod(user);
    }

    public void setAdmin(String user){
        commandHandler.setAdmin(user);
    }

}
