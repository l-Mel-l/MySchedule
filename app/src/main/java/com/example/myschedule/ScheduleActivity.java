package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    long timeRemaining;
    long timeMax;

    DataBase database = new DataBase();
    String dayOfWeekInRussian;
    LocalTime perStart;
    LocalTime perEnd;
    public static String scheduleid;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        getSupportActionBar().hide();
        DataBase dataBase = new DataBase();

        // тут типа if(number != 1)}{ что-бы если мы переходим с нового окна не менять тут id расписания
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            scheduleid = (String) extras.get("number");
            dataBase.setScheduleId(scheduleid);
            SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("savedNumber", scheduleid);
            editor.apply();
        }
        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        scheduleid = preferences.getString("savedNumber", null);
        if (scheduleid == null) {
            dataBase.getScheduleId();
        }
        TextView nuberweektext = findViewById(R.id.NumberWeekText);
        TextView weektext = findViewById(R.id.WeekText);
        TextView nowlessonname = findViewById(R.id.NowLessonName);
        TextView nowtime = findViewById(R.id.NowTime);
        TextView nowcab = findViewById(R.id.NowCab);
        TextView timetext = findViewById(R.id.TimeText);
        TextView nextlessonname = findViewById(R.id.NextLessonName);
        TextView nexttime = findViewById(R.id.NextTime);
        TextView nextcab = findViewById(R.id.NextCab);
        TextView textview = findViewById(R.id.textView5);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        CardView fullschedule = findViewById(R.id.fullSchedule);
        Button backbtn = findViewById(R.id.BackBtn);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleActivity.this, FirstSettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalTime CurrentTime = getCurrentTime();
        // Получить текущий день недели
        DayOfWeek dayOfWeek = currentDateTime.getDayOfWeek();
        dayOfWeekInRussian = dayOfWeek.getDisplayName(TextStyle.FULL, new Locale("ru"));
        dayOfWeekInRussian = dayOfWeekInRussian.substring(0, 1).toUpperCase() + dayOfWeekInRussian.substring(1).toLowerCase();
        GetInfo(nuberweektext, CurrentTime, weektext, nowlessonname, nowtime, nowcab,timetext,nextlessonname,nexttime,nextcab,textview,progressBar);

        fullschedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleActivity.this, FullScheduleActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void GetInfo(TextView nuberweektext, LocalTime CurrentTime, TextView weektext, TextView nowlessonname, TextView nowtime, TextView nowcab,TextView timetext, TextView nextlessonname, TextView nexttime, TextView nextcab, TextView textview, ProgressBar progressBar){
        DataBase.ScheduleInfoCallback callback = new DataBase.ScheduleInfoCallback() {
            @Override
            public void onScheduleInfoReceived(String weekName, String lessonName, String weekday, String roomNumber, LocalTime startTime, LocalTime endTime, String perStartTime, String perEndTime, String nextLessonName, LocalTime nextStartTime, LocalTime nextEndTime, String nextPerStartTime, String nextPerEndTime, String nextRoomNumber) {
                nuberweektext.setText(weekName);
                weektext.setText(weekday);
                nowcab.setText("Кабинет " + roomNumber);
                if (!perStartTime.isEmpty() && !perEndTime.isEmpty()) {
                    perStart = LocalTime.parse(perStartTime);
                    perEnd = LocalTime.parse(perEndTime);
                }
                nowlessonname.setText(lessonName);
                if (perStartTime.isEmpty() && perEndTime.isEmpty()) {
                    if (lessonName == "" && CurrentTime.isBefore(nextStartTime)){
                        nowcab.setText("Отдыхайте");
                        nowlessonname.setText("Перемена");
                        nowtime.setText(endTime + " - " + nextStartTime);
                        Duration durationMax = Duration.between(endTime, nextStartTime);
                        timeMax = durationMax.getSeconds();
                        progressBar.setMax((int) timeMax); // Установка максимального значения ProgressBar
                        Duration duration = Duration.between(CurrentTime, nextStartTime);
                        timeRemaining = duration.getSeconds();
                        progressBar.setProgress((int) timeRemaining);
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, weekName,nuberweektext, weektext,nextlessonname,nexttime,nextcab,textview,perStart,perEnd,weekday,roomNumber,lessonName);
                    }
                    if (CurrentTime.isAfter(startTime) && CurrentTime.isBefore(endTime)){
                        LocalTime nextime = CurrentTime.minusSeconds(3);
                        nowtime.setText(startTime + " - " + endTime);
                        Duration duration = Duration.between(nextime, endTime);
                        timeRemaining = duration.getSeconds();
                        Duration durationMax = Duration.between(startTime, endTime);
                        timeMax = durationMax.getSeconds();
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, weekName,nuberweektext, weektext,nextlessonname,nexttime,nextcab,textview,perStart,perEnd,weekday,roomNumber,lessonName);
                    }
                }
                if (!perStartTime.isEmpty() && !perEndTime.isEmpty()) {
                    if (CurrentTime.isAfter(perStart) && CurrentTime.isBefore(perEnd)) {
                        nowcab.setText("Отдыхайте");
                        nowlessonname.setText("Перемена");
                        nowtime.setText(perStart + " - " + perEnd);
                        Duration durationMax = Duration.between(perStart, perEnd);
                        timeMax = durationMax.getSeconds();
                        progressBar.setMax((int) timeMax); // Установка максимального значения ProgressBar
                        Duration duration = Duration.between(CurrentTime, perEnd);
                        timeRemaining = duration.getSeconds();
                        progressBar.setProgress((int) timeRemaining);
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, weekName,nuberweektext, weektext,nextlessonname,nexttime,nextcab,textview,perStart,perEnd,weekday,roomNumber,lessonName);
                    }
                    if (CurrentTime.isAfter(startTime) && CurrentTime.isBefore(perStart)) {
                        nowtime.setText(startTime + " - " + perStartTime + "  " + perEndTime + " - " + endTime);
                        Duration duration = Duration.between(CurrentTime, perStart);
                        timeRemaining = duration.getSeconds();
                        Duration durationMax = Duration.between(startTime, perStart);
                        timeMax = durationMax.getSeconds();
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, weekName,nuberweektext, weektext,nextlessonname,nexttime,nextcab,textview,perStart,perEnd,weekday,roomNumber,lessonName);
                    }
                    if (CurrentTime.isAfter(perEnd) && CurrentTime.isBefore(endTime)) {
                        nowtime.setText(startTime + " - " + perStartTime + "  " + perEndTime + " - " + endTime);
                        Duration duration = Duration.between(CurrentTime, endTime);
                        timeRemaining = duration.getSeconds();
                        Duration durationMax = Duration.between(perEnd, endTime);
                        timeMax = durationMax.getSeconds();
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, weekName,nuberweektext, weektext,nextlessonname,nexttime,nextcab,textview,perStart,perEnd,weekday,roomNumber,lessonName);
                    }
                    if (CurrentTime.isAfter(endTime) && CurrentTime.isBefore(nextStartTime)){
                        nowcab.setText("Отдыхайте");
                        nowlessonname.setText("Перемена");
                        nowtime.setText(endTime + " - " + nextStartTime);
                        Duration durationMax = Duration.between(perEnd, nextStartTime);
                        timeMax = durationMax.getSeconds();
                        progressBar.setMax((int) timeMax); // Установка максимального значения ProgressBar
                        Duration duration = Duration.between(CurrentTime, nextStartTime);
                        timeRemaining = duration.getSeconds();
                        progressBar.setProgress((int) timeRemaining);
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, weekName, nuberweektext, weektext, nextlessonname, nexttime, nextcab, textview, perStart, perEnd,weekday,roomNumber,lessonName);
                    }
                }
                progressBar.setMax((int) timeMax); // Установка максимального значения ProgressBar
                progressBar.setProgress((int) timeRemaining);

                nextlessonname.setText(nextLessonName);
                if(nextRoomNumber.isEmpty()){
                    nextcab.setText("");
                }else{
                    nextcab.setText("Кабинет " + nextRoomNumber);}
                if (nextPerStartTime.isEmpty() && nextPerEndTime.isEmpty()) {
                    nexttime.setText(nextStartTime + " - " + nextEndTime);
                    if(nextStartTime == null && nextEndTime == null ){
                        nexttime.setText("Отдыхайте");
                    }
                } else {
                    nexttime.setText(nextStartTime + " - " + nextPerStartTime + "  " + nextPerEndTime + " - " + nextEndTime);
                    if(nextStartTime == null && nextEndTime == null ) {
                        nexttime.setText("Отдыхайте");
                    }
                }
            }
        };
        database.getScheduleInfo(dayOfWeekInRussian, CurrentTime, callback);

    }
    private void createTimer(LocalTime startTime, LocalTime endTime, ProgressBar progressBar,TextView timetext, TextView nowtime, TextView nowcab, TextView nowlessonname, LocalTime nextStartTime, LocalTime nextEndTime, String currentWeekName, TextView nuberweektext, TextView weektext,TextView nextlessonname,TextView nexttime, TextView nextcab, TextView textview, LocalTime perStart, LocalTime perEnd,String weekday,String roomNumber,String lessonName) {

        new CountDownTimer(timeRemaining * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress((int) (millisUntilFinished / 1000)); // Обновление значения ProgressBar
                int hours = (int) (millisUntilFinished / 3600000); // Перевод миллисекунд в часы
                int minutes = (int) ((millisUntilFinished % 3600000) / 60000); // Перевод оставшихся миллисекунд в минуты
                int seconds = (int) ((millisUntilFinished % 60000) / 1000); // Перевод оставшихся миллисекунд в секунды

                // Формирование строки с отформатированным временем
                String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                // Обновление TextView с отформатированным временем
                timetext.setText(timeString);
            }

            @Override
            public void onFinish() {
                LocalTime currentTime = getCurrentTime();
                Log.d("Tag", "lessonName: " + currentTime);
                LocalTime nextTime = currentTime.plusSeconds(3);
                if (perStart == null && perEnd == null) {
                    if (nextStartTime == null) {
                        textview.setText("Текущее занятие");
                        nowcab.setText("Отдыхайте");
                        nowlessonname.setText("Отсутствует");
                        nowtime.setText("");
                        progressBar.setProgress(0);
                        return;
                    }
                    if (nextTime.isAfter(endTime) && nextTime.isBefore(nextStartTime)) {
                        nowcab.setText("Отдыхайте");
                        nowlessonname.setText("Перемена");
                        nowtime.setText(endTime + " - " + nextStartTime);
                        Duration durationMax = Duration.between(endTime, nextStartTime);
                        timeMax = durationMax.getSeconds();
                        progressBar.setMax((int) timeMax); // Установка максимального значения ProgressBar
                        currentTime = getCurrentTime();
                        Duration duration = Duration.between(currentTime, nextStartTime);
                        timeRemaining = duration.getSeconds();
                        progressBar.setProgress((int) timeRemaining);
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, currentWeekName, nuberweektext, weektext, nextlessonname, nexttime, nextcab, textview, perStart, perEnd,weekday,roomNumber,lessonName);
                    }
                }
                    nextTime = currentTime.plusSeconds(3);
                    if (nextTime.isAfter(nextStartTime) && nextTime.isBefore(nextEndTime)) {
                        GetInfo(nuberweektext, nextTime, weektext, nowlessonname, nowtime, nowcab, timetext, nextlessonname, nexttime, nextcab, textview, progressBar);
                    }
                if (perStart != null && perEnd != null) {
                    if (nextTime.isAfter(perStart) && nextTime.isBefore(perEnd)) {
                        nowcab.setText("Отдыхайте");
                        nowlessonname.setText("Перемена");
                        nowtime.setText(perStart + " - " + perEnd);
                        Duration durationMax = Duration.between(perStart, perEnd);
                        timeMax = durationMax.getSeconds();
                        progressBar.setMax((int) timeMax); // Установка максимального значения ProgressBar
                        currentTime = getCurrentTime();
                        Duration duration = Duration.between(currentTime, perEnd);
                        timeRemaining = duration.getSeconds();
                        progressBar.setProgress((int) timeRemaining);
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, currentWeekName, nuberweektext, weektext, nextlessonname, nexttime, nextcab, textview, perStart, perEnd,weekday,roomNumber,lessonName);
                    }
                    if (nextTime.isAfter(startTime) && nextTime.isBefore(perStart)) {
                        nuberweektext.setText(currentWeekName);
                        weektext.setText(weekday);
                        nowcab.setText("Кабинет " + roomNumber);
                        nowlessonname.setText(lessonName);
                        nowtime.setText(startTime + " - " + perStart + "  " + perEnd + " - " + endTime);
                        Duration duration = Duration.between(currentTime, perStart);
                        timeRemaining = duration.getSeconds();
                        Duration durationMax = Duration.between(startTime, perStart);
                        timeMax = durationMax.getSeconds();
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, currentWeekName, nuberweektext, weektext, nextlessonname, nexttime, nextcab, textview, perStart, perEnd,weekday,roomNumber,lessonName);
                    }
                    if (nextTime.isAfter(perEnd) && nextTime.isBefore(endTime)){
                        nuberweektext.setText(currentWeekName);
                        weektext.setText(weekday);
                        nowcab.setText("Кабинет " + roomNumber);
                        nowlessonname.setText(lessonName);
                        nowtime.setText(startTime + " - " + perStart + "  " + perEnd + " - " + endTime);
                        Duration duration = Duration.between(currentTime, endTime);
                        timeRemaining = duration.getSeconds();
                        Duration durationMax = Duration.between(perEnd, endTime);
                        timeMax = durationMax.getSeconds();
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, currentWeekName, nuberweektext, weektext, nextlessonname, nexttime, nextcab, textview, perStart, perEnd,weekday,roomNumber,lessonName);
                    }
                    if (nextTime.isAfter(endTime) && nextTime.isBefore(nextStartTime)){
                        nowcab.setText("Отдыхайте");
                        nowlessonname.setText("Перемена");
                        nowtime.setText(endTime + " - " + nextStartTime);
                        Duration durationMax = Duration.between(perEnd, nextStartTime);
                        timeMax = durationMax.getSeconds();
                        progressBar.setMax((int) timeMax); // Установка максимального значения ProgressBar
                        currentTime = getCurrentTime();
                        Duration duration = Duration.between(currentTime, nextStartTime);
                        timeRemaining = duration.getSeconds();
                        progressBar.setProgress((int) timeRemaining);
                        createTimer(startTime, endTime, progressBar, timetext, nowtime, nowcab, nowlessonname, nextStartTime, nextEndTime, currentWeekName, nuberweektext, weektext, nextlessonname, nexttime, nextcab, textview, perStart, perEnd,weekday,roomNumber,lessonName);
                    }
                }
            }
        }.start();
    }
    private LocalTime getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        String hour = currentDateTime.getHour() < 10 ? "0" + currentDateTime.getHour() : Integer.toString(currentDateTime.getHour());
        String minute = currentDateTime.getMinute() < 10 ? "0" + currentDateTime.getMinute() : Integer.toString(currentDateTime.getMinute());
        String sec = currentDateTime.getSecond() < 10 ? "0" + currentDateTime.getSecond() : Integer.toString(currentDateTime.getSecond());
        return LocalTime.parse(hour + ":" + minute + ":" + sec);
    }

}