package com.mrtheedge.twitchbot;

import com.mrtheedge.twitchbot.exceptions.InvalidSyntaxException;

import java.io.Serializable;
import java.util.TimerTask;

/**
 * Created by E.J. Schroeder on 9/16/2015.
 */

public class CustomCommand implements Serializable {

    /*
        <command_name> [-LVL] <This is what the command will say>
        Split command on "space"
        Index 0: Command Name
        Index 1: User level or command response (Perform Check for -LVL)
        Index 2+: Continuation of command response
    */

    protected String commandName;
    protected char userLevel;
    protected String response;
    protected int useCount;
    protected transient CommandHandler parentHandler;
    protected boolean requiresTarget = false;
    protected boolean requiresSender = false;

    public CustomCommand(){
        commandName = "default";
        userLevel = 'd';
        response = "Someone didn't initialize me correctly.";
        useCount = 0;
    }

    /**
     * Alternate way to create a command if nothing needs to be parsed first.
     *
     * @param name
     * @param prLvl
     * @param response
     */
    public CustomCommand(String name, char prLvl, String response){
        this.commandName = name;
        this.userLevel = prLvl;
        this.response = response;
        useCount = 0;
        if (response.contains(Constants.TARGET_USER))
            requiresTarget = true;
        if (response.contains(Constants.USER_VAR))
            requiresSender = true;

    }

    public CustomCommand(String cmdString) throws InvalidSyntaxException {
        parseCommand(cmdString);
    }

    /**
     * Takes a string directly from the chat, breaks it up and parses it
     * so that it can be turned into a new CustomCommand.
     *
     * @param cmd
     * @throws IllegalArgumentException
     */
    private void parseCommand(String cmd) throws InvalidSyntaxException {
        /*
            splitMessage[0] -> command name
            splitMessage[1] -> permission level || beginning of command
            splitMessage[2] -> beginning of command || continuation of command
            splitMessage[3...] -> Rest of command
         */

        if (cmd.startsWith("!")) // If command is passed in with !, trim it
            cmd = cmd.substring(1);

        String[] splitMessage = cmd.split(" ");

        if ( splitMessage.length < 2) // Command doesn't supply required arguments
            throw new InvalidSyntaxException("Not enough arguments to create a command.");

        commandName = splitMessage[0]; // Index 0: Command Name

        if ( splitMessage[1].startsWith("-") ){ // Command has optional userlvl parameters

            if (splitMessage.length < 3)
                throw new InvalidSyntaxException("No response given for command");

            setUserLevel(splitMessage[1]);
            finalizeResponseString(splitMessage, 2);

        } else { // Command can be used by everyone
            userLevel = 'D';
            finalizeResponseString(splitMessage, 1);
        }
    } // End of: parseCommand(String cmd)

    /**
     * Takes a string in the format "-M" (presumably) and sets the appropriate user
     * level required to call this command. If the string does not follow the format
     * an InvalidSyntaxException is thrown. User level defaults to D (default) if the
     * string is formatted correctly but incorrect identifier is given.
     *
     * @param strToParse
     * @throws InvalidSyntaxException
     */
    private void setUserLevel(String strToParse) throws InvalidSyntaxException {
        try {

            userLevel = Character.toUpperCase(strToParse.charAt(1));
            if (userLevel != Constants.ADMIN && userLevel != Constants.MODERATOR) // Incorect value for userlvl
                userLevel = Constants.DEFAULT;

        } catch (IndexOutOfBoundsException ex){
            throw new InvalidSyntaxException("Invalid use of user level flag. Correct use: -M");
        }
    }

    /**
     * Takes an array of strings that were previously split on space, and an index
     * to start at. Reassembles the split string into one string that is then stored
     * as the command response whenever it is called. Also sets flags for the command
     * if it uses any variables that prevent it from being scheduled.
     *
     * @param splitMessage
     * @param startIndex
     */
    private void finalizeResponseString(String[] splitMessage, int startIndex){
        String tempMessage = "";
        for (int i = startIndex; i < splitMessage.length; i++){
            if (i == splitMessage.length-1)
                tempMessage += splitMessage[i];
            else
                tempMessage += splitMessage[i] + " ";
        }

        if (tempMessage.contains(Constants.TARGET_USER))
            requiresTarget = true;
        if (tempMessage.contains(Constants.USER_VAR))
            requiresSender = true;

        response = tempMessage;

    }

    /**
     * Takes a sender, target (if required), and the channel to send to.
     * Replaces variables in the response string with the appropriate values
     * and then creates a new ChatMessage to be sent out.
     *
     * @param sender
     * @param target
     * @param channel
     * @return
     * @throws InvalidSyntaxException
     */
    public ChatMessage callCommand(String sender, String target, String channel) throws InvalidSyntaxException {

        if ( this.requiresTargetParam() && target == null ){
            throw new InvalidSyntaxException("Command requires target parameter");
        }

        String modifiedResponse = response;

        modifiedResponse = modifiedResponse.replaceAll(Constants.USER_VAR, sender);
        modifiedResponse = modifiedResponse.replaceAll(Constants.TARGET_USER, target);
        modifiedResponse = modifiedResponse.replaceAll(Constants.USE_COUNT_VAR, String.valueOf(useCount));

        if (modifiedResponse.contains(Constants.UPTIME_VAR)){ // Separate block for API calls so they aren't called every time
            modifiedResponse = modifiedResponse.replaceAll(Constants.UPTIME_VAR, TwitchAPI.getChannelUptime(channel));
        }

        useCount++;
        return new ChatMessage(channel, null, null, null, modifiedResponse);
    }

    /**
     * Returns a new instance of TimerTask for the specific command. Allows this command
     * to be scheduled to run on a Timer.
     *
     * @return
     * @throws InvalidSyntaxException If the command contains variables that don't make sense in a repeated command.
     */
    public TimerTask getNewTimerTask() throws InvalidSyntaxException {
        if (requiresTarget || requiresSender)
            throw new InvalidSyntaxException("Commands with <user> or <target> cannot be scheduled");

        return new TimerTask(){
            @Override
            public void run() {
                try {
                    parentHandler.sendScheduledMessage(callCommand(null, null, parentHandler.getChannel()));
                } catch (InvalidSyntaxException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Returns the name of the command used to call it within the chat
     *
     * @return
     */
    public String getName(){
        return commandName;
    }

    /**
     * Returns a char that represents the required level of user to call the command
     *  A - Admin
     *  M - Mod
     *  D - Default
     *
     * @return
     */
    public char getReqLevel(){
        return userLevel;
    }

    /**
     * Returns the string that determines what the command will respond with
     * when called from the chat.
     *
     * @return
     */
    public String getResponse() { return response; }

    /**
     * Returns the number of times this command has been called.
     *
     * @return
     */
    public int getUseCount(){
        return useCount;
    }

    /**
     * Returns true if the command requires a target parameter to be supplied
     * when it is called.
     *
     * @return
     */
    public boolean requiresTargetParam(){
        return requiresTarget;
    }

    /**
     * Sets the parent command handler which allows the command to
     * send itself directly in the case of it being scheduled.
     *
     * @param ch
     */
    public void setParentHandler(CommandHandler ch) {
        parentHandler = ch;
    }
}
