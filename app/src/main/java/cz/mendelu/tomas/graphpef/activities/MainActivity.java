package cz.mendelu.tomas.graphpef.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.Serializable;

import cz.mendelu.tomas.graphpef.R;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class MainActivity extends AppCompatActivity implements Serializable{

    private static final String TAG = "MainActivity";
    private Button mainScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mainScreenButton =  findViewById(R.id.button3);
        mainScreenButton.setText(getText(R.string.start_app));

        //TextView textView = findViewById(R.id.mainScreenDisclaimer);
        //textView.setText(getText(R.string.disclaimer_main_page));


        mainScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG,"onClick: Clicked button mainScreen");
                Intent intent = new Intent(MainActivity.this, GraphMenuListActivity.class);
                startActivity(intent);
            }
        });
        presentShowcaseSequence();


        //TODO add signing via FireBase
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


}
