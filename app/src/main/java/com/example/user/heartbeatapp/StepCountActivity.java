package com.example.user.heartbeatapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Environment;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StepCountActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private SensorManager sManger;
    private Sensor mSensorAccelerometter;
    private TextView tv_step;
    private Button btn_start;
    private int step = 0;//步數
    private double oriValue = 0;//原始值
    private double lstValue = 0;//上次的值
    private double curValue = 0;//當前值
    private boolean motivState = true;//是否處於運動狀態
    private boolean processState = false;//標記當前是否已經在計步

    private Button btnTxtRead;
    private Button btnTxtWrite;
    private Button btnFileDel;
    private EditText txtWrite;
    private TextView txtRead;
    String fileName = "dateRec.dat";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);
        sManger = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAccelerometter = sManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sManger.registerListener(this, mSensorAccelerometter, SensorManager.SENSOR_DELAY_UI);
        bindViews();

        txtWrite = (EditText) findViewById(R.id.tv_count);
        txtRead = (TextView) findViewById(R.id.tv_countview);

        btnTxtRead = (Button) findViewById(R.id.btn_read);
        btnTxtWrite = (Button) findViewById(R.id.btn_write);
        btnFileDel = (Button) findViewById(R.id.btn_del);

        btnTxtRead.setOnClickListener(btnListener);
        btnTxtWrite.setOnClickListener(btnListener);
        btnFileDel.setOnClickListener(btnListener);

    }




    private void bindViews(){
        tv_step = (TextView) findViewById(R.id.tv_step);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        double range = 8;
        float[] value = event.values;
        curValue = magnitude(value[0], value[1]);

        if(motivState == true){
            if (curValue >= lstValue) lstValue = curValue;
            else{
                //檢測到一次峰值
                if(Math.abs(curValue - lstValue) > range){
                    oriValue = curValue;
                    motivState = false;
                }
            }
        }

        //向下加速的狀態
        if(motivState == false){
            if(curValue <= lstValue) lstValue = curValue;
            else{
                if(Math.abs(curValue - lstValue) > range){
                    //檢測到一次峰值
                    oriValue = curValue;
                    if(processState == true){
                        step++;
                        if(processState == true){
                            tv_step.setText(step + "");//數據更新
                        }
                    }
                    motivState = true;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onClick(View v){
        step = 0;
        tv_step.setText("0");
        if(processState == true){
            btn_start.setText("開始");
            processState = false;
        }else{
            btn_start.setText("停止");
            processState = true;
        }
    }

    //向量求模
    public double magnitude(float x, float y){
        double magnitude = 0;
        magnitude = Math.sqrt(x * x + y * y );
        return magnitude;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sManger.unregisterListener(this);
    }


    private void writer() {
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(txtWrite.getText().toString().getBytes());
            fos.close();

            File file = new File(getFilesDir() + "/" + fos);
            Toast.makeText(getApplicationContext(), file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void reader() {
        FileInputStream fos = null;
        BufferedInputStream buffered = null;
        txtRead.setText("");

        try {
            fos = openFileInput(fileName);
            buffered = new BufferedInputStream(fos);
            byte[] buffbyte = new byte[200];
            txtRead.setText("");
            do {
                int flag = buffered.read(buffbyte);
                if (flag == -1) {
                    break;
                } else {
                    txtRead.append(new String(buffbyte), 0, flag);
                }
            }while (true);
            buffered.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fileDel() {
        File file = new File(getFilesDir() + "/" + fileName);
        if (file.exists()){
            file.delete();
            Toast.makeText(getBaseContext(),
                    "刪除紀錄",
                    Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getBaseContext(),
                    "沒有檔案可刪除",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_read:
                    try {
                        Toast.makeText(getApplicationContext(),
                                "請稍後...", Toast.LENGTH_SHORT).show();
                        reader();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.btn_write:
                    try {
                        Toast.makeText(getApplicationContext(),
                                "請稍後...", Toast.LENGTH_SHORT).show();
                        writer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.btn_del:
                    try {
                        Toast.makeText(getApplicationContext(),
                                "請稍後...", Toast.LENGTH_SHORT).show();
                        fileDel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }


        }


    };

}
