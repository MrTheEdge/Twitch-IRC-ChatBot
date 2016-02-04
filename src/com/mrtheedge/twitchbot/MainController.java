package com.mrtheedge.twitchbot;

import org.jibble.pircbot.IrcException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by E.J. Schroeder on 1/7/2016.
 */
public class MainController implements Runnable {

    private LinkedBlockingQueue<ChatMessage> inboundQueue;
    private LinkedBlockingQueue<ChatMessage> outboundQueue;
    private List<ChatEventListener> _LISTENERS = new ArrayList<>();
    private ChatBot chatBot;
    private MessageHandler messageHandler;
    private UIController uiController;
    private Thread mainControllerThread;
    private volatile boolean RUNNING = true;

    public MainController(MessageHandler mh){
        inboundQueue = new LinkedBlockingQueue<>();
        outboundQueue = new LinkedBlockingQueue<>();
        messageHandler = mh;
        messageHandler.setParentController(this);
    }

    public synchronized void addListener(ChatEventListener listener){
        _LISTENERS.add(listener);
    }

    public synchronized void removeListener(ChatEventListener listener){
        _LISTENERS.remove(listener);
    }

    private synchronized void fireEvent(String message){
        ChatEvent e = new ChatEvent(this, message);
        for(ChatEventListener listener : _LISTENERS)
            listener.handleChatEvent(e);
    }

    public void addInboundMessage(ChatMessage cm){
        try {
            inboundQueue.put(cm);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addOutboundMessage(ChatMessage cm){ // TODO Get rid of outbound queue
        try{
            outboundQueue.put(cm);
        } catch (InterruptedException e){
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

    public void shutdown(){

        fireEvent("ChatBot disconnecting from irc.twitch.tv down");
        messageHandler.shutdown();

        this.RUNNING = false;
        chatBot.disconnect();
        chatBot.dispose();

    }

    private void handleInboundMessages(){
        //System.out.println("Handling inbound messages.");
        Optional result;
        ChatMessage msg;
        while(!inboundQueue.isEmpty()){
            msg = inboundQueue.remove();
            result = messageHandler.handle(msg);

            if (result.isPresent()){

                msg = (ChatMessage)result.get();
                if (msg.getMessage() == null){ // Null message means it was spam
                    chatBot.timeoutUser( msg.getSender() );
                    fireEvent( msg.getSender() + "'s message was deleted for spam.");
                } else {
                    sendMessage(msg);
                }

            } else {
                return;
            }

        }
    }

    private void sendMessage(ChatMessage cm){
        chatBot.sendMessage(cm.getChannel(), cm.getMessage());
    }

    public void connectToIrc(String user, String oauth, String channel) {
        fireEvent("Starting ChatBot...");
        if (channel.startsWith("#")){
            channel = channel.substring(1, channel.length());
        }

        chatBot.setBotname(user);
        chatBot.setChannel("#" + channel);
        chatBot.setVerbose(true); // TODO Debug flag
        try {
            chatBot.connect("irc.twitch.tv", 6667, oauth);
        } catch (IOException e) {
            fireEvent("Unable to connect to irc.twitch.tv.");
        } catch (IrcException e) {
            fireEvent("Error while connecting to irc.twitch.tv, username or password may be incorrect");
            e.printStackTrace();
        }
        messageHandler.setAdmin(channel);

        mainControllerThread = new Thread(this);
        mainControllerThread.start();
        fireEvent( "ChatBot connected to twitch.tv on channel #" + channel);

    }

    @Override
    public void run() {

        while(RUNNING){

            if (!inboundQueue.isEmpty()){
                handleInboundMessages();
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
