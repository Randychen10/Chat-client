package chatclient.scenes;

import chatclient.UserSession;
import chatclient.responses.LoginResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Class used to represent a Login Screen
 * You may modify the class as you see fit.
 */
public class Login implements Screen {
    private Stage mainStage;
    private TextField usernameField;
    private PasswordField passwordField;

    public Login(Stage mainStage) {
        this.mainStage = mainStage;
    }

    /**
     * Builds the Login Scene and returns the root of that scene.
     * @return A build Scene
     */
    public Pane build() {
        //TODO NEEDS TO BE REPLACED ONLY HERE SO APP RUNS
        VBox vb = new VBox();
        vb.setSpacing(20);

        //Labels for displaying login and username
        Label heading = new Label("Login");
        Label username = new Label("Username:");
        Label password = new Label("Password:");

        //Fields for typing in username and password
        this.usernameField = new TextField();
        this.passwordField = new PasswordField();

        // creates a gird for the loginGUI
        GridPane grid = new GridPane();
        grid.addRow(0, username, usernameField);
        grid.addRow(1, password, passwordField);

        //Buttons for login and clear
        Button login = new Button("Login");
        Button clear = new Button("Clear");

        //Handles actions for when buttons are pressed
        login.setOnAction(this::handleLogin);
        clear.setOnAction(this::handleReset);

        //HBox for the login and clear buttons
        HBox hb = new HBox(login, clear);
        hb.setSpacing(10);

        //vb styling
        vb.setAlignment(Pos.CENTER);
        vb.setPrefWidth(Region.USE_COMPUTED_SIZE);
        vb.setPadding(new Insets(20));

        //Other styling
        String labelStyle = "-fx-font-weight: bold;";

        heading.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
        username.setStyle(labelStyle);
        password.setStyle(labelStyle);

        //hb Styling
        hb.setAlignment(Pos.CENTER);

        //Field styling
        String fieldStyle = "-fx-background-radius: 15px; -fx-border-radius: 20px;";
        usernameField.setStyle(fieldStyle);
        passwordField.setStyle(fieldStyle);

        //Button styling
        String buttonStyle = "-fx-background-radius: 15px; -fx-border-radius: 20px;";
        login.setStyle(buttonStyle);
        clear.setStyle(buttonStyle);

        //Grid styling
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(0, 60, 0, 0));
        grid.setHgap(10);
        grid.setVgap(10);

        //Width of the fields
        double prefWidth = 300;
        usernameField.setPrefWidth(prefWidth);
        passwordField.setPrefWidth(prefWidth);

        vb.getChildren().addAll(heading, grid, hb);
        return vb;
    }

    /**
     * used to handle login button click.
     * This function should send username and password to the server
     * and then depending on the response should either display
     * invalid login credentials or transition to the Servers Screen.
     * @param ev Event from button click
     */
    private void handleLogin(ActionEvent ev) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Use Gson to create JSON payload
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("password", password);
        String requestBody = gson.toJson(json);

        // API key as header
        String apiKey = "csc413_7f86ec83e53b5a63_0efa02ccb89804470f99d5e714e639d2721762af5111cbbea96a14753bec";

        // Create an HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Create an HTTP request with POST method and JSON payload
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://csc413.ajsouza.com/users/login/app"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    // Handle only incorrect username/password case
                    if (response.statusCode() == 200) {
                        // Parse JSON response using LoginResponse class
                        LoginResponse loginResponse = gson.fromJson(response.body(), LoginResponse.class);

                        if ("success".equals(loginResponse.getStatus())) {
                            // Initialize UserSession with login response data
                            String token = loginResponse.getToken();
                            int userId = loginResponse.getUser_id();
                            String usernameResponse = loginResponse.getUsername();

                            UserSession userSession = UserSession.getInstance();
                            userSession.initSession(usernameResponse, userId, token);

                            // Transition to the Servers Screen
                            Platform.runLater(() -> {
                                Stage primaryStage = (Stage) ((javafx.scene.Node) ev.getSource()).getScene().getWindow();

                                // Initialize Servers screen
                                Servers serversScreen = new Servers(primaryStage);
                                Scene serversScene = new Scene(serversScreen.build(), 800, 600);

                                // Set and show the Servers scene on the primary stage
                                primaryStage.setScene(serversScene);
                                primaryStage.setTitle("Servers");
                            });
                        } else {
                            // Handle incorrect username/password
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setHeaderText(loginResponse.getMessage()); // Display error message from the response
                                alert.showAndWait();
                            });
                        }
                    }
                });
    }


    /**
     * used to handle reset button click.
     * @param ev Event from button click
     */
    private void handleReset(ActionEvent ev) {
        usernameField.clear();
        passwordField.clear();
    }
}
