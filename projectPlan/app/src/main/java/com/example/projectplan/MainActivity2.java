package com.example.projectplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "error";
    userDefinedFunctions functions = new userDefinedFunctions();
    TextView wishes, username, tap, activity, save, hrView, hrvView, ignore;
    EditText label;
    AlertDialog userDialog, helpDialog, resultDialog;
    ImageButton help;
    SurfaceView surfaceView;
    GraphView graph;
    Camera mCamera;
    int frameRate, minDuration, maxDuration;
    int[] pixels;
    CheckBox check;
    boolean autoStop = true;
    ProgressBar progressBar;
    LineGraphSeries<DataPoint> series;
    int pointsPlotted = 0;
    ProgressDialog pd;
    Button history;
    String dateTime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String hrOnly, hrvOnly;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        wishes = findViewById(R.id.textView2);
        username = findViewById(R.id.textView11);
        help = findViewById(R.id.imageButton2);
        tap = findViewById(R.id.textView6);
        surfaceView = findViewById(R.id.surfaceView);
        activity = findViewById(R.id.textView3);
        graph = findViewById(R.id.graph);
        check = findViewById(R.id.checkBox2);
        progressBar = findViewById(R.id.determinateBar);
        history = findViewById(R.id.button);


        wishes.setText(functions.getWishes());

        //region To get user name
        Intent secondIntent = getIntent();
        if (secondIntent.getStringExtra(MainActivity.key_useName).equals("first_run")) {
            getUserName();
            open_min_components();
        }

        SharedPreferences getShared = getSharedPreferences("obtained_name",MODE_PRIVATE);
        String value = getShared.getString("str","");
        username.setText(value);

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserName();
                open_min_components();
            }
        });
        //endregion

        //region To open help dialog
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_help_dialog();
                open_min_components();
            }
        });
        //endregion

        //region tap here to start
        tap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_max_components();
            }
        });
        //endregion

        //region autoStop check box
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check.isChecked()) {
                    autoStop = true;
                }
                else {
                    autoStop = false;
                }
            }
        });
        //endregion

        //region history button
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
                startActivity(intent);
            }
        });
        //endregion

        series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 0)});
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.NONE );
        series.setColor(Color.rgb(214, 78, 82));
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(pointsPlotted-2000);
        graph.getViewport().setMaxX(pointsPlotted);
        graph.addSeries(series);



    }

    //region open_min_components() and open_max_components() method
    private void open_min_components() {
        tap.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.INVISIBLE);
        activity.setVisibility(View.INVISIBLE);
        graph.setVisibility(View.INVISIBLE);

    }

    private void open_max_components() {
        get_camera();
        tap.setVisibility(View.INVISIBLE);
        surfaceView.setVisibility(View.VISIBLE);
        activity.setVisibility(View.VISIBLE);
        graph.setVisibility(View.VISIBLE);

    }
    //endregion

    //region open_help_dialog() method
    private void open_help_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.help,null);
        TextView help_ok = view.findViewById(R.id.helpok);
        builder.setCancelable(false);
        builder.setView(view);
        helpDialog = builder.create();
        helpDialog.show();

        help_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog.dismiss();
            }
        });
    }
    //endregion

    //region get_camera() method
    private void get_camera() {

        List<Double> redData = new ArrayList<Double>();
        
        mCamera = Camera.open();
        Camera.Parameters cameraParam = mCamera.getParameters();
        cameraParam.setPreviewFormat(ImageFormat.NV21);
        cameraParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        frameRate = cameraParam.getPreviewFrameRate();
        progressBar.setMax(frameRate * 15);
        minDuration = frameRate * 15;    // 30*15 -> 15 seconds
        maxDuration = frameRate * 120;   // 30*120 -> 2 min

        cameraParam.setPreviewFrameRate(frameRate);
        cameraParam.setPreviewSize(640,480);
//        cameraParam.setZoom((cameraParam.getMaxZoom()));

        mCamera.setParameters(cameraParam);
        surfaceView.setKeepScreenOn(true);

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
                progressBar.setProgress(0);
                cameraParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(cameraParam);
                open_min_components();
            }
        });

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                int frameHeight = camera.getParameters().getPreviewSize().height;
                int frameWidth = camera.getParameters().getPreviewSize().width;

                pixels = new int[frameWidth * frameHeight];

                functions.decodeYUV420SP(pixels, data, frameWidth, frameHeight);

                double redTotal = 0 , greenTotal = 0 , blueTotal = 0;
                int red = 0 , green = 0 , blue = 0;
                for (int i=0 ; i< pixels.length ; i++) {
                    red = Color.red(pixels[i]);
                    green = Color.green(pixels[i]);
                    blue = Color.blue(pixels[i]);

                    redTotal = redTotal + red;
                    greenTotal = greenTotal + green;
                    blueTotal = blueTotal + blue;
                }
                double redAvg = redTotal / pixels.length;
                double greenAvg = greenTotal / pixels.length;
                double blueAvg = blueTotal / pixels.length;

                double hue = functions.getHue(redAvg, greenAvg, blueAvg);

                series.appendData(new DataPoint(pointsPlotted*((1.0/frameRate)*1000),redAvg),true,pointsPlotted);
                pointsPlotted++;


                if (hue <= 20 || hue >= 346) {
                    activity.setText("Detecting...");
                    redData.add(redAvg);
                    progressBar.incrementProgressBy(1);
                    
                    if (redData.size() == maxDuration) {
                        mCamera.stopPreview();
                        processData(redData, frameRate);
                        redData.clear();
                        open_min_components();
                    }
                    if (redData.size() == minDuration && autoStop == true) {
                        mCamera.stopPreview();
                        processData(redData, frameRate);
                        redData.clear();
                        open_min_components();

                    }
                    
                }
                else {
                    if (redData.size() >= minDuration) {
                        mCamera.stopPreview();
                        processData(redData, frameRate);
                        redData.clear();
                        open_min_components();
                    }
                    else {
                        if (!redData.isEmpty()) {
                            redData.clear();
                            Toast.makeText(MainActivity2.this, "Minimum 15 sec reading is required", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    activity.setText("Unable to detect pulse...");
                    progressBar.setProgress(0);


                }


            }
        });
    }
    
    //endregion

    private void processData(List<Double> redData, int frameRate) {
        double[] reqData = functions.getArray(redData);
        int duration = redData.size() / frameRate;

        pd = new ProgressDialog(this);
        pd.setTitle("Processing....");
        pd.setMessage("Please wait it will take some time.");
        pd.setCancelable(false);
        pd.show();

        Thread mThread = new Thread() {
            @Override
            public void run() {
                // to start python
                if (! Python.isStarted()) {
                    Python.start(new AndroidPlatform(MainActivity2.this));
                }

                // to create python instance (to read .py file)
                Python py = Python.getInstance();


                // to create python obj (to read myfile.py)
                PyObject pyobj = py.getModule("myfile");

                // to read which function
                PyObject obj = pyobj.callAttr("main",reqData,frameRate,duration);

                runOnUiThread(new Runnable() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {

                        // Stuff that updates the UI
                        // instructions.setText(result.toString());
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                        builder.setCancelable(false);
                        View view = getLayoutInflater().inflate(R.layout.result,null);
                        save = view.findViewById(R.id.textView5);
                        hrView = view.findViewById(R.id.textView9);
                        hrvView = view.findViewById(R.id.textView10);
                        label = view.findViewById(R.id.editTextTextPersonName2);
                        ignore = view.findViewById(R.id.textView13);
                        TextView vlf = view.findViewById(R.id.textView16);
                        TextView lf = view.findViewById(R.id.textView17);
                        TextView hf = view.findViewById(R.id.textView15);
                        TextView ratio = view.findViewById(R.id.textView14);


                        GraphView graph2 = view.findViewById(R.id.graph2);
                        int resultPlotted = 0;
                        graph2.getGridLabelRenderer().setVerticalLabelsVisible(false);
                        graph2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
                        graph2.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.NONE );
                        graph2.getViewport().setXAxisBoundsManual(true);
                        graph2.getViewport().setMinX(resultPlotted-2000);
                        graph2.getViewport().setMaxX(resultPlotted);
                        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(new DataPoint[] {
                                new DataPoint(0, 0),
                        });
                        series2.setColor(Color.rgb(214, 78, 82));
                        graph2.addSeries(series2);
                        graph2.getViewport().setScrollable(true);


                        for(resultPlotted=0 ; resultPlotted<reqData.length ; resultPlotted++) {
                            series2.appendData(new DataPoint(resultPlotted*((1.0/frameRate)*1000),reqData[resultPlotted]),true,resultPlotted);
                        }


                        String[] obtained_result = obj.toString().split(" ");
                        hrView.setText("HR: " + obtained_result[0] + " bpm");
                        hrvView.setText("HRV: " + obtained_result[1] + " ms");
                        vlf.setText("VLF: " + obtained_result[2] + " Hz");
                        lf.setText("LF: " + obtained_result[3] + " Hz");
                        hf.setText("HF: " + obtained_result[4] + " Hz");
                        ratio.setText("LF/HF(abs power): " + obtained_result[5]);

                        hrOnly = obtained_result[0] + " bpm";
                        hrvOnly = obtained_result[1] + " ms";

                        calendar = Calendar.getInstance();
                        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy   HH:mm:ss");
                        dateTime = simpleDateFormat.format(calendar.getTime()).toString();

                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dbHelper dataBaseHelper = new dbHelper(MainActivity2.this);
                                boolean success = dataBaseHelper.addOne(MainActivity2.this);
                                Toast.makeText(MainActivity2.this,"Saved", Toast.LENGTH_SHORT).show();
                                resultDialog.dismiss();
                            }
                        });

                        ignore.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                resultDialog.dismiss();
                            }
                        });

                        builder.setView(view);
                        resultDialog = builder.create();
                        pd.dismiss();
                        resultDialog.show();


                    }
                });
            }
        };
        mThread.start();
        
        
        
    }

    //region getUserName() method
    private void getUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.username,null);
        EditText getName = view.findViewById(R.id.editTextTextPersonName);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = getName.getText().toString();

                SharedPreferences shared = getSharedPreferences("obtained_name",MODE_PRIVATE);
                SharedPreferences.Editor editor = shared.edit();

                editor.putString("str",name);
                editor.apply();
                username.setText(name);
                userDialog.dismiss();
            }
        });
        builder.setView(view);
        userDialog = builder.create();
        userDialog.show();
    }
    //endregion
}