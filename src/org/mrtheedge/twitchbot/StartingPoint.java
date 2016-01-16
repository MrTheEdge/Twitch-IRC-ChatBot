package org.mrtheedge.twitchbot;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import java.io.IOException;

/**
 * Created by E.J. Schroeder on 9/12/2015.
 * Twitch Chat Bot v0.5 beta
 * Some features are yet to be implemented, and some may be broken
 * or lacking functionality.
 */
public class StartingPoint {

    // TODO Change config template in ConfigurationReader. It's out of date.

    private static boolean _DEBUG = true;

    public static void main(String[] args){
        ConfigurationReader configReader = new ConfigurationReader("config.txt");

        SpamHandler spamHandler = new SpamHandler();
        CommandHandler cmdHandler = new CommandHandler();
        MessageHandler msgHandler = new MessageHandler(cmdHandler, spamHandler);

        MainController mainCtrl = new MainController(msgHandler);
        ChatBot cb = new ChatBot(mainCtrl);

        applyConfig(configReader);
        connectBot(cb, configReader);

        mainCtrl.setChatBot(cb); // TODO This is here because setChatBot uses the channel value that is set in conn. Fix it later.

        Thread mainCtrlThread = new Thread(mainCtrl);

        mainCtrlThread.start();

    }

    public static void connectBot(ChatBot cb, ConfigurationReader configReader){
        cb.setTwitchBotname(configReader.getValue("botname"));
        cb.setTwitchChannel("#" + configReader.getValue("channel"));

        try {
            if (_DEBUG)
                cb.setVerbose(true);

            cb.connect("irc.twitch.tv", 6667, configReader.getValue("pass"));

        } catch( IOException ex) {
            System.out.println("The server was not found. Exit application and try again.");
            //ex.printStackTrace();
        } catch( NickAlreadyInUseException ex){
            System.out.println("Nickname is already in use. Try looking at the config file,");
            System.out.println("making the appropriate changes, and restarting the program.");
            //ex.printStackTrace();
        } catch( IrcException ex){
            System.out.println("The server did not accept the connection. Make sure that the");
            System.out.println("OAuth token is correct. Restart and try again.");
            //ex.printStackTrace();
        }
    }

    public static void applyConfig(ConfigurationReader configReader){
        if ( configReader.getValue("botname") == null){
            System.out.println("Botname not set in 'config.txt'. Make appropriate changes and restart.");
            System.exit(0); // Exits program if bot name is not set correctly in config.txt
        }

        if (configReader.getValue("pass") == null) {
            System.out.println("OAuth Token not set in 'config.txt'. Refer to file for more information.");
            System.exit(0);
        }

        if (configReader.getValue("channel") == null){
            System.out.println("No channel set in 'config.txt'. Add a channel for the bot to join, then restart.");
            System.exit(0);
        }
    }

}
