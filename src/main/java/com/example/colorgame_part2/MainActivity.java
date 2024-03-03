package com.example.colorgame_part2;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private List<ImageView> colorImageViews = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private MediaPlayer chain;
    private ImageView diceImageView1;
    private int selectedBoxIndex = -1; // User's selected gray box index
    private int lastWinningColorIndex = -1; // Index of the last winning color
    private TextView Win;

    private int[] grayDrawables = {
            R.drawable.gray1,
            R.drawable.gray2,
            R.drawable.gray3,
            R.drawable.gray4,
            R.drawable.gray5,
            R.drawable.gray6
    };

    private int[] colorDrawables = {
            R.drawable.dice1,
            R.drawable.dice2,
            R.drawable.dice3,
            R.drawable.dice4,
            R.drawable.dice5,
            R.drawable.dice6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = MediaPlayer.create(this, R.raw.colorblindgameeebg);
        startBackgroundMusic(); // Start the background music

        chain = MediaPlayer.create(this, R.raw.chainsound);

        initializeImageViews();

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            if (selectedBoxIndex != -1) {
                startColorReveal();
            } else {
                Toast.makeText(MainActivity.this, "Please select a box first", Toast.LENGTH_SHORT).show();
            }
        });

        diceImageView1 = findViewById(R.id.diceImageView1);

        // Find the back button view by ID
        ImageView backButton = findViewById(R.id.backbtn);

        // Set OnClickListener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the HomePage activity
                Intent intent = new Intent(MainActivity.this, HomePage.class);
                startActivity(intent);
            }
        });
    }

    private void initializeImageViews() {
        for (int i = 1; i <= 6; i++) {
            int resID = getResources().getIdentifier("color" + i, "id", getPackageName());
            ImageView imageView = findViewById(resID);
            colorImageViews.add(imageView); // Add the ImageView to the list
            final int index = i - 1;
            imageView.setOnClickListener(v -> selectBox(index));
        }
    }

    private void selectBox(int index) {
        // Reset previous selection if any
        if (selectedBoxIndex != -1) {
            colorImageViews.get(selectedBoxIndex).setImageResource(grayDrawables[selectedBoxIndex]);
        }
        selectedBoxIndex = index;
        showToast("Selected box: " + (index + 1));
        animateBox(colorImageViews.get(index));
    }

    private void startColorReveal() {
        // Generate a random number between 1 and 6 (inclusive) to simulate rolling a dice
        Random random = new Random();
        int diceResult = random.nextInt(6) + 1;

        // Display the rolled dice result with rolling animation
        diceImageView1.setVisibility(View.VISIBLE);
        animateDiceRoll(diceImageView1, diceResult);

        // Choose the winning color index, giving less weight to the last winning color
        int winningColorIndex = chooseWinningColorIndex(random);

        // Set the colors for the image views using the shuffled array
        for (int i = 0; i < colorImageViews.size(); i++) {
            colorImageViews.get(i).setImageResource(colorDrawables[i]);
        }
        colorImageViews.get(0).postDelayed(this::resetGame, 3000); // 3 seconds delay
    }


    private void animateDiceRoll(ImageView diceImageView, int diceResult) {
        // Generate a list of random dice images
        List<Integer> diceImages = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            diceImages.add(getDiceImageResource(i));
        }
        Collections.shuffle(diceImages);

        // Create an ObjectAnimator to animate the dice roll
        ObjectAnimator animator = ObjectAnimator.ofFloat(diceImageView, "rotation", 0, 360);
        animator.setDuration(1000); // Set duration for the animation
        animator.start();

        // Set the dice result image after the animation completes
        Handler handler = new Handler();
        diceImageView.postDelayed(new Runnable() {
            int rotationCounter = 0;

            @Override
            public void run() {
                // Change the background color based on the current rotation
                switch (rotationCounter % 6) {
                    case 0:
                        diceImageView.setImageResource(R.drawable.dice1);
                        break;
                    case 1:
                        diceImageView.setImageResource(R.drawable.dice2);
                        break;
                    case 2:
                        diceImageView.setImageResource(R.drawable.dice3);
                        break;
                    case 3:
                        diceImageView.setImageResource(R.drawable.dice4);
                        break;
                    case 4:
                        diceImageView.setImageResource(R.drawable.dice5);
                        break;
                    case 5:
                        diceImageView.setImageResource(R.drawable.dice6);
                        break;
                }

                // Increment the rotation counter
                rotationCounter++;

                // If the animation is still running, schedule the next color change
                if (animator.isRunning()) {
                    handler.postDelayed(this, 50); // Adjust the delay based on your preference
                } else {
                    // If the animation has finished, set the final dice result image
                    diceImageView.setImageResource(getDiceImageResource(diceResult));
                }
            }
        }, 0);
    }



    private int chooseWinningColorIndex(Random random) {
        // Set probabilities for each color
        double[] probabilities = {0.15, 0.15, 0.15, 0.15, 0.15, 0.25};

        // If a color won last time, reduce its probability
        if (lastWinningColorIndex != -1) {
            probabilities[lastWinningColorIndex] *= 0.5;
            normalizeProbabilities(probabilities);
        }

        // Choose a color index based on the probabilities
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;

        for (int i = 0; i < probabilities.length; i++) {
            cumulativeProbability += probabilities[i];
            if (randomValue <= cumulativeProbability) {
                return i;
            }
        }

        // Default to a random color if something goes wrong
        return random.nextInt(colorDrawables.length);
    }

    private void normalizeProbabilities(double[] probabilities) {
        double sum = 0.0;

        for (double probability : probabilities) {
            sum += probability;
        }

        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] /= sum;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void animateBox(ImageView box) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.2f, // Start and end X scale
                1.0f, 1.2f, // Start and end Y scale
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f, // Pivot X type and value
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f); // Pivot Y type and value
        scaleAnimation.setDuration(500); // Animation duration in milliseconds
        scaleAnimation.setRepeatCount(1); // Repeat animation once
        scaleAnimation.setRepeatMode(ScaleAnimation.REVERSE); // Reverse animation at the end
        box.startAnimation(scaleAnimation);
    }


    private int getDiceImageResource(int diceResult) {
        switch (diceResult) {
            case 1:
                return R.drawable.dice1;
            case 2:
                return R.drawable.dice2;
            case 3:
                return R.drawable.dice3;
            case 4:
                return R.drawable.dice4;
            case 5:
                return R.drawable.dice5;
            case 6:
                return R.drawable.dice6;
            default:
                return R.drawable.dice1; // Default to dice1 if an invalid result
        }
    }


    // BACKGROUND MUSIC METHOD ==============================================

    public void InstructionClicked(View view) {
        // Find the instructionImageView by ID
        ImageView instructionImageView = findViewById(R.id.slotMachineInstruction);

        // Create an ObjectAnimator to animate the translationY property
        ObjectAnimator animator = ObjectAnimator.ofFloat(instructionImageView, "translationY", 0, -instructionImageView.getHeight());
        animator.setDuration(2000); // Set duration for the animation in milliseconds
        animator.start(); // Start the animation
        chain.start();

        // Disable click listener to prevent further clicks
        instructionImageView.setOnClickListener(null);

        // Pause the chain sound after 1500 milliseconds
        new Handler().postDelayed(() -> {
            if (chain != null && chain.isPlaying()) {
                chain.pause();
            }
        }, 1400);
    }




    private void startBackgroundMusic() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start(); // Start the background music
        }
    }

    private void stopBackgroundMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // Pause the background music
        }
    }

    private void resetGame() {
        for (ImageView imageView : colorImageViews) {
            imageView.setImageResource(grayDrawables[colorImageViews.indexOf(imageView)]);
        }
        selectedBoxIndex = -1; // Reset selected box index
    }

    protected void onResume() {
        super.onResume();
        startBackgroundMusic(); // Resume the background music when the activity is resumed
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBackgroundMusic(); // Pause the background music when the activity is paused
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBackgroundMusic(); // Stop the background music when the activity is destroyed
        mediaPlayer.release(); // Release resources associated with the MediaPlayer
    }

    private void playChainSound() {
        if (!chain.isPlaying()) {
            chain.start();
        }
    }

    private void stopChainSound() {
        if (chain.isPlaying()) {
            chain.pause();
            chain.seekTo(0); // Reset the chain sound to the beginning
        }
    }

}