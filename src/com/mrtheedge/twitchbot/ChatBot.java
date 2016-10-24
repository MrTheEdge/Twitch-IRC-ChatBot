package com.mrtheedge.twitchbot;

import org.jibble.pircbot.PircBot;

/**
 * Created by E.J. Schroeder on 9/12/2015.
 */
public class ChatBot extends PircBot {

    private BotController botController;
    private String channel;


    public ChatBot (BotController botController){
        this.botController = botController;
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

        // TODO Fill out onUnkown so that we can get the information from tags. Un-comment to enable tags!!!
        //sendRawLine("CAP REQ :twitch.tv/tags");

        this.joinChannel(channel);
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
//        System.out.println("Channel: " + channel);
//        System.out.println("Sender: " + sender);
//        System.out.println("Login: " + login);
//        System.out.println("Hostname: " + hostname);
//        System.out.println("Message: " + message);
        botController.addInboundMessage( new ChatMessage(channel, sender, login, hostname, message) );

    }

    protected void onUnknown(String line){
        /*
            Requesting tags from the IRC server allows us to view many useful attributes of the sending user. PircBot is
            not able to recognize the full message, so it must be parsed manually.

            Set temporary tags for each attribute then create a new user object and pass it to onMessage. Later on use
            it to further customize what users get effected by what.
         */
        // @color=#0000FF;display-name=MrTheEdge95;emotes=;mod=1;room-id=18599683;subscriber=0;turbo=0;user-id=36887921;user-type=mod :mrtheedge95!mrtheedge95@mrtheedge95.tmi.twitch.tv PRIVMSG #wysocereal :hello

        /*
            splitLine[0] -> tags
            splitLine[1] -> server info (':' prefix)
            splitLine[2] -> message type
            splitLine[3] -> channel
            splitLine[4] -> message (':' prefix)
         */
        String[] splitLine = line.split(" ");
        String tag;
        String messageType = splitLine[2];

        switch (messageType){
            case "PRIVMSG":
                break;
            case "USERSTATE":
                break;
            case "GLOBALUSERSTATE":
                break;
            case "ROOMSTATE":
                break;
            case "USERNOTICE":
                break;
            case "CLEARCHAT":
                break;
            default:
        }


        /*switch(tag){
            case "color":
            case "display-name":
            case "emotes":
            case "mod":
            case "room-id":
            case "subscriber":
            case "turbo":
            case "user-id":
            case "user-type":
        }*/

        // TODO Finish

        //System.out.println(line);
    }

    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        //System.out.println("Target Nick: " + targetNick);
        //System.out.println("Mode: " + mode);
        String[] modeChange = mode.split(" ");
        if (modeChange.length == 3){
            if (modeChange[1].equals("+o")){
                botController.addMod( modeChange[2].trim() );
            } else if (modeChange[1].equals("-o")){
                botController.removeMod( modeChange[2].trim() );
            }
        }
    }

    protected void timeoutUser(String user){
        sendMessage(channel, ".timeout " + user + " 1");
    }

}

