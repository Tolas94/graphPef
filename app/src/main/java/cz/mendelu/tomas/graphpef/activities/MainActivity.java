package cz.mendelu.tomas.graphpef.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import cz.mendelu.tomas.graphpef.R;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Button mainScreenButton = (Button) findViewById(R.id.button3);



        mainScreenButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Log.d(TAG,"onClick: Clicked button mainScreen");
                Intent intent = new Intent(MainActivity.this,GraphMenuListActivity.class);
                startActivity(intent);
            }
        });

        //TODO add signing via FireBase

    }


}
