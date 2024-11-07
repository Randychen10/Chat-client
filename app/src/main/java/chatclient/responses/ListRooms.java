package chatclient.responses;

import java.util.List;
/**
 * Class used by Gson when parsing JSON responses
 * for fetching current list of active Rooms.
 */
public class ListRooms {
    private String status;
    private String message;
    private int rooms_count;
    private List<Room> rooms;

    public List<Room> getRooms() {
        return rooms;
    }

    public int getRooms_count() {
        return rooms_count;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

}
