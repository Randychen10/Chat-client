package chatclient;

import java.util.Map;

import chatclient.scenes.ChatRoom;
import chatclient.scenes.Login;
import chatclient.scenes.Screen;
import chatclient.scenes.Servers;

import java.util.HashMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    //Map used to store references to our UI Screens
    private static final Map<String, Screen> screens = new HashMap<>();

    public static void main(String[] args) {
        //launch JavaFX Application
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage)  {
        //changing the base CSS for the JavaFX App.
        Application.setUserAgentStylesheet("css/nord-dark.css");
        Login loginScreen = new Login(primaryStage);
        Servers serverScreen = new Servers(primaryStage);
        ChatRoom cr =  new ChatRoom(primaryStage);
        screens.put("login",loginScreen);
        screens.put("servers",serverScreen);
        screens.put("chatroom", cr);
        Scene login = new Scene(screens.get("login").build());

        //set title of JavaFX window.
        primaryStage.setTitle("CSC 413 Chat App");
        //set the current Screen
        primaryStage.setScene(login);
        //Make the stage(window) viewable.
        primaryStage.show();
    }

    /**
     * use to return a reference to a Class used to build a Scene.
     * @param screenName of the Scene to be built and displayed
     * @return a Screen given a screenName
     */
    public static Screen getScreen(String screenName){
        return App.screens.get(screenName);
    }
}
