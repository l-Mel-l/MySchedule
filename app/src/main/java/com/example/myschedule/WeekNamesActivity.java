package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class WeekNamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_week_names);
        int activeButton = getIntent().getIntExtra("activeButton", 0);

        TextView weektext1 = findViewById(R.id.WeekText1);
        TextView weektext2 = findViewById(R.id.WeekText2);
        TextView weektext3 = findViewById(R.id.WeekText3);
        TextView weektext4 = findViewById(R.id.WeekText4);
        EditText weeknamefirs1 = findViewById(R.id.WeekNameFirst1);
        EditText weeknamefirs2 = findViewById(R.id.WeekNameFirst2);
        EditText weeknamefirs3 = findViewById(R.id.WeekNameFirst3);
        EditText weeknamefirs4 = findViewById(R.id.WeekNameFirst4);

        weektext1.setVisibility(View.GONE);
        weeknamefirs1.setVisibility(View.GONE);
        weektext2.setVisibility(View.GONE);
        weeknamefirs2.setVisibility(View.GONE);
        weektext3.setVisibility(View.GONE);
        weeknamefirs3.setVisibility(View.GONE);
        weektext4.setVisibility(View.GONE);
        weeknamefirs4.setVisibility(View.GONE);
        switch (activeButton) {
            case 1:
                weektext1.setVisibility(View.VISIBLE);
                weeknamefirs1.setVisibility(View.VISIBLE);
                break;
            case 2:
                weektext1.setVisibility(View.VISIBLE);
                weeknamefirs1.setVisibility(View.VISIBLE);
                weektext2.setVisibility(View.VISIBLE);
                weeknamefirs2.setVisibility(View.VISIBLE);
                break;
            case 3:
                weektext1.setVisibility(View.VISIBLE);
                weeknamefirs1.setVisibility(View.VISIBLE);
                weektext2.setVisibility(View.VISIBLE);
                weeknamefirs2.setVisibility(View.VISIBLE);
                weektext3.setVisibility(View.VISIBLE);
                weeknamefirs3.setVisibility(View.VISIBLE);
                break;
            case 4:
                weektext1.setVisibility(View.VISIBLE);
                weeknamefirs1.setVisibility(View.VISIBLE);
                weektext2.setVisibility(View.VISIBLE);
                weeknamefirs2.setVisibility(View.VISIBLE);
                weektext3.setVisibility(View.VISIBLE);
                weeknamefirs3.setVisibility(View.VISIBLE);
                weektext4.setVisibility(View.VISIBLE);
                weeknamefirs4.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

    }
}