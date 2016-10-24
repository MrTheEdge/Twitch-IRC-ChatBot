package com.mrtheedge.twitchbot;

import com.mrtheedge.twitchbot.event.LogEvent;
import com.mrtheedge.twitchbot.event.LogEventListener;
import com.mrtheedge.twitchbot.exceptions.ParsingException;
import org.jibble.pircbot.IrcException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by E.J. Schroeder on 1/7/2016.
 */
public class BotController implements Runnable {

    private ChatBot chatBot;
    private MessageHandler messageHandler;

    private LinkedBlockingQueue<ChatMessage> inboundQueue;
    private List<LogEventListener> _LISTENERS = new ArrayList<>();

    private Thread mainControllerThread;
    private volatile boolean RUNNING = true;
    private boolean hasPreviouslyConnected = false;
    private boolean _DEBUG = true;
    private String username;
    private String oAuthToken;

    public BotController(MessageHandler mh){
        inboundQueue = new LinkedBlockingQueue<>();
        messageHandler = mh;
        messageHandler.setParentController(this);
    }

    public synchronized void addListener(LogEventListener listener){
        _LISTENERS.add(listener);
    }

    public synchronized void removeListener(LogEventListener listener){
        _LISTENERS.remove(listener);
    }

    private synchronized void fireEvent(String message){
        LogEvent e = new LogEvent(this, message);
        for(LogEventListener listener : _LISTENERS)
            listener.handle(e);
    }

    public void addInboundMessage(ChatMessage cm){
        try {
            inboundQueue.put(cm);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopController(){
        RUNNING = false;
    }

    public void setChatBot(ChatBot cb){
        this.chatBot = cb;
    }

    public void addMod(String user){
        messageHandler.addMod(user);
        fireEvent( user + " was added as a mod");
    }

    public void removeMod(String user){
        messageHandler.removeMod(user);
        fireEvent( user + " was removed as a mod");
    }

    public void disconnect(){
        fireEvent("ChatBot disconnecting from irc.twitch.tv down");
        messageHandler.shutdown();
        this.RUNNING = false;
        chatBot.disconnect();
    }

    public void shutdown(){

        disconnect();
        if (hasPreviouslyConnected)
            chatBot.dispose();

    }

    private void handleInboundMessages(){
        //System.out.println("Handling inbound messages.");
        Optional result;
        ChatMessage msg;
        while(!inboundQueue.isEmpty()){
            msg = inboundQueue.remove();

            try {
                result = messageHandler.handle(msg);
                handleResultFromParsing(result);
            } catch (ParsingException e) {
                e.printStackTrace();
            }

        }
    }

    private void handleResultFromParsing(Optional result) {
        if (result.isPresent()){

            ChatMessage msg = (ChatMessage) result.get();
            if ( msg.isSpam() ){

                // Generate a custom spam response for a specific sender and sends a timeout
                chatBot.timeoutUser( msg.getSender() );
                String spamResponse = msg.getSender() + " -> " + getSpamResponse(msg.getSpamType());
                sendMessage(new ChatMessage(msg.getChannel(), null, null, null, spamResponse));

                // Fire an event for the log.
                fireEvent( msg.getSender() + "'s message was deleted for spam (" + msg.getSpamType() + "). " + msg.getMessage());

            } else {
                sendMessage(msg);
            }

        }
    }

    private String getSpamResponse(SpamType spamType) {

        switch(spamType){

            case CONSEC_CHARS:
                return Constants.CONSEC_CHARS_SPAM_RESPONSE;
            case CAPS:
                return Constants.CAPS_SPAM_RESPONSE;
            case REPETITION:
                return Constants.REPETITION_SPAM_RESPONSE;
            case LENGTH:
                return Constants.WORD_LENGTH_SPAM_RESPONSE;
            case LINK:
                return Constants.LINK_SPAM_RESPONSE;
            default:
                return "Warning: That was spam.";
        }
    }

    public void sendMessage(ChatMessage cm){
        chatBot.sendMessage(cm.getChannel(), cm.getMessage());
    }

    private void reconnectToIrc(){
        try {
            fireEvent("Reconnecting ChatBot...");

            if (mainControllerThread.isAlive()){
                fireEvent("Unable to reconnect. Try again later or restart the application.");
                return;
            }

            RUNNING = true;
            mainControllerThread = new Thread(this);
            mainControllerThread.start();
            chatBot.reconnect();
            chatBot.joinChannel(chatBot.getChannel());
            fireEvent("ChatBot successfully reconnected to channel " + chatBot.getChannel());
        } catch (IOException e) {
            fireEvent("Unable to reconnect to irc.twitch.tv.");
        } catch (IrcException e) {
            fireEvent("Error while reconnecting to irc.twitch.tv.");
        }
    }

    public void connectToIrc(String channel) {
        fireEvent("Starting ChatBot...");
        if (channel.startsWith("#")){
            channel = channel.substring(1, channel.length());
        }
        if (username == null || oAuthToken == null){
            fireEvent("Unable to connect, no login information. Please login through Twitch.");
            return;
        }

        if (hasPreviouslyConnected){
            reconnectToIrc();
        } else {
            chatBot.setBotname(this.username);
            chatBot.setChannel("#" + channel);
            if (_DEBUG) chatBot.setVerbose(true);
            try {

                chatBot.connect("irc.chat.twitch.tv", 6667, this.oAuthToken);
                messageHandler.setAdmin(channel);

                mainControllerThread = new Thread(this);
                mainControllerThread.start();
                hasPreviouslyConnected = true;
                fireEvent("ChatBot connected to twitch.tv on channel #" + channel);

            } catch (IOException e) {
                fireEvent("Unable to connect to irc.twitch.tv.");
            } catch (IrcException e) {
                fireEvent("Error while connecting to irc.twitch.tv, username or password may be incorrect");
            }

        }
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setOAuthToken(String token){
        if (!token.startsWith("oauth:"))
            token = "oauth:" + token;

        this.oAuthToken = token;
    }

    public void enableDebugMessages(){
        _DEBUG = true;
    }

    @Override
    public void run() {

        while(RUNNING){

            if (!inboundQueue.isEmpty())
                handleInboundMessages();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
