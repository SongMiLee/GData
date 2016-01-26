package data.hci.gdata.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonObject;

import data.hci.gdata.Network.RestClient;
import data.hci.gdata.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RecommendActivity extends AppCompatActivity {
    RestClient restClient; //네트워크 변수
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        latitude = getIntent().getDoubleExtra("Latitude", 30);
        longitude = getIntent().getDoubleExtra("Longitude", 120);

        Log.d("gps ra", latitude+" "+longitude);

        restClient = RestClient.newInstance();
        restClient.requestRecommend(latitude, longitude, new Callback<JsonObject>() {
            @Override
            public void success(JsonObject jsonObject, Response response) {
                Log.d("gps ra", jsonObject.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });

    }
}
