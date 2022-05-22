package com.example.projectplan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Timer;

public class MainActivity4 extends AppCompatActivity {

    int resultPlotted = 0;
    double[] reqData;
    int frameRate;
    TextView result;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        result = findViewById(R.id.textView4);

        Intent secondIntent = getIntent();
        reqData = secondIntent.getDoubleArrayExtra(MainActivity2.key_name);
        frameRate = secondIntent.getIntExtra(MainActivity2.key_name2,30);

        // to plot graph (Graph view)
        GraphView graph = (GraphView) findViewById(R.id.graph2);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 0),
        });
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(resultPlotted-1000);
        graph.getViewport().setMaxX(resultPlotted);
        graph.setTitle("Real time graph");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Amplitude");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time(ms)");
        graph.getGridLabelRenderer().setTextSize(18);
        graph.getViewport().setScrollable(true);

        for(resultPlotted=0 ; resultPlotted<reqData.length ; resultPlotted++) {
            series.appendData(new DataPoint(resultPlotted*((1.0/frameRate)*1000),reqData[resultPlotted]),true,resultPlotted);
        }

        pd = new ProgressDialog(MainActivity4.this);
        pd.setTitle("Processing....");
        pd.setMessage("Please wait it will take some time.");
        pd.setCancelable(false);
        pd.show();

        // to start python
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // to create python instance (to read .py file)
        Python py = Python.getInstance();

        Thread mThread = new Thread() {
            @Override
            public void run() {
                // to create python obj (to read myfile.py)
                PyObject pyobj = py.getModule("myfile");

                // to read which function
                PyObject obj = pyobj.callAttr("main",reqData,frameRate);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Stuff that updates the UI
                        // instructions.setText(result.toString());
                        result.setText(obj.toString());
                        pd.dismiss();

                    }
                });
            }
        };
        mThread.start();

    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }
}