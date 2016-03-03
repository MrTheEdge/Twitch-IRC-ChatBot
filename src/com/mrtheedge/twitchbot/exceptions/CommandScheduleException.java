package com.mrtheedge.twitchbot.exceptions;

/**
 * Created by E.J. Schroeder on 3/2/2016.
 */
public class CommandScheduleException extends ParsingException {

    public CommandScheduleException(){

    }

    public CommandScheduleException(String message){
        super(message);
    }

}
