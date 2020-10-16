package com.realdos.screenbehind;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.Display;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class PipActivity extends AppCompatActivity {
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    AppCompatActivity thisActivity = null;

    public static int WidthInPercent = 30;
    public static int HeightInPercent = 30;

    public static boolean InPipMode = false;
    public static boolean EnableFlashLight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        thisActivity = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
            if (!InPipMode)
                StartPipMode();
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
                preview.setSurfaceProvider(((PreviewView) findViewById(R.id.viewFinder)).createSurfaceProvider());

                // Select back camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();

                    // Bind use cases to camera
                    Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)thisActivity, cameraSelector, preview);

                    camera.getCameraControl().enableTorch(EnableFlashLight);

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

    void StartPipMode()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        int x = (width * WidthInPercent / 100);
        int y = (height * HeightInPercent / 100);

        x = x - (x % 8);
        y = y - (y % 8);

        float ratio = x / y;

        PictureInPictureParams.Builder pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
        Rational aspectRatio = new Rational(WidthInPercent, HeightInPercent);
        pictureInPictureParamsBuilder.setAspectRatio(aspectRatio);

        enterPictureInPictureMode(pictureInPictureParamsBuilder.build());

        InPipMode = true;
    }
}