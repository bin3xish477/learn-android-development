package com.example.bmicalculator;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
//import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView result;
    private RadioButton maleBtn;
    private RadioButton femaleBtn;
    private EditText ageTxt;
    private EditText feetTxt;
    private EditText inchesTxt;
    private EditText weightTxt;
    private Button calculateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setupCalculateBtnOnClickListener();
    }

    private void findViews() {
        result = findViewById(R.id.text_view_result);
        maleBtn = findViewById(R.id.radio_button_male);
        femaleBtn = findViewById(R.id.radio_button_female);
        ageTxt = findViewById(R.id.edit_text_age);
        feetTxt = findViewById(R.id.edit_text_feet);
        inchesTxt = findViewById(R.id.edit_text_inches);
        weightTxt = findViewById(R.id.edit_text_weight);
        calculateBtn = findViewById(R.id.button_calculate);
    }

    private void setupCalculateBtnOnClickListener() {
        calculateBtn.setOnClickListener(v -> {
            int age = Integer.parseInt(ageTxt.getText().toString());
            int feet = Integer.parseInt(feetTxt.getText().toString());
            int inches = Integer.parseInt(inchesTxt.getText().toString());
            int weight = Integer.parseInt(weightTxt.getText().toString());
            String values = String.format(
                    Locale.ENGLISH, "age=%d,feet=%d,inch=%d,weight=%d",
                    age, feet, inches, weight);
            Log.d("MainActivity", values);

            double bmi = calculateBmi(age, feet, inches, weight);
            displayResult(bmi);
        });
    }

    private double calculateBmi(int age, int feet, int inches, int weight) {
        double heightInMeters = (feet * 12 + inches) * 0.0254;
        Log.d("MainActivity", String.format(Locale.ENGLISH, "height=%.2f", heightInMeters));
        return (double) weight / Math.pow(heightInMeters, 2);
    }

    private void displayResult(double bmi) {
        Log.d("MainActivity", String.format(Locale.ENGLISH, "bmi=%.2f", bmi));
//            Toast.makeText(this, String.format(Locale.ENGLISH, "You're bmi is %f", bmi), Toast.LENGTH_SHORT).show();
        if (maleBtn.isChecked()) {
        } else if (femaleBtn.isChecked()) {
        } else {
        }
        String resultMsg;
        if (bmi < 18.5) {
            resultMsg = String.format(Locale.ENGLISH, "You're BMI is %.2f - you're underweight!", bmi);
        } else if (bmi > 25) {
            resultMsg = String.format(Locale.ENGLISH, "You're BMI is %.2f! - you're overweight!", bmi);
        } else {
            resultMsg = String.format(Locale.ENGLISH, "You're BMI is %.2f! - you're healthy!", bmi);
        }
        result.setText(resultMsg);
    }
}