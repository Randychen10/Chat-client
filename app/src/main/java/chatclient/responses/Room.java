package chatclient.responses;

/**
 * Class used by Gson when parsing JSON responses
 * for representing a Room.
 */
public class Room {
    private int id;
    private String room_name;
    private int owner_id;
    private int active;
    private String created_at;

    /**
     * @return the active
     */
    public int getActive() {
        return active;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the owner_id
     */
    public int getOwner_id() {
        return owner_id;
    }

    /**
     * @return the created_at
     */
    public String getCreated_at() {
        return created_at;
    }

    /**
     * @return the room_name
     */
    public String getRoom_name() {
        return room_name;
    }

}
