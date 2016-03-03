package com.mrtheedge.twitchbot.exceptions;

/**
 * Created by E.J. Schroeder on 3/2/2016.
 */
public class InvalidSyntaxException extends ParsingException {

    public InvalidSyntaxException(){

    }

    public InvalidSyntaxException(String message) {
        super(message);
    }
}
