package cz.mendelu.tomas.graphpef;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;




public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Button mainScreen = (Button) findViewById(R.id.button3);



        mainScreen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: Clicked button mainScreen");
                Intent intent = new Intent(MainActivity.this,MainScreenController.class);
                startActivity(intent);
            }
        });

    }


}
