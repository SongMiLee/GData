package data.hci.gdatawatch.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import data.hci.gdatawatch.Data.EnvironmentData;
import data.hci.gdatawatch.Data.PersonalPreference;
import data.hci.gdatawatch.Network.EnvironmentPostTask;


/**
 * 숙대 서버로 보내는 서비스
 * */
public class SService extends Service {
    private Handler handler;
    private Thread networkThread;
    EnvironmentData ed;

    public SService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ed = new EnvironmentData();
        ed.setID(PersonalPreference.getId());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        handler = new Handler();
        handler.post(updateStatus);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateStatus);
        handler = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Runnable updateStatus = new Runnable() {
        @Override
        public void run() {
            // do something here
            networkThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new EnvironmentPostTask().execute(ed.getData());
                        //Thread.sleep(1000 * 60);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            networkThread.start();
            handler.postDelayed(updateStatus, 1000 * 60);
        }
    };
}
