package com.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 9/12/2015.
 *
 * Checks strings for characteristics like repeated characters, to many capitals, word length, etc
 * If they exceed the values that are set, it marks it as spam.
 */
public class SpamHandler {

    // Add check for duplicate messages within a time limit

    private int WORD_LENGTH = 15;
    private int CONSEC_CHARS = 10;
    private int WORD_REPETITION = 3;
    private int MIN_WORD_LENGTH = 10; // Only used when determining caps percentage
    private double PERCENTAGE_CAPS = 0.75;

    private boolean CHECK_WORD_LENGTH = true;
    private boolean CHECK_CONSEC_CHARS = true;
    private boolean CHECK_WORD_REPETITION = true;
    private boolean CHECK_PERCENTAGE_CAPS = true;

    public SpamHandler(){

    }

    public boolean checkMessage(String message){

        if ( CHECK_WORD_LENGTH && checkWordLength(message) ){ return true;}
        if ( CHECK_CONSEC_CHARS && checkConsecChars(message) ){ return true; }
        if ( CHECK_WORD_REPETITION && checkWordRepetition(message) ){ return true; }
        if ( CHECK_PERCENTAGE_CAPS && checkPercentageCaps(message) ){ return true; }

        return false;
    }

    private boolean checkWordLength(String message){

        int c = 0; // Counter for word length
        int lw = 0; // longest word counter
        for (int i = 0; i < message.length(); i++){
            if (message.charAt(i) == ' '){
                c = 0;
            } else {
                c++;
                if ( c > lw)
                    lw = c;
            }
        }

        return (lw > WORD_LENGTH); // If longest word is longer than accepted word length, return true for spam
    }

    private boolean checkConsecChars(String message){

        char prevChar, temp;
        int consecCount = 0;

        if (message.length() > 2){
            prevChar = Character.toLowerCase(message.charAt(0)); // characters converted to lowercase for consistency.

            for (int i = 1; i < message.length(); i++){
                // Assign current character to temp variable
                temp = Character.toLowerCase(message.charAt(i));
                if ( temp == prevChar){ // check if current char is the same as previous
                    consecCount++;
                    if (consecCount > CONSEC_CHARS)
                        return true;
                } else {
                    // If no consecutive character is found, reset count to 0
                    consecCount = 0;
                }
                // Current character is now previous character
                prevChar = temp;
            }

            return false;

        } else {
            return false;
        }

    }

    private boolean checkWordRepetition(String message){
        String[] messageArray = message.split(" ");
        int c = 0;

        if (messageArray.length > 1) {
            for (int i = 1; i < messageArray.length; i++){
                if ( messageArray[i].toLowerCase().equals( messageArray[i-1].toLowerCase() ) ){
                    c++;
                    if (c > WORD_REPETITION)
                        return true;
                } else {
                    c = 0;
                }
            }
        } else {
            return false;
        }

        return false;
    }

    private boolean checkPercentageCaps(String message){

        int msgLength = message.length();
        int capsCount = 0;
        double percentage;

        if ( msgLength > MIN_WORD_LENGTH){

            for (int i = 0; i < msgLength; i++){ // Counting number of capital characters
                if ( Character.isUpperCase( message.charAt(i) )){
                    capsCount++;
                }
            }

            percentage = ((double) capsCount) / msgLength; // Calculate caps percentage

            if (percentage > PERCENTAGE_CAPS)
                return true;
            else
                return false;

        } else {
            return false;
        }

    }

    public void setWordLength(int word_length) {
        this.WORD_LENGTH = word_length;
    }

    public void setConsecChars(int consec_chars) {
        this.CONSEC_CHARS = consec_chars;
    }

    public void setWordRepetition(int word_repetition) {
        this.WORD_REPETITION = word_repetition;
    }

    public void setPercentageCaps(double percentage_caps) {
        if (percentage_caps > 1){
            percentage_caps = percentage_caps / 100;
        }
        this.PERCENTAGE_CAPS = percentage_caps;
    }

    public void setMinWordLength(int min_word_length){
        this.MIN_WORD_LENGTH = min_word_length;
    }

    public double getPercentageCaps() {
        return PERCENTAGE_CAPS;
    }

    public int getMinWordLength() {
        return MIN_WORD_LENGTH;
    }

    public int getWordRepetition() {
        return WORD_REPETITION;
    }

    public int getConsecChars() {
        return CONSEC_CHARS;
    }

    public int getWordLength() {
        return WORD_LENGTH;
    }

    public void setCheckWordLength(boolean checkWordLength) {
        this.CHECK_WORD_LENGTH = checkWordLength;
    }

    public void setCheckConsecChars(boolean checkConsecChars) {
        this.CHECK_CONSEC_CHARS = checkConsecChars;
    }

    public void setCheckWordRepetition(boolean checkWordRepetition) {
        this.CHECK_WORD_REPETITION = checkWordRepetition;
    }

    public void setCheckPercentageCaps(boolean checkPercentageCaps) {
        this.CHECK_PERCENTAGE_CAPS = checkPercentageCaps;
    }

}