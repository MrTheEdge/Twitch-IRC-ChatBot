package com.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 1/17/2016.
 */

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class BotGUI extends Application {

    private Parent createContent(){

        VBox mainPane = new VBox();

        TabPane tabPane = new TabPane();

        Tab logTab = new Tab();
        logTab.setText("Logs");
        logTab.setContent(new Button("Example 1"));
        logTab.setClosable(false);
        tabPane.getTabs().add(logTab);

        Tab settingsTab = new Tab();
        settingsTab.setText("Settings");
        settingsTab.setContent(new Button("Example 2"));
        settingsTab.setClosable(false);
        tabPane.getTabs().add(settingsTab);

        HBox connButtonLayout = new HBox();
        connButtonLayout.setAlignment(Pos.CENTER);
        connButtonLayout.setSpacing(5);

        Button connectBtn = new Button("Connect");
        Button disconnectBtn = new Button("Disconnect");

        connButtonLayout.getChildren().addAll(connectBtn, disconnectBtn);

        mainPane.getChildren().addAll(tabPane, connButtonLayout);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        return mainPane;
    }

    @Override
    public void start(Stage stage) {

        Scene scene = new Scene( createContent(), 500, 500 );

        stage.setScene(scene);
        stage.setTitle("Twitch Chat Bot");
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
