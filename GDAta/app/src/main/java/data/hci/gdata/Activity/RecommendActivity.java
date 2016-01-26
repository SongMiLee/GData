package data.hci.gdata.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import data.hci.gdata.Adapter.RecommendAdapter;
import data.hci.gdata.Adapter.RecommendItem;
import data.hci.gdata.Network.RestClient;
import data.hci.gdata.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RecommendActivity extends AppCompatActivity {
    double latitude, longitude;
    RestClient restClient;

    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        restClient = RestClient.newInstance();//네트워크 객체 생성

        latitude = getIntent().getDoubleExtra("Latitude", 30);
        longitude = getIntent().getDoubleExtra("Longitude", 120);

        Log.d("gps ra", latitude + " " + longitude);

        init();
        addItem();

    }

    //gps 초기화 작업
    public void init(){
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    public void addItem(){
        //100m 이내의 건물 정보들을 받아온다.
        restClient.requestRecommend(latitude, longitude, new Callback<JsonObject>() {
            @Override
            public void success(JsonObject jsonObject, Response response) {
                JsonArray tmpArray = jsonObject.getAsJsonArray("results");

                List<RecommendItem> items = new ArrayList<RecommendItem>();
                RecommendItem[] item = new RecommendItem[tmpArray.size()];

                for(int i =0; i < tmpArray.size() ;i++){
                    JsonObject tmp = tmpArray.get(i).getAsJsonObject();
                    String name = tmp.get("name").getAsString();
                    String icon = tmp.get("icon").getAsString();
                    Log.d("gps name&icon", name+" "+icon);
                    item[i] = new RecommendItem(icon, name);
                    items.add(item[i]);
                }

                RecommendAdapter adapter = new RecommendAdapter(items);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();//네트워크 에러 표시
            }
        });
    }
}
