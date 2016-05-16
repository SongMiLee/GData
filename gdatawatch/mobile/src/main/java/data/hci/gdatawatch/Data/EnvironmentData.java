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

    public void setAction(int type, int confidence){
        actionType = type;
        actionConfidence = confidence;
    }

    public static String getData(){
        String data = "uid=" + uid + "&lat=" + lat + "&lng=" + lng
                + "&gyroX=" + gyroX + "&gyroY=" + gyroY + "&gyroZ=" + gyroZ
                + "&accelX=" + accelX + "&accelY=" + accelY + "&accelZ="
                + accelZ+"&activityType="+actionType+"&activityConfidence="+actionConfidence;

        Log.d("Activity type log ", actionType+"");
        return data;
    }
}
