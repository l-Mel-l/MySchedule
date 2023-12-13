package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class ChooseLessonsActivity extends AppCompatActivity {

    int activeButton2 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_lessons);
        getSupportActionBar().hide();
        SetVisibility(activeButton2);

        Button lessonbtn1 = findViewById(R.id.LessonBtn1);
        Button lessonbtn2 = findViewById(R.id.LessonBtn2);
        Button lessonbtn3 = findViewById(R.id.LessonBtn3);
        Button lessonbtn4 = findViewById(R.id.LessonBtn4);
        Button lessonbtn5 = findViewById(R.id.LessonBtn5);
        Button lessonbtn6 = findViewById(R.id.LessonBtn6);
        lessonbtn1.setBackgroundResource(R.drawable.rounded_button_selected);

        lessonbtn1.setOnClickListener(v -> {
            lessonbtn1.setBackgroundResource(R.drawable.rounded_button_selected);
            lessonbtn2.setBackgroundResource(R.color.button);
            lessonbtn3.setBackgroundResource(R.color.button);
            lessonbtn4.setBackgroundResource(R.color.button);
            lessonbtn5.setBackgroundResource(R.color.button);
            lessonbtn6.setBackgroundResource(R.drawable.rounded_button2);
            activeButton2 = 1;
            SetVisibility(activeButton2);
        });
        lessonbtn2.setOnClickListener(v -> {
            lessonbtn1.setBackgroundResource(R.drawable.rounded_button);
            lessonbtn2.setBackgroundResource(R.color.buttonSelected);
            lessonbtn3.setBackgroundResource(R.color.button);
            lessonbtn4.setBackgroundResource(R.color.button);
            lessonbtn5.setBackgroundResource(R.color.button);
            lessonbtn6.setBackgroundResource(R.drawable.rounded_button2);
            activeButton2 = 2;
            SetVisibility(activeButton2);
        });
        lessonbtn3.setOnClickListener(v -> {
            lessonbtn1.setBackgroundResource(R.drawable.rounded_button);
            lessonbtn2.setBackgroundResource(R.color.button);
            lessonbtn3.setBackgroundResource(R.color.buttonSelected);
            lessonbtn4.setBackgroundResource(R.color.button);
            lessonbtn5.setBackgroundResource(R.color.button);
            lessonbtn6.setBackgroundResource(R.drawable.rounded_button2);
            activeButton2 = 3;
            SetVisibility(activeButton2);
        });
        lessonbtn4.setOnClickListener(v -> {
            lessonbtn1.setBackgroundResource(R.drawable.rounded_button);
            lessonbtn2.setBackgroundResource(R.color.button);
            lessonbtn3.setBackgroundResource(R.color.button);
            lessonbtn4.setBackgroundResource(R.color.buttonSelected);
            lessonbtn5.setBackgroundResource(R.color.button);
            lessonbtn6.setBackgroundResource(R.drawable.rounded_button2);
            activeButton2 = 4;
            SetVisibility(activeButton2);
        });
        lessonbtn5.setOnClickListener(v -> {
            lessonbtn1.setBackgroundResource(R.drawable.rounded_button);
            lessonbtn2.setBackgroundResource(R.color.button);
            lessonbtn3.setBackgroundResource(R.color.button);
            lessonbtn4.setBackgroundResource(R.color.button);
            lessonbtn5.setBackgroundResource(R.color.buttonSelected);
            lessonbtn6.setBackgroundResource(R.drawable.rounded_button2);
            activeButton2 = 5;
            SetVisibility(activeButton2);
        });
        lessonbtn6.setOnClickListener(v -> {
            lessonbtn1.setBackgroundResource(R.drawable.rounded_button);
            lessonbtn2.setBackgroundResource(R.color.button);
            lessonbtn3.setBackgroundResource(R.color.button);
            lessonbtn4.setBackgroundResource(R.color.button);
            lessonbtn5.setBackgroundResource(R.color.button);
            lessonbtn6.setBackgroundResource(R.drawable.rounded_button2_selected);
            activeButton2 = 6;
            SetVisibility(activeButton2);
        });
    }
    public void SetVisibility(int activeButton2){
        RelativeLayout lesson1 = findViewById(R.id.Lesson1);
        RelativeLayout lesson2 = findViewById(R.id.Lesson2);
        RelativeLayout lesson3 = findViewById(R.id.Lesson3);
        RelativeLayout lesson4 = findViewById(R.id.Lesson4);
        RelativeLayout lesson5 = findViewById(R.id.Lesson5);
        RelativeLayout lesson6 = findViewById(R.id.Lesson6);
        lesson1.setVisibility(View.GONE);
        lesson2.setVisibility(View.GONE);
        lesson3.setVisibility(View.GONE);
        lesson4.setVisibility(View.GONE);
        lesson5.setVisibility(View.GONE);
        lesson6.setVisibility(View.GONE);
        switch (this.activeButton2) {
            case 1:
                lesson1.setVisibility(View.VISIBLE);
                break;
            case 2:
                lesson1.setVisibility(View.VISIBLE);
                lesson2.setVisibility(View.VISIBLE);
                break;
            case 3:
                lesson1.setVisibility(View.VISIBLE);
                lesson2.setVisibility(View.VISIBLE);
                lesson3.setVisibility(View.VISIBLE);
                break;
            case 4:
                lesson1.setVisibility(View.VISIBLE);
                lesson2.setVisibility(View.VISIBLE);
                lesson3.setVisibility(View.VISIBLE);
                lesson4.setVisibility(View.VISIBLE);
                break;
            case 5:
                lesson1.setVisibility(View.VISIBLE);
                lesson2.setVisibility(View.VISIBLE);
                lesson3.setVisibility(View.VISIBLE);
                lesson4.setVisibility(View.VISIBLE);
                lesson5.setVisibility(View.VISIBLE);
                break;
            case 6:
                lesson1.setVisibility(View.VISIBLE);
                lesson2.setVisibility(View.VISIBLE);
                lesson3.setVisibility(View.VISIBLE);
                lesson4.setVisibility(View.VISIBLE);
                lesson5.setVisibility(View.VISIBLE);
                lesson6.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
}