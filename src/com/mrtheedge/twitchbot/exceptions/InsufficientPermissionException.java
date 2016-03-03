package com.mrtheedge.twitchbot.exceptions;

/**
 * Created by E.J. Schroeder on 3/2/2016.
 */
public class InsufficientPermissionException extends ParsingException {

    public InsufficientPermissionException(){
    }

    public InsufficientPermissionException(String message){
        super(message);
    }

}
