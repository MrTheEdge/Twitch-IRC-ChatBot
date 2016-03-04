package com.mrtheedge.twitchbot;


import com.mrtheedge.twitchbot.exceptions.*;
import com.mrtheedge.twitchbot.event.UserEvent;
import com.mrtheedge.twitchbot.event.UserEventListener;

import java.util.*;

/**
 * Created by E.J. Schroeder on 1/7/2016.
 */
public class MessageHandler {

    private CommandHandler commandHandler;
    private SpamHandler spamHandler;
    private BotController parentController;
    private HashSet<String> uniqueUsers; // Need to make lookups fast and enforce uniqueness
    private ArrayList<UserEventListener> _LISTENERS = new ArrayList<>();

    public MessageHandler(CommandHandler ch, SpamHandler sh){
        this.commandHandler = ch;
        this.spamHandler = sh;
        this.commandHandler.setParentMessageHandler(this);
        uniqueUsers = new HashSet<>();
    }

    /**
     * Takes a ChatMessage and determines if it is spam or a command.
     * Ignores spam for moderators, but checks for spam for everyone else
     * before trying to parse a command. This prevents users from getting
     * around spam protection by beginning a message with "!". Also checks
     * if the sender is unique.
     *
     * @param chatMessage
     * @return
     * @throws ParsingException
     */
    public Optional handle(ChatMessage chatMessage) throws ParsingException {

        checkForUniqueUser(chatMessage.getSender());

        ChatMessage outMessage;

        if ( checkMod(chatMessage.getSender()) ){
            outMessage = handleCommandMessage(chatMessage);
        } else {
            if (spamHandler.checkMessage(chatMessage)){
                outMessage = chatMessage;
            } else {
                outMessage = handleCommandMessage(chatMessage);
            }
        }

        return Optional.ofNullable(outMessage);
    }

    /**
     * Determines whether a ChatMessage is a command or not. If it is, it returns a
     * new ChatMessage with the response from the command. If not, the method will return
     * null indicating that nothing else needs to be done with this ChatMessage.
     *
     * @param chatMessage
     * @return
     * @throws ParsingException
     */
    private ChatMessage handleCommandMessage(ChatMessage chatMessage) throws ParsingException {
        if (chatMessage.getMessage().startsWith("!")){
            return commandHandler.parse(chatMessage);
        } else {
            return null; // Wasn't a command, return null
        }
    }

    /**
     * Checks if a given user is already in the collection of unique users.
     * If not, the user is added and an event is fired that notifies the GUI
     * to add the user to the ListView.
     *
     * @param user
     */
    private void checkForUniqueUser(String user){

        if (!uniqueUsers.contains(user)){
            uniqueUsers.add(user);
            fireUserEvent(user);
        }

    }

    /**
     * Adds a listener to this object. Listener will be notified when an
     * event is fired.
     *
     * @param evListener
     */
    public void addListener(UserEventListener evListener){
        _LISTENERS.add(evListener);
    }

    /**
     * Removes a given UserEventListener from this object.
     *
     * @param eventListener
     */
    public void removeListener(UserEventListener eventListener){
        _LISTENERS.remove(eventListener);
    }

    /**
     * Fires a new UserEvent that notifies any listeners that a new
     * user was just added to the collection.
     *
     * @param user
     */
    public void fireUserEvent(String user){
        for (UserEventListener ev : _LISTENERS){
            ev.handle(new UserEvent(this, user));
        }
    }

    /**
     * Returns the SpamHandler associated with this object.
     *
     * @return
     */
    public SpamHandler getSpamHandler(){
        return spamHandler;
    }

    /**
     * Returns the CommandHandler associated with this object.
     *
     * @return
     */
    public CommandHandler getCommandHandler(){
        return commandHandler;
    }

    /**
     * Terminates all currently running Timers within the
     * CommandHandler object.
     *
     */
    public void shutdown(){
        commandHandler.stopAllCommands();
    }

    /**
     * Sends the contents of a ChatMessage to the IRC server.
     *
     * @param cm
     */
    public void sendMessage(ChatMessage cm){
        parentController.sendMessage(cm);
    }

    public void setParentController(BotController mc){
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
