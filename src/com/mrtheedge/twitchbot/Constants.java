package com.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 10/31/2015.
 */
public class Constants {

    public static final char ADMIN = 'A';
    public static final char MODERATOR = 'M';
    public static final char DEFAULT = 'D';

    public static final String ADD_PREFIX = "add";
    public static final String DEL_PREFIX = "del";
    public static final String SCHEDULE_PREFIX = "schedule";
    public static final String STOP_PREFIX = "stop";
    public static final String DISCONNECT = "disconnect";

    public static final String USER_VAR = "<user>";
    public static final String TARGET_USER = "<target>";
    public static final String UPTIME_VAR = "<uptime>";
    public static final String USE_COUNT_VAR = "<usecount>";

    public static final String WORD_LENGTH_SPAM_RESPONSE = "Warning: That was spam.";
    public static final String CONSEC_CHARS_SPAM_RESPONSE = "Warning: That was spam.";
    public static final String CAPS_SPAM_RESPONSE = "Warning: Too many caps!";
    public static final String REPETITION_SPAM_RESPONSE = "Warning: That was spam.";
    public static final String LINK_SPAM_RESPONSE = "Warning: Don't post links!";
    public static final String CLIENT_ID = "YOUR_CLIENT_ID_HERE";

    private Constants(){
        throw new AssertionError();
    }

}
