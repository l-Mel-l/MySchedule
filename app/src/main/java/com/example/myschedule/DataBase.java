package com.example.myschedule;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;

public class DataBase {
    private FirebaseAuth auth;
    private DatabaseReference schedules;


    private static String newTableName;

    public void CreateDataBase(String tableName) {
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        schedules = db.getReference(tableName);
    }
    public void DataBaseName() {
        DatabaseReference namesRef = FirebaseDatabase.getInstance().getReference("Имена Таблиц Расписания");

        namesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    String lastTableName = "";
                    // Если есть дочерние значения, получи последнюю таблицу
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        lastTableName = childSnapshot.getKey();
                    }

                    // Получи цифру из последнего имени таблицы
                    int lastTableNumber = extractNumberFromTableName(lastTableName);

                    // Увеличь цифру на 1
                    int newTableNumber = lastTableNumber + 1;

                    // Создай новое имя таблицы
                    newTableName = "Расписание " + newTableNumber;

                    // Создай новую таблицу расписания с новым именем
                    CreateDataBase(newTableName);
                    namesRef.child(newTableName).setValue(true);
                } else {
                    // Если нет дочерних значений, создай таблицу "Расписание 1"
                    newTableName = "Расписание 1";
                    CreateDataBase(newTableName);
                    namesRef.child(newTableName).setValue(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при чтении из базы данных
            }
        });
    }

    private int extractNumberFromTableName(String tableName) {
        String numberString = tableName.replaceAll("\\D+", "");
        return Integer.parseInt(numberString);
    }

    public void register(Schedule schedule) {
        schedules.push().setValue(schedule)
                .addOnSuccessListener(aVoid -> {
                    // Успешно добавлено в базу данных
                })
                .addOnFailureListener(e -> {
                    // Произошла ошибка при добавлении в базу данных
                });
    }
    public void DataBaseGetInfo(String CurrentDayOfWeek, String CurrentTime){
        // Получение ссылки на базу данных Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(DataBase.newTableName);

        // Создание запроса к базе данных
        Query query = reference.orderByChild("weekday").equalTo(CurrentDayOfWeek);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                    // Получение значений из снимка данных
                    String startTime = scheduleSnapshot.child("startTime").getValue(String.class);
                    String endTime = scheduleSnapshot.child("endTime").getValue(String.class);

                    LocalTime currentTime = LocalTime.parse(CurrentTime);
                    LocalTime startles = LocalTime.parse(startTime);
                    LocalTime endles = LocalTime.parse(endTime);

                    // Сравнение времени начала и конца занятия с текущим временем
                    if (isTimeInRange(startles, endles, currentTime)) {
                        // обработать полученные данные
                        System.out.println(scheduleSnapshot.getValue());
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