package com.mrtheedge.twitchbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

/**
 * Created by E.J. Schroeder on 9/15/2015.
 */
public class CommandHandler {

    // TODO Add shutdown hook method so that all running timers are cancelled.
    // TODO Add way to show each commands layout
    // TODO Add help commands

    private ArrayList<String> modList;
    private String channelAdmin;
    private HashMap<String, CustomCommand> customCmdMap;
    private HashMap<String, Timer> scheduledCommands;
    private MessageHandler parentMessageHandler;

    public CommandHandler() {
        customCmdMap = new HashMap<>();
        modList = new ArrayList<>();
        scheduledCommands = new HashMap<>();
    }
    /*
        Base Commands: add, del, disconnect
        Parse the command
            add - Create and add command (Pass in command without add)
            del - Remove command from command list
            schedule - puts the specified command on a timer for repeated messages
            stop - stops a scheduled command
            disconnect - send keyword back to ChatBot
     */
    public ChatMessage parse(ChatMessage chatMessage){
        // TODO Use a switch, and preferably create classes for the built in commands
        /*
            Split the command
            Determine which command it is
            Psrse the command
            Call the command
            Return the command response message
        */
        int prefixIndex = chatMessage.getMessage().indexOf(" ");
        String prefix;

        if (prefixIndex < 0){
            prefix = chatMessage.getMessage().substring(1, chatMessage.getMessage().length());
        } else {
            prefix = chatMessage.getMessage().substring(1, prefixIndex);
        }
        System.out.println(prefix);

        switch(prefix){
            case Constants.ADD_PREFIX:
                return parseAddCommand(chatMessage);

            case Constants.DEL_PREFIX:
                return parseDelCommand(chatMessage);

            case Constants.SCHEDULE_PREFIX:
                return parseScheduleCommand(chatMessage);

            case Constants.STOP_PREFIX:
                return stopScheduledCommand(chatMessage);

            case Constants.DISCONNECT:
                return parseDisconnectCommand(chatMessage);

            default:
                if( commandExists(prefix) ) {
                    String[] msgArray = chatMessage.getMessage().split(" ");

                    if (msgArray.length > 1){ //TODO Use 'requiresTarget' flag from within callCommand method!!!
                        return customCmdMap.get(prefix).callCommand(chatMessage.getSender(), msgArray[1], chatMessage.getChannel());
                    } else {
                        return customCmdMap.get(prefix).callCommand(chatMessage.getSender(), null, chatMessage.getChannel());
                    }

                    // Return callCommand

                } else {
                    return new ChatMessage(chatMessage.getChannel(), null, null, null, "[Invalid Command]");
                }
        }
    }

    private ChatMessage parseDisconnectCommand(ChatMessage cm) {
        String admin = cm.getChannel().substring(1, cm.getChannel().length());
        if (cm.getSender().equals(admin)){
            return new ChatMessage(null, null, null, null, null);
        } else {
            return new ChatMessage(cm.getChannel(), cm.getSender(), null, null, "You don't have permission to disconnect me!");
        }
    }

    public void setParentMessageHandler(MessageHandler mh){
        this.parentMessageHandler = mh;
    }

    public void sendScheduledMessage(ChatMessage cm){
        parentMessageHandler.sendMessage(cm);
    }

    private ChatMessage parseAddCommand(ChatMessage cm){

        if (cm.getSender().equals(channelAdmin)){
            try {
                CustomCommand custom = new CustomCommand(cm.getMessage().substring(5, cm.getMessage().length()));
                //System.out.println(custom.getName());
                custom.setParentHandler(this);
                addCommand(custom);
                return new ChatMessage(cm.getChannel(), null, null, null, "Command added.");
            } catch (IllegalArgumentException ex){
                return new ChatMessage(cm.getChannel(), null, null, null, "Error adding command.");
            }
        } else {
            return new ChatMessage(cm.getChannel(), null, null, null, "You don't have permission to add commands.");
        }

    }

    private ChatMessage parseDelCommand(ChatMessage cm){
        String[] cmdArray = cm.getMessage().split(" ");
        if (cm.getSender().equals(channelAdmin)) {
            if (commandExists(cmdArray[1])) {
                delCommand(cmdArray[1]);
                return new ChatMessage(cm.getChannel(), null, null, null, "Command deleted.");
            } else {
                //Return error message
                return new ChatMessage(cm.getChannel(), null, null, null, "Error deleting command.");
            }
        } else {
            return new ChatMessage(cm.getChannel(), null, null, null, "You don't have permission to delete commands.");
        }
    }

    private ChatMessage stopScheduledCommand(ChatMessage cm) {
        if (userHasPermission(cm.getSender(), Constants.ADMIN)){
            String[] splitCom = cm.getMessage().split(" ");
            if (scheduledCommands.containsKey(splitCom[1])) {
                scheduledCommands.get(splitCom[1]).cancel();
                scheduledCommands.remove(splitCom[1]);
                return new ChatMessage(cm.getChannel(), null, null, null, "Scheduled commands have been stopped.");
            } else {
                return new ChatMessage(cm.getChannel(), null, null, null, "Command is not running");
            }
        } else {
            return new ChatMessage(cm.getChannel(), null, null, null, "You don't have permission to stop a scheduled command.");
        }
    }

    private ChatMessage parseScheduleCommand(ChatMessage cm) {

        if (userHasPermission(cm.getSender(), Constants.ADMIN)){
            String[] splitCom = cm.getMessage().split(" "); // TODO Fix this... Check if command is already running
            if (commandExists(splitCom[1])) {
                CustomCommand custCmd = customCmdMap.get(splitCom[1]);
                Timer t = new Timer();
                int seconds = Integer.parseInt(splitCom[2]); // TODO ArrayOutOfBounds if no time given
                int milli = seconds * 1000;
                t.schedule(custCmd, milli, milli);
                custCmd.setScheduledSender(cm.getSender());
                custCmd.setScheduledChannel(cm.getChannel());
                scheduledCommands.put(custCmd.getName(), t);
                return new ChatMessage(cm.getChannel(), null, null, null, "Command scheduled to run.");
            } else {
                return new ChatMessage(cm.getChannel(), null, null, null, "Command does not exist!");
            }
        } else {
            return new ChatMessage(cm.getChannel(), null, null, null, "You don't have permission to use this.");
        }
    }

    private boolean userHasPermission( String sender, char reqLvl ){ // Pass in sender and required level to use command
        // TODO Fill this method out
        // User Level d: Default
        // User Level m: Mod
        // User Level a: Admin
        if (reqLvl == Constants.ADMIN){
            return sender.equals(channelAdmin);
        } else if (reqLvl == Constants.MODERATOR) {
            return modList.contains(sender);
        } else
            return (reqLvl == Constants.DEFAULT); // Should be default, only level left is 'd'
    }

    private boolean commandExists(String cmdName){
        return customCmdMap.containsKey(cmdName);
    }

    private void addCommand(CustomCommand command){
        customCmdMap.put(command.getName(), command);
    }

    private void delCommand(String cmdName){
        customCmdMap.remove(cmdName);
    }

    public void setAdmin(String user){
        channelAdmin = user;
    }

    public String getAdmin(){
        return channelAdmin;
    }

    public void addMod(String user){
        modList.add(user);
    }

    public void removeMod(String user){
        modList.remove(user);
    }

    public boolean isMod(String user){
        return (user.equals(getAdmin()) || modList.contains(user));
    }

    public void stopAllCommands() {

        for(Map.Entry<String, Timer> e : scheduledCommands.entrySet()){
            e.getValue().cancel();
        }

    }
}