package com.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 2/26/2016.
 */
public class TwitchAPIResponse {

    private StreamData stream;
    private String error;
    private String message;
    private String status;

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public StreamData getStreamInfo(){
        return stream;
    }

}
