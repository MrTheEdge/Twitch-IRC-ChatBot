package com.mrtheedge.twitchbot;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import sun.plugin2.message.Message;

public class UIController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextArea eventLogText;
    @FXML private Text logSaveOutput;
    @FXML private ListView<String> usersListView;
    @FXML private Spinner<Integer> chatToolTimeSpinner; // Initialize
    @FXML private TableView<Map.Entry<String, CustomCommand>> commandTableView;
    @FXML private TableColumn<Map.Entry<String, CustomCommand>, String> cmdNameTableCol;
    @FXML private TableColumn<Map.Entry<String, CustomCommand>, Character> cmdLvlTableCol;
    @FXML private TableColumn<Map.Entry<String, CustomCommand>, String> cmdTextTableCol;
    @FXML private TextField commandNameField;
    @FXML private ChoiceBox<Character> userLevelDropdown; // Initialize
    @FXML private TextField commandTextField;
    @FXML private CheckBox wordLengthCheckBox;
    @FXML private CheckBox consecCharsCheckBox;
    @FXML private CheckBox wordRepetitionCheckBox;
    @FXML private CheckBox capitalLettersCheckBox;
    @FXML private CheckBox blockLinksCheckBox;
    @FXML private Spinner<Integer> wordLengthSpinner; // Initialize
    @FXML private Spinner<Integer> repeatWordsSpinner; // Initialize
    @FXML private Spinner<Integer> consecCharSpinner; // Initialize
    @FXML private Spinner<Integer> percentCapsSpinner; // Initialize
    @FXML private Spinner<Integer> wordSizeSpinner; // Initialize
    @FXML private TextField botNameField;
    @FXML private TextField oAuthKeyField;
    @FXML private TextField channelField;
    @FXML private Text aboutText;
    @FXML private Text versionText;
    @FXML private Hyperlink sourceLink;
    @FXML private Hyperlink oAuthKeyLink;
    @FXML private ImageView connectedImage;
    @FXML private Text connectedText;

    private Image connectedImg;
    private Image disconnectedImg;

    private boolean botIsConnected = false;
    private MainController botController;
    private ChatBot chatBot;
    private SpamHandler spamHandler;
    private CommandHandler cmdHandler;
    private MessageHandler messageHandler;


    @FXML
    private void addCommand(ActionEvent event) {

    }

    @FXML
    private void banUser(ActionEvent event) {
        if (botIsConnected){

        }
    }

    @FXML
    private void botConnect(ActionEvent event) {
        if (!botIsConnected){
            String botName = botNameField.getText();
            String oauth = oAuthKeyField.getText();
            String channel = channelField.getText();
            // TODO Add error log to log pane
            if (botName.equals("") || oauth.equals("") || channel.equals("") ){
                return;
            } else {
                System.out.println(botName + "\n" + oauth + "\n" + channel);
                botController.connectToIrc(botName, oauth, channel);

                botIsConnected = true;
                connectedImage.setImage(connectedImg);
                connectedText.setText("Connected");
            }


        }
    }

    @FXML
    private void botDisconnect(ActionEvent event) {
        if (botIsConnected){
            botController.shutdown();

            botIsConnected = false;
            connectedImage.setImage(disconnectedImg);
            connectedText.setText("Not Connected");
        }
    }

    @FXML
    private void deleteCommand(ActionEvent event) {

    }

    @FXML
    private void kickUser(ActionEvent event) {
        if (botIsConnected){
            String user = usersListView.getSelectionModel().getSelectedItem();
        }
    }

    @FXML
    private void saveLogs(ActionEvent event) {
        String text = eventLogText.getText();
        if ( !text.equals("") ){
            // TODO Do stuff here
        }
        eventLogText.setText("");
    }

    @FXML
    private void timeoutUser(ActionEvent event) {
        if (botIsConnected){

        }
    }

    @FXML
    private void toggleSpamCaps(ActionEvent event) {

    }

    @FXML
    private void toggleSpamConsecChars(ActionEvent event) {

    }

    @FXML
    private void toggleSpamWordLength(ActionEvent event) {

    }

    @FXML
    private void toggleSpamWordRepetition(ActionEvent event) {

    }

    public void botShutdown(){
        botDisconnect(null);
    }

    @FXML
    private void initialize() {
        assert eventLogText != null : "fx:id=\"eventLogText\" was not injected: check your FXML file 'ui.fxml'.";
        assert logSaveOutput != null : "fx:id=\"logSaveOutput\" was not injected: check your FXML file 'ui.fxml'.";
        assert usersListView != null : "fx:id=\"usersListView\" was not injected: check your FXML file 'ui.fxml'.";
        assert chatToolTimeSpinner != null : "fx:id=\"chatToolTimeSpinner\" was not injected: check your FXML file 'ui.fxml'.";
        assert commandTableView != null : "fx:id=\"commandTableView\" was not injected: check your FXML file 'ui.fxml'.";
        assert cmdNameTableCol != null : "fx:id=\"cmdNameTableCol\" was not injected: check your FXML file 'ui.fxml'.";
        assert cmdLvlTableCol != null : "fx:id=\"cmdLvlTableCol\" was not injected: check your FXML file 'ui.fxml'.";
        assert cmdTextTableCol != null : "fx:id=\"cmdTextTableCol\" was not injected: check your FXML file 'ui.fxml'.";
        assert commandNameField != null : "fx:id=\"commandNameField\" was not injected: check your FXML file 'ui.fxml'.";
        assert userLevelDropdown != null : "fx:id=\"userLevelDropdown\" was not injected: check your FXML file 'ui.fxml'.";
        assert commandTextField != null : "fx:id=\"commandTextField\" was not injected: check your FXML file 'ui.fxml'.";
        assert wordLengthCheckBox != null : "fx:id=\"wordLengthCheckBox\" was not injected: check your FXML file 'ui.fxml'.";
        assert consecCharsCheckBox != null : "fx:id=\"consecCharsCheckBox\" was not injected: check your FXML file 'ui.fxml'.";
        assert wordRepetitionCheckBox != null : "fx:id=\"wordRepetitionCheckBox\" was not injected: check your FXML file 'ui.fxml'.";
        assert capitalLettersCheckBox != null : "fx:id=\"capitalLettersCheckBox\" was not injected: check your FXML file 'ui.fxml'.";
        assert blockLinksCheckBox != null : "fx:id=\"blockLinksCheckBox\" was not injected: check your FXML file 'ui.fxml'.";
        assert wordLengthSpinner != null : "fx:id=\"wordLengthSpinner\" was not injected: check your FXML file 'ui.fxml'.";
        assert repeatWordsSpinner != null : "fx:id=\"repeatWordsSpinner\" was not injected: check your FXML file 'ui.fxml'.";
        assert consecCharSpinner != null : "fx:id=\"consecCharSpinner\" was not injected: check your FXML file 'ui.fxml'.";
        assert percentCapsSpinner != null : "fx:id=\"percentCapsSpinner\" was not injected: check your FXML file 'ui.fxml'.";
        assert wordSizeSpinner != null : "fx:id=\"wordSizeSpinner\" was not injected: check your FXML file 'ui.fxml'.";
        assert botNameField != null : "fx:id=\"botNameField\" was not injected: check your FXML file 'ui.fxml'.";
        assert oAuthKeyField != null : "fx:id=\"oAuthKeyField\" was not injected: check your FXML file 'ui.fxml'.";
        assert channelField != null : "fx:id=\"channelField\" was not injected: check your FXML file 'ui.fxml'.";
        assert aboutText != null : "fx:id=\"aboutText\" was not injected: check your FXML file 'ui.fxml'.";
        assert versionText != null : "fx:id=\"versionText\" was not injected: check your FXML file 'ui.fxml'.";
        assert sourceLink != null : "fx:id=\"sourceLink\" was not injected: check your FXML file 'ui.fxml'.";
        assert oAuthKeyLink != null : "fx:id=\"oAuthKeyLink\" was not injected: check your FXML file 'ui.fxml'.";
        assert connectedImage != null : "fx:id=\"connectedImage\" was not injected: check your FXML file 'ui.fxml'.";
        assert connectedText != null : "fx:id=\"connectedText\" was not injected: check your FXML file 'ui.fxml'.";

        userLevelDropdown.getItems().addAll('D', 'M', 'A');
        userLevelDropdown.getSelectionModel().selectFirst();

        chatToolTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7200, 1, 10));
        wordLengthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        repeatWordsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 20, 1));
        consecCharSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        percentCapsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        wordSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1, 10));

        connectedImg = new Image(getClass().getResource("/connected.png").toString());
        disconnectedImg = new Image(getClass().getResource("/disconnected.png").toString());

        connectedImage.setImage(disconnectedImg);

        sourceLink.setOnAction(e -> {
            if(Desktop.isDesktopSupported())
            {
                try {
                    Desktop.getDesktop().browse(new URI("http://www.github.com/mrtheedge/Twitch-IRC-ChatBot"));
                } catch (IOException e1) {
                    //e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    //e1.printStackTrace();
                }
            }
        });

        oAuthKeyLink.setOnAction(e -> {
            if(Desktop.isDesktopSupported())
            {
                try {
                    Desktop.getDesktop().browse(new URI("http://twitchapps.com/tmi/"));
                } catch (IOException e1) {
                    //e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    //e1.printStackTrace();
                }
            }
        });

        spamHandler = new SpamHandler();
        cmdHandler = new CommandHandler();
        messageHandler = new MessageHandler(cmdHandler, spamHandler);
        botController = new MainController(messageHandler);
        chatBot = new ChatBot(botController);
        botController.setChatBot(chatBot);

    }
}
