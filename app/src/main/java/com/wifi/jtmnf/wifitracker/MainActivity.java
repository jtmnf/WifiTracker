package com.wifi.jtmnf.wifitracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    /* Wifi */
    private WifiManager wifiManager;

    /* CheckBoxes */
    private CheckBox time;
    private CheckBox date;
    private CheckBox shortScan;

    /* Texts */
    private EditText fileName;
    private EditText local;
    private TextView lastScan;

    /* Button */
    private Button saveFile;

    /* Variables */
    String textFile;
    String textTime;
    String textDate;
    String textScan = "";

    String lastFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        time =      (CheckBox)findViewById(R.id.time);
        date =      (CheckBox)findViewById(R.id.date);
        shortScan = (CheckBox)findViewById(R.id.shortScan);

        fileName =  (EditText)findViewById(R.id.fileName);
        local =     (EditText)findViewById(R.id.local);
        lastScan =  (TextView)findViewById(R.id.lastScan);

        saveFile =  (Button)findViewById(R.id.scanButton);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        saveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFile.setEnabled(false);
                textScan = "sep=,\nTIME,DATE,BSSID,SSID,CAPABILITIES,FREQUENCY,LEVEL,LOCAL\n";

                wifiManager.startScan();

                /* LOCAL */
                if(local.getText().length() == 0){
                    Toast.makeText(getApplicationContext(), "Location needed!", Toast.LENGTH_LONG).show();

                    saveFile.setEnabled(true);
                    return ;
                }

                lastScan.setText("Local: \t\t\t" + local.getText().toString() + "\n");

                /* FILENAME */
                if(fileName.getText().length() == 0) {
                   textFile = UUID.randomUUID().toString().substring(0, 13);
                }
                else{
                    if(fileName.getText().toString().equals(lastFile)){
                        lastScan.setText("");
                        Toast.makeText(getApplicationContext(), "Change File Name!", Toast.LENGTH_LONG).show();

                        saveFile.setEnabled(true);
                        return ;
                    }

                    lastFile = fileName.getText().toString();
                    textFile = fileName.getText().toString();
                }

                lastScan.setText(lastScan.getText() + "FileName: \t\t" + textFile.toString() + "\n");

                /* TIME */
                if(!time.isChecked()){
                    textTime = "0";
                    lastScan.setText(lastScan.getText() + "Time: \t\t\t\tUnrecorded" + "\n");
                }
                else{
                    textTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
                    lastScan.setText(lastScan.getText() + "Time: \t\t\t\t" + textTime.toString() + "\n");
                }

                /* DATE */
                if(!date.isChecked()){
                    textDate = "0";
                    lastScan.setText(lastScan.getText() + "Date: \t\t\t\tUnrecorded" + "\n");
                }
                else{
                    textDate = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
                    lastScan.setText(lastScan.getText() + "Date: \t\t\t\t" + textDate.toString() + "\n");
                }

                /* SCAN */
                if(!shortScan.isChecked()){
                    String aux = "NULL";
                    List<ScanResult> results = wifiManager.getScanResults();

                    while(results == null){
                        wifiManager.startScan();
                        results = wifiManager.getScanResults();
                    }


                    for(ScanResult s : results){
                        textScan += textTime + "," + textDate + "," + s.BSSID + "," + s.SSID + "," + s.capabilities + "," + s.frequency + "," + s.level + "," + local.getText().toString();
                        textScan += "\n";
                        aux = s.SSID;
                    }

                    lastScan.setText(lastScan.getText() + "Scan: \t\t\t\tFULL (SSID: "+ aux +")" + "\n");
                }
                else{
                    String aux = "NULL";
                    List<ScanResult> results = wifiManager.getScanResults();

                    while(results == null){
                        wifiManager.startScan();
                        results = wifiManager.getScanResults();
                    }

                    for(ScanResult s : results){
                        textScan += textTime + "," + textDate + "," + s.BSSID + "," + "-" + "," + "-" +"," + s.frequency + "," + s.level + "," + local.getText().toString();
                        textScan += "\n";
                        aux = s.SSID;
                    }

                    lastScan.setText(lastScan.getText() + "Scan: \t\t\t\tSMALL (SSID: "+ aux +")" + "\n");
                }

                lastScan.setText(lastScan.getText() + "Path: \t\t\t\t" + Environment.getExternalStorageDirectory() + "/WifiTracker");

                saveText(textFile.toString(), textScan.toString());

                Toast.makeText(getApplicationContext(), "Completed", Toast.LENGTH_LONG).show();
                saveFile.setEnabled(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void saveText(String fileName, String toSave) {
        try {
            File myFile = new File(Environment.getExternalStorageDirectory() + "/WifiTracker", fileName + ".csv");
            if (!myFile.exists()) {
                myFile.createNewFile();
            }

            FileOutputStream fos;
            byte[] data = toSave.getBytes();
            try {
                fos = new FileOutputStream(myFile);
                fos.write(data);
                fos.flush();
                fos.close();

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("e: " + e);
        }
    }
}
