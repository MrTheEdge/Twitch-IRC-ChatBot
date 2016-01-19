package com.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 1/6/2016.
 */
public class ChatMessage {

    private static String prevChannel;

    private String sender;
    private String channel;
    private String login;
    private String hostname;
    private String message;

    public ChatMessage(String channel, String sender, String login, String hostname, String message){
        this.channel = channel;
        this.sender = sender;
        this.login = login;
        this.hostname = hostname;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getChannel() {
        return channel;
    }

    public String getLogin() {
        return login;
    }

    public String getHostname() {
        return hostname;
    }

    public String getMessage() {
        return message;
    }
}
