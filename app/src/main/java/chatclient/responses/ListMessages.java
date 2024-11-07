package chatclient.responses;

import java.util.List;
/**
 * Class used by Gson when parsing JSON responses
 * for fetching current messages in a active room.
 */
public class ListMessages {
    private String status;
    private int count;
    private List<Message> messages;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
