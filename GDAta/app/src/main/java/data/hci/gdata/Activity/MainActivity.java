package data.hci.gdata.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import java.util.Iterator;
import data.hci.gdata.R;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener {

    MapFragment mapFragment;
    GoogleMap mMap;
    UiSettings uiSettings;

    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;

    Button myLoc, searchBtn, recommendBtn;
    Boolean requestMyLoc = false;

    double latitude = 30, longitude = 100;
    private static final int RADIUS = 100;

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

        initUI();

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
        });
        recommendBtn = (Button)findViewById(R.id.btn_recommend);
        recommendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RecommendActivity.class);
                intent.putExtra("Latitude", latitude);
                intent.putExtra("Longitude", longitude);
                startActivity(intent);
            }
        });
        myLoc = (Button)findViewById(R.id.btn_loc);
        myLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                requestMyLoc = !(requestMyLoc);

                if(requestMyLoc)
                {
                    startLocationUpdate();
                    Log.d("gps request", requestMyLoc.toString());
                }
                else
                {
                    stopLocationUpdate();
                    mMap.clear();
                    Log.d("gps request", requestMyLoc.toString());
                }
            }
        });

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * 위치 자동 완성
     * */
    protected void createPickerActivity(){
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, 1);
          //  startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    //자동 검색 완성으로부터 나온 결과 값을 처리하는 함수
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
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

            // 초기 gps
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
     * 장소 갱신 설정
     * */
    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);//기본 2초마다 위치를 찾음
        locationRequest.setFastestInterval(2000);//빠르게 할 땐 1초마다 찾음.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//정확하게 위치를 찾음
    }

    //장소 업데이트
    protected void startLocationUpdate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    //장소 업데이트를 중지시킴
    protected void stopLocationUpdate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }


    /**
     * 화면 업데이트
     * */
    private void updateUI() {
        mMap.clear();

        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("MyLoc");
        mMap.addMarker(marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17));
        CircleOptions circle = new CircleOptions().center(new LatLng(latitude, longitude))
                .radius(RADIUS)// 100m 반경의 원을 그린다.
                .strokeColor(Color.RED)
                .strokeWidth(3);
        mMap.addCircle(circle);
    }


    /**
     * 장소 업데이트
     * */
    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        Log.d("gps lc", latitude + " " + longitude);
        updateUI();
    }

    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();

        if(requestMyLoc)
            startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopLocationUpdate();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {    }


    //사용자가 지도를 클릭했을 때 처리하는 함수
    @Override
    public void onMapClick(LatLng latLng) {
        longitude = latLng.longitude;
        latitude = latLng.latitude;
        Log.d("gps touch", longitude+" "+latitude);
        updateUI();
    }
}
