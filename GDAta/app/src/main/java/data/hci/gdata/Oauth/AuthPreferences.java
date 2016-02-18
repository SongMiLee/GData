package data.hci.gdata.Oauth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by user on 2016-02-18.
 */
public class AuthPreferences {
    private SharedPreferences preferences;

    public AuthPreferences(Context context){
        preferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public void setUser(String user){
        Editor editor = preferences.edit();
        editor.putString("user", user);
        editor.commit();
    }

    public void setToken(String password){
        Editor editor = preferences.edit();
        editor.putString("token", password);
        editor.commit();
    }

    public String getUser(){ return preferences.getString("user", null); }
    public String getToken(){ return  preferences.getString("token", null); }
}
