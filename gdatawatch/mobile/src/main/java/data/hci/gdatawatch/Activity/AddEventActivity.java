package data.hci.gdatawatch.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import data.hci.gdatawatch.Global.StaticVariable;
import data.hci.gdatawatch.Oauth.AuthPreferences;
import data.hci.gdatawatch.R;

public class AddEventActivity extends AppCompatActivity {
    EditText person, location, name;
    TextView startDate, startTime, endDate, endTime;
    Button insert;
    Calendar toCalendar, fromCalendar;


    GoogleAccountCredential credential;
    private final String SCOPE="https://www.googleapis.com/auth/calendar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(StaticVariable.CALENDAR_SCOPES)) //credential 초기화!
                .setBackOff(new ExponentialBackOff());
        credential.setSelectedAccountName(new AuthPreferences(getApplicationContext()).getUser());

        init();
    }

    void init(){
        person = (EditText)findViewById(R.id.event_person);
        location = (EditText)findViewById(R.id.event_location);
        name = (EditText)findViewById(R.id.event_name);
        startDate = (TextView)findViewById(R.id.select_start_date);
        endDate = (TextView)findViewById(R.id.select_end_date);
        startTime = (TextView)findViewById(R.id.select_start_time);
        endTime = (TextView)findViewById(R.id.select_end_time);
        insert = (Button)findViewById(R.id.insert_event);

        toCalendar = Calendar.getInstance();
        fromCalendar = Calendar.getInstance();

        //날짜&시간 설정 다이얼로그 호출
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddEventActivity.this, toDate, toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH), toCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(AddEventActivity.this, toTime, toCalendar.get(Calendar.HOUR_OF_DAY), toCalendar.get(Calendar.MINUTE), false).show();
            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddEventActivity.this, fromDate, fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(AddEventActivity.this, fromTime, fromCalendar.get(Calendar.HOUR_OF_DAY), fromCalendar.get(Calendar.MINUTE), false).show();
            }
        });

       insert.setOnClickListener(insertEvent);
    }

    DatePickerDialog.OnDateSetListener toDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            toCalendar.set(Calendar.YEAR, year);
            toCalendar.set(Calendar.MONTH, monthOfYear);
            toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            startDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(toCalendar.getTime()));
        }
    };

    TimePickerDialog.OnTimeSetListener toTime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            toCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            toCalendar.set(Calendar.MINUTE, minute);

            startTime.setText(new SimpleDateFormat("HH:mm").format(toCalendar.getTime()));
        }
    };

    DatePickerDialog.OnDateSetListener fromDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            fromCalendar.set(Calendar.YEAR, year);
            fromCalendar.set(Calendar.MONTH, monthOfYear);
            fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            endDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(fromCalendar.getTime()));
        }
    };

    TimePickerDialog.OnTimeSetListener fromTime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            fromCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            fromCalendar.set(Calendar.MINUTE, minute);

            endTime.setText(new SimpleDateFormat("HH:mm").format(fromCalendar.getTime()));
        }
    };

    View.OnClickListener insertEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MakeRequestTask(credential).execute();

            Intent intent = new Intent();
            intent.putExtra("result", StaticVariable.CALENDAR_ACCEPT);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        com.google.api.services.calendar.Calendar service = null;
        Exception error = null;

        public MakeRequestTask(GoogleAccountCredential credential){
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            com.google.api.client.json.JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            service = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName(String.valueOf(R.string.app_name))
                    .build();
        }

        @Override
        protected Void doInBackground(Void... params) {
            com.google.api.services.calendar.Calendar service = null;
            Exception error = null;

            try{
                insertData();
            }catch (Exception e){
                error = e;
                error.printStackTrace();
                cancel(true);

                return null;
            }

            return null;
        }

        private void insertData() throws IOException {
            Event event = new Event()
                    .setSummary(name.getText().toString())
                    .setLocation(location.getText().toString())
                    .setDescription("person="+person.getText().toString());

            DateTime startDateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ").format(toCalendar.getTime()));
            Log.d("date time", startDateTime.toString());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime);

            event.setStart(start);

            DateTime endDateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ").format(fromCalendar.getTime()));;
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime);

            event.setEnd(end);

            event = service.events().insert("primary", event).execute();
            Log.d("Event Created: %s", event.getHtmlLink());

        }
    }
}
