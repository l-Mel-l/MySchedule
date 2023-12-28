package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FullScheduleActivity extends AppCompatActivity {
    String dayOfWeekInRussian;
    DataBase database = new DataBase();
    int les = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_schedule);
        getSupportActionBar().hide();

        TextView numberweektext = findViewById(R.id.NumberWeekText);
        TextView weektext = findViewById(R.id.WeekText);
        TextView firstlessonname = findViewById(R.id.FirstLessonName);TextView firsttime = findViewById(R.id.FirstTime); TextView firstCab = findViewById(R.id.FirstCab);
        TextView seclessonname = findViewById(R.id.SecLessonName); TextView secTime = findViewById(R.id.SecTime); TextView secCab = findViewById(R.id.SecCab);
        TextView thirdlessonname = findViewById(R.id.ThirdLessonName); TextView thirdTime = findViewById(R.id.ThirdTime); TextView thirdCab = findViewById(R.id.ThirdCab);
        TextView fourthlessonname = findViewById(R.id.FourthLessonName); TextView FourthTime = findViewById(R.id.FourthTime); TextView fourthCab = findViewById(R.id.FourthCab);
        TextView fifthlessonname = findViewById(R.id.FifthLessonName); TextView fifthTime = findViewById(R.id.FifthTime); TextView FifthCab = findViewById(R.id.FifthCab);
        TextView sixthlessonname = findViewById(R.id.SixthLessonName); TextView sixthTime = findViewById(R.id.SixthTime); TextView SixthCab = findViewById(R.id.SixthCab);
        LinearLayout firstLes = findViewById(R.id.firstLes); LinearLayout secLes = findViewById(R.id.secLes); LinearLayout thirdLes = findViewById(R.id.thirdLes); LinearLayout fourthLes = findViewById(R.id.fourthLes); LinearLayout fifthLes = findViewById(R.id.fifthLes); LinearLayout sixthLes = findViewById(R.id.sixthLes);
        CardView back = findViewById(R.id.backbtn);

        LocalDateTime currentDateTime = LocalDateTime.now();
        // Получить текущий день недели
        DayOfWeek dayOfWeek = currentDateTime.getDayOfWeek();
        dayOfWeekInRussian = dayOfWeek.getDisplayName(TextStyle.FULL, new Locale("ru"));
        dayOfWeekInRussian = dayOfWeekInRussian.substring(0, 1).toUpperCase() + dayOfWeekInRussian.substring(1).toLowerCase();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FullScheduleActivity.this, ScheduleActivity.class);
                startActivity(intent);
                finish();
            }
        });

        DataBase.ScheduleFullInfoCallback callback = new DataBase.ScheduleFullInfoCallback() {
            @Override
            public void onScheduleFullInfoReceived(List<Map<String, String>> scheduleInfoList) {

                    int numLessons = scheduleInfoList.size();

                    firstLes.setVisibility(numLessons >= 1 ? View.VISIBLE : View.GONE);
                    secLes.setVisibility(numLessons >= 2 ? View.VISIBLE : View.GONE);
                    thirdLes.setVisibility(numLessons >= 3 ? View.VISIBLE : View.GONE);
                    fourthLes.setVisibility(numLessons >= 4 ? View.VISIBLE : View.GONE);
                    fifthLes.setVisibility(numLessons >= 5 ? View.VISIBLE : View.GONE);
                    sixthLes.setVisibility(numLessons >= 6 ? View.VISIBLE : View.GONE);

                    if (numLessons >= 1) {
                        firstlessonname.setText(scheduleInfoList.get(0).get("lessonName"));
                        firstCab.setText("Кабинет " + scheduleInfoList.get(0).get("roomNumber"));
                        if (scheduleInfoList.get(0).get("perStartTime").isEmpty() && scheduleInfoList.get(0).get("perEndTime").isEmpty()) {
                            firsttime.setText(scheduleInfoList.get(0).get("startTime") + " - " + scheduleInfoList.get(0).get("endTime"));
                        } else {
                            firsttime.setText(scheduleInfoList.get(0).get("startTime") + " - " + scheduleInfoList.get(0).get("perStartTime") + "  " + scheduleInfoList.get(0).get("perEndTime") + " - " + scheduleInfoList.get(0).get("endTime"));
                        }
                        les++;
                        firstLes.setVisibility(View.VISIBLE);
                        if (numLessons == 1){
                            firstLes.setBackgroundResource(R.drawable.rounded_linear4);
                        }
                    }
                    if (numLessons >= 2) {
                        seclessonname.setText(scheduleInfoList.get(1).get("lessonName"));
                        secCab.setText("Кабинет " + scheduleInfoList.get(1).get("roomNumber"));
                        if (scheduleInfoList.get(1).get("perStartTime").isEmpty() && scheduleInfoList.get(1).get("perEndTime").isEmpty()) {
                            secTime.setText(scheduleInfoList.get(1).get("startTime") + " - " + scheduleInfoList.get(1).get("endTime"));
                        } else {
                            secTime.setText(scheduleInfoList.get(1).get("startTime") + " - " + scheduleInfoList.get(1).get("perStartTime") + "  " + scheduleInfoList.get(1).get("perEndTime") + " - " + scheduleInfoList.get(1).get("endTime"));
                        }
                        les++;
                        secLes.setVisibility(View.VISIBLE);
                        if (numLessons == 2){
                            secLes.setBackgroundResource(R.drawable.rounded_linear3);
                        }
                    }
                    if (numLessons >= 3) {
                        thirdlessonname.setText(scheduleInfoList.get(2).get("lessonName"));
                        thirdCab.setText("Кабинет " + scheduleInfoList.get(2).get("roomNumber"));
                        if (scheduleInfoList.get(2).get("perStartTime").isEmpty() && scheduleInfoList.get(2).get("perEndTime").isEmpty()) {
                            thirdTime.setText(scheduleInfoList.get(2).get("startTime") + " - " + scheduleInfoList.get(2).get("endTime"));
                        } else {
                            thirdTime.setText(scheduleInfoList.get(2).get("startTime") + " - " + scheduleInfoList.get(2).get("perStartTime") + "  " + scheduleInfoList.get(2).get("perEndTime") + " - " + scheduleInfoList.get(2).get("endTime"));
                        }
                        les++;
                        thirdLes.setVisibility(View.VISIBLE);
                    }
                    if (numLessons >= 4) {
                        fourthlessonname.setText(scheduleInfoList.get(3).get("lessonName"));
                        fourthCab.setText("Кабинет " + scheduleInfoList.get(3).get("roomNumber"));
                        if (scheduleInfoList.get(3).get("perStartTime").isEmpty() && scheduleInfoList.get(3).get("perEndTime").isEmpty()) {
                            FourthTime.setText(scheduleInfoList.get(3).get("startTime") + " - " + scheduleInfoList.get(3).get("endTime"));
                        } else {
                            FourthTime.setText(scheduleInfoList.get(3).get("startTime") + " - " + scheduleInfoList.get(3).get("perStartTime") + "  " + scheduleInfoList.get(3).get("perEndTime") + " - " + scheduleInfoList.get(3).get("endTime"));
                        }
                        les++;
                        fourthLes.setVisibility(View.VISIBLE);
                    }
                    if (numLessons >= 5) {
                        fifthlessonname.setText(scheduleInfoList.get(4).get("lessonName"));
                        FifthCab.setText("Кабинет " + scheduleInfoList.get(4).get("roomNumber"));
                        if (scheduleInfoList.get(4).get("perStartTime").isEmpty() && scheduleInfoList.get(4).get("perEndTime").isEmpty()) {
                            fifthTime.setText(scheduleInfoList.get(4).get("startTime") + " - " + scheduleInfoList.get(4).get("endTime"));
                        } else {
                            fifthTime.setText(scheduleInfoList.get(4).get("startTime") + " - " + scheduleInfoList.get(4).get("perStartTime") + "  " + scheduleInfoList.get(4).get("perEndTime") + " - " + scheduleInfoList.get(4).get("endTime"));
                        }
                        les++;
                        fifthLes.setVisibility(View.VISIBLE);
                    }
                    if (numLessons >= 6) {
                        sixthlessonname.setText(scheduleInfoList.get(5).get("lessonName"));
                        SixthCab.setText("Кабинет " + scheduleInfoList.get(5).get("roomNumber"));
                        if (scheduleInfoList.get(5).get("perStartTime").isEmpty() && scheduleInfoList.get(5).get("perEndTime").isEmpty()) {
                            sixthTime.setText(scheduleInfoList.get(5).get("startTime") + " - " + scheduleInfoList.get(5).get("endTime"));
                        } else {
                            sixthTime.setText(scheduleInfoList.get(5).get("startTime") + " - " + scheduleInfoList.get(5).get("perStartTime") + "  " + scheduleInfoList.get(5).get("perEndTime") + " - " + scheduleInfoList.get(5).get("endTime"));
                        }
                        les++;
                        sixthLes.setVisibility(View.VISIBLE);
                    }
                }
        };
        database.getAllScheduleInfo(dayOfWeekInRussian,callback);
    }
}