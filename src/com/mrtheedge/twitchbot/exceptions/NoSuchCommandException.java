package com.mrtheedge.twitchbot.exceptions;

/**
 * Created by E.J. Schroeder on 3/2/2016.
 */
public class NoSuchCommandException extends ParsingException {

    public NoSuchCommandException(){

    }

    public NoSuchCommandException(String message){
        super(message);
    }

}
