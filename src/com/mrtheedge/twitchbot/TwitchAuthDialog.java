package com.mrtheedge.twitchbot;

import com.mrtheedge.twitchbot.event.AuthEvent;
import com.mrtheedge.twitchbot.event.AuthEventListener;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by E.J. Schroeder on 10/22/2016.
 */
public class TwitchAuthDialog {

    //TODO Add code to call the twitch api and find out the username for the token.

    private final String PERSONAL_REDIRECT_URI = "ejschroeder.me";
    private String token;
    private String username;

    private List<AuthEventListener> _LISTENERS = new ArrayList<>();

    public void addListener(AuthEventListener listener){
        _LISTENERS.add(listener);
    }

    private void fireAuthEvent(boolean success, String user, String authToken){
        AuthEvent ae = new AuthEvent(this, success, user, authToken);
        for (AuthEventListener el : _LISTENERS){
            el.handle(ae);
        }
    }

    public String getToken(){
        if (token != null){
            return token;
        }
        return "";
    }

    /*
        If the twitch login succeeded, it will redirect to another webpage that I specify.
        This method checks whether the login was successful.
     */
    private boolean loginSucceeded(String url){
        try {
            URL uri = new URL(url);

            // If the user was forwarded to the correct address, indicates
            return uri.getHost().equals(PERSONAL_REDIRECT_URI);

        } catch (MalformedURLException e) {
            System.out.println("Unable to authenticate at this time.");
            e.printStackTrace();
            return false;
        }
    }

    /*
        Extract access token from the URI that the Twitch authentication redirected us to.
     */
    private boolean parseURIForToken(String url){
        try {
            URL uri = new URL(url);
            String[] queryComp = uri.getRef().split("&");

            // Covering bases for possible changes in url structure
            // (token should be first parameter but may not be)
            for (int i = 0; i < queryComp.length; i++){
                if (queryComp[i].startsWith("access_token")){
                    this.token = queryComp[i].substring(queryComp[i].indexOf("=") + 1);
                    return true;
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Unable to authenticate.");
            //e.printStackTrace();
        }
        return false; // No auth token found.
    }

    public void create() {
        Stage window = new Stage();
        WebView wv = new WebView();
        wv.setPrefSize(325, 415);

        WebEngine engine = wv.getEngine();
        engine.load("https://api.twitch.tv/kraken/oauth2/authorize" +
                "?response_type=token" +
                "&client_id=" + Constants.CLIENT_ID +
                "&redirect_uri=http://ejschroeder.me/twitchauth" +
                "&scope=chat_login");

        engine.getLoadWorker().stateProperty().addListener( (ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED && loginSucceeded(engine.getLocation())) {
                if( parseURIForToken(engine.getLocation()) ) {
                    String user = getUsernameFromToken();
                    if (user.equals("")){ // Token was incorrect, or another error occurred. Not logged in.
                        fireAuthEvent(false, "", "");
                    } else {
                        fireAuthEvent(true, user, this.token);
                    }
                    window.close();
                }
            }
        });

        window.setOnCloseRequest(e -> {
            // Window was closed before successful authentication. Send an invalid auth event.
            fireAuthEvent(false, "", "");
        });

        Scene scene = new Scene(wv);

        window.setScene(scene);
        window.setTitle("Twitch Login");
        window.show();
    }

    private String getUsernameFromToken() {
        return TwitchAPI.usernameFromToken(token);
    }

}
