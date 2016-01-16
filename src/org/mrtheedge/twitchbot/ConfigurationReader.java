package org.mrtheedge.twitchbot; /**
 * Created by E.J. Schroeder on 9/12/2015.
 */

import java.io.*;
import java.util.HashMap;

public class ConfigurationReader {

    private File configFile;
    private BufferedReader fileReader;
    private HashMap<String, String> kvList;

    public ConfigurationReader(String fileName){

        kvList = new HashMap<>();
        try {
            configFile = new File(fileName);
            fileReader = new BufferedReader(new FileReader(configFile));
        } catch(IOException ex){
            // Assume no file in directory, so create one;
            this.createConfig("config.txt");
        }
        this.readFile();

    }

    public String getValue(String key){
        return kvList.get(key);
    }

    public void readFile() {
        String ln;
        String[] lnArray;

        try {
            while ((ln = fileReader.readLine()) != null) {

                // Trim line to check if line is empty
                ln = ln.trim();
                if (ln.equals(""))
                    continue;

                if (ln.charAt(0) == '/' || ln.charAt(0) == '#')
                    continue; //  '/' & '#' are comment characters

                // If not empty, split on equal sign
                lnArray = ln.split("=");
                // Trim each array value for consistency
                for (int i = 0; i < lnArray.length; i++) {
                    lnArray[i] = lnArray[i].trim();
                }
                if (lnArray.length == 2)
                    kvList.put(lnArray[0], lnArray[1]);
                // Since index zero is the setting Key, and index one is the setting Value, assign each to the hashmap
            }
            fileReader.close();

        } catch (IOException ex){
            System.out.println("This was never supposed to happen. What have you done!?");
            System.out.println("Error: Config file not found.");
            System.exit(0);
        }
    }

    private void createConfig(String filename){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename, "UTF-8");
            writer.print(CONFIG_FILE_TEXT);
            writer.close();
            fileReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if ( writer != null ) writer.close();
        }

    }


    private final String CONFIG_FILE_TEXT =
            "## --- Twitch IRC Chat Bot - Config File --- #\n" +
            "##       This config should be relatively forgiving, so extra spaces, and blank lines will be ignored.\n" +
            "##       All configurable properties have default values, so if something isn't working, double\n" +
            "##       check that the config file is correctly filled out.\n" +
            "\n" +
            "## - Basic Login Info\n" +
            "\n" +
            "## Username of the bot you want to use in your channel\n\n" +
            "user =\n" +
            "\n" +
            "## Password. This is an OAuth token.\n" +
            "## You can get one by linking the bot account here: http://twitchapps.com/tmi/\n" +
            "## It should look like this: oauth:some_key_here\n\n" +
            "pass =\n" +
            "\n" +
            "## - Built In Features\n" +
            "\n" +
            "spam-filter = on\n" +
            "\n" +
            "\n" +
            "## -- Spam Filter Configuration\n" +
            "##      This section will be ignored if spam filter is turned off.\n" +
            "\n" +
            "## Marks messages that have words longer than the specified length. This is to\n" +
            "##   prevent spam that is just long strings of characters. Default length is 15\n" +
            "##   characters. A space designates the beginning of a new word\n" +
            "\n" +
            "check_word_length = on\n" +
            "word_length = 15\n" +
            "\n" +
            "## Marks messages that have words containing too many matching consecutive characters.\n" +
            "##   Default value is 5 characters.\n" +
            "##   This prevents spam messages like: \"hhhhhhhhhhhhhh\" or \"Woooooooooooow\"\n" +
            "\n" +
            "check_consecutive_characters = on\n" +
            "consecutive_characters = 5\n" +
            "\n" +
            "## Marks messages that have to many duplicate consecutive words. This blocks simple\n" +
            "##   copy and paste spam attacks. Default number is 3 repeated words.\n" +
            "##  Example of allowed message: \"Spam spam spam that was just spam\"\n" +
            "##   Example of blocked message: \" Heres some spam spam spam spam\"\n" +
            "\n" +
            "check_word_repetition = on\n" +
            "word_repition = 3\n" +
            "\n" +
            "## Marks messages that have a capital letter percentage over the specified amount\n" +
            "##   Minimum word length is a pass for messages like \"LOL\" or \"HAHA\", allowing them\n" +
            "##   to bypass the capital letter check. Keep in mind, some twitch emojis have text\n" +
            "##   that is 50% capital letters or more. Some messages may be marked as spam for sending\n" +
            "##   an emoji that has to many capital letters.\n" +
            "##   The default percentage value is 0.50. Values higher than 1 or lower than 0 will be\n" +
            "##   ignored and the default value will replace them.\n" +
            "\n" +
            "check_percentage_caps = on\n" +
            "minimum_word_length = 5\n" +
            "percentage_caps = .50";

}
