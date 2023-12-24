package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChooseLessonsActivity extends AppCompatActivity {


    int activeButton2 = 1;
    String[] receivedArray = null;
    String[] weekArray = null;
    int currentIndexDay = 0;
    int currentIndexWeek = 0;

    EditText[] lessonnamefirst = new EditText[6];
    EditText[] firsttimenamefirst = new EditText[6];
    EditText[] sectimenamefirst = new EditText[6];
    EditText[] cabnumber = new EditText[6];
    EditText[] perfirsttimenamefirst = new EditText[6];
    EditText[] persectimenamefirst = new EditText[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_lessons);
        getSupportActionBar().hide();
        SetVisibility(activeButton2);
        DataBase database = new DataBase();
        database.CreateDataBase();

        Button lessonbtn1 = findViewById(R.id.LessonBtn1);
        Button lessonbtn2 = findViewById(R.id.LessonBtn2);
        Button lessonbtn3 = findViewById(R.id.LessonBtn3);
        Button lessonbtn4 = findViewById(R.id.LessonBtn4);
        Button lessonbtn5 = findViewById(R.id.LessonBtn5);
        Button lessonbtn6 = findViewById(R.id.LessonBtn6);
        Button nextdaybtn = findViewById(R.id.NextDayBtn);
        TextView textview = findViewById(R.id.textview1);

        lessonnamefirst[0] = findViewById(R.id.LessonNameFirst1); lessonnamefirst[1] = findViewById(R.id.LessonNameFirst2); lessonnamefirst[2] = findViewById(R.id.LessonNameFirst3); lessonnamefirst[3] = findViewById(R.id.LessonNameFirst4); lessonnamefirst[4] = findViewById(R.id.LessonNameFirst5); lessonnamefirst[5] = findViewById(R.id.LessonNameFirst6);
        firsttimenamefirst[0] = findViewById(R.id.FirstTimeNameFirst1); firsttimenamefirst[1] = findViewById(R.id.FirstTimeNameFirst2); firsttimenamefirst[2] = findViewById(R.id.FirstTimeNameFirst3); firsttimenamefirst[3] = findViewById(R.id.FirstTimeNameFirst4); firsttimenamefirst[4] = findViewById(R.id.FirstTimeNameFirst5); firsttimenamefirst[5] = findViewById(R.id.FirstTimeNameFirst6);
        sectimenamefirst[0] = findViewById(R.id.SecTimeNameFirst1); sectimenamefirst[1] = findViewById(R.id.SecTimeNameFirst2); sectimenamefirst[2] = findViewById(R.id.SecTimeNameFirst3); sectimenamefirst[3] = findViewById(R.id.SecTimeNameFirst4); sectimenamefirst[4] = findViewById(R.id.SecTimeNameFirst5); sectimenamefirst[5] = findViewById(R.id.SecTimeNameFirst6);
        cabnumber[0] = findViewById(R.id.CabNumber1); cabnumber[1] = findViewById(R.id.CabNumber2); cabnumber[2] = findViewById(R.id.CabNumber3); cabnumber[3] = findViewById(R.id.CabNumber4); cabnumber[4] = findViewById(R.id.CabNumber5); cabnumber[5] = findViewById(R.id.CabNumber6);
        perfirsttimenamefirst[0] = findViewById(R.id.PerFirstTimeNameFirst1); perfirsttimenamefirst[1] = findViewById(R.id.PerFirstTimeNameFirst2); perfirsttimenamefirst[2] = findViewById(R.id.PerFirstTimeNameFirst3); perfirsttimenamefirst[3] = findViewById(R.id.PerFirstTimeNameFirst4); perfirsttimenamefirst[4] = findViewById(R.id.PerFirstTimeNameFirst5); perfirsttimenamefirst[5] = findViewById(R.id.PerFirstTimeNameFirst6);
        persectimenamefirst[0] = findViewById(R.id.PerSecTimeNameFirst1); persectimenamefirst[1] = findViewById(R.id.PerSecTimeNameFirst2); persectimenamefirst[2] = findViewById(R.id.PerSecTimeNameFirst3); persectimenamefirst[3] = findViewById(R.id.PerSecTimeNameFirst4); persectimenamefirst[4] = findViewById(R.id.PerSecTimeNameFirst5); persectimenamefirst[5] = findViewById(R.id.PerSecTimeNameFirst6);

        TextView numberweektext = findViewById(R.id.NumberWeekText);
        lessonbtn1.setBackgroundResource(R.drawable.rounded_button_selected);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            receivedArray = (String[]) extras.get("weekend");
        } else {
            receivedArray = new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
        }
        if (extras != null) {
            weekArray = (String[]) extras.get("week");
        }

        while (receivedArray[currentIndexDay] == null){
            currentIndexDay++;
        }
        textview.setText(receivedArray[currentIndexDay]);
        numberweektext.setText(weekArray[currentIndexWeek]);
        currentIndexDay++;
        currentIndexWeek++;


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
        nextdaybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //отправка
                for (int i = 0; i < activeButton2; i++) {
                    Schedule schedule = new Schedule(
                        lessonnamefirst[i].getText().toString().trim(),
                        firsttimenamefirst[i].getText().toString().trim(),
                        sectimenamefirst[i].getText().toString().trim(),
                        cabnumber[i].getText().toString().trim(),
                        textview.getText().toString().trim(),
                        numberweektext.getText().toString().trim(),
                        perfirsttimenamefirst[i].getText().toString().trim(),
                        persectimenamefirst[i].getText().toString().trim());
                        database.register(schedule);
                }
                while (receivedArray.length > currentIndexDay && receivedArray[currentIndexDay] == null) {
                    currentIndexDay++; // Пропуск null значений
                }
                if (currentIndexDay < receivedArray.length) {
                    textview.setText(receivedArray[currentIndexDay]); // Установка следующего значения
                }
                if (currentIndexDay >= receivedArray.length) {
                    if(weekArray[currentIndexWeek].isEmpty()){
                        Intent intent = new Intent(ChooseLessonsActivity.this, ScheduleActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    currentIndexDay = 0; // Возвращаемся к первому элементу
                    textview.setText(receivedArray[currentIndexDay]);
                    currentIndexDay++;
                    while (weekArray.length > currentIndexWeek && weekArray[currentIndexWeek] == null) {
                        currentIndexWeek++; // Пропуск null значений
                    }
                    if (currentIndexWeek < weekArray.length) {
                        numberweektext.setText(weekArray[currentIndexWeek]); // Установка следующего значения недели
                        currentIndexWeek++;
                    }
                }
                currentIndexDay++;
            }
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