package com.mrtheedge.twitchbot;

import com.google.gson.Gson;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by E.J. Schroeder on 2/4/2016.
 */
public class TwitchAPI {

    private static final String JSON_URL = "https://api.twitch.tv/kraken/streams/";
    private static Gson gson = new Gson();

    public static TwitchAPIResponse getStreamData(String channel) throws IOException{
        if (channel.startsWith("#"))
            channel = channel.substring(1);

        String url = JSON_URL + channel + "?client_id=" + Constants.CLIENT_ID;

        String jsonResponse = readUrl(url);
        TwitchAPIResponse response = gson.fromJson(jsonResponse, TwitchAPIResponse.class);

        return response;

    }

    public static String getChannelUptime(String channel){
        TwitchAPIResponse resp;
        try {
            resp = getStreamData(channel);
        } catch (IOException e) {
            return "[Error Fetching Data]";
        }

        if (resp.getStreamInfo() == null)
            return "[Not Live]";

        String createdAt = resp.getStreamInfo().getCreatedAt();

        long totalMins = dateTimeToMinutes(createdAt);

        long hours = totalMins / 60;
        long mins = totalMins % 60;

        if (hours == 0)
            return mins + " minutes";

        return hours + " hours, " + mins + " minutes";

    }

    private static long dateTimeToMinutes(String isoTime){
        DateTime dt = new DateTime(isoTime);
        Duration uptime = new Interval(dt, new DateTime()).toDuration();
        long totalMins = uptime.getStandardMinutes();

        return totalMins;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static String readUrl(String urlString) throws MalformedURLException, IOException {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            return readAll(reader);

        } finally {
            if (reader != null)
                reader.close();
        }
    }



}
