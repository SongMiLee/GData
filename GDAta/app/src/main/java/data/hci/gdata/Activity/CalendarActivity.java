package data.hci.gdata.Activity;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;

import data.hci.gdata.Global.StaticVariable;
import data.hci.gdata.Oauth.AuthPreferences;
import data.hci.gdata.R;


public class CalendarActivity extends AppCompatActivity  {
    CalendarView calendarView;

    AccountManager accountManager;
    AuthPreferences authPreferences;

    private final String SCOPE = "https://www.googleapis.com/auth/googletalk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        accountManager = AccountManager.get(this);
        authPreferences = new AuthPreferences(this);
        if(authPreferences.getUser() != null && authPreferences.getToken() != null)
            doAuthenticatedStuff();
        else
            chooseAccount();

        initUI();
    }

    /**
     * UI 초기화
     * */
    void initUI() {
        calendarView = (CalendarView) findViewById(R.id.calendarView);

    }

    private void doAuthenticatedStuff(){ Log.d("Token", authPreferences.getToken());}

    private void chooseAccount(){
        Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
        startActivityForResult(intent, StaticVariable.REQUEST_PICK_ACCOUNT);
    }

    private void requestToken(){
        Account userAccount = null;
        String user = authPreferences.getUser();
        for(Account account : accountManager.getAccountsByType("com.google")){
            if(account.name.equals(user))
                userAccount = account;
            break;
        }
        accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, this, new OnTokenAcquired(), null);
    }

    private void invalidateToken(){
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken("com.google", authPreferences.getToken());
        authPreferences.setToken(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case StaticVariable.REQUEST_AUTHORIZATION:
                if(resultCode == RESULT_OK)
                    requestToken();
                break;

            case StaticVariable.REQUEST_PICK_ACCOUNT:
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                authPreferences.setUser(accountName);
                invalidateToken();

                requestToken();
                break;
        }
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();

                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, StaticVariable.REQUEST_AUTHORIZATION);
                } else {
                    String token = bundle
                            .getString(AccountManager.KEY_AUTHTOKEN);

                    authPreferences.setToken(token);

                    doAuthenticatedStuff();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
