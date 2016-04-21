package data.hci.gdatawatch.Thread;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import data.hci.gdatawatch.Data.EnvironmentData;
import data.hci.gdatawatch.Data.PersonalPreference;
import data.hci.gdatawatch.Network.EnvironmentPostTask;
import data.hci.gdatawatch.Network.RestRequestHelper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by user on 2016-03-18.
 */
public class SendEnviro implements Runnable {
    private Context context;
    private EnvironmentData ed;
    private RestRequestHelper restRequestHelper;

    public SendEnviro(EnvironmentData ed, Context context)
    {
        this.ed = ed;
        this.context = context;
        ed.setID(PersonalPreference.getId());

        restRequestHelper = RestRequestHelper.newInstance();
    }
    @Override
    public void run() {
        while(true) {
            try {
                //new EnvironmentPostTask().execute(ed.getData());k
                restRequestHelper.enviroData(ed.getData(), new Callback<Integer>() {
                    @Override
                    public void success(Integer integer, Response response) {
                        Log.d("Response Server", integer+"");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        error.printStackTrace();
                    }
                });
                Thread.sleep(1000*1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
