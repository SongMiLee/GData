package data.hci.gdatawatch.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.logging.Handler;

import data.hci.gdatawatch.Global.StaticVariable;
import data.hci.gdatawatch.R;
import data.hci.gdatawatch.Service.AccelService;
import data.hci.gdatawatch.Service.GyroService;
import data.hci.gdatawatch.Service.TimeService;

public class SensorActivity extends Activity {

    TextView gyroTextView;
    TextView accelTextView;

    //시간관련 변수
    TextView dateTextView;


    //test
    Button data;

    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Gyro
            if(intent.getAction().equals((StaticVariable.BROADCAST_GYRO))){
                double x = Math.round(intent.getFloatExtra("x", 0)*100d) / 100d;
                double y = Math.round(intent.getFloatExtra("y", 0)*100d) / 100d;
                double z = Math.round(intent.getFloatExtra("z", 0)*100d) / 100d;

                String gyroString = "자이로스코프값: " + "x : " + x + ", y : " + y + ", z : " +z;
                gyroTextView.setText(gyroString);

            }
            //Accel
            else if(intent.getAction().equals(StaticVariable.BROADCAST_ACCEL)){
                double x = Math.round(intent.getFloatExtra("x", 0)*100d) / 100d;
                double y = Math.round(intent.getFloatExtra("y", 0)*100d) / 100d;
                double z = Math.round(intent.getFloatExtra("z", 0)*100d) / 100d;

                accelTextView.setText("가속도 값: "+"x : "+x+" y : "+y+" z : "+z);
            }

            else if(intent.getAction().equals(StaticVariable.BROADCAST_TIME)){
                dateTextView.setText(intent.getStringExtra("date"));
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
        intentFilter.addAction(StaticVariable.BROADCAST_TIME);

        gyroTextView = (TextView)findViewById(R.id.tv_gyro);        //자이로
        accelTextView = (TextView)findViewById(R.id.tv_accel); //엑셀

        //시간텍스트
        dateTextView = (TextView) findViewById(R.id.tv_date);

        //test
        data = (Button)findViewById(R.id.bt_data);
        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DataActivity.class));
            }
        });//


        startService((new Intent(getApplicationContext(), GyroService.class)));
        startService((new Intent(getApplicationContext(), AccelService.class)));
        startService((new Intent(getApplicationContext(), TimeService.class)));
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

}
