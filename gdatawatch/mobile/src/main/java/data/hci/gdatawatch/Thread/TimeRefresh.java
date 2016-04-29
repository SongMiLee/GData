package data.hci.gdatawatch.Thread;


import android.app.Activity;
import android.os.AsyncTask;

import data.hci.gdatawatch.Activity.MapActivity;
import data.hci.gdatawatch.Data.TimeData;

/**
 * Created by user on 2016-04-22.
 */
public class TimeRefresh extends AsyncTask<String, Void, String> implements Runnable {
    private Activity activity;

    public TimeRefresh(Activity activity){
        this.activity = activity;
    }

    @Override
    public void run() {
        while(true){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MapActivity.setTimeText(new TimeData().getNowDate());
                }
            });
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {
        return null;
    }
}
