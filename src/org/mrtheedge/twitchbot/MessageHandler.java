package org.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 1/7/2016.
 */
public class MessageHandler {

    private CommandHandler commandHandler;
    private SpamHandler spamHandler;
    private MainController parentController;

    public MessageHandler(CommandHandler ch, SpamHandler sh){
        this.commandHandler = ch;
        this.spamHandler = sh;

        this.commandHandler.setParentMessageHandler(this);
    }

    public ChatMessage handle(ChatMessage c){

        if (c.getMessage().startsWith("!")){
            return commandHandler.parse(c);
        } else if (spamHandler.checkMessage(c.getMessage())) {
            return new ChatMessage(c.getChannel(), c.getSender(), null, null, null);
        }
        return null; // A null instead of message means nothing was wrong.
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
