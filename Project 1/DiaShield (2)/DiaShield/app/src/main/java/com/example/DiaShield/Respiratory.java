package com.example.DiaShield;

import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;

import java.util.Locale;

public class Respiratory extends AppCompatActivity {
    private static final int MOVING_AVG = 30;

    private SensorManager sensor;
    private Sensor accelerometer;
    private CountDownTimer timer;
    private TextView pTextView;
    private MovingAverage movingAverage;
    private boolean measure;
    private SensorEventListener accelerometerListener;
    private Button startButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_respiratory_rate);

        startButton = findViewById(R.id.startCalculation);
        pTextView = (TextView)findViewById(R.id.progressText);
        Prog(0);

        sensor = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensor.getDefaultSensor(Sensor.TYPE_GRAVITY);

        timer = new CountDownTimer(45000, 1000) {
            int ticker = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                ticker += 1;
                Prog(((int) ticker * 100 * 1000) / 45000);
            }

            @Override
            public void onFinish() {
                sensor.unregisterListener(accelerometerListener);

                Prog(100);
                Intent respiratoryRateData = new Intent();
                respiratoryRateData.putExtra(Constants.R_RATE,movingAverage.getNumPeaks());
                setResult(Activity.RESULT_OK, respiratoryRateData);
                finish();
            }
        };

        accelerometerListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int acc) {
                // No need to implement.
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                double z = event.values[2];
                movingAverage.addData(z);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        sensor.unregisterListener(accelerometerListener);
    }

    public void calcrate(View view) {
        if (measure) {
            return;
        }
        measure = true;
        startButton.setText("loading");
        timer.start();
        movingAverage = new MovingAverage(MOVING_AVG);
        sensor.registerListener(accelerometerListener, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void Prog(int curProgress) {

        pTextView.setText(String.format(
                Locale.US, "loading : %s%%", curProgress));
    }
}
