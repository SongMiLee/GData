package data.hci.gdatawatch.Thread;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;

import data.hci.gdatawatch.Data.EnvironmentData;
import data.hci.gdatawatch.Network.EnvironmentPostTask;

/**
 * Created by user on 2016-03-18.
 */
public class SendEnviro implements Runnable {
    private Context context;
    private EnvironmentData ed;

    public SendEnviro(EnvironmentData ed, Context context)
    {
        this.ed = ed;
        this.context = context;
        ed.setID(1);
    }
    @Override
    public void run() {
        while(true) {
            try {
                new EnvironmentPostTask().execute(ed.getData());
                //writeData(ed.getData(), context.getApplicationContext());
                Thread.sleep(1000*60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void writeData(String data, Context context){
        try{
            File file = new File(context.getFilesDir(), "Environment.txt");

            FileOutputStream fos = context.openFileOutput(file.getAbsolutePath(), Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();

        }catch (Exception e){ e.printStackTrace();}
    }
}
