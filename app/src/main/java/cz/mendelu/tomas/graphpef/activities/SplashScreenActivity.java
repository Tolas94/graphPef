package cz.mendelu.tomas.graphpef.activities;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.adapters.ProgressBarListAdapter;
import cz.mendelu.tomas.graphpef.database.DBHelper;
import io.fabric.sdk.android.Fabric;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class SplashScreenActivity extends AppCompatActivity implements Serializable, Observer {

    private static final String TAG = "SplashScreenActivity";
    private static final int QUIZ_REQUEST_CODE = 1;
    private static final int MAX_CATEGORIES_ON_MAINSCREEN = 3;

    private FirebaseAuth mAuth;
    private Button startGraphSection;
    //TODO: check startSignInButton directly in update UI and not as parameter
    private boolean registrationSequenceStarted;
    private boolean userOptInFlag = false;
    private boolean userUnlockCategoryDialogOpened = false;
    private boolean presentShowcaseStarted = false;

    private RecyclerView recyclerView;
    private DBHelper dbHelper;

    private TextView categoriesUnlockedValue, questionsAnsweredValue, questionsHighScoreStreakValue, questionsHighScoreValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        startGraphSection = findViewById(R.id.startAppButton);

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
        startGraphSection.setText(R.string.show_graph_list);
        categoriesUnlockedTitle.setText(R.string.mainScreenCategoriesUnlocked);
        questionsAnsweredTitle.setText(R.string.mainScreenQuestionAnswered);
        questionsHighScoreStreakTitle.setText(R.string.mainScreenHighScoreStreak);
        questionsHighScoreTitle.setText(R.string.mainScreenHighScore);
        startSignInButton.setText(R.string.signIn);
        EditText passwordText = findViewById(R.id.signInPassword);

        startRegisterButton.setOnClickListener(v -> {
            registrationSequenceStarted = true;
            updateUI(null, registrationSequenceStarted);
        });

        startSignInButton.setOnClickListener(v -> {
            registrationSequenceStarted = false;
            updateUI(null, registrationSequenceStarted);
        });

        passwordText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signInButton.performClick();
                return true;
            }
            return false;
        });


        //TextView appNameText = findViewById(R.id.mainScreenDisclaimer);
        //appNameText.setText(getText(R.string.disclaimer_main_page));
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
            dbHelper.addObserver(this);
        }
        logUser();
        if (!userOptInFlag) {
            checkOptInValue();
        }

        startGraphSection.setOnClickListener(view -> {
            Log.d(TAG, "onClick: Clicked button mainScreen");
            if (mAuth.getCurrentUser() != null) {
                mAuth.getCurrentUser().reload();
                if (mAuth.getCurrentUser().isEmailVerified()) {
                    Log.d(TAG, "onClick: Clicked button mainScreen - email verified");
                } else {
                    Log.d(TAG, "onClick: Clicked button mainScreen - email NOT verified");
                    Toast.makeText(SplashScreenActivity.this, getResources().getString(R.string.emailNotVerified),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                toastMessage(getResources().getString(R.string.signInHint));
            }
            Intent intent = new Intent(SplashScreenActivity.this, GraphMenuListActivity.class);
            startActivity(intent);
        });
        updateCategories();
    }

    private void presentShowcaseSequence() {
        Log.d(TAG, "presentShowcaseSequence started");
        if (presentShowcaseStarted) {
            Log.e(TAG, "presentShowcaseStarted true");
            return;
        }
        presentShowcaseStarted = true;

        RelativeLayout score = findViewById(R.id.mainScreenScoreLayout);
        RelativeLayout category = findViewById(R.id.mainScreenScoreUnlockableCategoriesRecycleViewLayout);


        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, TAG);
        sequence.setOnItemShownListener((itemView, position) -> {
        });
        sequence.setConfig(config);
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(score)
                        .setDismissText(getString(R.string.score_showcase))
                        .setContentText(getString(R.string.dismiss_showcase_text))
                        .withRectangleShape(true)
                        .setDismissOnTouch(true)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(category)
                        .setDismissText(getString(R.string.unlock_category_showcase))
                        .setContentText(getString(R.string.dismiss_showcase_text))
                        .withRectangleShape(true)
                        .setDismissOnTouch(true)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(startGraphSection)
                        .setDismissText(getString(R.string.show_graph_list_showcase))
                        .setContentText(getString(R.string.dismiss_showcase_text))
                        .withRectangleShape(true)
                        .setDismissOnTouch(true)
                        .build()
        );

        int pos = findViewById(R.id.startAppLayout).getTop();
        ScrollView scrollView = findViewById(R.id.splashScreenScrollView);
        sequence.setOnItemDismissedListener((itemView, position) -> {

            if (position == 1) {
                scrollView.scrollTo(0, pos);
            }

        });
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
                    dbHelper = new DBHelper(this);
                    dbHelper.addObserver(this);
                }
                userOptInFlag = dbHelper.getUserOptInFlag(user.getUid());
                dbHelper.createUserRef(user.getUid(), user.getEmail());
            } else {
                Log.d(TAG, "user == null");
            }
        } else {
            Log.d(TAG, "mAuth == null");
        }
    }

    private void checkOptInValue() {
        if (mAuth != null && mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid() != null) {
            customDialog(getString(R.string.firebaseUserOptInTitle),
                    getString(R.string.firebaseUserOptInText),
                    getString(R.string.firebaseUserOptInForbid),
                    getString(R.string.firebaseUserOptInAllow),
                    false,
                    true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser, registrationSequenceStarted);
    }

    private void startQuiz() {
        Intent intent = new Intent(SplashScreenActivity.this, QuizControllerActivity.class);
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
     * update UI changes main splash screen the
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
        animator.addUpdateListener(animation -> textView.setText(preText + animation.getAnimatedValue().toString()));
        animator.start();
    }

    private void updateScore() {
        TextView scoreTextView = findViewById(R.id.mainScreenScoreText);
        TextView emailTextView = findViewById(R.id.mainScreenScoreEmail);
        String email = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        assert email != null;
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
            Integer price = Integer.valueOf(entryList.get(1));
            // price - points
            int absoluteDifference = price - points;
            tempList.add(absoluteDifference);
            // how much points of price we do have
            double percentage = 0.00;
            if (points != 0) {
                percentage = ((double) points) / (double) (price) * 100;
            }
            tempList.add((int) percentage);

            if (percentage >= 100.00 && !userUnlockCategoryDialogOpened) {
                userUnlockCategoryDialogOpened = true;
                unlockCategoryDialog(price, entryList.get(2), entryList.get(0));
                points = dbHelper.getAvailablePoints();
            } else {
                priceList.add(tempList);
                stringsList.add(entryList.get(0));
            }
        }

        ProgressBarListAdapter progressBarListAdapter = new ProgressBarListAdapter(stringsList, priceList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
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
                Toast.makeText(SplashScreenActivity.this, getResources().getString(R.string.provideValidName),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void sendVerificationEmail() {
        Objects.requireNonNull(mAuth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(this, (OnCompleteListener) task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SplashScreenActivity.this,
                        getResources().getString(R.string.verificationEmail) + " " + SplashScreenActivity.this.getEmail(),
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "sendEmailVerification", task.getException());
                Toast.makeText(SplashScreenActivity.this,
                        getResources().getString(R.string.verificationEmailError),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpRegisterButton(Button registerButoon) {
        registerButoon.setOnClickListener(view -> {
            //Log.d(TAG,"onClick: Clicked button mainScreen");
            if (validateXname()) {
                SplashScreenActivity.this.getPassword();
                if (!SplashScreenActivity.this.getPassword().isEmpty()) {
                    mAuth.createUserWithEmailAndPassword(SplashScreenActivity.this.getEmail(), SplashScreenActivity.this.getPassword())
                            .addOnCompleteListener(SplashScreenActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    SplashScreenActivity.this.sendVerificationEmail();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SplashScreenActivity.this, getResources().getString(R.string.accountCreationFailed),
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null, registrationSequenceStarted);
                                }
                            });
                } else {
                    Toast.makeText(SplashScreenActivity.this, "Account creation failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpSignInButton(Button signInButoon) {
        signInButoon.setOnClickListener(view -> {
            Log.d(TAG, "onClick: Clicked SignIn mainScreen");
            if (validateXname()) {
                SplashScreenActivity.this.getPassword();
                if (!SplashScreenActivity.this.getPassword().isEmpty()) {
                    SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy//hh:mm:ss");
                    String ts = s.format(new Date());
                    Answers.getInstance().logLogin(new LoginEvent().putMethod("Login")
                            .putCustomAttribute("DateTime", ts));
                    mAuth.signInWithEmailAndPassword(SplashScreenActivity.this.getEmail(), SplashScreenActivity.this.getPassword())
                            .addOnCompleteListener(SplashScreenActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    logUser();
                                    updateUI(user, registrationSequenceStarted);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SplashScreenActivity.this, getResources().getString(R.string.signInFailed),
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null, registrationSequenceStarted);
                                }
                            }).addOnFailureListener(e -> Log.e(TAG, "login faile with:" + e));
                } else {
                    Toast.makeText(SplashScreenActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpSignOutButton(Button signOutButoon) {
        signOutButoon.setOnClickListener(view -> {
            //Log.d(TAG,"onClick: Clicked button mainScreen");
            mAuth.signOut();
            dbHelper.onLogout();
            updateUI(null, registrationSequenceStarted);
        });
    }

    private void setUpSendEmailButton(Button sendEmailButton) {
        sendEmailButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: start Email Called.");
            startEmail();
        });
    }

    private void setUpPassworResetButton(Button passwordResetButton) {
        passwordResetButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: reset Password Called.");
            passwordResetConfirmation();
        });
    }

    private void passwordResetConfirmation() {
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
        String version = "";
        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        customDialog(getResources().getString(R.string.infoOnMainscreenTitle)
                , getResources().getString(R.string.infoOnMainscreenText) + " " + version
                , getResources().getString(R.string.firebaseUserOptInTitle)
                , getResources().getString(R.string.dismiss_showcase_text)
                , true);

    }

    public void customDialog(String title, String message, final String cancelMethod, final String okMethod, final boolean dataProtection, final boolean dataProtectionDialog) {
        final androidx.appcompat.app.AlertDialog.Builder builderSingle = new androidx.appcompat.app.AlertDialog.Builder(this);
        builderSingle.setTitle(title);
        builderSingle.setMessage(message);
        builderSingle.setNeutralButton(cancelMethod, (dialog, which) -> {
            Log.d(TAG, "customDialog: neutral button");
            if (dataProtectionDialog) {
                Log.d(TAG, "onClick: dataprotectionDialog false");
                changeGDPRpolicy(false);
            }
            if (dataProtection) {
                Log.d(TAG, "onClick: CheckOptInValueCalled.");
                checkOptInValue();
            }
        });

        builderSingle.setPositiveButton(
                okMethod,
                (dialogInterface, i) -> {
                    Log.d(TAG, "customDialog: positive button");
                    if (dataProtectionDialog) {
                        Log.d(TAG, "onClick: dataprotectionDialog true");
                        changeGDPRpolicy(true);
                    } else if (!dataProtection) {
                        Log.d(TAG, "onClick: sendResetPassword() Called.");
                        sendResetPassword();
                    }

                });
        builderSingle.show();
    }

    private void changeGDPRpolicy(boolean allow) {

        dbHelper.updateUserOptInFlag(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), allow);
        if (allow) {
            //Only initialize Fabric is user opt-in is true
            Fabric.with(this, new Crashlytics());
            Fabric.with(this, new Answers());
            //saveToDatabase
        } else {
            //TODO disallow
        }
    }

    public void customDialog(String title, String message, final String cancelMethod, final String okMethod, final boolean dataProtection) {
        customDialog(title, message, cancelMethod, okMethod, dataProtection, false);
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
                (dialogInterface, i) -> {
                    userUnlockCategoryDialogOpened = false;
                    dbHelper.unlockCategory(categoryID, price);
                    updateScore();
                    updateCategories();
                });
        builderSingle.setOnCancelListener(dialogInterface -> userUnlockCategoryDialogOpened = false);
        builderSingle.show();
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d(TAG, "Observer update called");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            updateUI(user, registrationSequenceStarted);
        } else {
            Log.e(TAG, "update - user == null");
        }
    }
}

