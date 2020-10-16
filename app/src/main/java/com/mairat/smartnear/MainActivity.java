package com.mairat.smartnear;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    SeekBar heightBar = null;
    SeekBar widthBar = null;

    Switch enableFlashLight = null;

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    AppCompatActivity thisActivity = null;
    PreviewView previewView = null;
    Camera camera = null;

    public void onFlashClickClick(View view)
    {
        camera.getCameraControl().enableTorch(enableFlashLight.isChecked());
    }

    class ChangeListener implements SeekBar.OnSeekBarChangeListener
    {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            // TODO Auto-generated method stub
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            float width = size.x;
            float height = size.y;

            int x = widthBar.getProgress();
            int y = heightBar.getProgress();

            float ratio = (float)x / (float)y;

            //ratioText.setText(String.valueOf(ratio));

            if (ratio >= 0.418410 && ratio <= 2.390000)
                ;//ratioText.setTextColor(Color.BLACK);
            else
                ;//ratioText.setTextColor(Color.RED);

            if (ratio >= 0.418410 && ratio <= 2.390000)
            {
                previewView.setScaleX((float)x / 100.f);
                previewView.setScaleY((float)y / 100.f);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        thisActivity = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        heightBar = (SeekBar)findViewById(R.id.heightInPercent);
        widthBar = (SeekBar)findViewById(R.id.widthInPercent);
        previewView = (PreviewView) findViewById(R.id.viewFinder);
        enableFlashLight = (Switch) findViewById(R.id.enableFlashLight);

        heightBar.setOnSeekBarChangeListener(new ChangeListener());
        widthBar.setOnSeekBarChangeListener(new ChangeListener());

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }
    }

    private boolean allPermissionsGranted()
    {
        return ContextCompat.checkSelfPermission(getBaseContext(),  Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {

        Runnable t = new Runnable() {
            public void run() {

                // Used to bind the lifecycle of cameras to the lifecycle owner
                ProcessCameraProvider cameraProvider = null;
                try {
                    cameraProvider = cameraProviderFuture.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Preview
                Preview.Builder previewBuilder = new Preview.Builder();
                Preview preview = previewBuilder.build();
                preview.setSurfaceProvider(previewView.createSurfaceProvider());

                // Select back camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();

                    // Bind use cases to camera
                    camera = cameraProvider.bindToLifecycle((LifecycleOwner)thisActivity, cameraSelector, preview);

                } catch (Exception exc) {
                    Log.e("ScreenBehind", "Use case binding failed", exc);
                }
            }
        };

        cameraProviderFuture.addListener(t, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == 10)
        {
            if (allPermissionsGranted())
            {
                startCamera();
            }
            else
            {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    public void onClick(View view)
    {
        PipActivity.HeightInPercent = (int)(previewView.getHeight() * previewView.getScaleY());
        PipActivity.WidthInPercent = (int)(previewView.getWidth() * previewView.getScaleX());
        PipActivity.EnableFlashLight = enableFlashLight.isChecked();

        Intent intent = new Intent(this, PipActivity.class);
        startActivity(intent);

        finish();
    }
}