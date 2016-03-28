package data.hci.gdatawatch.Service;

/**
 * Created by 정민 on 2016-03-27.
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import data.hci.gdatawatch.Data.TimeData;
import data.hci.gdatawatch.Global.StaticVariable;


public class TimeService extends Service implements Runnable {

    Thread timeThread;
    @Override
    public void onCreate() {
        super.onCreate();

    }

    public int onStartCommand(Intent intent, int flags, int startId){

        timeThread = new Thread(this);
        timeThread.start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy(){
        super.onDestroy();
        timeThread.stop();
    }

    public void run() {
        while(true) {

            String nowDate =  new TimeData().getNowDate();

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(StaticVariable.BROADCAST_TIME);

            broadcastIntent.putExtra("date", nowDate);
            Log.d("시간", nowDate);

            sendBroadcast(broadcastIntent);

            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
