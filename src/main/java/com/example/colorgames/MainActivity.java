package com.example.colorgames;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView colorTextView;
    private Button redButton;
    private Button yellowButton;
    private Button blueButton;
    private Button whiteButton;
    private Button greenButton;
    private Button pinkButton;



    private String[] colors = {"Red", "Blue", "Green", "Yellow", "White", "Pink"};  // Add more colors as needed
    private String correctColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorTextView = findViewById(R.id.colorTextView);
        redButton = findViewById(R.id.redButton);
        blueButton = findViewById(R.id.blueButton);
        yellowButton = findViewById(R.id.yellowButton);
        whiteButton = findViewById(R.id.whiteButton);
        greenButton = findViewById(R.id.greenButton);
        pinkButton = findViewById(R.id.pinkButton);


        startNewRound();
    }

    private void startNewRound() {
        Random random = new Random();
        correctColor = colors[random.nextInt(colors.length)];
        colorTextView.setText(correctColor);
    }

    public void onButtonClick(View view) {
        String selectedColor = getColorName(view);

        if (selectedColor.equals(correctColor)) {
            colorTextView.setText("Correct!");
        } else {
            colorTextView.setText("Wrong! Try again.");
        }

        // Start a new round after a brief delay
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                startNewRound();
            }
        }, 1000);
    }

    private String getColorName(View view) {
        int buttonColor = ((Button) view).getDrawingCacheBackgroundColor();
        return getColorNameFromCode(buttonColor);
    }

    private String getColorNameFromCode(int colorCode) {
        if (colorCode == Color.RED) {
            return "Red";
        } else if (colorCode == getResources().getColor(R.color.blue)) {
            return "Blue";
        } else if (colorCode == getResources().getColor(R.color.yellow)) {
            return "Yellow";
        } else if (colorCode == getResources().getColor(R.color.white)) {
            return "White";
        }  else if (colorCode == getResources().getColor(R.color.green)) {
            return "Green";
        } else if (colorCode == getResources().getColor(R.color.pink)) {
            return "Pink";
        }
        return "";
    }
}
