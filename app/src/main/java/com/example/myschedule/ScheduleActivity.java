package com.example.myschedule;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        ProgressBar progressBar = findViewById(R.id.progressBar);

        LocalDateTime currentDateTime = LocalDateTime.now();
        String hour = currentDateTime.getHour() < 10 ? "0" + currentDateTime.getHour() : Integer.toString(currentDateTime.getHour());
        String minute = currentDateTime.getMinute() < 10 ? "0" + currentDateTime.getMinute() : Integer.toString(currentDateTime.getMinute());
        String sec = currentDateTime.getSecond() < 10 ? "0" + currentDateTime.getSecond() : Integer.toString(currentDateTime.getSecond());
        LocalTime CurrentTime = LocalTime.parse(hour + ":" + minute + ":" + sec);
// Получить текущий день недели
        DayOfWeek dayOfWeek = currentDateTime.getDayOfWeek();
        String dayOfWeekInRussian = dayOfWeek.getDisplayName(TextStyle.FULL, new Locale("ru"));
        dayOfWeekInRussian = dayOfWeekInRussian.substring(0, 1).toUpperCase() + dayOfWeekInRussian.substring(1).toLowerCase();

        DataBase database = new DataBase();

        DataBase.ScheduleDataCallback callback = new DataBase.ScheduleDataCallback() {
            @Override
            public void onScheduleDataReceived(String weekName, String lessonName, String weekday, String roomNumber, LocalTime startTime, LocalTime endTime, String perStartTime, String perEndTime) {
                nuberweektext.setText(weekName);
                weektext.setText(weekday);
                nowcab.setText("Кабинет " + roomNumber);
                nowlessonname.setText(lessonName);
                if (perStartTime.isEmpty() && perEndTime.isEmpty()) {
                    nowtime.setText(startTime + " - " + endTime);
                } else {
                    nowtime.setText(startTime + " - " + perStartTime + " " + perEndTime + " - " + endTime);
                }
                Duration duration = Duration.between(CurrentTime, endTime);
                long timeRemaining = duration.getSeconds();
                Duration duration2 = Duration.between(startTime, endTime);
                long timeMax = duration2.getSeconds();
                progressBar.setMax((int) timeMax); // Установка максимального значения ProgressBar
                progressBar.setProgress((int) timeRemaining);

                // Установка текущего значения ProgressBar

// Создание таймера для обновления ProgressBar каждую секунду
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
                        // Действия, которые нужно выполнить по завершении таймера
                    }
                }.start();

            }
        };

        database.DataBaseGetInfo(dayOfWeekInRussian, CurrentTime, callback);

        DataBase.ScheduleNextLessDataCallback callback2 = new DataBase.ScheduleNextLessDataCallback() {
            @Override
            public void onScheduleNextLessDataReceived(String nextlessonName, LocalTime nextstartTime, LocalTime nextendTime, String nextperStartTime, String nextperEndTime, String nextCab) {
                nextlessonname.setText(nextlessonName);
                nextcab.setText("Кабинет " + nextCab);
                if (nextperStartTime.isEmpty() && nextperEndTime.isEmpty()) {
                    nexttime.setText(nextstartTime + " - " + nextendTime);
                } else {
                    nexttime.setText(nextstartTime + " - " + nextperStartTime + "  " + nextperEndTime + " - " + nextendTime);
                }
            }
        };
        database.DataBaseNextLessGetInfo(dayOfWeekInRussian, CurrentTime, callback2);
    }
}