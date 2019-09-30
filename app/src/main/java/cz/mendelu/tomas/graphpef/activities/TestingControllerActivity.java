package cz.mendelu.tomas.graphpef.activities;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.helperObjects.QuizDBHelper;
import cz.mendelu.tomas.graphpef.helperObjects.QuizQuestion;

/**
 * Created by tomas on 14.09.2018.
 */

public class TestingControllerActivity extends AppCompatActivity  implements Serializable {
    private static final String TAG = "TestingControllerAct";
    private static final String INTENT_EXTRA_SCORE = "extraScore";
    private static final long COUNTDOWN_IN_MILIS = 60000;

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
    private ColorStateList counterColorCd;

    private CountDownTimer countDownTimer;
    private long timeLeftInMilis;

    private List<QuizQuestion> questionList;
    private int questionCounter = 0;
    private int questionsInCategory;
    private QuizQuestion currentQuestion;
    private QuizDBHelper dbHelper;

    private Integer score = 0;

    private boolean answered = false;
    private boolean answeredWrong = false;

    private long lastBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

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

        dbHelper = new QuizDBHelper(this);
        questionList = dbHelper.getAllQuestions();
        Collections.shuffle(questionList);
        questionsInCategory = questionList.size();

        textColorRb = quizAnswer1.getTextColors();
        counterColorCd = questionTimer.getTextColors();

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
        Log.d(TAG, "showNextQuestion: start");
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

            numberOcCorrectQuestionAnswers.setText(getText(R.string.quizAnswered).toString() + questionCounter);
            pointsAcquired.setText(getResources().getString(R.string.quizPoints) + " " + score);
            questionCounter++;
            answered = false;
            confirmButton.setText(getText(R.string.quizConfirmAnswer));

            timeLeftInMilis = COUNTDOWN_IN_MILIS;
            startCountDown();
        }else{
            Log.i(TAG, "showNextQuestion: quiz finished");
            finishQuiz();
        }
        Log.d(TAG, "showNextQuestion: end");
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMilis, 1000) {
            @Override
            public void onTick(long millisUntilFinish) {
                timeLeftInMilis = millisUntilFinish;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeftInMilis = 0;
                updateCountDownText();
                checkAnswer();
            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMilis / 1000) / 60;
        int seconds = (int) (timeLeftInMilis / 1000) % 60;

        String formatedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        if (minutes == 0 && seconds < 11) {
            questionTimer.setTextColor(Color.RED);
        } else {
            questionTimer.setTextColor(counterColorCd);
        }
        questionTimer.setText(formatedTime);
    }

    private void finishQuiz(){
        Log.d(TAG, "finishQuiz");
        Integer bonus = 0;

        if (score > dbHelper.getHighScore()) {
            // + 50 percent if score is highscore
            bonus += score / 2;
        }
        if (questionCounter > dbHelper.getHighScoreStreak()) {
            // + 50 percent if score is highScoreStreak
            bonus += score / 2;
        }

        endingDialog(score, bonus);
    }

    private void checkAnswer(){
        Log.d(TAG, "checkAnswer");
        answered = true;

        countDownTimer.cancel();

        RadioButton selected = findViewById(answersRadioGroup.getCheckedRadioButtonId());
        int answerID = answersRadioGroup.indexOfChild(selected) + 1;


        Log.d(TAG, "questionINseries[" + questionCounter + "] correct[" + currentQuestion.getCorrectAnswerId() + "]==selected[" + answerID + "]");
        if (currentQuestion.getCorrectAnswerId() == answerID){

            if (currentQuestion.isAnswered()) {
                score++;
            } else {
                dbHelper.addQuestionAnswered(currentQuestion.getQuestionID());
                score += 5;
            }

            pointsAcquired.setText(getResources().getString(R.string.quizPoints) + " " + score);
            selected.setTextColor(Color.GREEN);
            if (questionCounter < questionsInCategory){
                confirmButton.setText(getText(R.string.quizContinue));
            }else{
                Toast.makeText(TestingControllerActivity.this, getResources().getString(R.string.quizAnsweredAllQuestions),
                        Toast.LENGTH_LONG).show();
                confirmButton.setText(getText(R.string.quizFinish));
            }
        }else{
            answeredWrong = true;
            selected.setTextColor(Color.RED);
            confirmButton.setText(getText(R.string.quizFinish));
        }
    }

    @Override
    public void onBackPressed() {

        if (lastBackPressed + 2000 > System.currentTimeMillis()) {
            finishQuiz();
        } else {
            Toast.makeText(TestingControllerActivity.this, getResources().getString(R.string.quizTwoBackpress),
                    Toast.LENGTH_SHORT).show();
        }
        lastBackPressed = System.currentTimeMillis();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishQuiz();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


    public void endingDialog(Integer score, Integer bonus) {
        final androidx.appcompat.app.AlertDialog.Builder builderSingle = new androidx.appcompat.app.AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogContent = inflater.inflate(R.layout.quiz_ending_dialog, null);
        builderSingle.setView(dialogContent);
        TextView sumText = dialogContent.findViewById(R.id.quizEndindDialogScoreSumValue);
        TextView scoreText = dialogContent.findViewById(R.id.quizEndindDialogScoreValue);
        TextView bonusText = dialogContent.findViewById(R.id.quizEndindDialogBonusValue);

        //sumText.setText(Integer.toString(score + bonus));
        //scoreText.setText(Integer.toString(score));
        //bonusText.setText(Integer.toString(bonus));

        startCountAnimation(sumText, score + bonus);
        startCountAnimation(scoreText, score);
        startCountAnimation(bonusText, bonus);

        builderSingle.setPositiveButton(
                getString(R.string.quizFinish),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (questionCounter != 0) {
                            dbHelper.addQuizAnsered(score + bonus, questionCounter);
                        }
                        Intent results = new Intent();
                        results.putExtra(INTENT_EXTRA_SCORE, score);
                        setResult(RESULT_OK);
                        finish();
                    }
                });
        builderSingle.show();
    }

    private void startCountAnimation(TextView textView, Integer maxPoints) {
        ValueAnimator animator = ValueAnimator.ofInt(0, maxPoints);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText(animation.getAnimatedValue().toString());
            }
        });
        animator.start();
    }
}
