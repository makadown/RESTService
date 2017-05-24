package makadown.com.restservice;

import android.app.Application;

import makadown.com.restservice.data.User;

/**
 * Created by usuario on 24/05/2017.
 */

public class RESTServiceApplication extends Application {
    private static final String TAG = RESTServiceApplication.class.getName();
    private static RESTServiceApplication instance;

    private User user;
    private String accessToken;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        user = new User();
    }

    public static RESTServiceApplication getInstance()
    {
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
