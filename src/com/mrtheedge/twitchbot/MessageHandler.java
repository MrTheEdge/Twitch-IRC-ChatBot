package com.mrtheedge.twitchbot;


import java.util.*;

/**
 * Created by E.J. Schroeder on 1/7/2016.
 */
public class MessageHandler {

    private CommandHandler commandHandler;
    private SpamHandler spamHandler;
    private MainController parentController;
    private HashSet<String> uniqueUsers; // Need to make lookups fast and enforce uniqueness
    private ArrayList<UserEventListener> _LISTENERS = new ArrayList<>();

    public MessageHandler(CommandHandler ch, SpamHandler sh){
        this.commandHandler = ch;
        this.spamHandler = sh;

        this.commandHandler.setParentMessageHandler(this);
        uniqueUsers = new HashSet<>();
    }

    public Optional handle(ChatMessage chatMessage){

        if (!uniqueUsers.contains(chatMessage.getSender())){
            uniqueUsers.add(chatMessage.getSender());
            fireUserEvent(chatMessage.getSender());
        }

        ChatMessage outMessage;

        if (chatMessage.getMessage().startsWith("!")){
            outMessage = commandHandler.parse(chatMessage);
        } else if (spamHandler.checkMessage(chatMessage)) {
            outMessage = chatMessage; // Return modified CM with correct spam type.
        } else {
            outMessage = null;
        }

        return Optional.ofNullable(outMessage);
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
        parentController.sendMessage(cm);
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

    public boolean checkMod(String user){
        return commandHandler.checkMod(user);
    }

    public void setAdmin(String user){
        commandHandler.setAdmin(user);
    }

}
