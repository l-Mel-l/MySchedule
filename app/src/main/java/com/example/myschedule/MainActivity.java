package com.example.myschedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        EditText intextpersonname = findViewById(R.id.InTextPersonName);
        EditText intextpassword = findViewById(R.id.InTextPassword);

        DataBase dataBase = new DataBase();

        CardView cardview = findViewById(R.id.cardviewbtn);
        cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataBase.SingInUser(intextpersonname.getText().toString().trim(), intextpassword.getText().toString().trim(), MainActivity.this);
            }
        });
        TextView textView = findViewById(R.id.SignUpText);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}