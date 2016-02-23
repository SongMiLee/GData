package data.hci.gdata.Activity;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.hci.gdata.Global.StaticVariable;
import data.hci.gdata.Oauth.AuthPreferences;
import data.hci.gdata.R;


public class CalendarActivity extends AppCompatActivity  {
    CalendarView calendarView;

    AccountManager accountManager;
    AuthPreferences authPreferences;

    GoogleAccountCredential credential;

    Button invalidateBtn;

    private final String SCOPE = "https://www.googleapis.com/auth/googletalk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = (CalendarView) findViewById(R.id.calendarView);
        invalidateBtn = (Button)findViewById(R.id.btn_invalidate);
        invalidateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invalidateToken();
                finish();
            }
        });

        accountManager = AccountManager.get(this);
        authPreferences = new AuthPreferences(this);
        credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(StaticVariable.CALENDAR_SCOPES)) //credential 초기화!
                .setBackOff(new ExponentialBackOff());
        if(authPreferences.getUser() != null && authPreferences.getToken() != null)
            doAuthenticatedStuff();
        else
            chooseAccount();

    }

    private void doAuthenticatedStuff(){
        Log.d("Token", authPreferences.getToken());
        //이미 기존의 값이 있다면 기존 값으로 credentail 초기화
        credential.setSelectedAccountName(authPreferences.getUser());

        new MakeRequestTask(credential).execute();
    }

    /**
     * 어플리케이션과 연동할 Account 선택
     * */
    private void chooseAccount(){
        Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
        startActivityForResult(intent, StaticVariable.REQUEST_PICK_ACCOUNT);
    }

    /**
     * 구글에 토큰을 요청
     * */
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

    /**
     * 기존의 토큰을 없애버리는 함수
     * */
    private void invalidateToken(){
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken("com.google", authPreferences.getToken());
        authPreferences.setToken(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case StaticVariable.REQUEST_AUTHORIZATION://승인이 된 account의 토큰 요청
                if(resultCode == RESULT_OK){
                    requestToken();
                }
                break;

            case StaticVariable.REQUEST_PICK_ACCOUNT://Account를 고르는 요청
                if(resultCode == RESULT_OK && data!=null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    credential.setSelectedAccountName(accountName);//선택된 account를 credential에 설정
                    authPreferences.setUser(accountName);
                    invalidateToken();

                    requestToken();
                }
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

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>>{
        Calendar service = null;
        Exception error = null;

        public MakeRequestTask(GoogleAccountCredential credential){
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            com.google.api.client.json.JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            service = new Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName(String.valueOf(R.string.app_name))
                    .build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try{
                Log.d("make request task", "execute");
                return getDataFromApi();
            }catch (UserRecoverableAuthIOException e){//다시 계정을 선택한다.
                startActivityForResult(e.getIntent(), StaticVariable.REQUEST_PICK_ACCOUNT);
                return null;
            }catch (Exception e){
                e.printStackTrace();
                error = e;
                e.printStackTrace();
                cancel(true);
                return null;
            }
        }

        /**
         * Google Calender로부터 이벤트를 받아오는 함수
         * 현재 시간 이후의 이벤트를 가져온다.
         * */
        private List<String> getDataFromApi() throws IOException{
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();

            Events events = service.events().list("primary")
                    .execute();// Google로부터 내 캘린더 이벤트를 받아온다.

            List<Event> items = events.getItems();
            for(Event event : items){
                DateTime start = event.getStart().getDateTime();
                if(start == null){
                    start = event.getStart().getDate();
                }
                Log.d("event String", event.getSummary());
                eventStrings.add(String.format("%s (%s)", event.getSummary(), start));
            }

            return eventStrings;//모든 이벤트 내역을 돌려준다.
        }
    }
}
