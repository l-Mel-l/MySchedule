package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;

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

        daybtn1.setOnClickListener(new View.OnClickListener() {
            boolean isButtonSelected = true;
            @Override
            public void onClick(View v) {
                if (isButtonSelected) {
                    daybtn1.setBackgroundResource(R.drawable.rounded_button4_selected);
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
                } else {
                    daybtn7.setBackgroundResource(R.drawable.rounded_button5);
                }
                isButtonSelected = !isButtonSelected;
            }
        });


    }
}