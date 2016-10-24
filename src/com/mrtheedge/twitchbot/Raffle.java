package com.mrtheedge.twitchbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by E.J. Schroeder on 3/4/2016.
 */
public class Raffle {

    private Set<String> userEntries;
    private boolean isOpen;
    private boolean allowMultipleWins;
    private Random randomGen;

    private final String RANDOM_URL = "https://www.random.org/integers/?num=1&col=1&base=10&format=plain&rnd=new";

    public Raffle(){
        userEntries = new HashSet<>();
        randomGen = new Random();
        isOpen = true;
        allowMultipleWins = false;
    }

    public boolean addEntry(String user){

        if (isOpen) {
            userEntries.add(user);
            return true;
        }
        return false;
    }

    public void enableMultipleWins(){
        allowMultipleWins = true;
    }

    public void disableMultipleWins(){
        allowMultipleWins = false;
    }

    public void startNewRaffle(){
        userEntries.clear();
        openRaffle();
    }

    public void closeRaffle(){
        isOpen = false;
    }

    public void openRaffle(){
        isOpen = true;
    }

    public String drawWinner(){
        isOpen = false;
        int num = getRandomNumber(0, userEntries.size());

        String winner = userEntries.toArray(new String[1])[num];
        if (!allowMultipleWins)
            userEntries.remove(winner);
        return winner;
    }

    private int getRandomNumber(int low, int high) {

        // &min=0&max=100
        String queryStringParam = "&min=" + low + "&max=" + (high+1);

        BufferedReader reader;
        try {
            URL url = new URL(RANDOM_URL + queryStringParam);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String rand = reader.readLine();
            reader.close();
            //System.out.println("True Random!");
            return Integer.parseInt(rand);
        } catch (Exception e) {
            //System.out.println("Pseudo Random");
            return randomGen.nextInt(high) + low;
        }

    }


}
