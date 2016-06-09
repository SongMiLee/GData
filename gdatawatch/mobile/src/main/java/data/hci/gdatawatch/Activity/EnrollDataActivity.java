package data.hci.gdatawatch.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import data.hci.gdatawatch.Data.PersonalPreference;
import data.hci.gdatawatch.Network.RestRequestHelper;
import data.hci.gdatawatch.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class EnrollDataActivity extends AppCompatActivity {
    EditText name;
    Spinner job;
    RadioGroup selectLevel, selectGender;

    TextView birth;
    Calendar calendar;

    int gender, level;

    Button Enroll;
    String jobStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_data);

        init();
    }

    protected void init(){
        PersonalPreference pf = new PersonalPreference(this);
        if(pf.isData()) {//사용자 데이터가 있으면
            startActivity(new Intent(getApplicationContext(), PageActivity.class));//메인 화면 띄우고
            finish(); // 가입 화면 종료
        }

        name = (EditText)findViewById(R.id.nameText);
        birth = (TextView)findViewById(R.id.birthText);
        job = (Spinner)findViewById(R.id.jobText);

        calendar = Calendar.getInstance();
        selectGender = (RadioGroup)findViewById(R.id.genderGroup);

        /*Gender Radio Group 선택*/
        selectGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.maleButton:
                        gender = 1;
                        break;
                    case R.id.femaleButton:
                        gender = 2;
                        break;
                }
                Log.d("Gender", gender+"");
            }
        });

        /* 생년 월일*/
        birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EnrollDataActivity.this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        /* 레벨 선택*/
        selectLevel = (RadioGroup)findViewById(R.id.levelGroup);
        selectLevel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.upper:
                        level = 2;
                        break;
                    case R.id.lower:
                        level = 1;
                        break;
                }
            }
        });

        job.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                jobStr = parent.getItemAtPosition(position).toString();
                Log.d("job list", jobStr);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {    }
        });

        //enroll 버튼 클릭시
        Enroll = (Button)findViewById(R.id.enrollBtn);
        Enroll.setOnClickListener(enrollData);
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            birth.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    };

    View.OnClickListener enrollData = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(name.getText().toString().isEmpty() || birth.getText().toString().isEmpty() || jobStr.isEmpty()){
                Toast.makeText(getApplicationContext(), "Insert the value(name, birth or job )", Toast.LENGTH_LONG).show();
            } else {
                RestRequestHelper restRequestHelper = RestRequestHelper.newInstance();
                restRequestHelper.enrollUser(name.getText().toString(), birth.getText().toString(), gender, jobStr, level, new Callback<Integer>() {
                    @Override
                    public void success(Integer integer, Response response) {
                        System.out.println(integer);
                        PersonalPreference personalPreference = new PersonalPreference(EnrollDataActivity.this);
                        personalPreference.setData(integer, name.getText().toString(), birth.getText().toString(), gender, jobStr, level);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        error.printStackTrace();
                    }
                });
                startActivity(new Intent(getApplicationContext(), PageActivity.class));//뷰페이저 화면으로 이동
                finish();
            }
        }
    };

}
