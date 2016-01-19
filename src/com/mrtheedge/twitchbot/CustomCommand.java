package com.mrtheedge.twitchbot;

import java.util.TimerTask;

/**
 * Created by E.J. Schroeder on 9/16/2015.
 */
public class CustomCommand extends TimerTask {

    // TODO Check for any variables.
    // Determine whether an autorun command should be able to have variables.
    // Add fields for whether command has certain variables

    /*
        <command_name> [userlvl=m] <This is what the command will say>
        Split command on "space"
        Index 0: Command Name
        Index 1: User level or command response (Perform Check for userlvl=)
        Index 2+: Continuation of command response
    */

    protected String commandName;
    protected char userLevel;
    protected String response;
    protected int useCount;
    protected CommandHandler parentHandler;
    protected boolean requiresTarget;

    private String scheduledChannel;
    private String scheduledSender;

    public CustomCommand(){
        commandName = "default";
        userLevel = 'd';
        response = "Someone didn't initialize me correctly.";
        useCount = 0;
        requiresTarget = false;
    }

    public CustomCommand(String name, char prLvl, String response){ // Use with built in commands
        this.commandName = name;
        this.userLevel = prLvl;
        this.response = response;
        useCount = 0;
        if (response.contains("<target>"))
            requiresTarget = true;
        else
            requiresTarget = false;
    }

    public CustomCommand(String cmdString) {
        parseCommand(cmdString);
    }

    public ChatMessage callCommand(String sender, String target, String channel){

        if ( this.requiresTargetParam() && target == null ){
            return new ChatMessage(channel, null, null, null, "This command requires a target user parameter.");
        }

        this.useCount++;
        String modifiedResponse = response;

        modifiedResponse = modifiedResponse.replaceAll(Constants.userVar, sender);
        modifiedResponse = modifiedResponse.replaceAll(Constants.targetUser, target);
        modifiedResponse = modifiedResponse.replaceAll(Constants.uptimeVar, Uptime.getUptime(channel));
        modifiedResponse = modifiedResponse.replaceAll(Constants.useCountVar, String.valueOf(useCount));

        return new ChatMessage(channel, null, null, null, modifiedResponse);
    }

    public void setScheduledChannel(String scheduledChannel) {
        this.scheduledChannel = scheduledChannel;
    }

    public void setScheduledSender(String scheduledSender) {
        this.scheduledSender = scheduledSender;
    }

    private void parseCommand(String cmd) throws IllegalArgumentException {
        String[] splitCmd = cmd.split(" ");
        String tempMessage = "";

        if ( splitCmd.length < 2) // Command doesn't supply required arguments
            throw new IllegalArgumentException();

        commandName = splitCmd[0]; // Index 1: Command Name

        if ( splitCmd[1].contains("userlvl=") ){ // Command has optional userlvl requirements

            try { // TODO Check for <target> somewhere in here, set flag to true
                userLevel = splitCmd[1].charAt(8);
                if (userLevel != 'a' && userLevel != 'm') // Incorect value for userlvl
                    throw new IllegalArgumentException();
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

        if (tempMessage.contains("<target>")) {
            requiresTarget = true;
        } else {
            requiresTarget = false;
        }

        response = tempMessage;

    } // End of: parseCommand(String cmd)

    public void run() { // TODO don't allow commands that need a target
        // callCommand(sender, target, channel)
        parentHandler.sendScheduledMessage( callCommand(scheduledSender, null, scheduledChannel) );
    }

    public String getName(){
        return commandName;
    }

    public char getReqLevel(){
        return userLevel;
    }

    public int getUseCount(){
        return useCount;
    }

    public boolean requiresTargetParam(){
        return requiresTarget;
    }

    public void setParentHandler(CommandHandler ch) {
        parentHandler = ch;
    }
}