package com.example.vjezba4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class QuizActivity extends AppCompatActivity {
    private TextView questionText;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4;
    private Button nextButton;

    private final List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionText = findViewById(R.id.question_text);
        optionsGroup = findViewById(R.id.options_group);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextButton = findViewById(R.id.next_button);

        if (savedInstanceState != null) {
            currentQuestionIndex = savedInstanceState.getInt("currentQuestionIndex");
            score = savedInstanceState.getInt("currentScore");
        }

        loadQuestionsFromFirebase();

        nextButton.setOnClickListener(v -> checkAnswerAndProceed());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentQuestionIndex", currentQuestionIndex);
        outState.putInt("score", score);
    }

    private void loadQuestionsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("questions");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    String question = questionSnapshot.child("question").getValue(String.class);
                    List<String> options = new ArrayList<>();
                    for (DataSnapshot optionSnapshot : questionSnapshot.child("options").getChildren()) {
                        options.add(optionSnapshot.getValue(String.class));
                    }
                    String answer = questionSnapshot.child("answer").getValue(String.class);

                    questionList.add(new Question(question, options, answer));
                }
                displayQuestion();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(QuizActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            Question currentQuestion = questionList.get(currentQuestionIndex);
            questionText.setText(currentQuestion.getQuestion());
            option1.setText(currentQuestion.getOptions().get(0));
            option2.setText(currentQuestion.getOptions().get(1));
            option3.setText(currentQuestion.getOptions().get(2));
            option4.setText(currentQuestion.getOptions().get(3));
        } else {
            finishQuiz();
        }
    }

    private void checkAnswerAndProceed() {
        int selectedOptionId = optionsGroup.getCheckedRadioButtonId();
        if (selectedOptionId == -1) {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedOption = findViewById(selectedOptionId);
        String selectedAnswer = selectedOption.getText().toString();
        Question currentQuestion = questionList.get(currentQuestionIndex);

        if (selectedAnswer.equals(currentQuestion.getAnswer())) {
            score++;
        }

        currentQuestionIndex++;
        optionsGroup.clearCheck();
        displayQuestion();
    }

    private void finishQuiz() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference resultsRef = FirebaseDatabase.getInstance().getReference("results").child(userId);

        // Kreiranje jedinstvenog ID-a za rezultat igre
        String resultId = resultsRef.push().getKey();

        // Kreiranje objekta za rezultat
        QuizResult result = new QuizResult(score, System.currentTimeMillis());

        // Spremanje rezultata u Firebase
        if (resultId != null) {
            resultsRef.child(resultId).setValue(result).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(QuizActivity.this, "Rezultat sačuvan!", Toast.LENGTH_SHORT).show();
                    navigateToResultsScreen(); // Navigacija na ekran s rezultatima
                } else {
                    Toast.makeText(QuizActivity.this, "Greška pri spremanju rezultata!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Navigacija na ResultsActivity
    private void navigateToResultsScreen() {
        Intent intent = new Intent(QuizActivity.this, ResultsActivity.class);
        startActivity(intent);
        finish();
    }
}
