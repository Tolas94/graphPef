package cz.mendelu.tomas.graphpef.activities;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.helperObjects.GraphChooseHelper;

/**
 * Created by tomas on 12.09.2018.
 */

public class GraphMenuListActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public enum GraphEnum {
        MarketDS,
        ProductionLimit,
        PerfectMarket,
        MonopolisticMarket,
        Oligopol,
        Monopol,
        AdmMonopol,
        Utility
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Choose graph");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        ListView graphMenu = findViewById(R.id.listOfGraphs);

        ArrayList<String> graphNames = new ArrayList<>();
        Log.d(TAG,"graphNames init");
        for(GraphEnum graphEnum: GraphEnum.values()){
            Log.d(TAG,"graphNames " + graphEnum.toString());
            graphNames.add(graphEnum.toString());
        }
        final Context activity = this.getBaseContext();

        MenuListAdapter adapter;
        adapter = new MenuListAdapter(GraphMenuListActivity.this, R.layout.graph_menu_item_layout, graphNames);
        graphMenu.setAdapter(adapter);
        graphMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //GraphChooseHelper chooseHelper = new GraphChooseHelper(MainScreenControllerActivity.,(String) parent.getItemAtPosition(position));
                //Log.d(TAG,"OnItemClickListener end");
                Intent intent = new Intent(GraphMenuListActivity.this,MainScreenControllerActivity.class);
                Log.d(TAG,"newGraph " + (String) parent.getItemAtPosition(position));
                intent.putExtra("GRAPH_KEY", (String) parent.getItemAtPosition(position));
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
