package com.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 1/6/2016.
 */
public class ChatMessage {

    private String sender;
    private String channel;
    private String login;
    private String hostname;
    private String message;

    private boolean isSpam = false;
    private SpamType spamType;

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

    public void setSpam(SpamType type){
        isSpam = true;
        spamType = type;
    }

    public boolean isSpam(){
        return isSpam;
    }

    public SpamType getSpamType(){
        if (isSpam){
            return spamType;
        }
        return null;
    }
}

enum SpamType {
    CONSEC_CHARS("Consecutive Characters"),
    CAPS("Capital Letters"),
    REPETITION("Word Repetition"),
    LENGTH("Word Length"),
    LINK("Link");

    String description;
    SpamType(String description){
        this.description = description;
    }

    public String toString(){
        return description;
    }
}