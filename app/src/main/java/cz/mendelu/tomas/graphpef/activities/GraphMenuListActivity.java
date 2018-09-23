package cz.mendelu.tomas.graphpef.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.helperObjects.StringIntegerPair;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by tomas on 12.09.2018.
 */

public class GraphMenuListActivity extends AppCompatActivity implements Serializable {

    private static final String TAG = "MainActivity";
    ListView graphMenu;

    public enum GraphEnum {
        MarketDS,
        ProductionLimit,
        IndifferentAnalysis,
        CostCurves,
        PerfectMarket
    }
    private HashMap<GraphEnum,Integer> grapLookupImagePair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.choose_graph));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        grapLookupImagePair = new HashMap<>();
        grapLookupImagePair.put(GraphEnum.MarketDS,R.drawable.market_ds);
        grapLookupImagePair.put(GraphEnum.ProductionLimit,R.drawable.ppf_lookup);
        grapLookupImagePair.put(GraphEnum.IndifferentAnalysis,R.drawable.indifferent_curve);
        grapLookupImagePair.put(GraphEnum.CostCurves,R.drawable.cost_curve);
        grapLookupImagePair.put(GraphEnum.PerfectMarket,R.drawable.doko);


        graphMenu = findViewById(R.id.listOfGraphs);

        String localized;

        ArrayList<StringIntegerPair> graphNames = new ArrayList<>();
        //Log.d(TAG,"graphNames init");
        for(GraphEnum graphEnum: GraphEnum.values()){
            Log.d(TAG,"graphNames " + graphEnum.toString());
            localized = getString(getResources().getIdentifier(graphEnum.toString(),"string",getPackageName()));
            graphNames.add(new StringIntegerPair(localized,grapLookupImagePair.get(graphEnum)));
        }


        MenuListAdapter adapter;
        adapter = new MenuListAdapter(GraphMenuListActivity.this, R.layout.graph_menu_item_layout, graphNames);
        graphMenu.setAdapter(adapter);
        graphMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //GraphChooseHelper chooseHelper = new GraphChooseHelper(MainScreenControllerActivity.,(String) parent.getItemAtPosition(position));
                //Log.d(TAG,"OnItemClickListener end");
                Intent intent = new Intent(GraphMenuListActivity.this,MainScreenControllerActivity.class);
                Log.d(TAG,"newGraph " + GraphEnum.values()[position].toString());
                intent.putExtra("GRAPH_KEY", GraphEnum.values()[position].toString());
                startActivity(intent);
            }
        });
        presentShowcaseSequence();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
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
        sequence.addSequenceItem(graphMenu,getString(R.string.graph_list_showcase),getString(R.string.dismiss_showcase_text));
        sequence.start();
    }
}
