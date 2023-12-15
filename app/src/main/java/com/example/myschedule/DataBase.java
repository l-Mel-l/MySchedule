package com.example.myschedule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataBase {
    private FirebaseAuth auth;
    private DatabaseReference schedules;

    public DataBase() {
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        schedules = db.getReference("Расписание 1");
    }

    public void register(Schedule schedule) {
        schedules.push().setValue(schedule)
                .addOnSuccessListener(aVoid -> {
                    // Успешно добавлено в базу данных
                    // Можете выполнять дополнительные действия здесь, если необходимо
                })
                .addOnFailureListener(e -> {
                    // Произошла ошибка при добавлении в базу данных
                    // Обработайте ошибку здесь, если необходимо
                });
    }
}