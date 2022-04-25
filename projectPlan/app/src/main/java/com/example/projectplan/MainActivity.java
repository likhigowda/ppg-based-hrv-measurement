package com.example.projectplan;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button start;
    int REQUEST_VIDEO_CAPTURE = 101;
    Context context;
    TextView instructions;
    int video_duration;
    int frameRate;
    int total_frames;
    int micro_per_frame;
    int i;
    Bitmap bmFrame;
    String imageString = "";
    Integer result = 0;
//    String result;


    Uri uri = Uri.parse("android.resource://com.example.projectplan/raw/sample");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.button);
        context = getApplicationContext();
        instructions = findViewById(R.id.textView);


        // invoking chaquopy
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // to create python instance (to read .py file)
        Python py = Python.getInstance();


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // frame, frame rate, duration -- MediaExtractor
                MediaExtractor mediaExtractor = new MediaExtractor();
                try {
                    mediaExtractor.setDataSource(getApplication(), uri,null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MediaFormat format = mediaExtractor.getTrackFormat(0);

                // duration
                video_duration = (int) format.getLong(MediaFormat.KEY_DURATION);

                // frame rate
                // frameRate = format.getInteger((MediaFormat.KEY_FRAME_RATE));
                frameRate = 25;   // setting constant frame rate

                // total frames
                total_frames = frameRate * (video_duration / 1000000);

                // microseconds per frames
                micro_per_frame = 1000000 / frameRate;


                // to get video frame at a particular duration (duration in microsecond) -- MediaMetadataRetriever
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(getApplication(), uri);

                // sending each frame to python via chaquopy to extract rgb values
                for(i=0 ; i<total_frames ; i++) {
                    bmFrame = mediaMetadataRetriever.getFrameAtTime(i * micro_per_frame); //unit in microsecond
                    imageString = getStringImage(bmFrame);

                    // passing encoded image to python script
                    // to create python obj (to read myfile.py)
                    PyObject pyobj = py.getModule("myfile");

                    // to read which function
                    PyObject obj = pyobj.callAttr("main",imageString);

                    result = result + Integer.parseInt(obj.toString());

                }

                instructions.setText(result.toString());

            }
        });

    }

    private String getStringImage(Bitmap bmFrame) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmFrame.compress(Bitmap.CompressFormat.PNG,100,baos);
        // store in byte array
        byte[] imageBytes = baos.toByteArray();
        // finally encode to string
        String encodeImage = android.util.Base64.encodeToString(imageBytes,Base64.DEFAULT);
        return encodeImage;
    }

}
