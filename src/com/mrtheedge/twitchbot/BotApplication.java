package com.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 1/17/2016.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotApplication extends Application {

    private VBox mainPanel;
    private UIController uiController;

    @Override
    public void start(Stage stage) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(BotApplication.class.getResource("ui.fxml"));
            mainPanel = (VBox) fxmlLoader.load();
            uiController = (UIController) fxmlLoader.getController();
        } catch (IOException e) {
            //e.printStackTrace();
            Logger.getLogger(BotApplication.class.getName()).log(Level.SEVERE, "Could not load ui.fxml to initialize window");
        }

        Scene scene = new Scene(mainPanel);

        stage.setScene(scene);
        stage.setTitle("Twitch Chat Bot");

        stage.setOnCloseRequest( e -> {
            uiController.botShutdown();
            System.exit(0);
        });

        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
