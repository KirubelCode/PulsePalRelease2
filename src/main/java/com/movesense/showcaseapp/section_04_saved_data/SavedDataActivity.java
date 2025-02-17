package com.movesense.showcaseapp.section_04_saved_data;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.movesense.showcaseapp.R;

public class SavedDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_data);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Saved Data");
        }
    }
}
