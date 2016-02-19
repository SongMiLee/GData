package data.hci.gdata.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import data.hci.gdata.Global.StaticVariable;

public class AccelService extends Service implements SensorEventListener {
    SensorManager sensorManager;//센서 매니저
    Sensor accelSensor;//가속도 센서

    public AccelService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);//센서 등록

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float []gravity = new float[3];
        if(event.sensor.getType()==Sensor.TYPE_GRAVITY){
            gravity[0] = event.values[0];
            gravity[1] = event.values[1];
            gravity[2] = event.values[2];
        }
        if(event.sensor.getType()==accelSensor.TYPE_ACCELEROMETER){
            final float alpha = 0.8f;

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            float x = event.values[0] - gravity[0];
            float y = event.values[1] - gravity[1];
            float z = event.values[2] - gravity[2];

            //엑셀 값을 브로드 캐스트로 보낸다.
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(StaticVariable.BROADCAST_ACCEL);

            broadcastIntent.putExtra("x",x);
            broadcastIntent.putExtra("y", y);
            broadcastIntent.putExtra("z", z);

            sendBroadcast(broadcastIntent);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
