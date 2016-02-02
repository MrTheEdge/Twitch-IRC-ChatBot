package com.mrtheedge.twitchbot;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by E.J. Schroeder on 1/7/2016.
 */
public class MainController implements Runnable {

    private LinkedBlockingQueue<ChatMessage> inboundQueue;
    private LinkedBlockingQueue<ChatMessage> outboundQueue;
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

    public void setChatBot(ChatBot cb){ //TODO FIX THIS RIDICULOUS CODE
        this.chatBot = cb;
        //messageHandler.setAdmin(cb.getTwitchChannel().substring(1, cb.getTwitchChannel().length()));
    }

    public void addMod(String user){
        messageHandler.addMod(user);
    }

    public void removeMod(String user){
        messageHandler.removeMod(user);
    }

    public void shutdown(){

        messageHandler.shutdown();

        this.RUNNING = false;
        chatBot.disconnect();
        chatBot.dispose();

    }

    private void handleInboundMessages(){
        //System.out.println("Handling inbound messages.");
        ChatMessage msg;
        while(!inboundQueue.isEmpty()){
            msg = inboundQueue.remove();
            msg = messageHandler.handle(msg);

            if (msg == null)
                return;

            if (msg.getMessage() == null){ // Null message means it was spam, or disconnect
                if (msg.getChannel() == null){ // Everything is null in a disconnect message.
                    shutdown();
                } else {
                    chatBot.timeoutUser(msg.getSender());
                }
            } else {
                addOutboundMessage(msg);
            }
        }
    }

    private void handleOutboundMessages() {
        //System.out.println("Handling outbound messages.");
        ChatMessage msg;
        while(!outboundQueue.isEmpty()){
            msg = outboundQueue.remove();
            chatBot.sendMessage(msg.getChannel(), msg.getMessage());
        }
    }

    private void sendMessage(ChatMessage cm){
        chatBot.sendMessage(cm.getChannel(), cm.getMessage());
    }

    public void connectToIrc(String user, String oauth, String channel){

        if (channel.startsWith("#")){
            channel = channel.substring(1, channel.length());
        }

        try {
            chatBot.setBotname(user);
            chatBot.setChannel("#" + channel);
            chatBot.connect("irc.twitch.tv", 6667, oauth);
            chatBot.setVerbose(true);
            messageHandler.setAdmin(channel);
        } catch (IOException e) {
            System.out.println("The server was not found. Exit application and try again.");
            //e.printStackTrace();
        } catch( NickAlreadyInUseException e){
            System.out.println("Nickname is already in use. Try looking at the config file,");
            System.out.println("making the appropriate changes, and restarting the program.");
            //e.printStackTrace();
        } catch (IrcException e) {
            System.out.println("The server did not accept the connection. Make sure that the");
            System.out.println("OAuth token is correct. Restart and try again.");
            //e.printStackTrace();
        }

        mainControllerThread = new Thread(this);
        mainControllerThread.start();

    }

    @Override
    public void run() {

        while(RUNNING){

            if (!inboundQueue.isEmpty()){
                handleInboundMessages();
            }

            if (!outboundQueue.isEmpty()){
                handleOutboundMessages();
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
