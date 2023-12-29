package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SelectSchedule extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_schedule);
        getSupportActionBar().hide();

        EditText weeknameFirst1 = findViewById(R.id.WeekNameFirst1);
        CardView savebtn = findViewById(R.id.savebtn);
        Button backbtn = findViewById(R.id.BackBtn);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectSchedule.this, FirstSettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String scheduleid = weeknameFirst1.getText().toString().trim();

                Intent intent = new Intent(SelectSchedule.this, ScheduleActivity.class);
                intent.putExtra("number",scheduleid);
                startActivity(intent);
                finish();
            }
        });

    }
}