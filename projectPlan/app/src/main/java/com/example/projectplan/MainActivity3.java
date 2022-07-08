package com.example.projectplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ajts.androidmads.library.SQLiteToExcel;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity3 extends AppCompatActivity {

    ListView myListView;
    Button clearData, export;
    AlertDialog delete, storagePath;
    dbHelper dataBaseHelper = new dbHelper(MainActivity3.this);
    private int STORAGE_PERMISSION_CODE = 102;
    TextView historyStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        myListView = findViewById(R.id.myListView);
        clearData = findViewById(R.id.button3);
        export = findViewById(R.id.button4);
        historyStatus = findViewById(R.id.textView12);



//        ArrayList<List<String>> result;
        List<String> result;
        result = dataBaseHelper.getEverything();
        Collections.reverse(result);

        if (result.isEmpty()) {
            clearData.setEnabled(false);
            export.setEnabled(false);
            historyStatus.setText("No History");
        }
        else {
            clearData.setEnabled(true);
            export.setEnabled(true);
            historyStatus.setText("History");
        }


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,result);
        myListView.setAdapter(arrayAdapter);

        clearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlertDialog(dataBaseHelper);
            }
        });

        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity3.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission();
                }
                else {
                    exportDB(dataBaseHelper);
                }
            }
        });

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

    }

    private void exportDB(dbHelper dbhelper) {
        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Backup/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        // Export SQLite DB as EXCEL FILE
        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(getApplicationContext(), dbhelper.getDatabaseName(), directory_path);
        sqliteToExcel.exportAllTables("data.xls", new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted(String filePath) {
                Toast.makeText(MainActivity3.this, "Successfully Exported", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity3.this);
                builder.setTitle("Storage Path");
                builder.setMessage(directory_path);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        storagePath.dismiss();
                    }
                });
                storagePath = builder.create();
                storagePath.show();
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }



    private void openAlertDialog(dbHelper dataBaseHelper) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?");
        builder.setMessage("Do you really want to delete these records?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataBaseHelper.deleteData();
                Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete.dismiss();
            }
        });

        delete = builder.create();
        delete.show();

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "PLease grant Storage permission", Toast.LENGTH_SHORT).show();
            }
            else {
                exportDB(dataBaseHelper);
            }
        }
    }

}