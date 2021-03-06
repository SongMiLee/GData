package data.hci.gdatawatch.Activity;

import android.Manifest;
import android.app.Activity;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
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
import data.hci.gdatawatch.Global.StaticVariable;
import data.hci.gdatawatch.Network.GetXMLTask;
import data.hci.gdatawatch.R;
import data.hci.gdatawatch.Service.AccelService;
import data.hci.gdatawatch.Service.DetectActivityIntentService;
import data.hci.gdatawatch.Service.GpsService;
import data.hci.gdatawatch.Service.GyroService;
import data.hci.gdatawatch.Service.PlaceService;
import data.hci.gdatawatch.Service.SService;
import data.hci.gdatawatch.Service.SendDataService;
import data.hci.gdatawatch.Thread.TimeRefresh;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener, ResultCallback<Status> {

    MapFragment mapFragment;
    GoogleMap mMap;
    UiSettings uiSettings;

    GoogleApiClient googleApiClient;

    Button myLoc, calendarBtn;
    ProgressBar progressBar;
    Boolean requestMyLoc = false;

    static TextView weatherTextview, dateTextView;
    TextView gyroTextView, accelTextView;

    //스레드
    EnvironmentData ed;

    double latitude = 37.5652894, longitude = 126.849467;

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

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
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

        registerReceiver(broadcastReceiver, intentFilter);
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

        myLoc = (Button) findViewById(R.id.btn_loc);
        myLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMyLoc = !(requestMyLoc);

                if (requestMyLoc) {
                    startService(new Intent(getApplicationContext(), GpsService.class));
                    startService((new Intent(getApplicationContext(), GyroService.class)));
                    startService((new Intent(getApplicationContext(), AccelService.class)));
                    startService(new Intent(getApplicationContext(), SendDataService.class));
                    startService(new Intent(getApplicationContext(), SService.class));
                    startService(new Intent(getApplicationContext(), PlaceService.class));
                    progressBar.setVisibility(View.VISIBLE);//프로그래스 바 화면에 표시
                    new GetXMLTask().execute("http://www.kma.go.kr/wid/queryDFS.jsp?gridx=" + latitude + "&gridy=" + longitude);

                    myLoc.setText(R.string.str_location_off);
                } else {
                    mMap.clear();
                    stopService(new Intent(getApplicationContext(), GpsService.class));
                    stopService((new Intent(getApplicationContext(), GyroService.class)));
                    stopService((new Intent(getApplicationContext(), AccelService.class)));
                   // stopService(new Intent(getApplicationContext(), SendDataService.class));
                  //  stopService(new Intent(getApplicationContext(), SService.class));
                    stopService(new Intent(getApplicationContext(), PlaceService.class));
                    myLoc.setText(R.string.str_location_on);
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

        weatherTextview = (TextView) findViewById(R.id.tv_temp); // 기상청
        gyroTextView = (TextView) findViewById(R.id.tv_gyro);        //자이로 텍스트뷰 지정
        accelTextView = (TextView) findViewById(R.id.tv_accel); //가속도

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dateTextView = (TextView) findViewById(R.id.tv_date); //시간텍스트 지정
        new Thread(new TimeRefresh(this)).start();//시간 업데이트
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

    /**
     * Activity 종료
     * 모든 서비스 종료
     * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (broadcastReceiver != null) unregisterReceiver(broadcastReceiver);

            //서비스 중단
            stopService(new Intent(getApplicationContext(), GpsService.class));
            stopService((new Intent(getApplicationContext(), GyroService.class)));
            stopService((new Intent(getApplicationContext(), AccelService.class)));
            stopService(new Intent(getApplicationContext(), SendDataService.class));
            stopService(new Intent(getApplicationContext(), SService.class));

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
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, StaticVariable.GPS_PERMISSION);
            } else {// 항상 허용일 경우
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

    protected void onResume() {    super.onResume();   }

    protected void onPause() {     super.onPause();   }

    public static void setXMLText(String text) {  weatherTextview.setText(text);   }

    public static void setTimeText(String text) { dateTextView.setText(text); }

    @Override
    public void onResult(Status status) {
        if(status.isSuccess()){

        } else{
            Log.d("status Error ", status.getStatusMessage());
        }
    }

    /**
     * GPS 권한 설정
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case StaticVariable.GPS_PERMISSION:
                progressBar.setVisibility(View.INVISIBLE);
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Log.d("Map Activity", "GPS Permission granted");

                }
                else{
                    Log.d("Map Activity", "GPS Permission denied");
                    myLoc.setClickable(false);
                }

                break;
        }
    }


}

