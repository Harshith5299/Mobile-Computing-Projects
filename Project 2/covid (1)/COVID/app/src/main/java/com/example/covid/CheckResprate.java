package com.example.covid;

import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

public class CheckResprate extends AppCompatActivity {
    private static final int MOVING_AVG = 30;

    private SensorManager managesensor;
    private Sensor accelerometer;
    private CountDownTimer timer;
    private TextView progTxtView;
    private MovingAverage movingAvg;
    private boolean measure;
    private SensorEventListener accelerometerListen;
    private Button CalcButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_respiratory_rate);

        CalcButton = findViewById(R.id.startCalculation);
        progTxtView = (TextView)findViewById(R.id.progressText);
        displayProgress(0);

        managesensor = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = managesensor.getDefaultSensor(Sensor.TYPE_GRAVITY);

        timer = new CountDownTimer(45000, 1000) {
            int ticker = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                ticker += 1;
                displayProgress(((int) ticker * 100 * 1000) / 45000);
            }

            @Override
            public void onFinish() {
                managesensor.unregisterListener(accelerometerListen);
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(Constants.VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(Constants.VIBRATION_DURATION);
                }
                displayProgress(100);
                Intent respiratoryRateData = new Intent();
                respiratoryRateData.putExtra(Constants.RESPIRATORY_RATE, movingAvg.getPeakCount());
                setResult(Activity.RESULT_OK, respiratoryRateData);
                finish();
            }
        };

        accelerometerListen = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int acc) {
                // No need to implement.
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                double z = event.values[2];
                movingAvg.addData(z);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        managesensor.unregisterListener(accelerometerListen);
    }

    public void startCalculation(View view) {
        if (measure) {
            return;
        }
        measure = true;
        CalcButton.setText("Measuring, please wait");
        timer.start();
        movingAvg = new MovingAverage(MOVING_AVG);
        managesensor.registerListener(accelerometerListen, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void displayProgress(int curProgress) {
        progTxtView.setText(String.format(
                Locale.US, "Completion Progress : %s%%", curProgress));
    }
}
