package cz.mendelu.tomas.graphpef.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.helperObjects.QuizDBHelper;
import cz.mendelu.tomas.graphpef.helperObjects.QuizQuestion;

/**
 * Created by tomas on 14.09.2018.
 */

public class TestingControllerActivity extends AppCompatActivity  implements Serializable {
    private static final String TAG = "TestingControllerAct";
    private TextView numberOcCorrectQuestionAnswers;
    private TextView pointsAcquired;
    private TextView questionCategory;
    private TextView questionTimer;
    private TextView questionText;
    private RadioGroup answersRadioGroup;
    private RadioButton quizAnswer1;
    private RadioButton quizAnswer2;
    private RadioButton quizAnswer3;
    private RadioButton quizAnswer4;
    private Button confirmButton;

    private ColorStateList textColorRb;

    private List<QuizQuestion> questionList;
    private int questionCounter = 0;
    private int questionsInCategory;
    private QuizQuestion currentQuestion;

    private int score = 0;

    private boolean answered = false;
    private boolean answeredWrong = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);

        numberOcCorrectQuestionAnswers = findViewById(R.id.questionNumberOfCorrectAnswersText);
        pointsAcquired = findViewById(R.id.questionPointsAcquired);
        questionCategory = findViewById(R.id.questionCategoryText);
        questionTimer = findViewById(R.id.questionTimer);
        questionText = findViewById(R.id.questionText);
        answersRadioGroup = findViewById(R.id.quizAnswerGroup);
        quizAnswer1 = findViewById(R.id.quizAnswer1);
        quizAnswer2 = findViewById(R.id.quizAnswer2);
        quizAnswer3 = findViewById(R.id.quizAnswer3);
        quizAnswer4 = findViewById(R.id.quizAnswer4);
        confirmButton = findViewById(R.id.answerQuizButton);

        QuizDBHelper dbHelper = new QuizDBHelper(this);
        questionList = dbHelper.getAllQuestions();
        Collections.shuffle(questionList);
        questionsInCategory = questionList.size();

        textColorRb = quizAnswer1.getTextColors();

        showNextQuestion();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "confirm button pressed");
                if(!answered){
                    Log.d(TAG, "confirm button pressed");
                    if (quizAnswer1.isChecked() || quizAnswer2.isChecked() || quizAnswer3.isChecked() || quizAnswer4.isChecked()){
                        checkAnswer();
                    }else{
                        Toast.makeText(TestingControllerActivity.this , "Select answer",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    showNextQuestion();
                }
            }
        });

    }

    private void showNextQuestion(){
        quizAnswer1.setTextColor(textColorRb);
        quizAnswer2.setTextColor(textColorRb);
        quizAnswer3.setTextColor(textColorRb);
        quizAnswer4.setTextColor(textColorRb);
        answersRadioGroup.clearCheck();

        if (questionCounter < questionsInCategory && !answeredWrong){
            currentQuestion = questionList.get(questionCounter);

            questionText.setText(currentQuestion.getQuestion());
            quizAnswer1.setText(currentQuestion.getOption1());
            quizAnswer2.setText(currentQuestion.getOption2());
            quizAnswer3.setText(currentQuestion.getOption3());
            quizAnswer4.setText(currentQuestion.getOption4());

            questionCategory.setText(currentQuestion.getCategory());

            numberOcCorrectQuestionAnswers.setText("Answered "  + questionCounter);
            questionCounter++;
            answered = false;
            confirmButton.setText("Confirm Answer");
        }else{
            finishQuiz();
        }
    }

    private void finishQuiz(){
        finish();
    }

    private void checkAnswer(){
        answered = true;

        RadioButton selected = findViewById(answersRadioGroup.getCheckedRadioButtonId());
        int answerID = answersRadioGroup.indexOfChild(selected) + 1;


        Log.d(TAG," correct["+currentQuestion.getCorrectAnswerId()+"]==selected["+answerID+"]");
        if (currentQuestion.getCorrectAnswerId() == answerID){
            score++;
            pointsAcquired.setText("Points: " + score);
            selected.setTextColor(Color.GREEN);
            if (questionCounter < questionsInCategory){
                confirmButton.setText("Continue");
            }else{
                confirmButton.setText("Finish");
            }
        }else{
            answeredWrong = true;
            selected.setTextColor(Color.RED);
            confirmButton.setText("Finish");
        }
    }
}
