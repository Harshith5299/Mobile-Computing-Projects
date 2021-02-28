package com.example.covid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper mydb;

    private static final int HRATE = 101;
    private static final int RESPRATE = 102;
    private static final int LOCATION = 103;

    private static final String DB_PATH = "/data/data/" + BuildConfig.APPLICATION_ID +
            "/databases/" + DatabaseHelper.DATABASE_NAME;
    private static final String PRIVATE_SERVER_UPLOAD_URL = "http://192.168.0.10/upload_file.php";
    private static final String CHARSET = "UTF-8";

    private float heartRate = 0.0f;
    private float respiratoryRate = 0.0f;
    private float longitude = 0.0f;
    private float latitude = 0.0f;
    private TextView hratetxtview;
    private TextView respratetxtview;
    private TextView locationtxtview;

    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mydb = new DatabaseHelper(this);

        Date date = new Date();
        timestamp = date.getTime();
        if (!mydb.insertNewRow(timestamp)) {
            Toast.makeText(this, "New Row Creation Failed", Toast.LENGTH_SHORT).show();
        }

        hratetxtview = (TextView)findViewById(R.id.hearRateText);
        respratetxtview = (TextView)findViewById(R.id.respiratoryRateText);
        locationtxtview = (TextView)findViewById(R.id.locationText);

        showhrate();
        showresprate();
        showlocation();
    }

    public void checkhrate(View view) {
        Intent intent = new Intent(MainActivity.this, CheckHeartRate.class);
        startActivityForResult(intent, HRATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == HRATE) {
            heartRate = ((float)data.getIntExtra(Constants.HEART_RATE, 70) *4) / 3;
            showhrate();
        }

        if (resultCode == RESULT_OK && requestCode == RESPRATE) {
            respiratoryRate = ((float)data.getIntExtra(Constants.RESPIRATORY_RATE, 20) * 4) / 3;
            showresprate();
        }

        if (resultCode == RESULT_OK && requestCode == LOCATION) {
            longitude = (float)data.getDoubleExtra(Constants.LONGITUDE, 0.0);
            latitude = (float)data.getDoubleExtra(Constants.LATITUDE, 0.0);
            showlocation();
        }
    }

    public void checkresprate(View view) {
        Intent intent = new Intent(MainActivity.this, CheckResprate.class);
        //myDB.deleteAllRows();
        startActivityForResult(intent, RESPRATE);
    }

    public void uploadsign(View view) {
        if(mydb.updateRowMain(timestamp, heartRate, respiratoryRate, longitude, latitude)) {
            Toast.makeText(this, "Signs and Location Upload Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Signs and Location Upload Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void checklocation(View view) {
        Intent intent = new Intent(MainActivity.this, LocationActivity.class);
        startActivityForResult(intent, LOCATION);
    }

    public void checksymptoms(View view) {
        Intent intent = new Intent(this, SymptomsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.TIMESTAMP, timestamp);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void dbserverupload(View view) {
        new Thread(() -> {
            try  {
                File uploadFile = new File(DB_PATH);

                ServerUtility multipart = new ServerUtility(PRIVATE_SERVER_UPLOAD_URL, CHARSET);
                multipart.addFile("fileUpload", uploadFile);

                List<String> response = multipart.finishRequest();
                for (String line : response) {
                    if (!line.equals("DB Uploaded Successfully to the Server")) {
                        throw new IOException(line);
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, line, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showhrate() {
        hratetxtview.setText(String.format(
                Locale.US, "Heart Rate: %.2f", heartRate));
    }

    private void showresprate() {
        respratetxtview.setText(String.format(
                Locale.US, "Respiratory Rate: %.2f", respiratoryRate));
    }

    private void showlocation() {
        locationtxtview.setText(String.format(
                Locale.US, "Longitude: %.4f \n Latitude: %.4f", longitude, latitude));
    }
}