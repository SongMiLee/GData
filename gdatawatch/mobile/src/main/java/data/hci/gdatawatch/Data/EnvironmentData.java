package data.hci.gdatawatch.Data;

import android.util.Log;

/**
 * Created by user on 2016-03-17.
 *
 * 사용자가 생성하는 개인정보
 */
public class EnvironmentData {
    //user id
    private static int uid;

    //accelerometer
    private static double accelX=0;//+- 16g
    private static double accelY=0;
    private static double accelZ=0;

    //gyroscope 17.453293
    private static double gyroX=0;
    private static double gyroY=0;
    private static double gyroZ=0;

    //gps
    private static double lat;// -90 ~ 90
    private static double lng; // -180 ~ 180

    private static int actionType = 4; // unknown
    private static int actionConfidence = 0;

    private static double temp,rain,humidity;

    private static String placeName=null, placeType=null;

    public void setID(int id){ uid=id;  }
    public void setAccel(double x, double y, double z){
        accelX = x;
        accelY = y;
        accelZ = z;
    }
    public void setGyro(double x, double y, double z){
        gyroX = x;
        gyroY = y;
        gyroZ = z;
    }

    public void setGPS(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public static double getLat(){ return lat; }
    public static double getLng(){ return lng; }

    public void setAction(int type, int confidence){
        actionType = type;
        actionConfidence = confidence;
    }

    public static void setWeather(double t, double r, double h){
        temp = t;
        rain = r;
        humidity = h;
    }

    public static void setPlace(String name, String type){
        placeName = name;
        placeType = type;
    }

    public static String getData(){
        String data = "uid=" + uid + "&lat=" + lat + "&lng=" + lng
                +"&temp="+temp+"&rain="+rain+"&humidity="+humidity
                + "&gyroX=" + gyroX + "&gyroY=" + gyroY + "&gyroZ=" + gyroZ
                + "&accelX=" + accelX + "&accelY=" + accelY + "&accelZ="
                + accelZ+"&activityType="+actionType+"&activityConfidence="+actionConfidence
                +"&placeName="+placeName+"&placeType="+placeType;

        Log.d("Activity type log ", actionType+"");
        return data;
    }
}
