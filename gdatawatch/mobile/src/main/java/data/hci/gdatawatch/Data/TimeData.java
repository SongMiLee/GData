package data.hci.gdatawatch.Data;

import java.util.Calendar;

/**
 * Created by user on 2016-03-18.
 */
public class TimeData {
    //시간관련 변수
    Calendar calendar;
    int year;
    int month;
    int day;
    int hour;
    int minute;
    int season;
    String[] strSeason = {"봄","여름","가을","겨울"};
    String nowDate;

    public String getNowDate(){
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);

        nowDate = year + "/";
        if (month < 10) {
            nowDate += "0";
        }
        nowDate += month + "/";
        if (day < 10) {
            nowDate += "0";
        }
        nowDate += day + " ";
        if (hour < 10) {
            nowDate += "0";
        }
        nowDate += hour + ":";
        if (minute < 10) {
            nowDate += "0";
        }
        nowDate += minute;

        switch (month){
            case 3:case 4:case 5:
                season = 0;
                break;
            case 6:case 7:case 8:
                season = 1;
                break;
            case 9:case 10:case 11:
                season = 2;
                break;
            default:
                season = 3;
        }

        return nowDate += strSeason[season];
    }

}
