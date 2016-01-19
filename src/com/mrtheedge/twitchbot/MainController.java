package com.mrtheedge.twitchbot;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by E.J. Schroeder on 1/7/2016.
 */
public class MainController implements Runnable {

    private LinkedBlockingQueue<ChatMessage> inboundQueue;
    private LinkedBlockingQueue<ChatMessage> outboundQueue;
    private ChatBot chatBot;
    private MessageHandler messageHandler;
    private volatile boolean RUNNING = true;

    public MainController(MessageHandler mh){
        inboundQueue = new LinkedBlockingQueue<>();
        outboundQueue = new LinkedBlockingQueue<>();
        this.messageHandler = mh;
        this.messageHandler.setParentController(this);
    }

    public void addInboundMessage(ChatMessage cm){
        try {
            inboundQueue.put(cm);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addOutboundMessage(ChatMessage cm){
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
        messageHandler.setAdmin(cb.getTwitchChannel().substring(1, cb.getTwitchChannel().length()));
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
                    chatBot.timeoutUser(msg.getChannel(), msg.getSender());
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