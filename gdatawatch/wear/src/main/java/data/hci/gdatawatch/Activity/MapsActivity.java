package data.hci.gdatawatch.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.DismissOverlayView;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import data.hci.gdatawatch.Global.StaticVariable;
import data.hci.gdatawatch.R;
import data.hci.gdatawatch.Service.GpsService;
import data.hci.gdatawatch.Service.GyroService;

public class MapsActivity extends Activity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    /**
     * Overlay that shows a short help text when first launched. It also provides an option to
     * exit the app.
     */
    Document doc;
    GoogleApiClient googleApiClient;

    TextView tempview;
    Button sensor,myLoc,calender;
    ProgressBar progressBar;

    Boolean requestMyLoc = false;
    double latitude = 30, longitude = 100;

    private DismissOverlayView mDismissOverlay;

    /**
     * The map. It is initialized when the map has been fully loaded and is ready to be used.
     *
     * @see #onMapReady(com.google.android.gms.maps.GoogleMap)
     */
    private GoogleMap mMap;

    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //GPS
            if (intent.getAction().equals(StaticVariable.BROADCAST_GPS)) {
                latitude = intent.getDoubleExtra("Latitude", latitude);
                longitude = intent.getDoubleExtra("Longitude", longitude);

                updateUI();//화면 갱신
            }
        }
    };

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        // Set the layout. It only contains a MapFragment and a DismissOverlay.
        setContentView(R.layout.activity_maps);

        progressBar = (ProgressBar)findViewById(R.id.main_progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        tempview = (TextView) findViewById(R.id.tv_temp); // 기상청



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
                task.execute("http://www.kma.go.kr/wid/queryDFS.jsp?gridx=" + latitude+ " &gridy= " + longitude);

            }
        });


        sensor = (Button)findViewById(R.id.btn_sensor);
        sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SensorActivity.class));
            }
        });

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        calender = (Button)findViewById(R.id.btn_calender);
        calender.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), CalenderActivity.class));
            }
        });

        intentFilter = new IntentFilter();
        intentFilter.addAction(StaticVariable.BROADCAST_GPS);

        registerReceiver(broadcastReceiver, intentFilter);

        // Retrieve the containers for the root of the layout and the map. Margins will need to be
        // set on them to account for the system window insets.
        final FrameLayout topFrameLayout = (FrameLayout) findViewById(R.id.root_container);
        final FrameLayout mapFrameLayout = (FrameLayout) findViewById(R.id.map_container);

        // Set the system view insets on the containers when they become available.
        topFrameLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Call through to super implementation and apply insets
                insets = topFrameLayout.onApplyWindowInsets(insets);

                FrameLayout.LayoutParams params =
                        (FrameLayout.LayoutParams) mapFrameLayout.getLayoutParams();

                // Add Wearable insets to FrameLayout container holding map as margins
                params.setMargins(
                        insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom());
                mapFrameLayout.setLayoutParams(params);

                return insets;
            }
        });

        // Obtain the DismissOverlayView and display the introductory help text.
        mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDismissOverlay.setIntroText(R.string.intro_text);
        mDismissOverlay.showIntroIfNecessary();

        // Obtain the MapFragment and set the async listener to be notified when the map is ready.
        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {    }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Map is ready to be used.
        mMap = googleMap;

        // Set the long click listener as a way to exit the map.
        mMap.setOnMapLongClickListener(this);

        // Add a marker in Sydney, Australia and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // Display the dismiss overlay with a button to exit this activity.
        mDismissOverlay.show();
    }

    private void updateUI() {
        mMap.clear();
        progressBar.setVisibility(View.VISIBLE);
        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("MyLoc");
        mMap.addMarker(marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 18));
        CircleOptions circle = new CircleOptions().center(new LatLng(latitude, longitude))
                .radius(StaticVariable.RADIUS)// 100m 반경의 원을 그린다.
                .strokeColor(Color.RED)
                .strokeWidth(3);
        mMap.addCircle(circle);
        progressBar.setVisibility(View.INVISIBLE);
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
            s += "강우량= "+  rainList.item(0).getChildNodes().item(0).getNodeValue() +"\n";

            tempview.setText(s);

            super.onPostExecute(doc);
        }


    }//end inner class - GetXMLTask
}
