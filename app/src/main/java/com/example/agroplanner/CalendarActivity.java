package com.example.agroplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";

    private CalendarView mCalendarView;
    String plantName, plotName, plotid;
    int day, month, year;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        mCalendarView = (CalendarView) findViewById(R.id.calendarView);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            plantName = extras.getString("plantName");
            plotName = extras.getString("plotName");
            plotid = extras.getString("plotid");
        }

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int i, int i1, int i2) {
                String date =  + i2 + "/" + (i1 + 1) + "/" + i;
                day = i2;
                month = (i1 + 1);
                year = i;
                Log.d(TAG, "onSelectedDayChange: dd/mm/yyyy: " + date);

                Intent intent = new Intent(CalendarActivity.this, CalendarDisplay.class);
                Bundle extras = new Bundle();
                extras.putString("date", date);
                extras.putInt("day", day);
                extras.putInt("month", month);
                extras.putInt("year", year);
                extras.putString("plantName", plantName);
                extras.putString("plotName", plotName);
                extras.putString("plotid", plotid);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    public void Return (View view){
        Intent intent = new  Intent(getApplicationContext(), Plants.class);
        startActivity(intent);
    }
}
