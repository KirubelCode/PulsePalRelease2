package com.movesense.showcaseapp.section_00_mainView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.movesense.showcaseapp.R;

public class MainViewActivity extends AppCompatActivity {

    private Button startExerciseSessionButton, postWorkoutResultsButton, settingsButton, signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        startExerciseSessionButton = findViewById(R.id.startExerciseSessionButton);
        postWorkoutResultsButton = findViewById(R.id.postWorkoutResultsButton);
        settingsButton = findViewById(R.id.settingsButton);
        signOutButton = findViewById(R.id.signOutButton);

        startExerciseSessionButton.setOnClickListener(view ->
                startActivity(new Intent(MainViewActivity.this, ExerciseSessionActivity.class)));

        postWorkoutResultsButton.setOnClickListener(view ->
                startActivity(new Intent(MainViewActivity.this, WorkoutResultsActivity.class)));

        settingsButton.setOnClickListener(view ->
                startActivity(new Intent(MainViewActivity.this, SettingsActivity.class)));

        signOutButton.setOnClickListener(view -> {
            startActivity(new Intent(MainViewActivity.this, LoginActivity.class));
            finish();
        });
    }
}
