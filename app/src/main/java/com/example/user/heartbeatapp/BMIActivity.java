package com.example.user.heartbeatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BMIActivity extends AppCompatActivity {

    EditText height, weight;
    Button calculate;
    Button clear;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        height = (EditText)findViewById(R.id.height);
        weight = (EditText)findViewById(R.id.weight);
        calculate = (Button)findViewById(R.id.culcalate);
        clear = (Button)findViewById(R.id.clear);
        result = (TextView)findViewById(R.id.result);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                height.setText("");
                weight.setText("");
                result.setText("");
            }
        });

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateBMI();
            }
        });
    }

    private void calculateBMI(){
        String heightStr = height.getText().toString();
        String weightStr = weight.getText().toString();


        if(height != null && !"".equals(heightStr) && weight != null && !"".equals(weightStr)){
            float heightValue = Float.parseFloat(heightStr) / 100;
            float weightValue = Float.parseFloat(weightStr);

            float bmi = weightValue / (heightValue * heightValue);
            displayBMI(bmi);

        }

    }
    private void displayBMI(float bmi){
        String bmiLabel="";

        if(Float.compare(bmi, 18.5f) <= 0){
            bmiLabel = "體重過輕,請多吃點唷~";
        }
        else if(Float.compare(bmi, 18.5f) >= 0 && Float.compare(bmi, 24f) < 0){
            bmiLabel = "正常範圍,繼續保持!!";
        }
        else if(Float.compare(bmi, 24.f) >= 0 && Float.compare(bmi, 27f) < 0){
            bmiLabel = "體重過重,請注意飲食!!";
        }
        else if(Float.compare(bmi, 27f) >= 0 && Float.compare(bmi, 30f) < 0){
            bmiLabel = "輕度肥胖,請注意飲食!!";
        }
        else if(Float.compare(bmi, 30f) >= 0 && Float.compare(bmi, 35f) < 0){
            bmiLabel = "中度肥胖,請注意飲食!!";
        }
        else {
            bmiLabel = "重度肥胖,請注意飲食!!";
        }

        bmiLabel = bmi + "\n" + bmiLabel;
        result.setText(bmiLabel);
    }

}
