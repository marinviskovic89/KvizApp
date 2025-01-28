package com.example.vjezba4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {

    private ListView resultsListView;
    private ResultsAdapter resultsAdapter;
    private final ArrayList<String> resultsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        resultsListView = findViewById(R.id.results_list_view);
        Button startButton = findViewById(R.id.start_button);
        resultsAdapter = new ResultsAdapter(this, resultsList);
        resultsListView.setAdapter(resultsAdapter);
        Button rankButton = findViewById(R.id.ranking_button);

        rankButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, RankingActivity.class);
            startActivity(intent);
        });

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, StartActivity.class);
            startActivity(intent);
        });

        loadResultsFromFirebase();
    }

    private void loadResultsFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference resultsRef = FirebaseDatabase.getInstance().getReference("results").child(userId);

        resultsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resultsList.clear();
                List<QuizResult> tempResultsList = new ArrayList<>();

                for (DataSnapshot resultSnapshot : snapshot.getChildren()) {
                    QuizResult result = resultSnapshot.getValue(QuizResult.class);
                    if (result != null) {
                        tempResultsList.add(result);
                    }
                }
                tempResultsList.sort((r1, r2) -> Integer.compare(r2.getScore(), r1.getScore()));

                for (QuizResult result : tempResultsList) {
                    String formattedResult = formatResult(result);
                    resultsList.add(formattedResult);
                }
                resultsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ResultsActivity.this, "Greška pri učitavanju rezultata!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatResult(QuizResult result) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String date = dateFormat.format(new Date(result.getTimestamp()));
        return "Rezultat: " + result.getScore() + " bodova, Datum: " + date;
    }
}

