package com.example.DiaShield;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
public class MainActivity extends AppCompatActivity {

    private Dbhelp myDB;

    private static final int Hrate_code = 101;
    private static final int Rrate_CODE = 102;
    private float hRate = 0.0f;
    private float rRate = 0.0f;
    private TextView hrateView;
    private TextView rrateView;

    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDB = new Dbhelp(this);

        Date date = new Date();
        timestamp = date.getTime();
        if (!myDB.insertNewRow(timestamp)) {
            Toast.makeText(this, "New Row Creation Failed", Toast.LENGTH_SHORT).show();
        }

        hrateView = (TextView)findViewById(R.id.hearRateText);
        rrateView = (TextView)findViewById(R.id.respiratoryRateText);

        DisplayHeartRate();
        DisplayRespiratoryRate();
    }

    public void measurehrate(View view) {
        Intent intent = new Intent(MainActivity.this, Hrate.class);
        startActivityForResult(intent, Hrate_code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == Hrate_code) {
            hRate = ((float)data.getIntExtra(Constants.H_RATE, 70) *4) / 3;
            hRate = hRate /2;
            DisplayHeartRate();
        }

        if (resultCode == RESULT_OK && requestCode == Rrate_CODE) {
            rRate = ((float)data.getIntExtra(Constants.R_RATE, 20) * 4) / 3;

            DisplayRespiratoryRate();
        }
    }

    public void measureR_Rate(View view) {
        Intent intent = new Intent(MainActivity.this, Respiratory.class);
        //myDB.deleteAllRows();
        startActivityForResult(intent, Rrate_CODE);
    }

    public void upload(View view) {
        if(myDB.updateRowMain(timestamp, hRate, rRate)) {
            Toast.makeText(this, "Signs Upload Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Signs Upload Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void openSymptoms(View view) {
        Intent intent = new Intent(this, SymptomsPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.TIMESTAMP, timestamp);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void DisplayHeartRate() {
        hrateView.setText(String.format(
                Locale.US, "Heart Rate: %.2f", hRate));
    }

    private void DisplayRespiratoryRate() {
        rrateView.setText(String.format(
                Locale.US, "Respiratory Rate: %.2f", rRate));
    }
}