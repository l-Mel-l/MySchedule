package com.example.myschedule;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        getSupportActionBar().hide();

        TextView nuberweektext = findViewById(R.id.NumberWeekText);
        TextView weektext = findViewById(R.id.WeekText);
        TextView nowlessonname = findViewById(R.id.NowLessonName);
        TextView nowtime = findViewById(R.id.NowTime);
        TextView nowcab = findViewById(R.id.NowCab);
        TextView timetext = findViewById(R.id.TimeText);
        TextView nextlessonname = findViewById(R.id.NextLessonName);
        TextView nexttime = findViewById(R.id.NextTime);
        TextView nextcab = findViewById(R.id.NextCab);

        LocalDateTime currentDateTime = LocalDateTime.now();
        int hour = currentDateTime.getHour();
        int minute = currentDateTime.getMinute();
        String CurrentTime = hour + ":" + minute;
// Получить текущий день недели
        DayOfWeek dayOfWeek = currentDateTime.getDayOfWeek();
        String dayOfWeekInRussian = dayOfWeek.getDisplayName(TextStyle.FULL, new Locale("ru"));
        dayOfWeekInRussian = dayOfWeekInRussian.substring(0, 1).toUpperCase() + dayOfWeekInRussian.substring(1).toLowerCase();

        DataBase database = new DataBase();
        database.DataBaseGetInfo(dayOfWeekInRussian,CurrentTime);


    }
}