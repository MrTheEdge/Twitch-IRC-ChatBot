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
    public ChatMessage parse(ChatMessage chatMessage) throws InvalidSyntaxException, CommandScheduleException,
            InsufficientPermissionException, NoSuchCommandException {

        int prefixIndex = chatMessage.getMessage().indexOf(" ");
        String prefix;

        if (prefixIndex < 0){
            prefix = chatMessage.getMessage().substring(1, chatMessage.getMessage().length());
        } else {
            prefix = chatMessage.getMessage().substring(1, prefixIndex);
        }

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
                return parseGenericCommand(chatMessage, prefix);
        }
    }

    /**
     * Parses a command if it not one of the built-in commands. Checks if it
     * exists and if so, calls the appropriate command. Returns a new ChatMessage
     * with the response.
     *
     * @param chatMessage
     * @param prefix
     * @return
     * @throws InvalidSyntaxException
     * @throws NoSuchCommandException
     */
    private ChatMessage parseGenericCommand(ChatMessage chatMessage, String prefix) throws InvalidSyntaxException, NoSuchCommandException {

        if( commandExists(prefix) ) {
            String[] msgArray = chatMessage.getMessage().split(" ");

            if (msgArray.length > 1){
                return customCmdMap.get(prefix).callCommand(chatMessage.getSender(), msgArray[1], chatMessage.getChannel());
            } else {
                return customCmdMap.get(prefix).callCommand(chatMessage.getSender(), null, chatMessage.getChannel());
            }

            // Return callCommand

        } else {
            throw new NoSuchCommandException("No command with that name: " + prefix);
        }
    }

    /**
     * Parses a command that has been determined to be an add command. This will create a new
     * CustomCommand from the data in the message and add it to the collection.
     *
     * @param chatMessage
     * @return
     */
    private ChatMessage parseAddCommand(ChatMessage chatMessage) throws InvalidSyntaxException, InsufficientPermissionException {

        if (!userHasPermission(chatMessage.getSender(), Constants.MODERATOR))
            throw new InsufficientPermissionException("Only moderators or an admin can add a command");

        // Sample message -> !add test This is a new command!
        //                        ^ starts at index 5
        String commandString = chatMessage.getMessage().substring(5, chatMessage.getMessage().length());
        CustomCommand customCommand = new CustomCommand(commandString);

        addCommand(customCommand);
        return new ChatMessage(chatMessage.getChannel(), null, null, null, "Command added.");

    }

    /**
     * Parses a command that has been determined to be a delete command.
     * This command removes a CustomCommand from the collection when it no
     * longer need to be used.
     *
     * @param cm
     * @return
     */
    private ChatMessage parseDelCommand(ChatMessage cm) throws InsufficientPermissionException, NoSuchCommandException, InvalidSyntaxException {

        if (!userHasPermission(cm.getSender(), Constants.MODERATOR))
            throw new InsufficientPermissionException("Only moderators or higher can delete commands.");

        // cmdArray[1] is name of command
        String[] cmdArray = cm.getMessage().split(" ");
        if (cmdArray.length < 2)
            throw new InvalidSyntaxException("!del must supply a parameter. Usage: !del <command>");

        if (commandExists(cmdArray[1])) {

            deleteCommand(cmdArray[1]);
            return new ChatMessage(cm.getChannel(), null, null, null, "Command deleted.");
        } else {
            throw new NoSuchCommandException("No command to delete with the name: " + cmdArray[1]);
        }

    }

    /**
     * Parses a command that has been determined to be a stop command. This command
     * will stop a previously started scheduled command.
     *
     * @param cm
     * @return
     * @throws NoSuchCommandException
     * @throws CommandScheduleException
     * @throws InsufficientPermissionException
     * @throws InvalidSyntaxException
     */
    private ChatMessage parseStopCommand(ChatMessage cm) throws NoSuchCommandException, CommandScheduleException, InsufficientPermissionException, InvalidSyntaxException {
        if (!userHasPermission(cm.getSender(), Constants.ADMIN))
            throw new InsufficientPermissionException("User does not have permission to stop scheduled commands");

        String[] splitCom = cm.getMessage().split(" ");
        if (splitCom.length < 2)
            throw new InvalidSyntaxException("!stop command requires parameter. Use: !stop <command>");

        stopScheduledCommand(splitCom[1]);
        return new ChatMessage(cm.getChannel(), null, null, null, "Scheduled commands have been stopped.");
    }

    /**
     * Parses a command that has been determined to be a schedule command. This will cause
     * the specified command to run on a timer, without having to be called by a user.
     *
     * @param chatMessage
     * @return
     * @throws InvalidSyntaxException
     * @throws NoSuchCommandException
     * @throws CommandScheduleException
     * @throws InsufficientPermissionException
     */
    private ChatMessage parseScheduleCommand(ChatMessage chatMessage) throws InvalidSyntaxException, NoSuchCommandException, CommandScheduleException, InsufficientPermissionException {

        if (!userHasPermission(chatMessage.getSender(), Constants.ADMIN))
            throw new InsufficientPermissionException("User does not have permission to schedule commands");

        String[] splitMessage = chatMessage.getMessage().split(" ");
        if (splitMessage.length < 3)
            throw new InvalidSyntaxException("!schedule must have 2 parameters. Usage: !schedule <command> <seconds>");

        try { // If time is not able to be parsed from the string
            int time = Integer.parseInt(splitMessage[2]);
            scheduleCommand(splitMessage[1], time);
        } catch (NumberFormatException e){
            throw new InvalidSyntaxException("!schedule time parameter was invalid. Usage: !schedule <command> <seconds>");
        }

        return new ChatMessage(chatMessage.getChannel(), null, null, null, "Command scheduled to run.");
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

    private boolean commandExists(CustomCommand command){
        return commandExists(command.getName());
    }

    /**
     * Adds a command to the map. Sets the CustomCommands parentHandler
     * and fires an event for the GUI that a command was added.
     *
     * @param command
     */
    public void addCommand(CustomCommand command){
        command.setParentHandler(this);

        if (commandExists(command)){ // Command with the same name
            deleteCommand(command.getName());
            fireCommandEvent(customCmdMap.get(command.getName()), CommandEvent.DELETE);
        }

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
    public void deleteCommand(String cmdName){
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

    /**
     * Schedules a command for the number of seconds supplied. Can throw an InvalidSyntaxException if the
     * command contains variables that are not allowed. Can also throw a CommandScheduleException if the
     * command is already scheduled to run.
     *
     * @param command
     * @param time
     * @throws CommandScheduleException
     * @throws InvalidSyntaxException
     */
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

    /**
     * Sets the channel admin for use in determining command permissions
     * Also sets the channel based on the admin.
     *
     * @param user
     */
    public void setAdmin(String user){
        channelAdmin = user;
        setChannel("#" + user);
    }

    public String getAdmin(){
        return channelAdmin;
    }

    /**
     * Adds a user to the mod list. Enables users to call commands
     * only available for mods.
     *
     * @param user
     */
    public void addMod(String user){
        modList.add(user);
    }

    /**
     * Removes a user from the mod list.
     *
     * @param user
     */
    public void removeMod(String user){
        modList.remove(user);
    }

    /**
     * Returns true if the user is either the admin or is a mod.
     *
     * @param user
     * @return
     */
    public boolean checkMod(String user){
        return (user.equals(getAdmin()) || modList.contains(user));
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel(){
        return channel;
    }

    /**
     * Stops all scheduled commands and clears them from the
     * collection.
     *
     */
    public void stopAllCommands() {

        for(Map.Entry<String, Timer> e : scheduledCommands.entrySet())
            e.getValue().cancel();

        scheduledCommands.clear();

    }

}