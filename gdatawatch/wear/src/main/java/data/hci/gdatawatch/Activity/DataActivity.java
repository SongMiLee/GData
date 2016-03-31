package data.hci.gdatawatch.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import data.hci.gdatawatch.Global.StaticVariable;
import data.hci.gdatawatch.R;

public class DataActivity extends Activity {

    private double ax = 0;
    private double ay = 0;
    private double az = 0;

    private double gx = 0;
    private double gy = 0;
    private double gz = 0;

    private double pAx = 0;
    private double pAy = 0;
    private double pAz = 0;

    private double pGx = 0;
    private double pGy = 0;
    private double pGz = 0;

    int C=0;

    double sumA=0;
    double sumG=0;
    int count = 0;

    ListView dataListView;
    ArrayAdapter<String> dataAdapter;
    TextView averTextView;

    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Gyro
            if(intent.getAction().equals((StaticVariable.BROADCAST_GYRO))){
                gx = Math.round(intent.getFloatExtra("x", 0)*100d) / 100d;
                gy = Math.round(intent.getFloatExtra("y", 0)*100d) / 100d;
                gz = Math.round(intent.getFloatExtra("z", 0)*100d) / 100d;
            }
            //Accel
            else if(intent.getAction().equals(StaticVariable.BROADCAST_ACCEL)){
                ax = Math.round(intent.getFloatExtra("x", 0)*100d) / 100d;
                ay = Math.round(intent.getFloatExtra("y", 0)*100d) / 100d;
                az = Math.round(intent.getFloatExtra("z", 0)*100d) / 100d;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        intentFilter = new IntentFilter();
        intentFilter.addAction(StaticVariable.BROADCAST_GYRO);
        intentFilter.addAction(StaticVariable.BROADCAST_ACCEL);

        averTextView = (TextView)findViewById(R.id.tv_Aver);
        dataListView = (ListView)findViewById(R.id.lv_data);
        dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);
        dataListView.setAdapter(dataAdapter);

        registerReceiver(broadcastReceiver, intentFilter);

        new Thread(new dataThread()).start();
    }

    public class dataThread implements Runnable {
        @Override
        public void run() {
            while(C<40) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        double Ax =ax;
                        double Ay =ay;
                        double Az =az;
                        double Gx =gx;
                        double Gy =gy;
                        double Gz =gz;
                        String aver = "";
                        String data = "";

                        if(C == 1)
                        {
                            pAx=Ax;pAy=Ay;pAz=Az;
                            pGx=Gx;pGy=Gy;pGz=Gz;
                        }

                        double accChange1 = Math.abs(Ax-pAx)+Math.abs(Ay-pAy)+Math.abs(Az-pAz);
                        double gyroChange1 = Math.abs(Gx-pGx)+Math.abs(Gy-pGy)+Math.abs(Gz-pGz);
                        double accChange2 = Math.abs(Ax+Ay+Az-pAx-pAy-pAz);
                        double gyroChange2 = Math.abs(Gx+Gy+Gz-pGx-pGy-pGz);
                        double accChange3 = Math.sqrt(Ax * Ax + Ay * Ay + Az * Az);
                        double gyroChange3 = Math.sqrt(Gx*Gx + Gy*Gy + Gz*Gz);

                        //가속도의 속도변화

                        count++;

                        sumA += accChange1;//nowX;
                        sumG += gyroChange1;//nowY;
                        aver += "averA: " + (float)sumA/count +", averG: " + (float)sumG/count;
                        data += "A: " + accChange1/*Math.abs(Ax+Ay+Az-pAx-pAy-pAz)*//*(float)nowX*/ + ", G: " + gyroChange1/*(float)nowY*/;
                        Log.d(aver, data);

                        //
                        pAx = Ax;
                        pAy = Ay;
                        pAz = Az;
                        pGx = Gx;
                        pGy = Gy;
                        pGz = Gz;
                        //

                        averTextView.setText(aver);
                        dataAdapter.add(data);
                    }
                });
                C++;
            }
        }
    }//스레드 끝
}
