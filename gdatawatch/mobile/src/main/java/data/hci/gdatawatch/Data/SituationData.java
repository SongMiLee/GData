package data.hci.gdatawatch.Data;

import android.content.Context;

import java.util.Calendar;

/**
 * Created by user on 2016-05-10.
 */
public class SituationData {
    int uid;
    String eventName, eventPlace, eventPerson;
    int eventStartYear, eventStartMonth, eventStartDate, eventStartHour, eventStartMinute;
    int eventEndYear, eventEndMonth, eventEndDate, eventEndHour, eventEndMinute;

    public SituationData(Context context,String name, String place, Calendar to, Calendar from, String person){
        uid = new PersonalPreference(context).getId();

        eventName = name;
        eventPlace = place;

        eventStartYear = to.getTime().getYear()+1900;
        eventStartMonth = to.getTime().getMonth()+1;
        eventStartDate = to.getTime().getDate();
        eventStartHour =to.getTime().getHours();
        eventStartMinute = to.getTime().getMinutes();

        eventEndYear = from.getTime().getYear()+1900;
        eventEndMonth = from.getTime().getMonth()+1;
        eventEndDate = from.getTime().getDate();
        eventEndHour = from.getTime().getHours();
        eventEndMinute = from.getTime().getMinutes();

        eventPerson =person;
    }

    public String getData(){

        if(eventPerson.isEmpty())
            eventPerson="no people";

        String result = "uid="+uid+"&eventName="+eventName+"&eventPlace="+eventPlace+"&eventPeople="+eventPerson+"&eventStartYear="+eventStartYear+
                "&eventStartMonth="+eventStartMonth+"&eventStartDate="+eventStartDate+"&eventStartHour="+eventStartHour+"&eventStartMinute="+eventStartMinute+
                "&eventEndYear="+eventEndYear+"&eventEndMonth="+eventEndMonth+"&eventEndDate="+eventEndDate+"&eventEndHour="+eventEndHour+"&eventEndMinute="+eventStartMinute;

        return  result;
    }
}
