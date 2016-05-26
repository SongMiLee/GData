package data.hci.gdatawatch.Network;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.hci.gdatawatch.Activity.CalendarActivity;
import data.hci.gdatawatch.R;

/**
 * Created by user on 2016-05-26.
 */
public class MakeRequestTask extends AsyncTask<Void, Void, ArrayList<ArrayList<String>>> {

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
    protected ArrayList<ArrayList<String>> doInBackground(Void... params) {
        try{
            Log.d("make request task", "execute");
            return getDataFromApi();
        }catch (UserRecoverableAuthIOException e){//다시 계정을 선택한다.
            Log.d("Auth Error","you need to re-choose Auth");
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
     * */
    private ArrayList<ArrayList<String>> getDataFromApi() throws IOException {
        List<String> eventStrings = new ArrayList<String>();

        Events events = service.events().list("primary")
                .setOrderBy("startTime")//이벤트 시작 순서
                .setSingleEvents(true)
                .execute();// Google로부터 내 캘린더 이벤트를 받아온다.

        List<Event> items = events.getItems();//캘린더 이벤트 집합

        ArrayList<String> name = new ArrayList<String>();
        ArrayList<String> place = new ArrayList<String>();
        ArrayList<String> startDate = new ArrayList<String>();
        ArrayList<String> endDate = new ArrayList<String>();
        ArrayList<String> person = new ArrayList<String>();

        ArrayList<ArrayList<String>> collection = new ArrayList<ArrayList<String>>();

        for(Event event : items){
            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();
            String eventName = event.getSummary();
            String eventPlace = event.getLocation();
            String eventPerson = event.getDescription();

            if(eventName == null) { eventName = "no event name"; }
            if(eventPlace == null ){ eventPlace = "no place"; }
            if(eventPerson == null || eventPerson.equals("person="))
            { eventPerson = "no person"; } else if(eventPerson.split("=")[0].equals("person")){     eventPerson=eventPerson.split("=")[1];        }
            if(start == null){   start = event.getStart().getDate();  }
            if(end == null){    end = event.getEnd().getDate(); }

            Log.d("event String", eventName + " " + eventPlace + " " + start.toString() + " " + end.toString() + " " + eventPerson);

            name.add(eventName); place.add(eventPlace);   startDate.add(start.toString());    endDate.add(end.toString());       person.add(eventPerson);
            //eventStrings.add(String.format("%s (%s) ~ (%s)", event.getSummary(), start, end));
        }

        collection.add(name);   collection.add(place); collection.add(startDate); collection.add(endDate); collection.add(person);
        return collection;//모든 이벤트 내역을 돌려준다.
    }

    @Override
    protected void onPostExecute(ArrayList<ArrayList<String>> arrayLists) {
        super.onPostExecute(arrayLists);
        CalendarActivity.addData(arrayLists.get(0), arrayLists.get(1), arrayLists.get(2), arrayLists.get(3), arrayLists.get(4));
    }
}
