package com.mrtheedge.twitchbot;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import com.mrtheedge.twitchbot.event.CommandEvent;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class UIController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextArea eventLogText;
    @FXML private Text logSaveOutput;
    @FXML private ListView<String> usersListView;
    @FXML private Spinner<Integer> chatToolTimeSpinner; // Initialize
    @FXML private TableView<CustomCommand> commandTableView;
    @FXML private TableColumn<CustomCommand, String> cmdNameTableCol;
    @FXML private TableColumn<CustomCommand, Character> cmdLvlTableCol;
    @FXML private TableColumn<CustomCommand, String> cmdTextTableCol;
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
    @FXML private TextField channelField;
    @FXML private Button twitchLoginButton;
    @FXML private Label loginStatusLabel;
    @FXML private Button connectButton;
    @FXML private Text aboutText;
    @FXML private Text versionText;
    @FXML private Hyperlink sourceLink;
    @FXML private Hyperlink oAuthKeyLink;
    @FXML private ImageView connectedImage;
    @FXML private Text connectedText;

    private Image connectedImg;
    private Image disconnectedImg;

    private boolean botIsConnected = false;
    private BotController botController;
    private ChatBot chatBot;
    private SpamHandler spamHandler;
    private CommandHandler cmdHandler;
    private MessageHandler messageHandler;

    private ObservableList<String> activeUsers;
    private SortedList<String> sortedActiveUsers;

    private ObservableList<CustomCommand> commandList;

    DateTimeFormatter timeFormat = DateTimeFormat.shortDateTime();

    @FXML
    private void addCommand(ActionEvent event) {
        String cName = commandNameField.getText();
        char lvl = userLevelDropdown.getSelectionModel().getSelectedItem();
        String cText = commandTextField.getText();
        if (!cName.equals("") && !cText.equals("")){
            cmdHandler.addCommand(new CustomCommand(cName, lvl, cText));
        }
    }

    @FXML
    private void banUser(ActionEvent event) {
        if (botIsConnected){
            String user = usersListView.getSelectionModel().getSelectedItem();
            chatBot.sendMessage(chatBot.getChannel(), ".ban " + user);
        }
    }

    @FXML
    private void botConnect(ActionEvent event) {
        if (!chatBot.isConnected()){
            //String botName = botNameField.getText();
            //String oauth = oAuthKeyField.getText();
            String channel = channelField.getText();
            if ( channel.equals("") ){
                addEventToLog("No channel specified. The bot was unable to connect.");
            } else {
                //System.out.println(botName + "\n" + oauth + "\n" + channel);

                botController.connectToIrc(channel);

                if (chatBot.isConnected()) {
                    botIsConnected = true;
                    connectedImage.setImage(connectedImg);
                    connectedText.setText("Connected");
                }
            }


        }
    }

    @FXML
    private void botDisconnect(ActionEvent event) {
        if (chatBot.isConnected()){
            botController.disconnect();

            botIsConnected = false;
            connectedImage.setImage(disconnectedImg);
            connectedText.setText("Not Connected");

        }
    }

    @FXML
    private void deleteCommand(ActionEvent event) {
        String cName = commandTableView.getSelectionModel().getSelectedItem().getName();
        if (!cName.equals("")){
            cmdHandler.deleteCommand(cName);
        }
    }

    @FXML
    private void kickUser(ActionEvent event) {
        if (botIsConnected){
            String user = usersListView.getSelectionModel().getSelectedItem();
            chatBot.sendMessage(chatBot.getChannel(), ".kick " + user);
        }
    }

    @FXML
    private void saveLogs(ActionEvent event) {
        String text = eventLogText.getText();
        if ( !text.equals("") ){
            LogWriter.writeLogsToFile(text);
            eventLogText.setText("");
        }

    }

    @FXML
    private void timeoutUser(ActionEvent event) {
        if (botIsConnected){
            String user = usersListView.getSelectionModel().getSelectedItem();
            int time = chatToolTimeSpinner.getValue();
            chatBot.sendMessage(chatBot.getChannel(), ".timeout " + user + " " + time);
        }
    }

    @FXML
    private void toggleSpamCaps(ActionEvent event) {
        CheckBox source = (CheckBox)event.getSource();
        boolean bool = source.isSelected();
        spamHandler.setCheckPercentageCaps(bool);
    }

    @FXML
    private void toggleSpamConsecChars(ActionEvent event) {
        CheckBox source = (CheckBox)event.getSource();
        boolean bool = source.isSelected();
        spamHandler.setCheckConsecChars(bool);
    }

    @FXML
    private void toggleSpamWordLength(ActionEvent event) {
        CheckBox source = (CheckBox)event.getSource();
        boolean bool = source.isSelected();
        spamHandler.setCheckWordLength(bool);
    }

    @FXML
    private void toggleSpamWordRepetition(ActionEvent event) {
        CheckBox source = (CheckBox)event.getSource();
        boolean bool = source.isSelected();
        spamHandler.setCheckWordRepetition(bool);
    }

    public void botShutdown(){
        botController.shutdown();
        saveData();
    }

    private void saveData() {
        BotDataFile saveData = new BotDataFile();

        String channelName = channelField.getText();

        saveData.addSpamHandler( spamHandler )
                .addCommandList( cmdHandler.getCommandsAsList() )
                .addLoginData(channelName)
                .write();
    }

    private void addEventToLog(String message){
        // Create a new timestamp and add it to the log with message
        String time = "[" + timeFormat.print(DateTime.now()) + "] ";
        eventLogText.appendText(time + message + "\n");
    }

    private void loadData() {
        BotDataFile loadData = new BotDataFile();

        try {
            loadData.read();
        } catch (IOException e) {
            // Unable to read or find the file
            // Create a new one on close.
        }

        if (loadData.getSpamHandler().isPresent()){
            spamHandler.transferSettings(loadData.getSpamHandler().get());

        }
        if (loadData.getCommandList().isPresent()){
            // Add each new command to the map in cmdHandler
            for (CustomCommand c : loadData.getCommandList().get()){
                cmdHandler.addCommand(c);
            }
        }
        if (loadData.getChannelName().isPresent()){
            channelField.setText(loadData.getChannelName().get());
        }
    }

    private void updateSpamOptions() {

        consecCharSpinner.getValueFactory().setValue(spamHandler.getConsecChars());
        wordLengthSpinner.getValueFactory().setValue(spamHandler.getWordLength());
        wordSizeSpinner.getValueFactory().setValue(spamHandler.getMinWordLength());
        repeatWordsSpinner.getValueFactory().setValue(spamHandler.getWordRepetition());
        percentCapsSpinner.getValueFactory().setValue((int)(spamHandler.getPercentageCaps() * 100));

        blockLinksCheckBox.setSelected(spamHandler.getCheckLinks());
        capitalLettersCheckBox.setSelected(spamHandler.getCheckPercentageCaps());
        consecCharsCheckBox.setSelected(spamHandler.getCheckConsecChars());
        wordLengthCheckBox.setSelected(spamHandler.getCheckWordLength());
        wordRepetitionCheckBox.setSelected(spamHandler.getCheckWordRepetition());


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
        wordLengthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 15));
        repeatWordsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 20, 3));
        consecCharSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 10));
        percentCapsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 75));
        wordSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5));

        eventLogText.setWrapText(true);

        connectedImg = new Image(getClass().getResource("/connected.png").toString());
        disconnectedImg = new Image(getClass().getResource("/disconnected.png").toString());

        connectedImage.setImage(disconnectedImg);

        activeUsers = FXCollections.observableArrayList();
        commandList = FXCollections.observableArrayList();
        sortedActiveUsers = new SortedList<>(activeUsers);
        usersListView.setItems(sortedActiveUsers);
        usersListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        ImageView twitchLoginImgView = new ImageView(getClass().getResource("/ConnectWithTwitch.png").toString());
        twitchLoginButton.setGraphic(twitchLoginImgView);

        loginStatusLabel.setText("");

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

        // TODO Make all this ugliness more modular. This is atrocious.
        cmdHandler = new CommandHandler();
        spamHandler = new SpamHandler();
        messageHandler = new MessageHandler(cmdHandler, spamHandler);
        botController = new BotController(messageHandler);
        chatBot = new ChatBot(botController);
        botController.setChatBot(chatBot);

        messageHandler.addListener( e ->
                activeUsers.add(e.getUser())
        );

        botController.addListener( e ->
                addEventToLog(e.getMessage())
        );

        cmdHandler.addListener(e -> {
            if (e.getType() == CommandEvent.ADD){
                commandList.add(e.getCommand());
            } else {
                commandList.remove(e.getCommand());
            }
        });

        // Value factories for the data from each CustomCommand. Name/User Level/Response
        cmdNameTableCol.setCellValueFactory( c -> new SimpleStringProperty(c.getValue().getName()) );
        cmdLvlTableCol.setCellValueFactory( c -> new SimpleObjectProperty<>( Character.toUpperCase(c.getValue().getReqLevel()) ));
        cmdTextTableCol.setCellValueFactory( c -> new SimpleStringProperty(c.getValue().getResponse()));
        commandTableView.setItems(commandList);
        commandTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Load preferences from file here...

        loadData();
        updateSpamOptions();

        repeatWordsSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            spamHandler.setWordRepetition(newValue);
        });
        wordLengthSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            spamHandler.setWordLength(newValue);
        });
        consecCharSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            spamHandler.setConsecChars(newValue);
        });
        percentCapsSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            spamHandler.setPercentageCaps(newValue);
        });
        wordSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            spamHandler.setMinWordLength(newValue);
        });

    }

    public void twitchLoginButtonOnAction(ActionEvent actionEvent) {
        twitchLoginButton.setDisable(true);                     // Disable the login button until confirmation of success or failure is reported
        TwitchAuthDialog dialogWindow = new TwitchAuthDialog();
        dialogWindow.addListener(el -> {
            if (!el.isSuccessful()){ // Login unsuccessful, allow user to click login again.
                twitchLoginButton.setDisable(false);
            } else {
                botController.setUsername(el.getUsername());
                botController.setOAuthToken(el.getToken());
                loginStatusLabel.setText("Logged in as: " + el.getUsername());
                connectButton.setDisable(false);
            }
        });

        dialogWindow.create();

    }
}
