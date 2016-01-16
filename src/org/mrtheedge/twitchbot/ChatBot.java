package org.mrtheedge.twitchbot;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

import java.io.IOException;

/**
 * Created by E.J. Schroeder on 9/12/2015.
 */
public class ChatBot extends PircBot {

    private MainController mc;
    private boolean _DEBUG = true;

    private String channel;


    public ChatBot (MainController mc) {
        this.mc = mc;
    }

    public void setTwitchBotname(String botname) {
        setName(botname);
    }

    public void setTwitchChannel(String channel) {
        this.channel = channel;
    }

    public String getTwitchChannel(){
        return this.channel;
    }

    @Override
    protected void onConnect() {
        // Request membership to get mod information.
        sendRawLine("CAP REQ :twitch.tv/membership");

        this.joinChannel(this.channel);
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {

        mc.addInboundMessage( new ChatMessage(channel, sender, login, hostname, message) );

    }

    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        //System.out.println("Target Nick: " + targetNick);
        //System.out.println("Mode: " + mode);
        String[] modeChange = mode.split(" ");
        if (modeChange.length == 3){
            if (modeChange[1].equals("+o")){
                mc.addMod( modeChange[2].trim() );
            } else if (modeChange[1].equals("-o")){
                mc.removeMod( modeChange[2].trim() );
            }
        }
    }

    protected void timeoutUser(String channel, String user){
        sendMessage(channel, ".timeout " + user + " 1");
        sendMessage(channel, ".w " + user + " Watch it! That was spam. To much can get you muted or banned!");
    }

}

