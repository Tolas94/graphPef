package cz.mendelu.tomas.graphpef.activities;


import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.adapters.MenuListAdapter;
import cz.mendelu.tomas.graphpef.helperObjects.StringIntegerPair;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by tomas on 12.09.2018.
 */

public class GraphMenuListActivity extends AppCompatActivity implements Serializable {

    private static final String TAG = "GraphMenuListActivity";
    private RecyclerView graphMenu;
    private MenuListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public enum GraphEnum {
        ProductionLimit,
        MarketDS,
        IndifferentAnalysis,
        CostCurves,
        PerfectMarket,
        MonopolisticMarket
    }

    private HashMap<GraphEnum, Integer> grapLookupImagePair;
    private HashMap<GraphEnum, ArrayList<String>> grapLookupStringsPair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.choose_graph));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        popuplateImages();
        populateStrings();

        graphMenu = findViewById(R.id.listOfGraphs);

        ArrayList<StringIntegerPair> graphNames = new ArrayList<>();
        Log.d(TAG, "graphNames init");
        for (GraphEnum graphEnum : GraphEnum.values()) {
            Log.d(TAG, "graphNames " + graphEnum.toString());
            graphNames.add(new StringIntegerPair(grapLookupStringsPair.get(graphEnum), grapLookupImagePair.get(graphEnum)));
        }
        mAdapter = new MenuListAdapter(graphNames);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        graphMenu.setLayoutManager(mLayoutManager);
        graphMenu.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
        Log.d(TAG, "showcase");

        if (mLayoutManager != null) {
            if (mLayoutManager.getChildCount() > 2) {
                sequence.addSequenceItem(
                        new MaterialShowcaseView.Builder(this)
                                .setTarget(mLayoutManager.findViewByPosition(1).findViewById(R.id.menu_item_relative_parent))
                                .setDismissText(getString(R.string.graph_list_showcase))
                                .setContentText(getString(R.string.dismiss_showcase_text))
                                .withRectangleShape(true)
                                .setDismissOnTouch(true)
                                .build()
                );
            }
        }

        sequence.start();
    }

    private void popuplateImages() {
        grapLookupImagePair = new HashMap<>();
        grapLookupImagePair.put(GraphEnum.MarketDS, R.drawable.market_ds);
        grapLookupImagePair.put(GraphEnum.ProductionLimit, R.drawable.ppf_lookup);
        grapLookupImagePair.put(GraphEnum.IndifferentAnalysis, R.drawable.indifferent_curve);
        grapLookupImagePair.put(GraphEnum.CostCurves, R.drawable.cost_curve);
        grapLookupImagePair.put(GraphEnum.PerfectMarket, R.drawable.doko);
        grapLookupImagePair.put(GraphEnum.MonopolisticMarket, R.drawable.monopolistic);
    }

    private void populateStrings() {
        grapLookupStringsPair = new HashMap<>();

        grapLookupStringsPair.put(GraphEnum.ProductionLimit, new ArrayList<>(Arrays.asList(getString(getResources().getIdentifier(GraphEnum.ProductionLimit.toString(), "string", getPackageName())), getResources().getString(R.string.mi1), "Cvičení 2")));
        grapLookupStringsPair.put(GraphEnum.MarketDS, new ArrayList<>(Arrays.asList(getString(getResources().getIdentifier(GraphEnum.MarketDS.toString(), "string", getPackageName())), getResources().getString(R.string.mi1), "Cvičení 3")));
        grapLookupStringsPair.put(GraphEnum.IndifferentAnalysis, new ArrayList<>(Arrays.asList(getString(getResources().getIdentifier(GraphEnum.IndifferentAnalysis.toString(), "string", getPackageName())), getResources().getString(R.string.mi1), "Cvičení 4")));
        grapLookupStringsPair.put(GraphEnum.CostCurves, new ArrayList<>(Arrays.asList(getString(getResources().getIdentifier(GraphEnum.CostCurves.toString(), "string", getPackageName())), getResources().getString(R.string.mi1), "Cvičení 5")));
        grapLookupStringsPair.put(GraphEnum.PerfectMarket, new ArrayList<>(Arrays.asList(getString(getResources().getIdentifier(GraphEnum.PerfectMarket.toString(), "string", getPackageName())), getResources().getString(R.string.mi1), "Cvičení 6-7")));

        grapLookupStringsPair.put(GraphEnum.MonopolisticMarket, new ArrayList<>(Arrays.asList(getString(getResources().getIdentifier(GraphEnum.MonopolisticMarket.toString(), "string", getPackageName())), getResources().getString(R.string.mi1), "Cvičení 8")));
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        presentShowcaseSequence();
    }
}
