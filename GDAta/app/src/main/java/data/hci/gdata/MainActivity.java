package data.hci.gdata;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    MapFragment mapFragment;
    GoogleMap mMap;

    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;

    double latitude=30, longitude=100;

    boolean requestingLocationUpdates=false;
    Button findLocButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        findLocButton = (Button) findViewById(R.id.btn_loc);
        findLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestingLocationUpdates =!(requestingLocationUpdates);
                Log.d("gps", requestingLocationUpdates+"");
            }
        });
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
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //마시멜로우 버전 이상일 때만 권한 설정이 적용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //권한이 없을 때
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
        } else//마시멜로우 이하일때
            mMap.setMyLocationEnabled(true);//현재 위치 버튼 표시

    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);//기본 2초마다 위치를 찾음
        locationRequest.setFastestInterval(1000);//빠르게 할 땐 1초마다 찾음.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//정확하게 위치를 찾음
    }

    protected void startLocationUpdate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void updateUI() {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(R.string.str_location + ""));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 19));
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
        }
        createLocationRequest();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        switch (i){
            case CAUSE_NETWORK_LOST:
                Log.d("gps cs", "Network Lost");
                break;
            case CAUSE_SERVICE_DISCONNECTED:
                Log.d("gps cs", "Service disconnected");
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        Log.d("gps", latitude+" "+longitude);
        updateUI();
    }

}
