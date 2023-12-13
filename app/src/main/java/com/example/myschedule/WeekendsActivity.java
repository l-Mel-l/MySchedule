package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WeekendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_weekends);
        Button daybtn1 = findViewById(R.id.DayBtn1);
        Button daybtn2 = findViewById(R.id.DayBtn2);
        Button daybtn3 = findViewById(R.id.DayBtn3);
        Button daybtn4 = findViewById(R.id.DayBtn4);
        Button daybtn5 = findViewById(R.id.DayBtn5);
        Button daybtn6 = findViewById(R.id.DayBtn6);
        Button daybtn7 = findViewById(R.id.DayBtn7);
        Button savebtn = findViewById(R.id.SaveBtn);

        String[] weekendDays = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
        daybtn1.setOnClickListener(new View.OnClickListener() {
            boolean isButtonSelected = true;
            @Override
            public void onClick(View v) {
                if (isButtonSelected) {
                    daybtn1.setBackgroundResource(R.drawable.rounded_button4_selected);
                    weekendDays[0] = null;
                } else {
                    daybtn1.setBackgroundResource(R.drawable.rounded_button4);
                }
                isButtonSelected = !isButtonSelected;
            }
        });
        daybtn2.setOnClickListener(new View.OnClickListener() {
            boolean isButtonSelected = true;
            @Override
            public void onClick(View v) {
                if (isButtonSelected) {
                    daybtn2.setBackgroundResource(R.color.buttonSelected);
                    weekendDays[1] = null;
                } else {
                    daybtn2.setBackgroundResource(R.color.button);
                }
                isButtonSelected = !isButtonSelected;
            }
        });
        daybtn3.setOnClickListener(new View.OnClickListener() {
            boolean isButtonSelected = true;
            @Override
            public void onClick(View v) {
                if (isButtonSelected) {
                    daybtn3.setBackgroundResource(R.color.buttonSelected);
                    weekendDays[2] = null;
                } else {
                    daybtn3.setBackgroundResource(R.color.button);
                }
                isButtonSelected = !isButtonSelected;
            }
        });
        daybtn4.setOnClickListener(new View.OnClickListener() {
            boolean isButtonSelected = true;
            @Override
            public void onClick(View v) {
                if (isButtonSelected) {
                    daybtn4.setBackgroundResource(R.color.buttonSelected);
                    weekendDays[3] = null;
                } else {
                    daybtn4.setBackgroundResource(R.color.button);
                }
                isButtonSelected = !isButtonSelected;
            }
        });
        daybtn5.setOnClickListener(new View.OnClickListener() {
            boolean isButtonSelected = true;
            @Override
            public void onClick(View v) {
                if (isButtonSelected) {
                    daybtn5.setBackgroundResource(R.color.buttonSelected);
                    weekendDays[4] = null;
                } else {
                    daybtn5.setBackgroundResource(R.color.button);
                }
                isButtonSelected = !isButtonSelected;
            }
        });
        daybtn6.setOnClickListener(new View.OnClickListener() {
            boolean isButtonSelected = true;
            @Override
            public void onClick(View v) {
                if (isButtonSelected) {
                    daybtn6.setBackgroundResource(R.color.buttonSelected);
                    weekendDays[5] = null;
                } else {
                    daybtn6.setBackgroundResource(R.color.button);
                }
                isButtonSelected = !isButtonSelected;
            }
        });
        daybtn7.setOnClickListener(new View.OnClickListener() {
            boolean isButtonSelected = true;
            @Override
            public void onClick(View v) {
                if (isButtonSelected) {
                    daybtn7.setBackgroundResource(R.drawable.rounded_button5_selected);
                    weekendDays[6] = null;
                } else {
                    daybtn7.setBackgroundResource(R.drawable.rounded_button5);
                }
                isButtonSelected = !isButtonSelected;
            }
        });
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeekendsActivity.this, FirstSettingsActivity.class);
                intent.putExtra("weekend", weekendDays);
                startActivity(intent);
                finish();
            }
        });


    }
}