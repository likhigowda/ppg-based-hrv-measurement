package com.example.projectplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    
    FloatingActionButton help;
    private static final String TAG = "error";
    TextView amplitude, seconds, activity;
    int[] pixels;
    double result=0;
    SurfaceView surfaceView;
    Camera mCamera;
    userDefinedFunctions functions = new userDefinedFunctions();
    LineGraphSeries<DataPoint> series;
    int pointsPlotted = 0;
    int frameRate;
    List<Double> redData = new ArrayList<Double>();
    int minRedValue = 180;
    int minDuration;
    int maxDuration;

    public static final String key_name = "com.example.projectplan.screen2";
    public static final String key_name2 = "com.example.projectplan.screen2b";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        
        help = findViewById(R.id.floatingActionButton);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.stopPreview();
                Intent intent = new Intent(MainActivity2.this,MainActivity3.class);
                startActivity(intent);
            }
        });

        amplitude = findViewById(R.id.textView12);
        seconds = findViewById(R.id.textView10);
        surfaceView = findViewById(R.id.surfaceView);
        activity = findViewById(R.id.textView3);


        // graph view
        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 0)});
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(pointsPlotted-1100);
        graph.getViewport().setMaxX(pointsPlotted);
        graph.setTitle("Real time graph");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Amplitude");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time(ms)");
        graph.getGridLabelRenderer().setTextSize(14);
        graph.getGridLabelRenderer().setPadding(30);

        // getting camera instance
        mCamera = Camera.open();
        Camera.Parameters cameraParam = mCamera.getParameters();
        cameraParam.setPreviewFormat(ImageFormat.NV21);
        cameraParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        frameRate = cameraParam.getPreviewFrameRate();
        minDuration = frameRate * 15;    // 30*15 -> 15 seconds
        maxDuration = frameRate * 180;   // 30*180 -> 3 min

        cameraParam.setPreviewFrameRate(frameRate);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(cameraParam);
        surfaceView.setKeepScreenOn(true);

        int frameHeight = mCamera.getParameters().getPreviewSize().height;
        int frameWidth = mCamera.getParameters().getPreviewSize().width;

        pixels = new int[frameWidth * frameHeight];

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    Log.d(TAG, "Error setting camera preview: " + e.getMessage());
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                mCamera.stopPreview();
                mCamera.release();
            }
        });

        // to perform operation on each frame
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                int frameHeight = camera.getParameters().getPreviewSize().height;
                int frameWidth = camera.getParameters().getPreviewSize().width;

                functions.decodeYUV420SP(pixels, data, frameWidth, frameHeight);

                double avg = 0;
                int red = 0;
                for (int i=0 ; i<pixels.length ; i++) {
                    red = Color.red(pixels[i]);
                    avg = avg + red;
                }
                result = avg/pixels.length;
                amplitude.setText(String.valueOf(result));
                series.appendData(new DataPoint(pointsPlotted*((1.0/frameRate)*1000),result),true,pointsPlotted);
                pointsPlotted++;

                if(result > minRedValue) {
                    redData.add(result);
                    if(redData.size() == maxDuration) {
                        // when reached 3 min
                        mCamera.stopPreview();
                        Intent intent = new Intent(MainActivity2.this,MainActivity4.class);
                        intent.putExtra(key_name,functions.getArray(redData));
                        intent.putExtra(key_name2,frameRate);
                        startActivity(intent);
                    }
                }
                else {
                    if(!redData.isEmpty()) {
                        if(redData.size() >= minDuration) {
                            // min 15 sec available
                            mCamera.stopPreview();
                            Intent intent = new Intent(MainActivity2.this,MainActivity4.class);
                            intent.putExtra(key_name,functions.getArray(redData));
                            intent.putExtra(key_name2,frameRate);
                            startActivity(intent);
                        }
                        else {
                            redData.clear();
                            Toast.makeText(MainActivity2.this, "Minimum 15 sec reading is required", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                int duration = redData.size() /frameRate;
                seconds.setText(duration + " sec");
                if(duration == 0) {
                    activity.setText("Unable to detect pulse...");
                }
                else {
                    activity.setText("Reading...");
                }

            }
        });

    }


}