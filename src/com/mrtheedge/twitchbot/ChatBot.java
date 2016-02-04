package com.mrtheedge.twitchbot;

import org.jibble.pircbot.PircBot;

/**
 * Created by E.J. Schroeder on 9/12/2015.
 */
public class ChatBot extends PircBot {

    private MainController mainController;
    private boolean _DEBUG = true;
    private String channel;


    public ChatBot (MainController mainController){
        this.mainController = mainController;
    }

    public void setBotname(String botname){
        setName(botname);
    }

    public void setChannel(String channel){
        this.channel = channel;
    }

    public String getChannel(){
        return this.channel;
    }

    @Override
    protected void onConnect() {
        // Request membership to get mod information.
        sendRawLine("CAP REQ :twitch.tv/membership");

        this.joinChannel(channel);
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {

        mainController.addInboundMessage( new ChatMessage(channel, sender, login, hostname, message) );

    }

    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        //System.out.println("Target Nick: " + targetNick);
        //System.out.println("Mode: " + mode);
        String[] modeChange = mode.split(" ");
        if (modeChange.length == 3){
            if (modeChange[1].equals("+o")){
                mainController.addMod( modeChange[2].trim() );
            } else if (modeChange[1].equals("-o")){
                mainController.removeMod( modeChange[2].trim() );
            }
        }
    }

    protected void timeoutUser(String user){
        sendMessage(channel, ".timeout " + user + " 1");
    }

}

