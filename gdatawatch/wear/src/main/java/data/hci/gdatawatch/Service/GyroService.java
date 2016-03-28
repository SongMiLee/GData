package data.hci.gdatawatch.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import data.hci.gdatawatch.Global.StaticVariable;


public class GyroService extends Service implements SensorEventListener {

    SensorManager mSensorManager;
    Sensor gyroSensor;

    private float[] rolling = new float[3];
    private float mFilteringFactor = 0.8f;
    private KalmanFilter mKalmanAccX;
    private KalmanFilter mKalmanAccY;
    private KalmanFilter mKalmanAccZ;

    @Override
    public void onCreate() {
        mKalmanAccX = new KalmanFilter(0.0f);
        mKalmanAccY = new KalmanFilter(0.0f);
        mKalmanAccZ = new KalmanFilter(0.0f);
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);   //센서 매니저 사용
        gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this,gyroSensor,SensorManager.SENSOR_DELAY_NORMAL);

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


            // smooth data (low-pass filter)
            rolling[0] = (x * mFilteringFactor) + (rolling[0] * (1.0f - mFilteringFactor));
            rolling[1] = (y * mFilteringFactor) + (rolling[1] * (1.0f - mFilteringFactor));
            rolling[2] = (z * mFilteringFactor) + (rolling[2] * (1.0f - mFilteringFactor));

            x = (float)mKalmanAccX.update(rolling[0]);
            y = (float)mKalmanAccY.update(rolling[1]);
            z = (float)mKalmanAccZ.update(rolling[2]);

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

    class KalmanFilter {

        private double Q = 0.00001;
        private double R = 0.001;
        private double X = 0, P = 1, K;

        KalmanFilter(double initValue) {
            X = initValue;
        }

        private void measurementUpdate(){
            K = (P + Q) / (P + Q + R);
            P = R * (P + Q) / (R + P + Q);
        }

        public double update(double measurement){
            measurementUpdate();
            X = X + (measurement - X) * K;

            return X;
        }

    }
}
