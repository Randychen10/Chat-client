package chatclient.scenes;

import chatclient.App;
import chatclient.UserSession;
import chatclient.listviewcells.MessageListCell;
import chatclient.responses.*;
import com.google.gson.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import javax.websocket.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatRoom implements Screen {
    private final Stage stage;
    private UserSession userSession;
    private Session session;
    private ListView<String> users;
    private ListView<MessageListCell> messages;
    private int roomId;
    String timestamp = LocalDateTime.now().toString();
    String apiKey = "csc413_7f86ec83e53b5a63_0efa02ccb89804470f99d5e714e639d2721762af5111cbbea96a14753bec";

    public ChatRoom(Stage primaryStage) {
        this.stage = primaryStage;
        messages = new ListView<>();
        users = new ListView<>();
        this.userSession = UserSession.getInstance();
    }

    /**
     * Builds the Scene for viewing a chat room.
     * @return
     */
    public Pane build() {
        // Create the VBox for the main layout
        VBox vb = new VBox();
        vb.setSpacing(10);
        vb.setPadding(new Insets(10, 10, 10, 10));

        // Hbox for back button
        HBox h = new HBox();
        Label heading = new Label("Chat Room");
        Button back = new Button("<-");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        heading.setPadding(new Insets(5, 0, 0,0));
        back.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-border-radius: 20px");

        back.setOnAction(e -> {
            // Retrieve and build the Servers screen directly
            Servers serversScreenInstance = (Servers) App.getScreen("servers");
            Pane serversPane = serversScreenInstance.build();

            // Switch to the Servers screen
            stage.setScene(new Scene(serversPane, 800, 600));  // Adjust size as needed
            stage.setTitle("Server Listing");

            // Refresh the server list after setting the scene
            serversScreenInstance.refreshServerList(new ActionEvent());

            stage.show();
        });

        h.setSpacing(10);
        h.getChildren().addAll(back, heading);

        messages.setPrefHeight(450);
        messages.setPrefWidth(500);

        // Create an HBox for the message input and send button
        HBox messageBox = new HBox();
        messageBox.setSpacing(10);

        // TextField for typing messages
        TextField messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setPrefWidth(500);

        Button send = new Button("Send");
        send.setStyle("-fx-background-radius: 20px; -fx-border-radius: 20px");
        send.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                sendMessage(message, e);
                messageField.clear();
            }
        });

        Button refresh = new Button("Refresh");
        refresh.setStyle("-fx-background-radius: 20px; -fx-border-radius: 20px");
        refresh.setOnAction(e -> {
            loadMessages();
        });

        // Add the TextField and Send Button to the messageBox
        messageBox.getChildren().addAll(messageField, send, refresh);

        // Set dimensions for the users ListView
        users.setPrefWidth(250);
        users.setPrefHeight(450);

        // Create an HBox to hold the message list and the user list side by side
        HBox chatAndUsersBox = new HBox();
        chatAndUsersBox.setSpacing(10);
        chatAndUsersBox.getChildren().addAll(messages, users);  // Add messageListView and users

        // Add heading, chatAndUsersBox, and messageBox to the main VBox layout
        vb.getChildren().addAll(h, chatAndUsersBox, messageBox);

        return vb;  // Return the VBox as the pane for this scene
    }

    /**
     * Initializes a connection of a WebSocket to the chat server.
     * @param roomId which room connecting to
     * @param userId who's connecting to the room
     * @param roomName the name of the room.
     * @return whether or not the connection was made.
     */
    public boolean initWebsocket(final int roomId, final int userId, final String roomName) {
        this.roomId = roomId;
        try {
            // Construct the WebSocket URI using roomId, userId, and token
            String uri = "wss://csc413.ajsouza.com/chat?room_id=" + roomId + "&user_id=" + userId + "&token=" + apiKey;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            // Connect to the WebSocket server
            this.session = container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    setSession(session);
                    System.out.println("WebSocket connected to room '" + roomName + "' with ID: " + roomId);

                    // Only add the message handler if none is registered
                    if (session.getMessageHandlers().isEmpty()) {
                        session.addMessageHandler(String.class, message -> handleIncomingMessage(message));
                    }
                }

                @Override
                public void onError(Session session, Throwable throwable) {
                    System.out.println("WebSocket error in room '" + roomName + "': " + throwable.getMessage());
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    System.out.println("WebSocket closed for room '" + roomName + "': " + closeReason.getReasonPhrase());
                }
            }, URI.create(uri));

            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect WebSocket for room '" + roomName + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Set a reference to the WebSocket Session
     * @param s reference to WebSocket Session.
     */
    public void setSession(Session s){
        this.session = s;
    }

    /**
     * handles an incoming message from the Chat Server.
     * Gson is needed here to parse the Incoming Message
     * to a WebSocketResponse Type.
     * Depending on the value of event, this function should
     * handling a regular chat message, a join event,
     * and a leave event.
     * @param msg
     */
    public void handleIncomingMessage(final String msg) {
        // Create a Gson instance to parse the message
        Gson gson = new Gson();

        try {
            // Parse the incoming message to a WebSocketResponse object
            WebSocketResponse response = gson.fromJson(msg, WebSocketResponse.class);

            // Determine the type of event and handle it accordingly
            String event = response.getEvent();
            String sender = response.getUsername();
            String message = response.getMessage();

            // Handle different events
            switch (event) {
                case "message":
                    // Handle regular chat message
                    System.out.println(sender + ": " + message);
                    Platform.runLater(() -> {
                        this.messages.getItems().add(
                                new MessageListCell(
                                        response.getUsername(),
                                        response.getMessage(),
                                        timestamp,
                                        messages
                                )
                        );
                    });
                    break;

                case "join":
                    // Handle join event
                    System.out.println(sender + " has joined the chat.");
                    Platform.runLater(() -> {
                        this.messages.getItems().add(
                                new MessageListCell(
                                        response.getUsername(),
                                        response.getMessage(),
                                        timestamp,
                                        messages
                                )
                        );
                        loadUsersPane();
                    });
                    break;

                case "leave":
                    // Handle leave event
                    System.out.println(sender + " has left the chat.");
                    Platform.runLater(() -> {
                        this.messages.getItems().add(
                                new MessageListCell(
                                        response.getUsername(),
                                        response.getMessage(),
                                        timestamp,
                                        messages
                                )
                        );
                        loadUsersPane();
                    });
                    break;

                default:
                    System.out.println("Message: " + msg);
                    break;
            }

        } catch (Exception e) {
            // Handle JSON parsing errors or other exceptions
            System.err.println("Error handling message: " + e.getMessage());
        }
    }

    /**
     * Send a message to the Chat Server.
     * The message must be in the correct format
     * when being sent to the server.
     * @param message to be send
     * @param e ActionEvent object.
     */
    public void sendMessage(String message, ActionEvent e) {

        try {
            // 4. Construct the JSON message using Gson
            JsonObject jsonMessage = new JsonObject();
            jsonMessage.addProperty("event", "message");
            jsonMessage.addProperty("room_id", roomId);
            jsonMessage.addProperty("user_id", userSession.getUserId());
            jsonMessage.addProperty("username", userSession.getUsername());
            jsonMessage.addProperty("message", message);
            jsonMessage.addProperty("timestamp", timestamp);

            // 5. Send the JSON string using the other sendMessage method
            sendMessage(new Gson().toJson(jsonMessage));

        } catch (Exception ex) {
            System.err.println("Error sending message: " + ex.getMessage());
        }
    }

    /**
     * send message to server using the current WebSocket
     * session.
     * @param message to be sent.
     */
    public void sendMessage(final String message) {
        this.session.getAsyncRemote().sendText(message);
    }

    /**
     * fetch current users in a active room.
     * Users should be added to the  users ListView
     */
    public void loadUsersPane() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://csc413.ajsouza.com/api/rooms/" + roomId + "/users"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    // Log the response body
                    System.out.println("Response Body: " + response.body());

                    if (response.statusCode() == 200) {
                        try {
                            // Parse the response into ListUsers object using Gson
                            ListUsers listUsers = new Gson().fromJson(response.body(), ListUsers.class);

                            // Check if the response state is "success"
                            if ("success".equals(listUsers.getStatus())) {
                                List<User> fetchedUsers = listUsers.getUsers();

                                // Update the UI on the JavaFX Application Thread
                                Platform.runLater(() -> {
                                    users.getItems().clear();

                                    if (fetchedUsers != null && !fetchedUsers.isEmpty()) {
                                        for (User user : fetchedUsers) {
                                            String username = user.getUsername();
                                            int userId = user.getUser_id();
                                            if (username != null) {
                                                users.getItems().add(username + " [" + userId + "]");
                                            }
                                        }
                                    } else {
                                        users.setPlaceholder(new Label("No users available"));
                                    }
                                });
                            }
                        } catch (JsonSyntaxException e) {
                            // No alert, just silent failure
                        }
                    }
                })
                .exceptionally(e -> null);
    }

    /**
     * fetch current messages in a active room.
     * Messages should be added to the messages ListView
     */
    public void loadMessages() {
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://csc413.ajsouza.com/api/rooms/" + roomId + "/messages";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)  // Extract response body
                .thenAccept(responseBody -> {
                    // Parse the JSON response
                    ListMessages listMessages = new Gson().fromJson(responseBody, ListMessages.class);

                    // Check if the response status is "success"
                    if ("success".equals(listMessages.getStatus())) {
                        Platform.runLater(() -> {
                            // Clear existing messages
                            messages.getItems().clear();

                            // Add each message to the ListView
                            if (listMessages.getMessages() != null) {
                                listMessages.getMessages().forEach(msg -> {
                                    // Create and add a new message cell for each message
                                    MessageListCell messageCell = new MessageListCell(
                                            msg.getUsername(),
                                            msg.getText(),
                                            msg.getCreated_at(),
                                            messages  // Pass the ListView reference
                                    );
                                    messages.getItems().add(messageCell);
                                });

                                // Scroll to the bottom to show the latest messages
                                if (!messages.getItems().isEmpty()) {
                                    messages.scrollTo(messages.getItems().size() - 1);
                                }
                            }
                        });
                    }
                })
                .exceptionally(e -> {
                    // Handle any exceptions silently
                    return null;
                });
    }
}