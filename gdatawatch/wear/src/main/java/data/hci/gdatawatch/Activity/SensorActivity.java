package data.hci.gdatawatch.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;

import data.hci.gdatawatch.Global.StaticVariable;
import data.hci.gdatawatch.R;
import data.hci.gdatawatch.Service.AccelService;
import data.hci.gdatawatch.Service.GyroService;

public class SensorActivity extends Activity {

    TextView gyroTextView;
    TextView accelTextView;

    //시간관련 변수
    Calendar calendar = Calendar.getInstance();
    int year;
    int month;
    int day;
    int hour;
    int minute;
    int season;
    String[] strSeason = {"봄","여름","가을","겨울"};
    String nowDate;
    TextView dateTextView;

    //스레드
    timeRefresh update;
    Thread Update;



    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Gyro
            if(intent.getAction().equals((StaticVariable.BROADCAST_GYRO))){
                double x = Math.round(intent.getFloatExtra("x", 0)*10d) / 10d;
                double y = Math.round(intent.getFloatExtra("y", 0)*10d) / 10d;
                double z = Math.round(intent.getFloatExtra("z", 0)*10d) / 10d;

                String gyroString = "자이로스코프값: " + "x : " + x + ", y : " + y + ", z : " +z;
                gyroTextView.setText(gyroString);
            }
            //Accel
            else if(intent.getAction().equals(StaticVariable.BROADCAST_ACCEL)){
                double x = Math.round(intent.getFloatExtra("x", 0)*10d) / 10d;
                double y = Math.round(intent.getFloatExtra("y", 0)*10d) / 10d;
                double z = Math.round(intent.getFloatExtra("z", 0)*10d) / 10d;

                accelTextView.setText("가속도 값: "+"x : "+x+" y : "+y+" z : "+z);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        intentFilter = new IntentFilter();
        intentFilter.addAction(StaticVariable.BROADCAST_GYRO);
        intentFilter.addAction(StaticVariable.BROADCAST_ACCEL);

        gyroTextView = (TextView)findViewById(R.id.tv_gyro);        //자이로
        accelTextView = (TextView)findViewById(R.id.tv_accel); //엑셀

        //시간텍스트
        dateTextView = (TextView) findViewById(R.id.tv_date);

        //시간 업데이트
        Update = new Thread(new timeRefresh());
        Update.start();

        startService((new Intent(getApplicationContext(), GyroService.class)));
        startService((new Intent(getApplicationContext(), AccelService.class)));
        registerReceiver(broadcastReceiver, intentFilter);



    }

    //리스???�록
    protected void onResume(){
        super.onResume();
    }
    //리스???�제
    protected void onPause(){
        super.onPause();
    }

    //시간 갱신을 위한 스레드
    public class timeRefresh implements Runnable {
        @Override
        public void run() {
            while(true) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                hour = calendar.get(Calendar.HOUR);
                minute = calendar.get(Calendar.MINUTE);

                nowDate = year + "/";
                if (month < 10) {
                    nowDate += "0";
                }
                nowDate += month + "/";
                if (day < 10) {
                    nowDate += "0";
                }
                nowDate += day + " ";
                if (hour < 10) {
                    nowDate += "0";
                }
                nowDate += hour + ":";
                if (minute < 10) {
                    nowDate += "0";
                }
                nowDate += minute;

                switch (month){
                    case 3:case 4:case 5:
                        season = 0;
                        break;
                    case 6:case 7:case 8:
                        season = 1;
                        break;
                    case 9:case 10:case 11:
                        season = 2;
                        break;
                    default:
                        season = 3;
                }
                nowDate += strSeason[season];

                //     Log.d(nowDate,nowDate);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        dateTextView.setText(nowDate);
                    }
                });


                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
