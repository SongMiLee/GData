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



public class AccelService extends Service implements SensorEventListener {
    SensorManager sensorManager;//센서 매니저
    Sensor accelSensor;//가속도 센서

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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
        float[] gravity = new float[3];
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            gravity[0] = event.values[0];
            gravity[1] = event.values[1];
            gravity[2] = event.values[2];
        }
        if (event.sensor.getType() == accelSensor.TYPE_ACCELEROMETER) {
            final float alpha = 0.8f;

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            float x = (float)mKalmanAccX.update(event.values[0] - gravity[0]);
            float y = (float)mKalmanAccX.update(event.values[1] - gravity[1]);
            float z = (float)mKalmanAccX.update(event.values[2] - gravity[2]);

            //엑셀 값을 브로드 캐스트로 보낸다.
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(StaticVariable.BROADCAST_ACCEL);

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

        private void measurementUpdate() {
            K = (P + Q) / (P + Q + R);
            P = R * (P + Q) / (R + P + Q);
        }

        public double update(double measurement) {
            measurementUpdate();
            X = X + (measurement - X) * K;

            return X;
        }
    }
}
