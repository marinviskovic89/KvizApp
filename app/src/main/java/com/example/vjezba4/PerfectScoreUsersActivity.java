package com.example.vjezba4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PerfectScoreUsersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PerfectScoreAdapter adapter;
    private final List<User> perfectScoreUsers = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private DatabaseReference resultsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfect_score_users);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PerfectScoreAdapter(perfectScoreUsers);
        recyclerView.setAdapter(adapter);
        resultsRef = FirebaseDatabase.getInstance().getReference("results");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        Button startButton = findViewById(R.id.start_button);

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(PerfectScoreUsersActivity.this, StartActivity.class);
            startActivity(intent);
        });

        // Dohvati korisnike sa score 100%
        fetchPerfectScoreUsers();
    }

    private void fetchPerfectScoreUsers() {
        resultsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    int perfectScoreCount = 0;
                    for (DataSnapshot quizSnapshot : userSnapshot.getChildren()) {
                        Integer score = quizSnapshot.child("score").getValue(Integer.class);
                        Integer maxScore = 10; // Pretpostavimo da je maksimalni broj bodova za quiz 10

                        if (score != null && score == maxScore) {
                            perfectScoreCount++;
                        }
                    }

                    //Log.d("ResultsActivity", "Korisnik " + userSnapshot.getKey() + " ima " + perfectScoreCount + " perfect score");
                    if (perfectScoreCount > 0) {
                        String userId = userSnapshot.getKey();
                        fetchUsername(userId, perfectScoreCount);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PerfectScoreUsersActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUsername(String userId, int perfectScoreCount) {
        usersRef.child(userId).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);
                if (username != null) {
                    users.add(new User(username, userId, perfectScoreCount));
                    sortUsersByPerfectScore();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PerfectScoreUsersActivity.this, "Error fetching username", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sortUsersByPerfectScore() {
        // Sortiraj korisnike prema broju 100% score
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                return Integer.compare(u2.getPerfectScoreCount(), u1.getPerfectScoreCount()); // Od najvećeg ka najmanjem
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PerfectScoreAdapter adapter = new PerfectScoreAdapter(users);
        recyclerView.setAdapter(adapter);
    }

    // Adapter za RecyclerView
    public static class PerfectScoreAdapter extends RecyclerView.Adapter<PerfectScoreAdapter.ViewHolder> {
        private final List<User> users;

        public PerfectScoreAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            holder.textView.setText(user.toString());
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}

