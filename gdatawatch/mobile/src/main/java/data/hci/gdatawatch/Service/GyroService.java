package data.hci.gdatawatch.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import data.hci.gdatawatch.Global.StaticVariable;


public class GyroService extends Service implements SensorEventListener {

    SensorManager mSensorManager;
    Sensor gyroSensor;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);   //센서 매니저 사용
        gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this,gyroSensor,SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy(){
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == gyroSensor.TYPE_GYROSCOPE){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //다른 액티비티들에게 내용을 보낸다.
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(StaticVariable.BROADCAST_GYRO);

            broadcastIntent.putExtra("x", x);
            broadcastIntent.putExtra("y", y);
            broadcastIntent.putExtra("z", z);

            sendBroadcast(broadcastIntent);

        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
