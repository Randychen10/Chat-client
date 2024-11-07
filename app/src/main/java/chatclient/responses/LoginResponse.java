package chatclient.responses;

/**
 * Class used by Gson when parsing JSON responses
 * for login response.
 */
public class LoginResponse {
    private String status;
    private String message;
    private int user_id;
    private String username;
    private String token;


    public LoginResponse(String status, String message, int user_id, String username, String token) {
        this.status = status;
        this.message = message;
        this.user_id = user_id;
        this.username = username;
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

}
