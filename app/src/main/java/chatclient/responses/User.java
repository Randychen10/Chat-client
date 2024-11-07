package chatclient.responses;


/**
 * Class used by Gson when parsing JSON responses
 * for representing a User.
 */
public class User {
    private int user_id;
    private String username;
    private String token;
    private String created_at;
    private String email;
    private String api_key;

    public int getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getEmail() {
        return email;
    }

    public String getApi_key() {
        return api_key;
    }
}
