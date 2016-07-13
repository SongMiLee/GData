package data.hci.gdatawatch.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import data.hci.gdatawatch.Data.EnvironmentData;
import data.hci.gdatawatch.Network.RestRequestHelper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PlaceService extends Service {
    private Handler handler;//반복 수행을 위해 사용하는 핸들러
    private RestRequestHelper restRequestHelper;//네트워크 연결
    private Thread networkThread;

    public PlaceService() {
        restRequestHelper = RestRequestHelper.newInstance();
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        handler.post(updateStatus);
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateStatus);//핸들러를 제거 => 반복 수행 끝냄
        handler = null;
    }

    private Runnable updateStatus = new Runnable() {
        @Override
        public void run() {
            // do something here
            networkThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    restRequestHelper.getPlace(EnvironmentData.getLat(), EnvironmentData.getLng(), new Callback<String>() {
                        @Override
                        public void success(String s, Response response) {
                            String [] token=s.split("'");

                            EnvironmentData.setPlace(token[0], token[1]);
                            Log.d("GPT", token[0]+" "+token[1]);
                        }
                        @Override
                        public void failure(RetrofitError error) {  error.printStackTrace();   }
                    });
                }
            });

            networkThread.start();
            handler.postDelayed(updateStatus, 1000 * 10);
        }
    };
}
