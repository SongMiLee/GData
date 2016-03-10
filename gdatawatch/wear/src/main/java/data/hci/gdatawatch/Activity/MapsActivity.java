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
import android.os.Bundle;
import android.support.wearable.view.DismissOverlayView;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import data.hci.gdatawatch.Global.StaticVariable;
import data.hci.gdatawatch.R;
import data.hci.gdatawatch.Service.GpsService;

public class MapsActivity extends Activity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    /**
     * Overlay that shows a short help text when first launched. It also provides an option to
     * exit the app.
     */
    GoogleApiClient googleApiClient;

    Button sensor,myLoc;
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17));
        CircleOptions circle = new CircleOptions().center(new LatLng(latitude, longitude))
                //.radius(StaticVariable.RADIUS)// 100m 반경의 원을 그린다.
                .strokeColor(Color.RED)
                .strokeWidth(3);
        mMap.addCircle(circle);
        progressBar.setVisibility(View.INVISIBLE);
    }
}
