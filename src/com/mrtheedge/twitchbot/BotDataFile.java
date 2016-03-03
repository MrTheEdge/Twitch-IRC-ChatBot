package com.mrtheedge.twitchbot;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by E.J. Schroeder on 2/29/2016.
 */
public class BotDataFile {

    private JsonObject mainJsonObject;
    private Gson gson;

    private final String COMMAND_ARRAY_ID = "commands";
    private final String SPAM_HANDLER_ID = "spam_config";
    private final String FILE_NAME = "bot_data.dat";
    private final String OAUTH_TOKEN = "oauth";
    private final String BOT_NAME = "bot_name";
    private final String CHANNEL_NAME = "channel";

    // Holds values after a file has been read.
    private List<CustomCommand> cmdListFromFile;
    private SpamHandler spamHandlerFromFile;
    private String botName;
    private String oauth;
    private String channel;


    public BotDataFile(){
        gson = new GsonBuilder().setPrettyPrinting().create();
        mainJsonObject = new JsonObject();
    }

    public void read() throws IOException {

        File file = new File(FILE_NAME);
        BufferedReader reader = new BufferedReader(new FileReader(file)); // FileNotFoundException

        JsonParser jsonParser = new JsonParser();
        String json = readAll(reader);

        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

        try {
            botName = jsonObject.getAsJsonPrimitive(BOT_NAME).getAsString();
            oauth = jsonObject.getAsJsonPrimitive(OAUTH_TOKEN).getAsString();
            channel = jsonObject.getAsJsonPrimitive(CHANNEL_NAME).getAsString();
        } catch (NullPointerException ex){
            // Some items were not serialized on save.
            botName = null;
            oauth = null;
            channel = null;
        }

        JsonArray commandArray = jsonObject.getAsJsonArray(COMMAND_ARRAY_ID);
        cmdListFromFile = commandArrayToList(commandArray);

        try {
            JsonObject spamHandlerObj = jsonObject.getAsJsonObject(SPAM_HANDLER_ID);
            spamHandlerFromFile = spamHandlerFromJson(spamHandlerObj);
        } catch (ClassCastException ex){
            // spamHandler was saved as null, ignore it
            spamHandlerFromFile = null;
        }

    }

    private SpamHandler spamHandlerFromJson(JsonObject spamHandlerObj) {

        return gson.fromJson(spamHandlerObj, SpamHandler.class);

    }

    private List<CustomCommand> commandArrayToList(JsonArray commandArray) {
        List<CustomCommand> cmdList = new ArrayList<>();
        for(JsonElement element : commandArray){
            cmdList.add(gson.fromJson(element, CustomCommand.class));
        }
        return cmdList;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public void write(){
        File dataFile = new File(FILE_NAME);

        try (PrintWriter writer = new PrintWriter(dataFile)) {

            String json = gson.toJson(mainJsonObject);
            writer.write(json);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public BotDataFile addSpamHandler(SpamHandler sh){

        mainJsonObject.add(SPAM_HANDLER_ID, gson.toJsonTree(sh, SpamHandler.class));

        return this;

    }

    public BotDataFile addCommandList(List<CustomCommand> cmdList){

        JsonArray cmdJsonArray = new JsonArray();

        for (CustomCommand cmd : cmdList){
            cmdJsonArray.add(gson.toJsonTree(cmd, CustomCommand.class));
        }

        mainJsonObject.add(COMMAND_ARRAY_ID, cmdJsonArray);

        return this;

    }

    public BotDataFile addLoginData(String botName, String oAuthToken, String channelName) {

        mainJsonObject.addProperty(BOT_NAME, botName);
        mainJsonObject.addProperty(OAUTH_TOKEN, oAuthToken);
        mainJsonObject.addProperty(CHANNEL_NAME, channelName);

        return this;
    }

    public Optional<String> getBotName(){
        return Optional.ofNullable(botName);
    }

    public Optional<String> getOauth(){
        return Optional.ofNullable(oauth);
    }

    public Optional<String> getChannelName(){
        return Optional.ofNullable(channel);
    }

    public Optional<List<CustomCommand>> getCommandList(){
        return Optional.ofNullable(cmdListFromFile);
    }

    public Optional<SpamHandler> getSpamHandler(){
        return Optional.ofNullable(spamHandlerFromFile);
    }

}
