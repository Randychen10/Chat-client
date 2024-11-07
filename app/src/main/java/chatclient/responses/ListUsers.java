package chatclient.responses;

import java.util.List;

/**
 * Class used by Gson when parsing JSON responses
 * for fetching current users in a active room.
 */
public class ListUsers {
    private String status;
    private String message;
    private Room room;
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }

    public Room getRoom() {
        return room;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }



}
