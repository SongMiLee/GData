package data.hci.gdatawatch.Data;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by user on 2016-03-17.
 *
 * 사용자가 생성하는 개인정보
 */
public class EnvironmentData {
    //user id
    private int uid;

    //accelerometer
    private double accelX=0;//+- 16g
    private double accelY=0;
    private double accelZ=0;

    //gyroscope 17.453293
    private double gyroX=0;
    private double gyroY=0;
    private double gyroZ=0;

    //gps
    private double lat;// -90 ~ 90
    private double lng; // -180 ~ 180

    private int actionType = -1; // unknown
    private int actionConfidence;

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

    public int getUid(){   return uid;  }
    public double getAccelX(){ return accelX; }
    public double getAccelY(){ return  accelY;}
    public double getAccelZ(){ return accelZ; }
    public double getGyroX(){ return gyroX; }
    public double getGyroY(){ return gyroY; }
    public double getGyroZ(){ return gyroZ;}
    public double getLat(){ return lat;}
    public double getLng(){ return lng;}
    public String getData(){
        String data = "uid=" + uid + "&lat=" + lat + "&lng=" + lng
                + "&gyroX=" + gyroX + "&gyroY=" + gyroY + "&gyroZ=" + gyroZ
                + "&accelX=" + accelX + "&accelY=" + accelY + "&accelZ="
                + accelZ+"&type="+actionType+"&confidence="+actionConfidence;

        return data;
    }
}
