package data.hci.gdatawatch.Activity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Iterator;

import data.hci.gdatawatch.Data.EnvironmentData;
import data.hci.gdatawatch.Data.PersonalPreference;
import data.hci.gdatawatch.Data.TimeData;
import data.hci.gdatawatch.Global.StaticVariable;
import data.hci.gdatawatch.R;
import data.hci.gdatawatch.Service.AccelService;
import data.hci.gdatawatch.Service.DetectActivityIntentService;
import data.hci.gdatawatch.Service.GpsService;
import data.hci.gdatawatch.Service.GyroService;
import data.hci.gdatawatch.Thread.SendEnviro;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener, ResultCallback<Status> {

    MapFragment mapFragment;
    GoogleMap mMap;
    UiSettings uiSettings;

    GoogleApiClient googleApiClient;

    Button myLoc, recommendBtn, calendarBtn;
    ProgressBar progressBar;
    Boolean requestMyLoc = false;

    static TextView textview;
    TextView gyroTextView;
    TextView accelTextView;

    //시간관련 변수
    TextView dateTextView;

    //스레드
    EnvironmentData ed;

    double latitude = 30, longitude = 100;

    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //GPS
            if (intent.getAction().equals(StaticVariable.BROADCAST_GPS)) {
                latitude = intent.getDoubleExtra("Latitude", latitude);
                longitude = intent.getDoubleExtra("Longitude", longitude);

                updateUI();//화면 갱신
                ed.setGPS(latitude, longitude);
            }
            //Gyro
            else if (intent.getAction().equals((StaticVariable.BROADCAST_GYRO))) {
                float x = intent.getFloatExtra("x", 0);
                float y = intent.getFloatExtra("y", 0);
                float z = intent.getFloatExtra("z", 0);

                String gyroString = "자이로스코프값 : " + "x : " + x + ", y : " + y + ", z : " + z;
                gyroTextView.setText(gyroString);
                ed.setGyro(x, y, z);
            }
            //Accel
            else if (intent.getAction().equals(StaticVariable.BROADCAST_ACCEL)) {
                float x = intent.getFloatExtra("x", 0);
                float y = intent.getFloatExtra("y", 0);
                float z = intent.getFloatExtra("z", 0);

                accelTextView.setText("가속도 값 : " + "x : " + x + " y : " + y + " z : " + z);
                ed.setAccel(x, y, z);
            }
            else if(intent.getAction().equals(StaticVariable.BROADCAST_ACTION)){
                ed.setAction(intent.getIntExtra("type", -1), intent.getIntExtra("confidence", -1));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        PersonalPreference pf = new PersonalPreference(this);
        if(!pf.isData())
            startActivity(new Intent(this,EnrollDataActivity.class));

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(ActivityRecognition.API)
                    .build();
        }

        //인텐트 필터를 설정하지 않으면 액션을 잡지 못한다.
        intentFilter = new IntentFilter();
        intentFilter.addAction(StaticVariable.BROADCAST_GPS);
        intentFilter.addAction(StaticVariable.BROADCAST_GYRO);
        intentFilter.addAction(StaticVariable.BROADCAST_ACCEL);
        intentFilter.addAction(StaticVariable.BROADCAST_ACTION);

        ed = new EnvironmentData();
        initUI();

        startService((new Intent(getApplicationContext(), GyroService.class)));
        startService((new Intent(getApplicationContext(), AccelService.class)));

        registerReceiver(broadcastReceiver, intentFilter);

        //시간텍스트 지정
        dateTextView = (TextView) findViewById(R.id.tv_date);

        //시간 업데이트
        new Thread(new TimeRefresh()).start();
        new Thread(new SendEnviro(ed, this)).start();

    }

    private PendingIntent getActivityDetectionPendingIntent(){
        Intent i = new Intent(this, DetectActivityIntentService.class);

        return PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * UI들을 설정한 함수
     */
    protected void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.main_progressbar);

        //추천 장소 목록 표시
        recommendBtn = (Button) findViewById(R.id.btn_recommend);
        recommendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RecommendActivity.class);
                intent.putExtra("Latitude", latitude);
                intent.putExtra("Longitude", longitude);
                startActivity(intent);//RecommendActivity를 호출
            }
        });
        myLoc = (Button) findViewById(R.id.btn_loc);
        myLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMyLoc = !(requestMyLoc);
                if (requestMyLoc) {
                    startService((new Intent(getApplicationContext(), GpsService.class)));
                    GpsService.isSend = false;
                    progressBar.setVisibility(View.VISIBLE);//프로그래스 바 화면에 표시
                    //new GetXMLTask().execute("http://www.kma.go.kr/wid/queryDFS.jsp?gridx=" + latitude + "&gridy=" + longitude);
                } else {
                    mMap.clear();
                    GpsService.isSend = false;
                    stopService((new Intent(getApplicationContext(), GpsService.class)));
                }

            }
        });


        calendarBtn = (Button) findViewById(R.id.btn_calendar);
        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
            }
        });

        textview = (TextView) findViewById(R.id.tv_temp); // 기상청
        gyroTextView = (TextView) findViewById(R.id.tv_gyro);        //자이로 텍스트뷰 지정
        accelTextView = (TextView) findViewById(R.id.tv_accel); //가속도

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        //사용자 활동 감지 내역을 없앤다.
        if(googleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleApiClient, getActivityDetectionPendingIntent()).setResultCallback(this);
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (broadcastReceiver != null) unregisterReceiver(broadcastReceiver);

            //서비스 중단
            stopService((new Intent(getApplicationContext(), GyroService.class)));
            stopService((new Intent(getApplicationContext(), GpsService.class)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 지도 초기 상태 설정
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        uiSettings = mMap.getUiSettings();

        //마시멜로우 버전 이상일 때만 권한 설정이 적용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //권한이 없을 때
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(StaticVariable.GPS_PERMISSION);//인텐트 액션 설정
                sendBroadcast(broadcastIntent);
            } else {
                mMap.setOnMapClickListener(this);
                uiSettings.setZoomControlsEnabled(true);

                // 초기 gps - 최근에 찍었던 장소를 표시해준다.
                PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(googleApiClient, null);
                result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                        Iterator<PlaceLikelihood> it = placeLikelihoods.iterator();
                        while (it.hasNext()) {
                            PlaceLikelihood temp = it.next();
                            LatLng tempLat = temp.getPlace().getLatLng();
                            longitude = tempLat.longitude;
                            latitude = tempLat.latitude;
                        }
                        Log.d("gps init", latitude + " " + longitude);
                        updateUI();
                    }
                });
            }
        } else {//마시멜로우 이하일때
            mMap.setOnMapClickListener(this);
            uiSettings.setZoomControlsEnabled(true);

            // 초기 gps - 최근에 찍었던 장소를 표시해준다.
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(googleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                    Iterator<PlaceLikelihood> it = placeLikelihoods.iterator();
                    while (it.hasNext()) {
                        PlaceLikelihood temp = it.next();
                        LatLng tempLat = temp.getPlace().getLatLng();
                        longitude = tempLat.longitude;
                        latitude = tempLat.latitude;
                    }
                    Log.d("gps init", latitude + " " + longitude);
                    updateUI();
                }
            });
        }
    }

    /**
     * 화면 업데이트
     */
    private void updateUI() {
        mMap.clear();//지도 깨끗이
        progressBar.setVisibility(View.VISIBLE);//프로그래스 바 보이게 함
        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("MyLoc");//해당 지점에 마커 생성
        mMap.addMarker(marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17));//마커가 된 부분에 줌
        CircleOptions circle = new CircleOptions().center(new LatLng(latitude, longitude))
                .radius(StaticVariable.RADIUS)// 100m 반경의 원을 그린다.
                .strokeColor(Color.RED)
                .strokeWidth(3);
        mMap.addCircle(circle);
        progressBar.setVisibility(View.INVISIBLE);//프로그래스 바 지우기
    }


    @Override
    public void onConnected(Bundle bundle) {
        //사용자의 활동 내역을 감지
       ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient,
                StaticVariable.DETECTION_INTERVAL_IN_MILLISECONDS,getActivityDetectionPendingIntent()).setResultCallback(this);
        Log.i("tag", "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionSuspended(int i) { googleApiClient.connect();  }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { Log.i("tag", "Connection failed "+connectionResult.getErrorMessage());    }


    //사용자가 지도를 클릭했을 때 처리하는 함수
    @Override
    public void onMapClick(LatLng latLng) {
        longitude = latLng.longitude;
        latitude = latLng.latitude;
        Log.d("gps touch", longitude + " " + latitude);
        updateUI();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    public static void setXMLText(String text) {
        textview.setText(text);
    }

    @Override
    public void onResult(Status status) {
        if(status.isSuccess()){

        } else{
            Log.d("status Error ", status.getStatusMessage());
        }
    }

    //시간 갱신을 위한 스레드
    public class TimeRefresh implements Runnable {
        @Override
        public void run() {
            while (true) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        dateTextView.setText(new TimeData().getNowDate());
                    }
                });
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }//스레드 끝
}

