package cz.mendelu.tomas.graphpef.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.fragments.GraphFragment;
import cz.mendelu.tomas.graphpef.fragments.InfoFragment;
import cz.mendelu.tomas.graphpef.fragments.SectionsPagerAdapter;
import cz.mendelu.tomas.graphpef.graphs.DefaultGraph;
import cz.mendelu.tomas.graphpef.graphs.IndifferentAnalysis;
import cz.mendelu.tomas.graphpef.graphs.MarketDS;
import cz.mendelu.tomas.graphpef.graphs.PerfectMarketFirm;
import cz.mendelu.tomas.graphpef.graphs.ProductionLimit;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.interfaces.GraphIfc;

/**
 * Created by tomas on 11.08.2018.
 */

public class MainScreenControllerActivity extends AppCompatActivity{

    private static final String TAG = "MainScreenControllerActivity";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private HashMap<GraphEnum, DefaultGraph> graphsDatabase;

    private static GraphEnum chosenGraph = GraphEnum.ProductionLimit;

    public enum GraphEnum {
        MarketDS,
        ProductionLimit,
        PerfectMarket,
        IndifferentAnalysis,
        MonopolisticMarket,
        Oligopol,
        Monopol,
        AdmMonopol,
        Utility
    }
    public enum LineEnum {
        Demand,
        DemandDefault,
        PriceLevel,
        Price,
        SupplyDefault,
        Supply,
        TotalCost,
        MarginalCost,
        AverageCost,
        TotalRevenue,
        MarginalRevenue,
        AverageRevenue,
        Quantity,
        ProductionCapabilities,
        ProductionCapabilitiesDefault,
        Taxes,
        Equilibrium,
        TotalUtility,
        AverageVariableCost,
        BudgetLine,
        IndifferentCurve
    }

    public enum Direction {
        up,
        down,
        left,
        right
    }

    static double precision = 0.1;
    static int maxDataPoints = 100;

    public static double getPrecision() {
        return precision;
    }

    public static int getMaxDataPoints() {
        return maxDataPoints;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_layout);
        graphsDatabase = new HashMap<>();
        populateGraphDatabase();
        setChosenGraph(getIntent().getExtras().getString("GRAPH_KEY"));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(graphsDatabase.get(chosenGraph).getTitle());
        //actionBar.setTitle(getIntent().getExtras().getString("GRAPH_KEY"));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);


        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //tabLayout.getTabAt(0).setIcon(R.drawable.ic_menu_black_24dp);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_multiline_chart_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_info_black_24dp);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(GraphFragment.newInstance(graphsDatabase.get(chosenGraph)));
        adapter.addFragment(InfoFragment.newInstance(graphsDatabase.get(chosenGraph)));
        viewPager.setAdapter(adapter);
    }

    private void populateGraphDatabase(){

        GraphHelperObject marketDS = new GraphHelperObject();
        GraphHelperObject productionLimit = new GraphHelperObject();
        GraphHelperObject perfectMarketFirm = new GraphHelperObject();
        GraphHelperObject indiferrentAnalysis = new GraphHelperObject();

        marketDS.setTitle("Market - Demand Supply");
        marketDS.setLabelX("Quantity [Units]");
        marketDS.setLabelY("Price [Czk]");
        marketDS.setGraphEnum(GraphEnum.MarketDS);
        marketDS.addToSeries(LineEnum.Supply,   new ArrayList<>(Arrays.asList(1,0)));
        marketDS.addToSeries(LineEnum.Demand,   new ArrayList<>(Arrays.asList(-1,10)));
        marketDS.addToSeries(LineEnum.SupplyDefault,   new ArrayList<>(Arrays.asList(1,0)));
        marketDS.addToSeries(LineEnum.DemandDefault,   new ArrayList<>(Arrays.asList(-1,10)));

        marketDS.setCalculateEqulibrium(true);
        marketDS.setEquilibriumCurves(new ArrayList<>(Arrays.asList(LineEnum.Demand, LineEnum.Supply)));
        marketDS.setDependantCurveOnEquilibrium(new ArrayList<>(Arrays.asList(LineEnum.Price, LineEnum.Quantity)));

        graphsDatabase.put(GraphEnum.MarketDS, new MarketDS(
                new ArrayList<String>(),
                new ArrayList<>(Arrays.asList(LineEnum.Supply,LineEnum.Demand)),
                LineEnum.Supply,
                marketDS.getSeries(),
                new ArrayList<String>(),
                marketDS));


        // ProductionLimit

        productionLimit.setTitle("Production Limit");
        productionLimit.setLabelX("Production of X [Units]");
        productionLimit.setLabelY("Production of Y [Units]");
        productionLimit.setGraphEnum(GraphEnum.ProductionLimit);
        productionLimit.addToSeries(LineEnum.ProductionCapabilities, new ArrayList<>(Arrays.asList(8,8)));
        productionLimit.addToSeries(LineEnum.ProductionCapabilitiesDefault, new ArrayList<>(Arrays.asList(8,8)));
        productionLimit.setCalculateEqulibrium(false);
        graphsDatabase.put(GraphEnum.ProductionLimit,new ProductionLimit(
                new ArrayList<String>(),
                new ArrayList<>(Arrays.asList(LineEnum.ProductionCapabilities)),
                LineEnum.ProductionCapabilities,
                productionLimit.getSeries(),
                new ArrayList<String>(),
                productionLimit));

        // Perfect Market competition

        perfectMarketFirm.setTitle("Perfect Market");
        perfectMarketFirm.setLabelX("Quantity [Units]");
        perfectMarketFirm.setLabelY("Price [Czk]");
        perfectMarketFirm.setGraphEnum(GraphEnum.PerfectMarket);
        perfectMarketFirm.addToSeries(LineEnum.MarginalCost, new ArrayList<>(Arrays.asList(0,-2,0,2,0)));
        perfectMarketFirm.addToSeries(LineEnum.AverageCost, new ArrayList<>(Arrays.asList(1,-2,6,0,1)));
        //perfectMarketFirm.addToSeries(LineEnum.AverageVariableCost, new ArrayList<>(Arrays.asList(3,-10,11,-1,1)));
        perfectMarketFirm.addToSeries(LineEnum.PriceLevel, new ArrayList<>(Arrays.asList(0,0,0,4,0)));

        perfectMarketFirm.setCalculateEqulibrium(true);

        ArrayList<LineEnum> equilibriumCurves2 = new ArrayList<>();
        equilibriumCurves2.add(LineEnum.MarginalCost);
        equilibriumCurves2.add(LineEnum.AverageCost);
        perfectMarketFirm.setEquilibriumCurves(equilibriumCurves2);

        perfectMarketFirm.setDependantCurveOnEquilibrium(new ArrayList<>(Arrays.asList(LineEnum.Price, LineEnum.Quantity)));
        HashMap<MainScreenControllerActivity.LineEnum, ArrayList<MainScreenControllerActivity.LineEnum>> hashMap = new HashMap<>();
        hashMap.put(LineEnum.AverageCost, new ArrayList<>(Arrays.asList(LineEnum.MarginalCost)));
        perfectMarketFirm.setDependantCurveOnCurve(hashMap);

        graphsDatabase.put(GraphEnum.PerfectMarket,new PerfectMarketFirm(
                new ArrayList<String>(),
                new ArrayList<>(Arrays.asList(LineEnum.PriceLevel,LineEnum.AverageCost)),
                LineEnum.PriceLevel,
                perfectMarketFirm.getSeries(),
                new ArrayList<String>(),
                perfectMarketFirm));


        indiferrentAnalysis.setTitle("Indifferent Analysis");
        indiferrentAnalysis.setLabelX("estate X [Units]");
        indiferrentAnalysis.setLabelY("estate Y [Units]");
        indiferrentAnalysis.setGraphEnum(GraphEnum.IndifferentAnalysis);
        indiferrentAnalysis.addToSeries(LineEnum.BudgetLine, new ArrayList<>(Arrays.asList(8,8,0)));
        indiferrentAnalysis.addToSeries(LineEnum.IndifferentCurve, new ArrayList<>(Arrays.asList(3,3,1)));
        indiferrentAnalysis.setCalculateEqulibrium(true);
        indiferrentAnalysis.setEquilibriumCurves(new ArrayList<>(Arrays.asList(LineEnum.BudgetLine, LineEnum.IndifferentCurve)));

        graphsDatabase.put(GraphEnum.IndifferentAnalysis, new IndifferentAnalysis(
                new ArrayList<String>(),//texty
                new ArrayList<LineEnum>(Arrays.asList(LineEnum.BudgetLine,LineEnum.IndifferentCurve)), //krivky na posun
                LineEnum.BudgetLine,
                indiferrentAnalysis.getSeries(),
                new ArrayList<String>(),
                indiferrentAnalysis));
    }

    //TODO show chosen graph in menu fragment
    public static GraphEnum getChosenGraph() {
        return chosenGraph;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public static void setChosenGraph(String string){
        Log.d(TAG, "setChosenGraph " + string);
        chosenGraph = GraphEnum.valueOf(string);
    }

    public void onChosenGraphChange() {
        Log.d(TAG, "onChosenGraphChange");
        if (graphsDatabase != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            InfoFragment infoFragment = InfoFragment.newInstance(graphsDatabase.get(chosenGraph));
            ft.replace(R.id.info_fragment,infoFragment);

            GraphFragment graphFragment = GraphFragment.newInstance(graphsDatabase.get(chosenGraph));
            ft.replace(R.id.graph_fragment,graphFragment);


            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }else{
            Log.d(TAG, "onChosenGraphChange: null ");
        }
    }
}
