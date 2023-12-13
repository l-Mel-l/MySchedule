package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstSettingsActivity extends AppCompatActivity {

    int clickedButton = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstsettings);
        getSupportActionBar().hide();

        Button btn1 = findViewById(R.id.button);
        Button btn2 = findViewById(R.id.button2);
        Button btn3 = findViewById(R.id.button3);
        Button btn4 = findViewById(R.id.button4);
        Button weeknamebtn = findViewById(R.id.WeekNameBtn);
        Button weekendsname = findViewById(R.id.ChooseWeekendsBtn);
        Button chooselessonbtn = findViewById(R.id.ChooseLessonsBtn);


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the background of button 1 when clicked
                btn1.setBackgroundResource(R.drawable.rounded_button_selected);
                btn2.setBackgroundResource(R.color.button);
                btn3.setBackgroundResource(R.color.button);
                btn4.setBackgroundResource(R.drawable.rounded_button2);
                clickedButton = 1;
            }
        });
        btn2.setOnClickListener (new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                btn1.setBackgroundResource(R.drawable.rounded_button);
                btn2.setBackgroundResource(R.color.buttonSelected);
                btn3.setBackgroundResource(R.color.button);
                btn4.setBackgroundResource(R.drawable.rounded_button2);
                clickedButton = 2;
            }
        });

        btn3.setOnClickListener (new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                btn1.setBackgroundResource(R.drawable.rounded_button);
                btn2.setBackgroundResource(R.color.button);
                btn3.setBackgroundResource(R.color.buttonSelected);
                btn4.setBackgroundResource(R.drawable.rounded_button2);
                clickedButton = 3;
            }
        });
        btn4.setOnClickListener (new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                btn1.setBackgroundResource(R.drawable.rounded_button);
                btn2.setBackgroundResource(R.color.button);
                btn3.setBackgroundResource(R.color.button);
                btn4.setBackgroundResource(R.drawable.rounded_button2_selected);
                clickedButton = 4;
            }
        });
        weeknamebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstSettingsActivity.this, WeekNamesActivity.class);
                intent.putExtra("activeButton", clickedButton); // Передача информации о состоянии кнопки
                startActivity(intent);
            }
        });
        weekendsname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstSettingsActivity.this, WeekendsActivity.class);
                startActivity(intent);
            }
        });
        chooselessonbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstSettingsActivity.this,ChooseLessonsActivity.class);
                startActivity(intent);
            }
        });
    }
}