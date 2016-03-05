package com.mrtheedge.twitchbot;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

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
    private Random randomGen;

    private final String RANDOM_URL = "https://www.random.org/integers/?num=1&min=0&max=100&col=1&base=10&format=plain&rnd=new";


    public Raffle(){
        userEntries = new HashSet<>();
        randomGen = new SecureRandom();
        isOpen = true;
    }

    public void addEntry(String user){
        if (isOpen)
            userEntries.add(user);
    }

    public void closeRaffle(){
        isOpen = false;
    }

    public void openRaffle(){
        isOpen = true;
    }

    public String drawWinner(){
        isOpen = false;
        int num = getTrueRandomNumber(0, userEntries.size());

        return (String)userEntries.toArray()[num];
    }

    private int getTrueRandomNumber(int low, int high) {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(RANDOM_URL);

        return randomGen.nextInt(high) + low;

    }


}
