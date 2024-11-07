package chatclient.scenes;

import chatclient.App;
import chatclient.UserSession;
import chatclient.listviewcells.ServerListCell;
import chatclient.responses.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

/**
 * Class used to represent a Server Listing Screen.
 * We should be able to create a new room and join a
 * current room from this screen.
 */

public class Servers implements Screen {
    private final Stage mainStage;
    private ListView<ServerListCell> servers;
    String apiKey = "csc413_7f86ec83e53b5a63_0efa02ccb89804470f99d5e714e639d2721762af5111cbbea96a14753bec";

    public Servers(Stage s) {
        this.mainStage = s;
        this.servers = new ListView<>();
        refreshServerList(new ActionEvent());
    }

    /**
     * Builds the Server listing Screen
     *
     * @return root to the Server Listing Screen
     */
    public Pane build() {
        VBox vb = new VBox();

        //Displaying servers
        servers = new ListView<>();
        servers.setPlaceholder(new Label("No servers available"));
        vb.getChildren().add(serverCountLabel);
        vb.getChildren().add(servers);

        //Refresh server list button
        Button refresh = new Button("Refresh Servers");
        refresh.setOnAction(this::refreshServerList);

        //Connect to room button
        Button connect = new Button("Connect to Server");
        connect.setOnAction(this::connectToRoom);

        //Create room button
        Button createRoom = new Button("Create Room");
        createRoom.setOnAction(this::showCreateRoomForm);

        Button logout = new Button("Logout");
        logout.setStyle("-fx-background-color: red; -fx-border-radius: 20px; -fx-background-radius: 20px; -fx-border-color: black");
        logout.setOnAction(e -> {
            Login login = (Login) App.getScreen("login");

            Pane serversPane = login.build();

            // Switch to the Servers screen
            mainStage.setScene(new Scene(serversPane, 500, 250));  // Adjust size as needed
            mainStage.setTitle("Server Listing");
        });

        //Styling the servers page
        vb.setAlignment(Pos.CENTER);
        vb.setSpacing(15);

        //Button styling
        String buttonStyle = "-fx-background-radius: 15px; -fx-border-radius: 20px;";
        createRoom.setStyle(buttonStyle);
        connect.setStyle(buttonStyle);
        refresh.setStyle(buttonStyle);

        vb.getChildren().addAll(createRoom, connect, refresh, logout);
        return vb;
    }

    private Label serverCountLabel = new Label("Available servers: 0");

    /**
     * Fetch active servers from the Chat Server
     *
     * @param ev
     */
    public void refreshServerList(ActionEvent ev) {
        // Create an HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Create an HTTP request to fetch the list of rooms
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://csc413.ajsouza.com/api/rooms"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey) // Adding API key to the header
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        // Parse the response into ListRooms object using Gson
                        Gson gson = new Gson();
                        ListRooms listRooms = gson.fromJson(response.body(), ListRooms.class);

                        // If the status is success, update the server list
                        if ("success".equals(listRooms.getStatus())) {
                            List<Room> rooms = listRooms.getRooms();

                            Platform.runLater(() -> {
                                // Clear the current list
                                servers.getItems().clear();

                                // Add each room to the ListView
                                for (Room room : rooms) {
                                    // Now passing room name, ownerId, and roomId
                                    servers.getItems().add(new ServerListCell(room.getRoom_name(), room.getOwner_id(), room.getId(), servers));
                                }

                                // Update server count label
                                serverCountLabel.setText("Available servers: " + listRooms.getRooms_count());

                                // Set placeholder if no rooms are available
                                if (rooms.isEmpty()) {
                                    servers.setPlaceholder(new Label("No servers available"));
                                }
                            });
                        } else {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Failed to fetch rooms");
                                alert.setHeaderText(null); // No header text
                                alert.setContentText(listRooms.getMessage());
                                alert.showAndWait();
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Failed to fetch rooms");
                            alert.setHeaderText(null); // No header text
                            alert.setContentText("Server returned status code: " + response.statusCode());
                            alert.showAndWait();
                        });
                    }
                })
                .exceptionally(e -> {
                    // Handle any exceptions such as network issues
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText(null); // No header text
                        alert.setContentText("Failed to fetch rooms. Please try again later.");
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    /**
     * Connect to the current room selected in the servers listing.
     * Should open a new websocket, and also fetch messages and users for
     * the connecting room.
     *
     * @param ev
     */
    private void connectToRoom(ActionEvent ev) {
        // Get selected server from the list
        ServerListCell selectedServer = servers.getSelectionModel().getSelectedItem();

        if (selectedServer == null) {
            System.out.println("No server selected.");
            return;
        }

        // Get room ID and user session details
        int roomId = selectedServer.getRoomId();
        UserSession userSession = UserSession.getInstance();
        int userId = userSession.getUserId();  // Get the user ID

        // Create a new ChatRoom instance
        ChatRoom chatRoom = new ChatRoom(mainStage);

        // Attempt to initialize WebSocket connection within ChatRoom
        boolean connected = chatRoom.initWebsocket(roomId, userId, selectedServer.getRoomName()); // Assuming getRoomName() returns the room name

        if (connected) {
            // If successfully connected, transition to the chat room UI
            Platform.runLater(() -> {
                Stage currentStage = (Stage) ((Node) ev.getSource()).getScene().getWindow();
                currentStage.setScene(new Scene(chatRoom.build(), 800, 600));
                currentStage.setTitle("Chat Room - Room ID: " + roomId);

                // Load users immediately after entering the chat room
                chatRoom.loadUsersPane(); // Load users as soon as the room is opened
                chatRoom.loadMessages();   // Load messages as well
            });
        } else {
            // Handle the case where the connection could not be established
            System.out.println("Failed to connect to WebSocket for Room ID: " + roomId);
        }
    }

    /**
     * Show form to create a new room. This can be a popup that prompts the user for room details,
     * and then calls a method to create the room on the server.
     *
     * @param ev Event from button click
     */
    private void showCreateRoomForm(ActionEvent ev) {
        // Create a dialog for room creation
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create New Room");
        dialog.setHeaderText("Enter the name for the new room");

        // Set the button types (OK and Cancel)
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create fields for room input
        TextField roomNameField = new TextField();
        roomNameField.setPromptText("Room Name");

        // Layout for the form
        VBox content = new VBox();
        content.setSpacing(10);
        content.getChildren().addAll(new Label("Room Name:"), roomNameField);

        dialog.getDialogPane().setContent(content);

        // Convert the result to a room name when the Create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return roomNameField.getText();
            }
            return null;
        });

        // Show the dialog and capture the result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(roomName -> {
            if (!roomName.trim().isEmpty()) {
                // Call method to handle room creation
                handleCreateRoom(roomName);
            } else {
                // Show an error if the room name is empty
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Room Name");
                alert.setHeaderText(null);
                alert.setContentText("Room name cannot be empty.");
                alert.showAndWait();
            }
        });
    }

    /**
     * Handle room creation logic here, by sending a request to the server.
     *
     * @param roomName The name of the new room to create.
     */
    private void handleCreateRoom(String roomName) {
        // Validate room name
        if (roomName == null || roomName.trim().isEmpty()) {
            // Show alert if the room name is invalid
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Room name cannot be empty. Please provide a valid room name.");
                alert.showAndWait();
            });
            return;
        }

        // Create the HTTP client and Gson instance
        HttpClient client = HttpClient.newHttpClient();
        Gson gson = new Gson();

        // Build the JSON payload
        JsonObject jsonPayload = new JsonObject();

        // Ensure the correct key is used for the room name - consult API docs for correct key
        jsonPayload.addProperty("room_name", roomName);

        // Create the API request to create the room
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://csc413.ajsouza.com/api/rooms"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)  // Add the API key in the Authorization header
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(jsonPayload)))  // Send JSON payload
                .build();

        // Send the request asynchronously and handle the response
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                    Platform.runLater(() -> {
                        // Safely check if "status" exists and is not null
                        if (jsonResponse.has("status") && !jsonResponse.get("status").isJsonNull()) {
                            String status = jsonResponse.get("status").getAsString();

                            if ("success".equals(status)) {
                                // Show success message
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Room Created");
                                alert.setHeaderText(null);
                                alert.setContentText("Room '" + roomName + "' was successfully created.");
                                alert.showAndWait();

                                // Optionally refresh the room list to show the new room
                                refreshServerList(null);
                            } else {
                                // Safely check if "message" exists and is not null
                                String errorMessage = jsonResponse.has("message") && !jsonResponse.get("message").isJsonNull()
                                        ? jsonResponse.get("message").getAsString()
                                        : "Unknown error occurred.";
                                // Show error message
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setHeaderText(null);
                                alert.setContentText("Failed to create the room: " + errorMessage);
                                alert.showAndWait();
                            }
                        } else {
                            // Show error if the "status" field is missing
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Failed to create the room. Missing or invalid response from server.");
                            alert.showAndWait();
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        // Handle any exceptions such as network or server issues
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to create the room. Please try again later.");
                        alert.showAndWait();
                    });
                    return null;
                });
    }
}

