package data.hci.gdatawatch.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import data.hci.gdatawatch.Data.EnvironmentData;
import data.hci.gdatawatch.Data.PersonalPreference;
import data.hci.gdatawatch.Network.RestRequestHelper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SendDataService extends Service {
    private Handler handler;//반복 수행을 위해 사용하는 핸들러
    private RestRequestHelper restRequestHelper;//네트워크 연결
    private Thread networkThread;
    EnvironmentData ed;

    public SendDataService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        restRequestHelper = RestRequestHelper.newInstance();
        handler = new Handler();
        ed = new EnvironmentData();
        ed.setID(PersonalPreference.getId());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        handler.post(updateStatus);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateStatus);//핸들러를 제거 => 반복 수행 끝냄
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
                        restRequestHelper.enviroData(ed.getData(), new Callback<Integer>() {
                            @Override
                            public void success(Integer integer, Response response) {
                                Log.d("Response Server", integer + "");
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                error.printStackTrace();
                            }
                        });
                        Thread.sleep(1000 * 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                }
            });

            networkThread.start();
            handler.postDelayed(updateStatus, 1000);
        }
    };
}
