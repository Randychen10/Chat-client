package chatclient;

import java.util.Objects;

public class UserSession {
    private static UserSession session = null;
    private String username;
    private int userId;
    private String token;

    /**
     * Private Constructor to not allow outside instantiation.
     */
    private UserSession(){}

    /**
     * use to get an instance of UserSession.
     * If there is no current user Session, a new one is made.
     * It is important that after you call this function call initSession
     * @return instance of UserSession
     */
    public static UserSession getInstance(){
        if (session == null) {
            session = new UserSession();
        }
        return session;
    }

    /**
     * Initial a UserSession from login response.
     * @param username of logged in user.
     * @param userId of logged is user
     * @param token used to access API and connect to WebSocket.
     */
    public void initSession(String username, int userId, String token){
        this.username = username;
        this.userId = userId;
        this.token = token;
    }

    public String getUsername() {
        if(Objects.isNull(username)){
            throw new IllegalStateException("User session it not correctly initialized :: username is missing");
        }
        return username;
    }

    public int getUserId() {
        if(this.userId < 0){
            throw new IllegalStateException("User session it not correctly initialized :: userId is missing");
        }
        return userId;
    }

    public String getToken() {
        if(Objects.isNull(token)){
            throw new IllegalStateException("User session it not correctly initialized :: token is missing");
        }
        return token;
    }

    /**
     * used to clean current UserSession. Be sure to
     * set all data-fields to values that denote the session
     * is not active.
     */
    public void clearSession(){
        this.userId = -1;
        this.token = null;
        this.username = null;
        UserSession.session = null;
    }
}
