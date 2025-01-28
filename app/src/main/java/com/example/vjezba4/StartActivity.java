package com.example.vjezba4;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button startButton = findViewById(R.id.startButton);
        //Button bestUsersButton = findViewById(R.id.bestUsersButton);
        Button resultsButton = findViewById(R.id.sortedResults);

        resultsButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, ResultsActivity.class);
            startActivity(intent);
        });

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        /* bestUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, PerfectScoreUsersActivity.class);
            startActivity(intent);
        }); */
    }
}
