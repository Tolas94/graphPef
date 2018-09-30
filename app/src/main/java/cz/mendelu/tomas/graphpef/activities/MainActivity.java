package cz.mendelu.tomas.graphpef.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.mendelu.tomas.graphpef.R;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements Serializable{

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private Button mainScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean userOptInFlag = CheckOptInValue();

        if (userOptInFlag == true){
            //Only initialize Fabtric is user opt-in is true
            Fabric.with(this, new Crashlytics());
            Fabric.with(this, new Answers());
        }
        mAuth = FirebaseAuth.getInstance();

        ((EditText)findViewById(R.id.signInPassword)).setHint(getResources().getString(R.string.password));
        ((EditText)findViewById(R.id.signInXname)).setHint(getResources().getString(R.string.xname));

        Button signInButton = findViewById(R.id.signInSubmitButton);
        Button registerButton = findViewById(R.id.registerSubmitButton);
        Button signOutButton = findViewById(R.id.signOutButton);
        Button resendEmai = findViewById(R.id.resendMailButton);

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
        setUpResendEmailButton(resendEmai);
        resendEmai.setText(R.string.resendEmail);

        mainScreenButton =  findViewById(R.id.startAppButton);
        mainScreenButton.setText(getText(R.string.start_app));

        //TextView appNameText = findViewById(R.id.mainScreenDisclaimer);
        //appNameText.setText(getText(R.string.disclaimer_main_page));

        logUser();

        mainScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG,"onClick: Clicked button mainScreen");
                if ( mAuth.getCurrentUser().isEmailVerified() ){
                    Intent intent = new Intent(MainActivity.this, GraphMenuListActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.emailNotVerified),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void presentShowcaseSequence() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200); // half second between each showcase view
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this,TAG);
        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
            @Override
            public void onShow(MaterialShowcaseView itemView, int position) {
            }
        });
        sequence.setConfig(config);
        sequence.addSequenceItem(mainScreenButton,getString(R.string.disclaimer_main_page),getString(R.string.dismiss_showcase_text));
        sequence.start();
    }

    private void logUser() {
        if (mAuth != null){
            FirebaseUser user = mAuth.getCurrentUser();
            if ( user != null ){
                Crashlytics.setUserIdentifier(user.getUid());
                Crashlytics.setUserEmail(user.getEmail());
            }
        }
    }

    boolean CheckOptInValue(){
        //check opt-in value
        //return true; //if user opted-in
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user){
        if (user != null){
            findViewById(R.id.signInLayout).setVisibility(View.GONE);
            findViewById(R.id.signedLayout).setVisibility(View.VISIBLE);
            presentShowcaseSequence();
        }else{
            findViewById(R.id.signInLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.signedLayout).setVisibility(View.GONE);
        }
    }

    private String getEmail(){
        String email = ((EditText)findViewById(R.id.signInXname)).getText().toString();
        if (email.matches("^x(([a-z]*)|([a-z]*[0-9]*))@mendelu.cz$")){
            Log.d(TAG,"return " + email);
            return email;
        }
        Log.d(TAG,"return " + email + "@mendelu.cz");
        return email + "@mendelu.cz";
    }

    private String getPassword(){
        return ((EditText)findViewById(R.id.signInPassword)).getText().toString();
    }

    private Boolean validateXname(){
        if (getEmail() != null){
            if( getEmail().matches("^x(([a-z]*)|([a-z]*[0-9]*))@mendelu.cz$")){
                return true;
            }else{
                Toast.makeText(MainActivity.this, getResources().getString(R.string.provideValidName),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }
    private void sendVerificationEmail(){
       mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.verificationEmail) + " " +  MainActivity.this.getEmail(),
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

    private void setUpRegisterButton(Button registerButoon){
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
                                            updateUI(null);
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(MainActivity.this, "Account creationn failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void setUpSignInButton(Button signInButoon){
        signInButoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG,"onClick: Clicked button mainScreen");
                if (validateXname()){
                    if (MainActivity.this.getPassword() != null && !MainActivity.this.getPassword().isEmpty()){
                        SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy//hh:mm:ss");
                        String ts = s.format(new Date());
                        Answers.getInstance().logLogin(new LoginEvent().putMethod("Login").putCustomAttribute("DateTime", ts));
                        mAuth.signInWithEmailAndPassword(MainActivity.this.getEmail(), MainActivity.this.getPassword())
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            updateUI(user);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                                            Toast.makeText(MainActivity.this, getResources().getString(R.string.signInFailed),
                                                    Toast.LENGTH_SHORT).show();
                                            updateUI(null);
                                        }
                                    }
                                });
                    }else{
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void setUpSignOutButton(Button signOutButoon){
        signOutButoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG,"onClick: Clicked button mainScreen");
                mAuth.signOut();
                updateUI(null);
            }
        });
    }

    private void setUpResendEmailButton(Button resendEmailButton){
        //TODO resend email verification
    }

    //TODO password reset

    private void showInfo(){

        PackageInfo pInfo = null;
        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
            customDialog(getResources().getString(R.string.infoOnMainscreenTitle)
                    ,getResources().getString(R.string.infoOnMainscreenText) + " " + version
                    ,"ok"
                    , "cancel");

    }

    public void customDialog(String title, String message, final String cancelMethod, final String okMethod){
        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setTitle(title);
        builderSingle.setMessage(message);
        builderSingle.setNeutralButton("Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick: start Email Called.");
                startEmail();
            }
        });

        builderSingle.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: OK Called.");
                    }
                });
        builderSingle.show();
    }

    public void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void startEmail(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "tolas94@gmail.com" });
        if(mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null){
            intent.putExtra(Intent.EXTRA_SUBJECT, "graphPef problem [" + mAuth.getUid() + "]");
            intent.putExtra(Intent.EXTRA_TEXT, mAuth.getCurrentUser().getEmail()
                    //TODO add info
                    + " - Mám problém se ");
        }else{
            intent.putExtra(Intent.EXTRA_SUBJECT, "graphPef problem - not logged in");
            intent.putExtra(Intent.EXTRA_TEXT, "Mám problém se ");
        }
        startActivity(Intent.createChooser(intent, ""));
    }
}

