package com.example.myschedule;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WeekNamesActivity extends AppCompatActivity {

    String[] editTexts = {"","","",""};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_week_names);
        int activeButton = getIntent().getIntExtra("activeButton", 0);

        TextView weektext1 = findViewById(R.id.WeekText1);
        TextView weektext2 = findViewById(R.id.WeekText2);
        TextView weektext3 = findViewById(R.id.WeekText3);
        TextView weektext4 = findViewById(R.id.WeekText4);
        EditText weeknamefirs1 = findViewById(R.id.WeekNameFirst1);
        EditText weeknamefirs2 = findViewById(R.id.WeekNameFirst2);
        EditText weeknamefirs3 = findViewById(R.id.WeekNameFirst3);
        EditText weeknamefirs4 = findViewById(R.id.WeekNameFirst4);
        Button savebtn = findViewById(R.id.SaveBtn);

        weektext1.setVisibility(View.GONE);
        weeknamefirs1.setVisibility(View.GONE);
        weektext2.setVisibility(View.GONE);
        weeknamefirs2.setVisibility(View.GONE);
        weektext3.setVisibility(View.GONE);
        weeknamefirs3.setVisibility(View.GONE);
        weektext4.setVisibility(View.GONE);
        weeknamefirs4.setVisibility(View.GONE);


        switch (activeButton) {
            case 1:
                weektext1.setVisibility(View.VISIBLE);
                weeknamefirs1.setVisibility(View.VISIBLE);
                editTexts[0] = "Неделя 1";
                break;
            case 2:
                weektext1.setVisibility(View.VISIBLE);
                weeknamefirs1.setVisibility(View.VISIBLE);
                weektext2.setVisibility(View.VISIBLE);
                weeknamefirs2.setVisibility(View.VISIBLE);
                editTexts[0] = "Неделя 1";
                editTexts[1] = "Неделя 2";
                break;
            case 3:
                weektext1.setVisibility(View.VISIBLE);
                weeknamefirs1.setVisibility(View.VISIBLE);
                weektext2.setVisibility(View.VISIBLE);
                weeknamefirs2.setVisibility(View.VISIBLE);
                weektext3.setVisibility(View.VISIBLE);
                weeknamefirs3.setVisibility(View.VISIBLE);
                editTexts[0] = "Неделя 1";
                editTexts[1] = "Неделя 2";
                editTexts[2] = "Неделя 3";
                break;
            case 4:
                weektext1.setVisibility(View.VISIBLE);
                weeknamefirs1.setVisibility(View.VISIBLE);
                weektext2.setVisibility(View.VISIBLE);
                weeknamefirs2.setVisibility(View.VISIBLE);
                weektext3.setVisibility(View.VISIBLE);
                weeknamefirs3.setVisibility(View.VISIBLE);
                weektext4.setVisibility(View.VISIBLE);
                weeknamefirs4.setVisibility(View.VISIBLE);
                editTexts[0] = "Неделя 1";
                editTexts[1] = "Неделя 2";
                editTexts[2] = "Неделя 3";
                editTexts[3] = "Неделя 4";
                break;
            default:
                break;
        }
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (editTexts[0].isEmpty() && editTexts[1].isEmpty() && editTexts[2].isEmpty() && editTexts[3].isEmpty()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(WeekNamesActivity.this)
                                .setMessage("Выберите хотя бы 1 неделю и введите её название")
                                .setPositiveButton("Ок", null)
                                .create();
                        alertDialog.show();
                    }else {
                        if (!weeknamefirs1.getText().toString().isEmpty()) {
                            editTexts[0] = weeknamefirs1.getText().toString();
                        }
                        if (!weeknamefirs2.getText().toString().isEmpty()) {
                            editTexts[1] = weeknamefirs2.getText().toString();
                        }
                        if (!weeknamefirs3.getText().toString().isEmpty()) {
                            editTexts[2] = weeknamefirs3.getText().toString();
                        }
                        if (!weeknamefirs4.getText().toString().isEmpty()) {
                            editTexts[3] = weeknamefirs4.getText().toString();
                        }
                        Intent intent = new Intent(WeekNamesActivity.this, FirstSettingsActivity.class);
                        intent.putExtra("week", editTexts);
                        startActivity(intent);
                        finish();
                    }

                }

        });

    }
}