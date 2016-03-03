package com.mrtheedge.twitchbot.exceptions;

/**
 * Created by E.J. Schroeder on 3/2/2016.
 */
public class ParsingException extends Exception {
    public ParsingException(){

    }

    public ParsingException(String message){
        super(message);
    }
}
