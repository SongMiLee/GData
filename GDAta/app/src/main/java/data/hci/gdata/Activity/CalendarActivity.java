package data.hci.gdata.Activity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;

import com.google.android.gms.common.AccountPicker;

import data.hci.gdata.Global.StaticVariable;
import data.hci.gdata.R;

public class CalendarActivity extends AppCompatActivity {
    CalendarView calendarView;
    CalendarContract.Calendars calendars;//캘린더별 정보, 행마다 캘린더의 세부정보 이름, 색상, 동기화 정보
    CalendarContract.Instances instances;//이벤트 시작과 종료 시간
    CalendarContract.Attendees attendees;//이벤트 참석자 정보
    CalendarContract.Reminders reminders;// 경고/알림 데이터.

    String accountName;
    String accountType="com.google";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initUI();
        pickUserAccount();
    }

    /**
     * UI 초기화
     * */
    void initUI(){
        calendarView = (CalendarView)findViewById(R.id.calendarView);

        Uri uri = CalendarContract.Calendars.CONTENT_URI;

    }

    private void pickUserAccount(){
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, StaticVariable.REQUEST_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == StaticVariable.REQUEST_PICK_ACCOUNT){
            if(resultCode==RESULT_OK){
                accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                Log.d("User name", accountName);
            }
        }
    }

    /**
     * 동기화 어댑터
     * */
    static Uri asSyncAdapter(Uri uri, String account, String accountType){
        return uri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType).build();
    }
}
