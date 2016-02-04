package com.mrtheedge.twitchbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.joda.time.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by E.J. Schroeder on 9/18/2015.
 */
public class Uptime {

    private static final String JSON_URL = "https://api.twitch.tv/kraken/streams/";
    private static JSONParser parser = new JSONParser();

    public static String getUptime(String channel) {
        try {

            String jsonResult = parseJSONTime(readUrl(JSON_URL + channel));

            if (jsonResult.charAt(0) == '[')
                return jsonResult;
            else {
                DateTime dt = new DateTime(jsonResult);
                Duration uptime = new Interval(dt, new DateTime()).toDuration();
                long totalMins = uptime.getStandardMinutes();

                long hours = totalMins / 60;
                long mins = totalMins % 60;

                if (hours == 0)
                    return mins + " minutes";

                return hours + " hours, " + mins + " minutes";
            }

        } catch (Exception ex) {
            return "[Error Retrieving Data: URL]";
        }

    }

    private static String parseJSONTime(String file){
        try {

            Object obj = parser.parse(file);
            JSONObject jobj = (JSONObject)obj;
            //System.out.println(jobj);
            if ( jobj.get("stream") == null ){
                return "[Stream is not live]";
            } else {
                obj = parser.parse(jobj.get("stream").toString());
                jobj = (JSONObject)obj;
                return jobj.get("created_at").toString();
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return "[Error Retrieving Data: JSON Error]";
        }

    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ( (read=reader.read(chars)) != -1 )
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }


}
