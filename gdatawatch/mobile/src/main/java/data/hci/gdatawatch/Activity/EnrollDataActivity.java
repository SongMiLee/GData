package data.hci.gdatawatch.Activity;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.Calendar;

import data.hci.gdatawatch.R;

public class EnrollDataActivity extends AppCompatActivity {
    EditText name;

    EditText Birth;
    Calendar calendar;
    RadioGroup selectGender;

    int gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_data);

        init();
    }

    protected void init(){
        name = (EditText)findViewById(R.id.nameText);
        Birth = (EditText)findViewById(R.id.birthText);
        calendar = Calendar.getInstance();
        selectGender = (RadioGroup)findViewById(R.id.genderGroup);

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

        Birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DatePickerDialog(getApplicationContext(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
        }
    };
}
