package com.example.vjezba4;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private TextView rankTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);  // Provjeri naziv layouta, bio je "acitvity_ranking"

        rankTextView = findViewById(R.id.rankTextView);

        loadRanking();
    }

    private void loadRanking() {
        // Dohvat ID trenutnog korisnika
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Dohvat referencije na "results" u Firebase
        DatabaseReference resultsRef = FirebaseDatabase.getInstance().getReference("results");

        resultsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Lista za pohranu svih rezultata
                List<QuizResult> allResults = new ArrayList<>();
                QuizResult lastUserResult = null;

                // Prolazimo kroz sve korisnike u bazi
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userKey = userSnapshot.getKey();  // Dohvaćamo ID korisnika

                    // Prolazimo kroz rezultate svakog korisnika
                    for (DataSnapshot resultSnapshot : userSnapshot.getChildren()) {
                        QuizResult result = resultSnapshot.getValue(QuizResult.class);
                        if (result != null) {
                            // Ako je korisnik trenutni, pohranjujemo njegov zadnji rezultat
                            if (userKey.equals(userId)) {
                                if (lastUserResult == null || result.getTimestamp() > lastUserResult.getTimestamp()) {
                                    lastUserResult = result;  // Pohranjujemo zadnji rezultat prema timestampu
                                }
                            } else {
                                // Dodajemo rezultat ostalih korisnika
                                allResults.add(result);
                            }
                        }
                    }
                }

                // Ako imamo zadnji rezultat trenutnog korisnika, dodajemo ga u listu
                if (lastUserResult != null) {
                    allResults.add(lastUserResult);
                }

                // Sortiramo rezultate prema score-u od najvećeg prema najmanjem
                Collections.sort(allResults, new Comparator<QuizResult>() {
                    @Override
                    public int compare(QuizResult r1, QuizResult r2) {
                        return Integer.compare(r2.getScore(), r1.getScore()); // Sortiranje DESC
                    }
                });

                // Pronađi rang trenutnog korisnika
                int userRank = -1;
                for (int i = 0; i < allResults.size(); i++) {
                    if (allResults.get(i).equals(lastUserResult)) {
                        userRank = i + 1; // Rang je 1-based
                        break;
                    }
                }

                // Prikazivanje rezultata
                if (userRank != -1) {
                    String message = "Korisnik je po rezultatu " + userRank + ". u odnosu na sve rezultate.";
                    rankTextView.setText(message);
                } else {
                    rankTextView.setText("Nema rezultata za trenutnog korisnika.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RankingActivity.this, "Error loading ranking", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
