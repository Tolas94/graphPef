package cz.mendelu.tomas.graphpef.activities;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.helperObjects.ProgressBarListAdapter;
import cz.mendelu.tomas.graphpef.helperObjects.QuizDBHelper;
import io.fabric.sdk.android.Fabric;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class MainActivity extends AppCompatActivity implements Serializable, Observer {

    private static final String TAG = "MainActivity";
    private static final int QUIZ_REQUEST_CODE = 1;
    private static final int MAX_CATEGORIES_ON_MAINSCREEN = 3;

    private FirebaseAuth mAuth;
    private Button mainScreenButton;
    //TODO: check startSignInButton directly in update UI and not as parameter
    private boolean registationSequenceStarted;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBarListAdapter progressBarListAdapter;
    private QuizDBHelper dbHelper;

    private TextView categoriesUnlockedValue, questionsAnsweredValue, questionsHighScoreStreakValue, questionsHighScoreValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean userOptInFlag = CheckOptInValue();

        if (userOptInFlag == true) {
            //Only initialize Fabric is user opt-in is true
            Fabric.with(this, new Crashlytics());
            Fabric.with(this, new Answers());
        }
        mAuth = FirebaseAuth.getInstance();

//        ((EditText)findViewById(R.id.signInPassword)).setHint(getResources().getString(R.string.password));
        //      ((EditText)findViewById(R.id.signInXname)).setHint(getResources().getString(R.string.xname));

        final Button signInButton = findViewById(R.id.signInSubmitButton);
        Button registerButton = findViewById(R.id.registerSubmitButton);
        Button startRegisterButton = findViewById(R.id.startRegisterSubmitButton);
        Button signOutButton = findViewById(R.id.signOutButton);
        Button sendEmail = findViewById(R.id.sendEmailButton);
        Button passwordResetButton = findViewById(R.id.passwordResetButton);
        Button startSignInButton = findViewById(R.id.startSignInButton);
        Button startQuizButton = findViewById(R.id.startQuizButton);
        recyclerView = findViewById(R.id.mainScreenScoreUnlockableCategoriesRecycleView);
        TextView categoriesUnlockedTitle = findViewById(R.id.mainScreenCategoriesUnlockedTitle);
        categoriesUnlockedValue = findViewById(R.id.mainScreenCategoriesUnlockedValue);
        TextView questionsAnsweredTitle = findViewById(R.id.mainScreenQuestionsAnsweredTitle);
        questionsAnsweredValue = findViewById(R.id.mainScreenQuestionsAnsweredValue);
        TextView questionsHighScoreStreakTitle = findViewById(R.id.mainScreenQuestionsHighScoreStreakTitle);
        questionsHighScoreStreakValue = findViewById(R.id.mainScreenQuestionsHighScoretreakValue);
        TextView questionsHighScoreTitle = findViewById(R.id.mainScreenQuestionsHighScoreTitle);
        questionsHighScoreValue = findViewById(R.id.mainScreenQuestionsHighScoreValue);
        mainScreenButton = findViewById(R.id.startAppButton);

        startQuizButton.setText(R.string.quizStartQuizButton);

        startQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz();
            }
        });

        ImageButton infoOnMainScreen = findViewById(R.id.infoOnMainScreen);

        infoOnMainScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo();
            }
        });


        setUpSignInButton(signInButton);
        signInButton.setText(R.string.signIn);
        setUpRegisterButton(registerButton);
        registerButton.setText(R.string.register);
        setUpSignOutButton(signOutButton);
        signOutButton.setText(R.string.signOut);
        setUpSendEmailButton(sendEmail);
        sendEmail.setText(R.string.sendEmail);
        setUpPassworResetButton(passwordResetButton);
        passwordResetButton.setText(R.string.passwordReset);
        startRegisterButton.setText(R.string.register);
        mainScreenButton.setText(R.string.start_app);
        categoriesUnlockedTitle.setText(R.string.mainScreenCategoriesUnlocked);
        questionsAnsweredTitle.setText(R.string.mainScreenQuestionAnswered);
        questionsHighScoreStreakTitle.setText(R.string.mainScreenHighScoreStreak);
        questionsHighScoreTitle.setText(R.string.mainScreenHighScore);
        startSignInButton.setText(R.string.signIn);
        EditText passwordText = findViewById(R.id.signInPassword);

        startRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registationSequenceStarted = true;
                updateUI(null, registationSequenceStarted);
            }
        });


        startSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registationSequenceStarted = false;
                updateUI(null, registationSequenceStarted);
            }
        });



        passwordText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signInButton.performClick();
                    return true;
                }
                return false;
            }
        });


        //TextView appNameText = findViewById(R.id.mainScreenDisclaimer);
        //appNameText.setText(getText(R.string.disclaimer_main_page));

        logUser();

        mainScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked button mainScreen");
                if (mAuth.getCurrentUser() != null) {
                    mAuth.getCurrentUser().reload();
                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        Log.d(TAG, "onClick: Clicked button mainScreen - email verified");
                    } else {
                        Log.d(TAG, "onClick: Clicked button mainScreen - email NOT verified");
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.emailNotVerified),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    toastMessage(getResources().getString(R.string.signInHint));
                }
                Intent intent = new Intent(MainActivity.this, GraphMenuListActivity.class);
                startActivity(intent);
            }
        });

        dbHelper = new QuizDBHelper(this);
        dbHelper.addObserver(this);

        updateCategories();
    }

    private void presentShowcaseSequence() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200); // half second between each showcase view
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, TAG);
        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
            @Override
            public void onShow(MaterialShowcaseView itemView, int position) {
            }
        });
        sequence.setConfig(config);
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(mainScreenButton)
                        .setDismissText(getString(R.string.disclaimer_main_page))
                        .setContentText(getString(R.string.dismiss_showcase_text))
                        .withRectangleShape(true)
                        .setDismissOnTouch(true)
                        .build()
        );
        sequence.start();
    }

    private void logUser() {
        Log.d(TAG, "logUser");
        if (mAuth != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Crashlytics.setUserIdentifier(user.getUid());
                Crashlytics.setUserEmail(user.getEmail());
                if (dbHelper == null) {
                    Log.e(TAG, "dbHelper == null");
                    dbHelper = new QuizDBHelper(this);
                    dbHelper.addObserver(this);
                }
                dbHelper.createUserRef(user.getUid(), user.getEmail());
            } else {
                Log.d(TAG, "user == null");
            }
        } else {
            Log.d(TAG, "mAuth == null");
        }
    }

    boolean CheckOptInValue() {
        //check opt-in value
        //return true; //if user opted-in
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser, registationSequenceStarted);
    }

    private void startQuiz() {
        Intent intent = new Intent(MainActivity.this, TestingControllerActivity.class);
        startActivityForResult(intent, QUIZ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QUIZ_REQUEST_CODE) {
            dbHelper.updateUserStats();
        }
    }

    /**
     * update UI changes main splash screenthe
     * possible flow is
     * 1. From Unsigned -> to logged in when user != null
     * 2. From Unsigned -> to register sequence
     * 3. From register sequence -> logged in
     * If returned the flow can also be
     * 4.From logged in -> Unsigned
     * 5.From register -> logged in
     * <p>
     * Other flow is not possible and shal not be possible without further touching of code
     */
    private void updateUI(FirebaseUser user, boolean registerSequence) {
        if (user != null) {
            //Signed in

            //make signing input layout invisible(gone)
            findViewById(R.id.signInLayout).setVisibility(View.GONE);
            //make score sheet visible
            findViewById(R.id.mainScreenScore).setVisibility(View.VISIBLE);
            //update score
            updateScore();
            updateCategories();

            //change position of startAppLayout to be directly under score
            RelativeLayout startAppLayout = findViewById(R.id.startAppLayout);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) startAppLayout.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.mainScreenScore);
            //findViewById(R.id.signedLayout).setVisibility(View.VISIBLE);
            InputMethodManager keyboard = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(findViewById(R.id.signInPassword).getWindowToken(), 0);

            findViewById(R.id.signOutButton).setVisibility(View.VISIBLE);
            presentShowcaseSequence();
        } else if (registerSequence) {
            //register in
            findViewById(R.id.signInSubmitButton).setVisibility(View.GONE);
            findViewById(R.id.startRegisterSubmitButton).setVisibility(View.GONE);
            findViewById(R.id.passwordResetButton).setVisibility(View.GONE);

            TextView text = findViewById(R.id.signInCardTitle);
            text.setText(R.string.register);

            findViewById(R.id.registerSubmitButton).setVisibility(View.VISIBLE);
            findViewById(R.id.startSignInButton).setVisibility(View.VISIBLE);
            findViewById(R.id.signInPasswordConfirmationLayout).setVisibility(View.VISIBLE);

        } else {
            //Sign in
            //make signing input layout visible
            findViewById(R.id.signInLayout).setVisibility(View.VISIBLE);
            //make score invisible
            findViewById(R.id.mainScreenScore).setVisibility(View.GONE);

            RelativeLayout startAppLayout = findViewById(R.id.startAppLayout);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) startAppLayout.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.signLayout);

            //findViewById(R.id.signedLayout).setVisibility(View.GONE);

            TextView text = findViewById(R.id.signInCardTitle);
            text.setText(R.string.signIn);

            findViewById(R.id.signInSubmitButton).setVisibility(View.VISIBLE);
            findViewById(R.id.startRegisterSubmitButton).setVisibility(View.VISIBLE);
            findViewById(R.id.passwordResetButton).setVisibility(View.VISIBLE);

            findViewById(R.id.registerSubmitButton).setVisibility(View.GONE);
            findViewById(R.id.startSignInButton).setVisibility(View.GONE);
            findViewById(R.id.signInPasswordConfirmationLayout).setVisibility(View.GONE);
            findViewById(R.id.signOutButton).setVisibility(View.GONE);
        }
    }

    private void startCountAnimation(TextView textView, Integer maxPoints) {
        startCountAnimation(textView, maxPoints, "");
    }

    private void startCountAnimation(TextView textView, Integer maxPoints, String preText) {
        ValueAnimator animator = ValueAnimator.ofInt(0, maxPoints);
        animator.setDuration(1500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText(preText + animation.getAnimatedValue().toString());
            }
        });
        animator.start();
    }

    private void updateScore() {
        TextView scoreTextView = findViewById(R.id.mainScreenScoreText);
        TextView emailTextView = findViewById(R.id.mainScreenScoreEmail);
        String email = mAuth.getCurrentUser().getEmail();
        email = email.substring(0, email.indexOf('@')) + ": ";
        emailTextView.setText(email);
        String scoreText = String.valueOf(dbHelper.getAvailablePoints());
        Log.d(TAG, "email: [" + email + "] scoreText: [" + scoreText + "]");
        //scoreTextView.setText(scoreText);
        startCountAnimation(scoreTextView, dbHelper.getAvailablePoints());
    }

    private void updateCategories() {
        List<String> stringsList = new ArrayList<>();
        ArrayList<ArrayList<Integer>> priceList = new ArrayList<>();

        int points = dbHelper.getAvailablePoints();

        //for each entry (name,price,id)
        List<List<String>> list = dbHelper.getAllUnlockableCategories();

        if (list.size() > MAX_CATEGORIES_ON_MAINSCREEN) {
            Log.d(TAG, "updateCategories list > " + MAX_CATEGORIES_ON_MAINSCREEN);
            list = new ArrayList<>(list.subList(0, MAX_CATEGORIES_ON_MAINSCREEN));
        }
        for (List<String> entryList : list) {
            ArrayList<Integer> tempList = new ArrayList<>();
            Integer price = new Integer(entryList.get(1));
            // price - points
            int absoluteDifference = price - points;
            tempList.add(absoluteDifference);
            // how much points of price we do have
            Double percentage = 0.00;
            if (points != 0) {
                percentage = ((double) points) / (double) (price) * 100;
            }
            tempList.add(percentage.intValue());

            if (percentage >= 100.00) {
                unlockCategoryDialog(price, entryList.get(2), entryList.get(0));
                points -= price;
            } else {
                priceList.add(tempList);
                stringsList.add(entryList.get(0));
            }
        }

        progressBarListAdapter = new ProgressBarListAdapter(stringsList, priceList);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(progressBarListAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        String questionsAnswered = dbHelper.getNumCorrectlyAnsweredQuestion() + "/" + dbHelper.getNumUnlockedQuestions();
        Log.d(TAG, "questionsAnswered [" + questionsAnswered + "]");
        questionsAnsweredValue.setText(questionsAnswered);
        String categoriesUnlocked = dbHelper.getNumUnlockedCategories() + "/" + dbHelper.getNumAllCategories();
        Log.d(TAG, "categoriesUnlocked [" + categoriesUnlocked + "]");
        categoriesUnlockedValue.setText(categoriesUnlocked);

        String questionsHighScoreStreakValueString = dbHelper.getHighAnswersScoreStreak().toString();
        questionsHighScoreStreakValue.setText(questionsHighScoreStreakValueString);
        Log.d(TAG, "questionsHighScoreStreakValue [" + questionsHighScoreStreakValueString + "]");
        String questionsHighScoreValueString = dbHelper.getHighPointsScore().toString();
        questionsHighScoreValue.setText(questionsHighScoreValueString);
        Log.d(TAG, "questionsHighScoreValue [" + questionsHighScoreValueString + "]");
    }

    private String getEmail() {
        String email = ((EditText) findViewById(R.id.signInXname)).getText().toString();
        if (email.matches("^x(([a-z]*)|([a-z]*[0-9]*))@mendelu.cz$")) {
            Log.d(TAG, "return " + email);
            return email;
        }
        Log.d(TAG, "return " + email + "@mendelu.cz");
        return email + "@mendelu.cz";
    }

    private String getPassword() {
        return ((EditText) findViewById(R.id.signInPassword)).getText().toString();
    }

    private Boolean validateXname() {
        if (getEmail() != null) {
            if (getEmail().matches("^x(([a-z]*)|([a-z]*[0-9]*))@mendelu.cz$")) {
                return true;
            } else {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.provideValidName),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void sendVerificationEmail() {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.verificationEmail) + " " + MainActivity.this.getEmail(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "sendEmailVerification", task.getException());
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.verificationEmailError),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpRegisterButton(Button registerButoon) {
        registerButoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG,"onClick: Clicked button mainScreen");
                if (validateXname()) {
                    if (MainActivity.this.getPassword() != null && !MainActivity.this.getPassword().isEmpty()) {
                        mAuth.createUserWithEmailAndPassword(MainActivity.this.getEmail(), MainActivity.this.getPassword())
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "createUserWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            MainActivity.this.sendVerificationEmail();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(MainActivity.this, getResources().getString(R.string.accountCreationFailed),
                                                    Toast.LENGTH_SHORT).show();
                                            updateUI(null, registationSequenceStarted);
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(MainActivity.this, "Account creation failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void setUpSignInButton(Button signInButoon) {
        signInButoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked SignIn mainScreen");
                if (validateXname()) {
                    if (MainActivity.this.getPassword() != null && !MainActivity.this.getPassword().isEmpty()) {
                        SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy//hh:mm:ss");
                        String ts = s.format(new Date());
                        Answers.getInstance().logLogin(new LoginEvent().putMethod("Login")
                                .putCustomAttribute("DateTime", ts));
                        mAuth.signInWithEmailAndPassword(MainActivity.this.getEmail(), MainActivity.this.getPassword())
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            logUser();
                                            updateUI(user, registationSequenceStarted);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                                            Toast.makeText(MainActivity.this, getResources().getString(R.string.signInFailed),
                                                    Toast.LENGTH_SHORT).show();
                                            updateUI(null, registationSequenceStarted);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "login faile with:" + e);
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void setUpSignOutButton(Button signOutButoon) {
        signOutButoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG,"onClick: Clicked button mainScreen");
                mAuth.signOut();
                dbHelper.onLogout();
                updateUI(null, registationSequenceStarted);
            }
        });
    }

    private void setUpSendEmailButton(Button sendEmailButton) {
        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: start Email Called.");
                startEmail();
            }
        });
    }

    private void setUpPassworResetButton(Button passwordResetButton) {
        passwordResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: reset Password Called.");
                passwordResetConfirmation();
            }
        });
    }

    private void passwordResetConfirmation() {

        PackageInfo pInfo = null;
        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        customDialog(getResources().getString(R.string.passwordResetDialogTitle)
                , getResources().getString(R.string.passwordResetDialogText)
                , getResources().getString(R.string.passwordResetDialogNegativeAnswer)
                , getResources().getString(R.string.passwordResetDialogPositiveAnswer)
                , false);
    }

    private void sendResetPassword() {
        Log.d(TAG, "sendResetPassword: password reset requested.");
        toastMessage(getResources().getString(R.string.passwordResetToastMessage));
        mAuth.sendPasswordResetEmail(getEmail());
    }

    private void showInfo() {

        PackageInfo pInfo = null;
        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        customDialog(getResources().getString(R.string.infoOnMainscreenTitle)
                , getResources().getString(R.string.infoOnMainscreenText) + " " + version
                , getResources().getString(R.string.sendEmail)
                , getResources().getString(R.string.dismiss_showcase_text)
                , true);

    }

    public void customDialog(String title, String message, final String cancelMethod, final String okMethod, final boolean email) {
        final androidx.appcompat.app.AlertDialog.Builder builderSingle = new androidx.appcompat.app.AlertDialog.Builder(this);
        builderSingle.setTitle(title);
        builderSingle.setMessage(message);
        builderSingle.setNeutralButton(cancelMethod, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (email) {
                    Log.d(TAG, "onClick: start Email Called.");
                    startEmail();
                }
            }
        });

        builderSingle.setPositiveButton(
                okMethod,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!email) {
                            Log.d(TAG, "onClick: sendResetPassword() Called.");
                            sendResetPassword();
                        }
                    }
                });
        builderSingle.show();
    }

    public void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tolas94@gmail.com"});
        String textOfMail = ((EditText) findViewById(R.id.contactAuthorInput)).getText().toString();

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, "graphPef problem [" + mAuth.getUid() + "]");
            if (textOfMail.isEmpty()) {
                textOfMail = mAuth.getCurrentUser().getEmail() + " - Mám problém se ";
            } else {
                textOfMail = mAuth.getCurrentUser().getEmail() + " - " + textOfMail;
            }
            intent.putExtra(Intent.EXTRA_TEXT, textOfMail);
        } else {
            intent.putExtra(Intent.EXTRA_SUBJECT, "graphPef problem - not logged in");
            if (textOfMail.isEmpty()) {
                intent.putExtra(Intent.EXTRA_TEXT, "Mám problém se ");
            } else {
                intent.putExtra(Intent.EXTRA_TEXT, textOfMail);
            }
        }
        startActivity(Intent.createChooser(intent, ""));
    }

    public void unlockCategoryDialog(Integer price, String categoryID, String categoryName) {
        final androidx.appcompat.app.AlertDialog.Builder builderSingle = new androidx.appcompat.app.AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogContent = inflater.inflate(R.layout.category_unlocked_dialog, null);
        builderSingle.setView(dialogContent);
        TextView categoryTextView = dialogContent.findViewById(R.id.categoryUnlockedCategoryText);
        TextView categoryPriceView = dialogContent.findViewById(R.id.categoryUnlockedPriceValue);

        categoryPriceView.setText(price.toString());
        String text = getString(R.string.categoryUnlockTextStart) + " \"" + categoryName + "\"";
        categoryTextView.setText(text);

        builderSingle.setPositiveButton(
                getString(R.string.categoryUnlockedAcknowledge),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dbHelper.unlockCategory(categoryID, price);
                        updateScore();
                        updateCategories();
                    }
                });

        builderSingle.show();
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d(TAG, "Observer update called");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            updateUI(user, registationSequenceStarted);
        } else {
            Log.e(TAG, "update - user == null");
        }
    }
}

