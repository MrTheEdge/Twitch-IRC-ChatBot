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

    public CustomCommand(String cmdString) {
        parseCommand(cmdString);
    }

    /**
     * Takes a string directly from the chat, breaks it up and parses it
     * so that it can be turned into a new CustomCommand.
     *
     * @param cmd
     * @throws IllegalArgumentException
     */
    private void parseCommand(String cmd) throws IllegalArgumentException {
        String[] splitCmd = cmd.split(" ");
        String tempMessage = "";

        if ( splitCmd.length < 2) // Command doesn't supply required arguments
            throw new IllegalArgumentException();

        commandName = splitCmd[0]; // Index 1: Command Name

        if ( splitCmd[1].startsWith("-") ){ // Command has optional userlvl requirements

            try {
                userLevel = Character.toUpperCase(splitCmd[1].charAt(1));
                if (userLevel != Constants.ADMIN && userLevel != Constants.MODERATOR) // Incorect value for userlvl
                    userLevel = Constants.DEFAULT;
            } catch (IndexOutOfBoundsException ex){
                throw new IllegalArgumentException();
            }

            if (splitCmd.length < 3)
                throw new IllegalArgumentException();

            for (int i = 2; i < splitCmd.length; i++){ // Start from index after userlvl arg
                if (i == splitCmd.length-1)
                    tempMessage += splitCmd[i];
                else
                    tempMessage += splitCmd[i] + " ";
            }
        } else { // Command can be used by everyone
            userLevel = 'd';
            for (int i = 1; i < splitCmd.length; i++){ // Start from index after commandName arg
                if (i == splitCmd.length-1)
                    tempMessage += splitCmd[i];
                else
                    tempMessage += splitCmd[i] + " ";
            }
        }

        if (tempMessage.contains(Constants.TARGET_USER))
            requiresTarget = true;
        if (tempMessage.contains(Constants.USER_VAR))
            requiresSender = true;

        response = tempMessage;

    } // End of: parseCommand(String cmd)

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
     * Returns a new instance of TimerTask for the specific
     * command.
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
