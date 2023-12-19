package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        DataBase dataBase = new DataBase();
        dataBase.CreateUserDataBase();

        EditText edittextpersonname = findViewById(R.id.editTextPersonName);
        EditText edittextpassword = findViewById(R.id.editTextPassword);

        CardView cardreg = findViewById(R.id.CardReg);
        cardreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataBase.registerUser(edittextpersonname.getText().toString().trim(),edittextpassword.getText().toString().trim(),RegisterActivity.this);
            }
        });
    }
}