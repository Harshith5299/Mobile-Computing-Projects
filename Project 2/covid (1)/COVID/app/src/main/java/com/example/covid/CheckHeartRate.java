package com.example.covid;

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

public class CheckHeartRate extends AppCompatActivity {
    private static final int MOVING_AVERAGE_PERIOD = 10;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private TextView progtxtview;
    private Button startcalcbutton;
    private PreviewView preview;
    private CountDownTimer timer;
    private ProcessCameraProvider processCamProvider;
    private CameraSelector camSelect;
    private MovingAverage movingAvg;
    private boolean calculate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_heart_rate);

        startcalcbutton = findViewById(R.id.startCalculation);
        preview = findViewById(R.id.preview);
        progtxtview = (TextView)findViewById(R.id.progressText);
        displayProgress(0);

        timer = new CountDownTimer(45000, 1000) {
            int ticker = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                ticker += 1;
                displayProgress(((int) ticker * 100 * 1000) / 45000);
            }

            @Override
            public void onFinish() {
                displayProgress(100);

                int heartRate = movingAvg.getPeakCount();

                Intent returnHeartRate = new Intent();
                returnHeartRate.putExtra(Constants.HEART_RATE, heartRate);
                setResult(Activity.RESULT_OK, returnHeartRate);
                finish();
            }
        };


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS);
        }

    }

    public void startCalculation(View view) {
        if (calculate) {
            return;
        }
        calculate = true;
        startcalcbutton.setText("Measuring, please wait");
        timer.start();
        movingAvg = new MovingAverage(MOVING_AVERAGE_PERIOD);
        ImageAnalysis imageAnalysis = startImageAnalysis();
        camSelect = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        processCamProvider.bindToLifecycle((LifecycleOwner) CheckHeartRate.this, camSelect,
                imageAnalysis);
    }

    public ImageAnalysis startImageAnalysis() {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), image -> {
            if(calculate) {
                Bitmap bitmap = preview.getBitmap();
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
                movingAvg.addData(((double) sum) / lengthOfPixels);
                image.close();
            }
        });
        return imageAnalysis;
    }

    public void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {

                processCamProvider = cameraProviderFuture.get();
                bindPreview(processCamProvider);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        camSelect = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(camSelect)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(camSelect);
        }

        preview.setSurfaceProvider(this.preview.createSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, camSelect, preview);
        CameraControl cameraControl = camera.getCameraControl();
        cameraControl.enableTorch(true);
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permission not given by user", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private void displayProgress(int curProgress) {
        progtxtview.setText(String.format(
                Locale.US, "Completion Progress : %s%%", curProgress));
    }
}