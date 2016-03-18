package data.hci.gdatawatch.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import data.hci.gdatawatch.R;

public class RecommendActivity extends AppCompatActivity {
    double latitude, longitude;

    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        latitude = getIntent().getDoubleExtra("Latitude", 30);
        longitude = getIntent().getDoubleExtra("Longitude", 120);

        Log.d("gps ra", latitude + " " + longitude);

        init();
        addItem();

    }

    //ui 초기화 작업
    public void init(){
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//프로그레스 진행 화면을 보여준다.

        layoutManager = new LinearLayoutManager(getApplicationContext());//아이템의 항목을 배치
        recyclerView.setLayoutManager(layoutManager);
    }

    public void addItem(){
        //100m 이내의 건물 정보들을 받아온다.
        progressBar.setVisibility(View.INVISIBLE);
    }
}
