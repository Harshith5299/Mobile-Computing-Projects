package com.example.DiaShield;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Hrate extends AppCompatActivity {
    private static final int WINDOW = 10;
    private static final int REQ_PERMISSION = 1001;
    private static final String[] REQ_PERMISSION2 = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private TextView progTView;
    private Button Calculation;
    private PreviewView prevView;
    private CountDownTimer Timer;
    private ProcessCameraProvider processCam;
    private CameraSelector camSelec;
    private MovingAverage movingAverage;
    private boolean measure;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_heart_rate);

        Calculation = findViewById(R.id.startCalculation);
        prevView = findViewById(R.id.preview);
        progTView = (TextView)findViewById(R.id.progressText);
        displayProgress(0);

        Timer = new CountDownTimer(45000, 1000) {
            int ticker = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                ticker += 1;
                displayProgress(((int) ticker * 100 * 1000) / 45000);
            }

            @Override
            public void onFinish() {
                displayProgress(100);

                int heartRate = movingAverage.getNumPeaks();

                Intent returnHeartRate = new Intent();
                returnHeartRate.putExtra(Constants.H_RATE, heartRate);
                setResult(Activity.RESULT_OK, returnHeartRate);
                finish();
            }
        };


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQ_PERMISSION2,
                    REQ_PERMISSION);
        }

    }

    public void startCalculation(View view) {
        if (measure) {
            return;
        }
        measure = true;
        Calculation.setText("Measuring...");
        Timer.start();
        movingAverage = new MovingAverage(WINDOW);
        ImageAnalysis imageAnalysis = startImageAnalysis();
        camSelec = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        processCam.bindToLifecycle((LifecycleOwner) Hrate.this, camSelec,
                imageAnalysis);
    }

    public ImageAnalysis startImageAnalysis() {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), image -> {
            if(measure) {
                Bitmap bitmap = prevView.getBitmap();
                if(bitmap == null){
                    return;
                }
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int lengthOfPixels = width * height;
                int[] pixels = new int[lengthOfPixels];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                int sum = 0;
                for (int i = 0; i < lengthOfPixels; i++) {
                    sum += (pixels[i] >> 16) & 0xff;
                }
                movingAverage.addData(((double) sum) / lengthOfPixels);
                image.close();
            }
        });
        return imageAnalysis;
    }

    public void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {

                processCam = cameraProviderFuture.get();
                bindPreview(processCam);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        camSelec = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(camSelec)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(camSelec);
        }

        preview.setSurfaceProvider(prevView.createSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, camSelec, preview);
        CameraControl cameraControl = camera.getCameraControl();
        cameraControl.enableTorch(true);
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQ_PERMISSION2) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_PERMISSION) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private void displayProgress(int curProgress) {
        progTView.setText(String.format(
                Locale.US, "%s%%", curProgress));
    }
}