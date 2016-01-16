package org.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 10/31/2015.
 */
public class Constants {

    public static final char ADMIN = 'a';
    public static final char MODERATOR = 'm';
    public static final char DEFAULT = 'd';

    public static final String ADD_PREFIX = "add";
    public static final String DEL_PREFIX = "del";
    public static final String SCHEDULE_PREFIX = "schedule";
    public static final String STOP_PREFIX = "stop";
    public static final String DISCONNECT = "disconnect";

    public static final String userVar = "<user>";
    public static final String targetUser = "<target>";
    public static final String uptimeVar = "<uptime>";
    public static final String useCountVar = "<usecount>";

    private Constants(){
        throw new AssertionError();
    }

}
