package com.example.myschedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.PrivateKey;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBase {
    private FirebaseAuth auth;
    private DatabaseReference lessons;

    private DatabaseReference schedules;
    LocalTime nextStartLessonTime;
    LocalTime nextEndLessonTime;
    String weekName;
    String lessonName;
    private static String scheduleNumb;

    private static DatabaseReference users = FirebaseDatabase.getInstance().getReference("Пользователи");
    private static String userKey;
    public void CreateScheduleDataBase(){
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        schedules = db.getReference("Расписания");
    }
    public void setScheduleId(String Scheduleid){

        scheduleNumb = Scheduleid;
    }
    public void getScheduleId() {
        DatabaseReference scheduleIDRef = FirebaseDatabase.getInstance().getReference("Расписания");

        scheduleIDRef.orderByChild("userId").equalTo(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    scheduleNumb = childSnapshot.getKey();
                    break;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void registerScheduleInfo(){
        DatabaseReference schedulesRef = FirebaseDatabase.getInstance().getReference("Расписания");

        // Добавление id пользователя в таблицу "Расписания"
        schedulesRef.push().child("userId").setValue(userKey);
    }
    public void CreateUserDataBase(){
        auth = FirebaseAuth.getInstance();
    }

    public void registerUser(String login, String password, Context context) {

        // Проверяем, существует ли логин в базе данных
        users.orderByChild("login").equalTo(login).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Логин уже существует, выдаем предупреждение
                    Toast.makeText(context, "Такой логин уже существует", Toast.LENGTH_SHORT).show();
                } else {
                    // Логин не существует, регистрируем пользователя
                    // Создаем объект пользователя
                    User user = new User(login, password);
                    // Генерируем уникальный id для пользователя
                    String userId = users.push().getKey();
                    // Добавляем пользователя в базу данных
                    users.child(userId).setValue(user);
                    // Регистрация успешна, переходим на главный экран
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок при чтении данных из базы данных
            }
        });
    }

    public void SingInUser(String login, String password, Context context) {
        users.orderByChild("login").equalTo(login).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user.getPassword().equals(password)) {
                            // Логин и пароль совпадают, открываем новое окно
                            userKey = userSnapshot.getKey();
                            DatabaseReference schedulesRef = FirebaseDatabase.getInstance().getReference("Расписания");
                            schedulesRef.orderByChild("userId").equalTo(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Если ScheduleID существует, переходим на главный экран
                                        Intent intent = new Intent(context, ScheduleActivity.class);
                                        context.startActivity(intent);
                                        ((Activity) context).finish();
                                    } else {
                                        // Если ScheduleID не существует, переходим на экран настроек
                                        Intent intent = new Intent(context, FirstSettingsActivity.class);
                                        context.startActivity(intent);
                                        ((Activity) context).finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }else {
                            // Если логин или пароль неверны, выводим ошибку
                            Toast.makeText(context, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок при чтении данных из базы данных
            }
        });
    }

    public void CreateDataBase() {
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        lessons = db.getReference("Занятия");
    }
    public void register(Schedule schedule) {
        // Получение ссылки на таблицу "Расписания"
        DatabaseReference schedulesRef = FirebaseDatabase.getInstance().getReference("Расписания");

        // Поиск расписания, которому принадлежит это занятие
        schedulesRef.orderByChild("userId").equalTo(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                        String scheduleId = scheduleSnapshot.getKey();
                        // Добавление id расписания в таблицу "Занятия"
                        DatabaseReference lessonsRef = FirebaseDatabase.getInstance().getReference("Занятия");
                        DatabaseReference newLessonRef = lessonsRef.push();
                        newLessonRef.child("endTime").setValue(schedule.getEndTime());
                        newLessonRef.child("lessonName").setValue(schedule.getLessonName());
                        newLessonRef.child("perEndTime").setValue(schedule.getPerEndTime());
                        newLessonRef.child("perStartTime").setValue(schedule.getPerStartTime());
                        newLessonRef.child("roomNumber").setValue(schedule.getRoomNumber());
                        newLessonRef.child("startTime").setValue(schedule.getStartTime());
                        newLessonRef.child("weekName").setValue(schedule.getWeekName());
                        newLessonRef.child("weekday").setValue(schedule.getWeekday());
                        newLessonRef.child("scheduleId").setValue(scheduleId);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок при чтении данных из базы данных
            }
        });
    }
    public interface ScheduleFullInfoCallback {
        void onScheduleFullInfoReceived(List<Map<String, String>> scheduleInfoList);
    }
    public void getAllScheduleInfo(String currentDayOfWeek, ScheduleFullInfoCallback callback) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Занятия");

        Query currentDayLessonsQuery = reference.orderByChild("weekday").equalTo(currentDayOfWeek);
        currentDayLessonsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, String>> scheduleInfoList = new ArrayList<>();
                for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                    String lessonScheduleId = scheduleSnapshot.child("scheduleId").getValue(String.class);
                    if (lessonScheduleId.equals(scheduleNumb)) {
                        Map<String, String> scheduleInfo = new HashMap<>();
                        scheduleInfo.put("weekName", scheduleSnapshot.child("weekName").getValue(String.class));
                        scheduleInfo.put("lessonName", scheduleSnapshot.child("lessonName").getValue(String.class));
                        scheduleInfo.put("weekday", scheduleSnapshot.child("weekday").getValue(String.class));
                        scheduleInfo.put("roomNumber", scheduleSnapshot.child("roomNumber").getValue(String.class));
                        scheduleInfo.put("startTime", scheduleSnapshot.child("startTime").getValue(String.class));
                        scheduleInfo.put("endTime", scheduleSnapshot.child("endTime").getValue(String.class));
                        scheduleInfo.put("perStartTime", scheduleSnapshot.child("perStartTime").getValue(String.class));
                        scheduleInfo.put("perEndTime", scheduleSnapshot.child("perEndTime").getValue(String.class));

                        scheduleInfoList.add(scheduleInfo);
                    }
                }
                callback.onScheduleFullInfoReceived(scheduleInfoList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок при чтении данных
            }
        });
    }

    public interface ScheduleInfoCallback {
        void onScheduleInfoReceived(String weekName, String lessonName, String weekday, String roomNumber, LocalTime startTime, LocalTime endTime, String perStartTime, String perEndTime, String nextLessonName, LocalTime nextStartTime, LocalTime nextEndTime, String nextPerStartTime, String nextPerEndTime, String nextRoomNumber);
    }

    public void getScheduleInfo(String currentDayOfWeek, LocalTime currentTime, ScheduleInfoCallback callback, String currentweek) {
        // Получение ссылки на базу данных Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Занятия");

        // Создание запроса к базе данных для текущего занятия
        Query currentLessonQuery = reference.orderByChild("weekday").equalTo(currentDayOfWeek);
        currentLessonQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                    // Получение значений из снимка данных
                    String lessonScheduleId = scheduleSnapshot.child("scheduleId").getValue(String.class);
                        if (lessonScheduleId.equals(scheduleNumb)) {
                            String startTime = scheduleSnapshot.child("startTime").getValue(String.class);
                            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                    .appendPattern("[H:mm][HH:mm]")
                                    .toFormatter();
                            LocalTime startles = LocalTime.parse(startTime, formatter);
                            String endTime = scheduleSnapshot.child("endTime").getValue(String.class);
                            DateTimeFormatter formatter2 = new DateTimeFormatterBuilder()
                                    .appendPattern("[H:mm][HH:mm]")
                                    .toFormatter();
                            LocalTime endles = LocalTime.parse(endTime, formatter2);

                            // Сравнение времени начала и конца занятия с текущим временем
                            if (isTimeInRange(startles, endles, currentTime)) {
                                // Обработать полученные данные для текущего занятия
                                String weekName = scheduleSnapshot.child("weekName").getValue(String.class);
                                String lessonName = scheduleSnapshot.child("lessonName").getValue(String.class);
                                String weekday = scheduleSnapshot.child("weekday").getValue(String.class);
                                String roomNumber = scheduleSnapshot.child("roomNumber").getValue(String.class);
                                String perStartTime = scheduleSnapshot.child("perStartTime").getValue(String.class);
                                String perEndTime = scheduleSnapshot.child("perEndTime").getValue(String.class);

                                // Создание запроса к базе данных для следующего занятия
                                Query nextLessonQuery = reference.orderByChild("weekday").equalTo(currentDayOfWeek);
                                nextLessonQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        LocalTime closestStartTime = null;
                                        DataSnapshot closestLessonSnapshot = null;

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            String startTime = snapshot.child("startTime").getValue(String.class);
                                            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                                    .appendPattern("[H:mm][HH:mm]")
                                                    .toFormatter();
                                            LocalTime lessonStartTime = LocalTime.parse(startTime, formatter);

                                            if (lessonStartTime.isAfter(endles) && (closestStartTime == null || lessonStartTime.isBefore(closestStartTime))) {
                                                closestStartTime = lessonStartTime;
                                                closestLessonSnapshot = snapshot;
                                            }
                                        }

                                        if (closestLessonSnapshot != null) {
                                            // Получение информации о ближайшем следующем занятии
                                            String nextLessonName = closestLessonSnapshot.child("lessonName").getValue(String.class);
                                            String nextStartTime = closestLessonSnapshot.child("startTime").getValue(String.class);
                                            String nextEndTime = closestLessonSnapshot.child("endTime").getValue(String.class);
                                            String nextPerStartTime = closestLessonSnapshot.child("perStartTime").getValue(String.class);
                                            String nextPerEndTime = closestLessonSnapshot.child("perEndTime").getValue(String.class);
                                            String nextRoomNumber = closestLessonSnapshot.child("roomNumber").getValue(String.class);

                                            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                                    .appendPattern("[H:mm][HH:mm]")
                                                    .toFormatter();
                                            LocalTime nextStartLessonTime = LocalTime.parse(nextStartTime, formatter);

                                            DateTimeFormatter formatter2 = new DateTimeFormatterBuilder()
                                                    .appendPattern("[H:mm][HH:mm]")
                                                    .toFormatter();
                                            LocalTime nextEndLessonTime = LocalTime.parse(nextEndTime, formatter2);

                                            // Возврат всей информации через колбек
                                            callback.onScheduleInfoReceived(weekName, lessonName, weekday, roomNumber, startles, endles, perStartTime, perEndTime, nextLessonName, nextStartLessonTime, nextEndLessonTime, nextPerStartTime, nextPerEndTime, nextRoomNumber);
                                        } else {
                                            String nextLessonName = "Отсутствует";
                                            String nextStartTime = "";
                                            String nextEndTime = "";
                                            String nextPerStartTime = "";
                                            String nextPerEndTime = "";
                                            String nextRoomNumber = "";
                                            LocalTime nextStartLessonTime = null;
                                            LocalTime nextEndLessonTime = null;

                                            callback.onScheduleInfoReceived(weekName, lessonName, weekday, roomNumber, startles, endles, perStartTime, perEndTime, nextLessonName, nextStartLessonTime, nextEndLessonTime, nextPerStartTime, nextPerEndTime, nextRoomNumber);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // Обработка ошибок
                                    }
                                });
                                return;
                            } else {
                                String weekday = "";
                                String roomNumber = "";
                                String perStartTime = "";
                                String perEndTime = "";

                                Query nextLessonQuery = reference.orderByChild("weekday").equalTo(currentDayOfWeek);
                                nextLessonQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        LocalTime closestStartTime = null;
                                        DataSnapshot closestLessonSnapshot = null;

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            String startTime = snapshot.child("startTime").getValue(String.class);
                                            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                                    .appendPattern("[H:mm][HH:mm]")
                                                    .toFormatter();
                                            LocalTime lessonStartTime = LocalTime.parse(startTime, formatter);

                                            if (lessonStartTime.isAfter(endles) && (closestStartTime == null || lessonStartTime.isBefore(closestStartTime))) {
                                                closestStartTime = lessonStartTime;
                                                closestLessonSnapshot = snapshot;
                                            }
                                        }

                                        if (closestLessonSnapshot != null) {
                                            // Получение информации о ближайшем следующем занятии
                                            String nextLessonName = closestLessonSnapshot.child("lessonName").getValue(String.class);
                                            String nextStartTime = closestLessonSnapshot.child("startTime").getValue(String.class);
                                            String nextEndTime = closestLessonSnapshot.child("endTime").getValue(String.class);
                                            String nextPerStartTime = closestLessonSnapshot.child("perStartTime").getValue(String.class);
                                            String nextPerEndTime = closestLessonSnapshot.child("perEndTime").getValue(String.class);
                                            String nextRoomNumber = closestLessonSnapshot.child("roomNumber").getValue(String.class);

                                            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                                    .appendPattern("[H:mm][HH:mm]")
                                                    .toFormatter();
                                            LocalTime nextStartLessonTime = LocalTime.parse(nextStartTime, formatter);

                                            DateTimeFormatter formatter2 = new DateTimeFormatterBuilder()
                                                    .appendPattern("[H:mm][HH:mm]")
                                                    .toFormatter();
                                            LocalTime nextEndLessonTime = LocalTime.parse(nextEndTime, formatter2);

                                            // Возврат всей информации через колбек
                                            callback.onScheduleInfoReceived(weekName, lessonName, weekday, roomNumber, startles, endles, perStartTime, perEndTime, nextLessonName, nextStartLessonTime, nextEndLessonTime, nextPerStartTime, nextPerEndTime, nextRoomNumber);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // Обработка ошибок
                                    }
                                });
                            }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок
            }
        });
    }


    // Метод для проверки, находится ли текущее время в заданном временном диапазоне
    private static boolean isTimeInRange(LocalTime startTime, LocalTime endTime, LocalTime currentTime) {
        return currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
    }
}