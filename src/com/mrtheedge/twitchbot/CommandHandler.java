package com.mrtheedge.twitchbot;

import com.mrtheedge.twitchbot.exceptions.CommandScheduleException;
import com.mrtheedge.twitchbot.exceptions.InsufficientPermissionException;
import com.mrtheedge.twitchbot.exceptions.InvalidSyntaxException;
import com.mrtheedge.twitchbot.exceptions.NoSuchCommandException;
import com.mrtheedge.twitchbot.event.CommandEvent;
import com.mrtheedge.twitchbot.event.CommandEventListener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by E.J. Schroeder on 9/15/2015.
 */
public class CommandHandler {

    // TODO Add way to show each commands layout
    // TODO Add help commands
    // TODO Finish converting over to exception
    // TODO Rewrite most parse methods to be more readable

    private Set<String> modList;
    private String channelAdmin;
    private String channel;
    private HashMap<String, CustomCommand> customCmdMap;
    private HashMap<String, Timer> scheduledCommands;
    private MessageHandler parentMessageHandler;
    private ArrayList<CommandEventListener> _LISTENERS = new ArrayList<>();

    public CommandHandler() {
        customCmdMap = new HashMap<>();
        modList = new HashSet<>();
        scheduledCommands = new HashMap<>();
    }

    /**
     * Primary method of the class. It is used to determine which command the user requested
     * and parses any arguments that may be needed to call the command. Returns a chat message
     * with a response, provided no exceptions occurred while parsing.
     *
     * @param chatMessage
     * @return
     * @throws InvalidSyntaxException
     */
    public ChatMessage parse(ChatMessage chatMessage) throws InvalidSyntaxException, CommandScheduleException, InsufficientPermissionException, NoSuchCommandException {
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
        //System.out.println(prefix);

        switch(prefix){
            case Constants.ADD_PREFIX:
                return parseAddCommand(chatMessage);

            case Constants.DEL_PREFIX:
                return parseDelCommand(chatMessage);

            case Constants.SCHEDULE_PREFIX:
                return parseScheduleCommand(chatMessage);

            case Constants.STOP_PREFIX:
                return parseStopCommand(chatMessage);

            default:
                if( commandExists(prefix) ) {
                    String[] msgArray = chatMessage.getMessage().split(" ");

                    if (msgArray.length > 1){
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

    /**
     * Sets the parent message handler for this command handler.
     *
     * @param mh
     */
    public void setParentMessageHandler(MessageHandler mh){
        this.parentMessageHandler = mh;
    }

    /**
     * Utility method for scheduled commands so that they can bypass having to be called.
     * They can use this method when the Timer calls the next command.
     *
     * @param cm
     */
    public void sendScheduledMessage(ChatMessage cm){
        parentMessageHandler.sendMessage(cm);
    }

    /**
     * Parses a command that has been determined to be an add command. This will create a new
     * CustomCommand from the data in the message and add it to the collection.
     *
     * @param cm
     * @return
     */
    private ChatMessage parseAddCommand(ChatMessage cm){

        if (userHasPermission(cm.getSender(), Constants.MODERATOR)){ // Throw exception
            try {
                CustomCommand custom = new CustomCommand(cm.getMessage().substring(5, cm.getMessage().length()));
                //System.out.println(custom.getName());
                addCommand(custom);
                return new ChatMessage(cm.getChannel(), null, null, null, "Command added.");
            } catch (IllegalArgumentException ex){
                return new ChatMessage(cm.getChannel(), null, null, null, "Error adding command.");
            }
        } else {
            return new ChatMessage(cm.getChannel(), null, null, null, "You don't have permission to add commands.");
        }

    }

    /**
     * Parses a command that has been determined to be a delete command.
     * This command removes a CustomCommand from the collection when it no
     * longer need to be used.
     *
     * @param cm
     * @return
     */
    private ChatMessage parseDelCommand(ChatMessage cm){
        String[] cmdArray = cm.getMessage().split(" ");
        if (userHasPermission(cm.getSender(), Constants.MODERATOR)) { // Throw exception
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

    /**
     * Parses a command that has been determined to be a stop command. This command
     * will stop a previously started scheduled command.
     *
     * @param cm
     * @return
     */
    private ChatMessage parseStopCommand(ChatMessage cm) {
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

    /**
     * Parses a command that has been determined to be a schedule command. This will cause
     * the specified command to run on a timer, without having to be called by a user.
     *
     * @param cm
     * @return
     * @throws InvalidSyntaxException
     * @throws NoSuchCommandException
     * @throws CommandScheduleException
     * @throws InsufficientPermissionException
     */
    private ChatMessage parseScheduleCommand(ChatMessage cm) throws InvalidSyntaxException, NoSuchCommandException, CommandScheduleException, InsufficientPermissionException {

        if (!userHasPermission(cm.getSender(), Constants.ADMIN))
            throw new InsufficientPermissionException("User does not have permission to schedule commands");

        String[] splitCom = cm.getMessage().split(" ");
        if (splitCom.length < 3)
            throw new InvalidSyntaxException("Error while parsing the command.");

        try { // If time is not able to be parsed from the string
            int time = Integer.parseInt(splitCom[2]);
            scheduleCommand(splitCom[1], time);
        } catch (NumberFormatException e){
            throw new InvalidSyntaxException("Time parameter was invalid.");
        }

        return new ChatMessage(cm.getChannel(), null, null, null, "Command scheduled to run.");
    }

    /**
     * Returns true if the user that is specified is at or above the required level
     * that is also specified.
     *
     * @param sender
     * @param reqLvl
     * @return
     */
    private boolean userHasPermission( String sender, char reqLvl ){ // Pass in sender and required level to use command
        if (reqLvl == Constants.ADMIN){
            return sender.equals(channelAdmin);
        } else if (reqLvl == Constants.MODERATOR) {
            return modList.contains(sender) || sender.equals(channelAdmin);
        } else
            return (reqLvl == Constants.DEFAULT); // Should be default, only level left is 'd'
    }

    /**
     * Adds a listener to the collection to be notified.
     *
     * @param evListener
     */
    public void addListener(CommandEventListener evListener){
        _LISTENERS.add(evListener);
    }

    /**
     * Removes a listener from the collection.
     *
     * @param eventListener
     */
    public void removeListener(CommandEventListener eventListener){
        _LISTENERS.remove(eventListener);
    }

    /**
     * Notifies all the listeners in the collection that a new event has occurred.
     * Used to notify the GUI that it needs to add a new element.
     *
     * @param c
     * @param type
     */
    public void fireCommandEvent(CustomCommand c, int type){
        for (CommandEventListener ev : _LISTENERS){
            ev.handle(new CommandEvent(this, c, type));
        }
    }

    /**
     * Returns true if a command exists with the name that is
     * passed in.
     *
     * @param cmdName
     * @return
     */
    private boolean commandExists(String cmdName){
        return customCmdMap.containsKey(cmdName);
    }

    /**
     * Adds a command to the map. Sets the CustomCommands parentHandler
     * and fires an event for the GUI that a command was added.
     *
     * @param command
     */
    public void addCommand(CustomCommand command){
        command.setParentHandler(this);
        customCmdMap.put(command.getName(), command);
        fireCommandEvent(command, CommandEvent.ADD);
    }

    /**
     * Deletes a command from the map if it exists.
     * Before it is removed, it attempts to stop any timers associated
     * with this command. Also sends an event for the GUI that a command
     * was deleted.
     *
     * @param cmdName
     */
    public void delCommand(String cmdName){
        try {
            stopScheduledCommand(cmdName);
        } catch (NoSuchCommandException e) {
            return; // No command, so no need to delete.
        } catch (CommandScheduleException e) {
            // Do nothing, because nothing was scheduled.
        }
        fireCommandEvent(customCmdMap.get(cmdName), CommandEvent.DELETE);
        customCmdMap.remove(cmdName);
    }

    /**
     * Wrapper for scheduleCommand(CustomCommand) that takes a command name,
     * finds it if one exists, and passes it on.
     *
     * @param command
     * @param time
     * @throws NoSuchCommandException
     * @throws CommandScheduleException
     * @throws InvalidSyntaxException
     */
    private void scheduleCommand(String command, int time) throws NoSuchCommandException, CommandScheduleException, InvalidSyntaxException {
        if (!customCmdMap.containsKey(command))
            throw new NoSuchCommandException("No custom command with that name.");

        scheduleCommand( customCmdMap.get(command), time );
    }

    private void scheduleCommand(CustomCommand command, int time) throws CommandScheduleException, InvalidSyntaxException {
        if ( scheduledCommands.containsKey(command.getName()) )
            throw new CommandScheduleException("Command already scheduled.");

        Timer timer = new Timer();
        timer.schedule(command.getNewTimerTask(), time*1000, time*1000);
        scheduledCommands.put(command.getName(), timer);
    }

    /**
     * Wrapper that takes a string, finds the associated command
     * and passed the CustomCommand to stopScheduledCommand()
     *
     * @param command
     * @throws NoSuchCommandException
     * @throws CommandScheduleException
     */
    private void stopScheduledCommand(String command) throws NoSuchCommandException, CommandScheduleException {
        if (!customCmdMap.containsKey(command))
            throw new NoSuchCommandException("No custom command with that name.");

        stopScheduledCommand(customCmdMap.get(command));
    }

    /**
     * If the command is scheduled, it cancels the timer and
     * removes the command from the collection of scheduled
     * commands.
     *
     * @param command
     * @throws CommandScheduleException
     */
    private void stopScheduledCommand(CustomCommand command) throws CommandScheduleException {
        if ( !scheduledCommands.containsKey(command.getName()) )
            throw new CommandScheduleException("Command is not currently scheduled.");

        scheduledCommands.get(command.getName()).cancel();
        scheduledCommands.remove(command.getName());
    }

    /**
     * Returns a list of all custom commands to be used in
     * saving when the application is closed
     *
     * @return
     */
    public List<CustomCommand> getCommandsAsList(){
        return customCmdMap.entrySet().stream()
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    public void setAdmin(String user){
        channelAdmin = user;
        setChannel("#" + user);
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

    public boolean checkMod(String user){
        return (user.equals(getAdmin()) || modList.contains(user));
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel(){
        return channel;
    }

    public void stopAllCommands() {

        for(Map.Entry<String, Timer> e : scheduledCommands.entrySet())
            e.getValue().cancel();

        scheduledCommands.clear();

    }

}