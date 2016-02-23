package data.hci.gdata.Activity;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import data.hci.gdata.Global.StaticVariable;
import data.hci.gdata.R;
import data.hci.gdata.Service.AccelService;
import data.hci.gdata.Service.GpsService;
import data.hci.gdata.Service.GyroService;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener {

    MapFragment mapFragment;
    GoogleMap mMap;
    UiSettings uiSettings;

    GoogleApiClient googleApiClient;

    Button myLoc, searchBtn, recommendBtn, calendarBtn;
    ProgressBar progressBar;
    Boolean requestMyLoc = false;

    TextView textview;
    TextView gyroTextView;
    TextView accelTextView;
    Document doc = null;

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

    double latitude = 30, longitude = 100;

    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //GPS
            if(intent.getAction().equals(StaticVariable.BROADCAST_GPS)){
                latitude = intent.getDoubleExtra("Latitude", latitude);
                longitude = intent.getDoubleExtra("Longitude", longitude);

                updateUI();//화면 갱신
            }
            //Gyro
            else if(intent.getAction().equals((StaticVariable.BROADCAST_GYRO))){
                float x = intent.getFloatExtra("x", 0);
                float y = intent.getFloatExtra("y", 0);
                float z = intent.getFloatExtra("z", 0);

                String gyroString = "자이로스코프값 : " + "x : " + x + ", y : " + y + ", z : " +z;
                gyroTextView.setText(gyroString);
            }
            //Accel
            else if(intent.getAction().equals(StaticVariable.BROADCAST_ACCEL)){
                float x = intent.getFloatExtra("x", 0);
                float y = intent.getFloatExtra("y", 0);
                float z = intent.getFloatExtra("z", 0);

                accelTextView.setText("가속도 값 : "+"x : "+x+" y : "+y+" z : "+z);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        //인텐트 필터를 설정하지 않으면 액션을 잡지 못한다.
        intentFilter = new IntentFilter();
        intentFilter.addAction(StaticVariable.BROADCAST_GPS);
        intentFilter.addAction(StaticVariable.BROADCAST_GYRO);
        intentFilter.addAction(StaticVariable.BROADCAST_ACCEL);

        initUI();

        startService((new Intent(getApplicationContext(), GyroService.class)));
        startService((new Intent(getApplicationContext(), AccelService.class)));
        registerReceiver(broadcastReceiver, intentFilter);

        //시간텍스트 지정
        dateTextView = (TextView) findViewById(R.id.tv_date);

        //시간 업데이트
        Update = new Thread(new timeRefresh());
        Update.start();
    }

    /**
     * UI들을 설정한 함수
     * */
    protected void initUI(){
        searchBtn = (Button)findViewById(R.id.btn_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPickerActivity();
            }
        });//검색 버튼시 picker Activity를 부른다.

        progressBar = (ProgressBar)findViewById(R.id.main_progressbar);

        //추천 장소 목록 표시
        recommendBtn = (Button)findViewById(R.id.btn_recommend);
        recommendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RecommendActivity.class);
                intent.putExtra("Latitude", latitude);
                intent.putExtra("Longitude", longitude);
                startActivity(intent);//RecommendActivity를 호출
            }
        });
        myLoc = (Button)findViewById(R.id.btn_loc);
        myLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMyLoc = !(requestMyLoc);
                if (requestMyLoc) {
                    startService((new Intent(getApplicationContext(), GpsService.class)));
                    GpsService.isSend = false;
                    progressBar.setVisibility(View.VISIBLE);//프로그래스 바 화면에 표시
                } else {
                    mMap.clear();
                    GpsService.isSend = false;
                    stopService((new Intent(getApplicationContext(), GpsService.class)));
                }

                GetXMLTask task = new GetXMLTask();
                task.execute("http://www.kma.go.kr/wid/queryDFS.jsp?gridx=" + latitude + "&gridy=" + longitude);
            }
        });

        calendarBtn = (Button)findViewById(R.id.btn_calendar);
        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
            }
        });

        textview = (TextView) findViewById(R.id.tv_temp); // 기상청
        gyroTextView = (TextView)findViewById(R.id.tv_gyro);        //자이로 텍스트뷰 지정
        accelTextView = (TextView)findViewById(R.id.tv_accel); //엑셀

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * 위치 자동 완성
     * */
    protected void createPickerActivity(){
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, StaticVariable.FINDLOCATION);
            //  startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    //자동 검색 완성으로부터 나온 결과 값을 처리하는 함수
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == StaticVariable.FINDLOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                Log.d("gps place", place.getAddress()+" "+place.getPlaceTypes());
                longitude = place.getLatLng().longitude;
                latitude = place.getLatLng().latitude;
                updateUI();
                Log.d("result", longitude+" "+latitude);
            }
        }

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            if(broadcastReceiver!=null)  unregisterReceiver(broadcastReceiver);
            stopService((new Intent(getApplicationContext(), GyroService.class)));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 지도 초기 상태 설정
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        uiSettings = mMap.getUiSettings();

        //마시멜로우 버전 이상일 때만 권한 설정이 적용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //권한이 없을 때
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    while(it.hasNext()){
                        PlaceLikelihood temp = it.next();
                        LatLng tempLat = temp.getPlace().getLatLng();
                        longitude = tempLat.longitude;
                        latitude = tempLat.latitude;
                    }
                    Log.d("gps init", latitude+" "+longitude);
                    updateUI();
                }
            });
        }
    }

    /**
     * 화면 업데이트
     * */
    private void updateUI() {
        mMap.clear();
        progressBar.setVisibility(View.VISIBLE);
        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("MyLoc");
        mMap.addMarker(marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17));
        CircleOptions circle = new CircleOptions().center(new LatLng(latitude, longitude))
                .radius(StaticVariable.RADIUS)// 100m 반경의 원을 그린다.
                .strokeColor(Color.RED)
                .strokeWidth(3);
        mMap.addCircle(circle);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {    }


    //사용자가 지도를 클릭했을 때 처리하는 함수
    @Override
    public void onMapClick(LatLng latLng) {
        longitude = latLng.longitude;
        latitude = latLng.latitude;
        Log.d("gps touch", longitude + " " + latitude);
        updateUI();
    }

    //리스너 등록
    protected void onResume(){
        super.onResume();
    }
    //리스너 해제
    protected void onPause(){
        super.onPause();
    }


    private class GetXMLTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
                doc.getDocumentElement().normalize();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {

            String s = "";
            //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("data");
            //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환

            int i = 0 ;
            //날씨 데이터를 추출
            s += "현 위치의 날씨 정보: ";
            Node node = nodeList.item(i);
            Element fstElmnt = (Element) node;
            NodeList nameList  = fstElmnt.getElementsByTagName("temp");
            Element nameElement = (Element) nameList.item(0);
            nameList = nameElement.getChildNodes();
            s += "온도 = "+ ((Node) nameList.item(0)).getNodeValue() +",";

            NodeList websiteList = fstElmnt.getElementsByTagName("reh");
            s += "습도 = "+  websiteList.item(0).getChildNodes().item(0).getNodeValue() +",";

            NodeList rainList = fstElmnt.getElementsByTagName("r06");
            s += "강우량 = "+  rainList.item(0).getChildNodes().item(0).getNodeValue() +"\n";


            textview.setText(s);

            super.onPostExecute(doc);
        }


    }//end inner class - GetXMLTask

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

                Log.d(nowDate,nowDate);
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
    }//스레드 끝
}