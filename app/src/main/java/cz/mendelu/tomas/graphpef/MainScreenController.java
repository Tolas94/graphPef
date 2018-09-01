package cz.mendelu.tomas.graphpef;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.graphs.DefaultGraph;
import cz.mendelu.tomas.graphpef.graphs.MarketDS;
import cz.mendelu.tomas.graphpef.graphs.ProductionLimit;

/**
 * Created by tomas on 11.08.2018.
 */

public class MainScreenController extends AppCompatActivity{

    private static final String TAG = "MainScreenController";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private HashMap<GraphEnum, DefaultGraph> graphsDatabase;

    private static GraphEnum chosenGraph = GraphEnum.ProductionLimit;

    private static Boolean graphChanged = false;

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
    public enum LineEnum {
        Demand,
        Price,
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
        TotalUtility
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

    public DefaultGraph getGraphByEnum(GraphEnum graphEnum){
        return graphsDatabase.get(graphEnum);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_layout);
        graphsDatabase = new HashMap<>();
        populateGraphDatabase();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);


        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_menu_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_multiline_chart_black_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_info_black_24dp);

        Log.d(TAG, "onCreate" + graphChanged.toString());
        if (graphChanged == true){
            graphChanged = false;
            onChosenGraphChange();
        }

    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MenuFragment());
        adapter.addFragment(new GraphFragment());
        adapter.addFragment(new InfoFragment());
        viewPager.setAdapter(adapter);
    }

    private void populateGraphDatabase(){
        //line dependancy
        /*
        for(MainScreenController.LineEnum lineEnum: MainScreenController.LineEnum.values()){
            mlineDependancy.put(lineEnum,new ArrayList<LineEnum>());
        }
*/
        GraphHelperObject marketDS = new GraphHelperObject();
        GraphHelperObject productionLimit = new GraphHelperObject();
        GraphHelperObject perfectMarketFirm = new GraphHelperObject();

        marketDS.setTitle("Market - Demand Supply");
        marketDS.setLabelX("Quantity");
        marketDS.setLabelY("Price");
        marketDS.setGraphEnum(GraphEnum.MarketDS);
        marketDS.addToSeries(LineEnum.Supply,   new ArrayList<>(Arrays.asList(0,0,1,0)));
        marketDS.addToSeries(LineEnum.Demand,   new ArrayList<>(Arrays.asList(0,0,-1,10)));
        marketDS.addToSeries(LineEnum.Price,    new ArrayList<>(Arrays.asList(0,0,0,5)));


        marketDS.setCalculateEqulibrium(true);
        ArrayList<LineEnum> equilibriumCurves = new ArrayList<>();
        equilibriumCurves.add(LineEnum.Demand);
        equilibriumCurves.add(LineEnum.Supply);
        marketDS.setEquilibriumCurves(equilibriumCurves);

        ArrayList<LineEnum> equilibriumDependantCurves = new ArrayList<>();
        equilibriumDependantCurves.add(LineEnum.Price);
        marketDS.setDependantCurveOnEquilibrium(equilibriumDependantCurves);

        graphsDatabase.put(GraphEnum.MarketDS, new MarketDS(
                new ArrayList<String>(),
                new ArrayList<>(Arrays.asList(LineEnum.Supply,LineEnum.Demand)),
                LineEnum.Supply,
                marketDS.getSeries(),
                new ArrayList<String>(),
                marketDS));


        // ProductionLimit

        productionLimit.setTitle("Production Limit");
        productionLimit.setLabelX("Production of Unit X");
        productionLimit.setLabelY("Production of Unit Y");
        productionLimit.setGraphEnum(GraphEnum.ProductionLimit);
        productionLimit.addToSeries(LineEnum.ProductionCapabilities, new ArrayList<>(Arrays.asList(0,0,0,0)));
        productionLimit.addToSeries(LineEnum.ProductionCapabilitiesDefault, new ArrayList<>(Arrays.asList(0,0,0,0)));
        productionLimit.setCalculateEqulibrium(false);
        graphsDatabase.put(GraphEnum.ProductionLimit,new ProductionLimit(
                new ArrayList<String>(),
                new ArrayList<>(Arrays.asList(LineEnum.ProductionCapabilities)),
                LineEnum.ProductionCapabilities,
                productionLimit.getSeries(),
                new ArrayList<String>(),
                productionLimit));
/*
        // Perfect Market competition

        perfectMarket.setTitle("Perfect Market");
        perfectMarket.setLabelX("Quantity");
        perfectMarket.setLabelY("Price");
        perfectMarket.setGraphEnum(GraphEnum.PerfectMarket);
        perfectMarket.addToSeries(LineEnum.MarginalCost,new ArrayList<>(Arrays.asList(0,0,0,0)));
        perfectMarket.addToSeries(LineEnum.AverageCost,new ArrayList<>(Arrays.asList(0,0,0,0)));
*/
    }

    //TODO show chosen graph in menu fragment
    public static GraphEnum getChosenGraph() {
        return chosenGraph;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public static void setChosenGraph(String string){
        Log.d(TAG, "setChosenGraph" + string);
        chosenGraph = GraphEnum.valueOf(string);
        graphChanged = true;
    }

    public void onChosenGraphChange() {
        Log.d(TAG, "onChosenGraphChange");
        if (graphsDatabase != null && graphChanged) {

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            GraphFragment graphFragment = GraphFragment.newInstance(graphsDatabase.get(chosenGraph));
            ft.replace(R.id.graph_fragment,graphFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }else{
            Log.d(TAG, "onChosenGraphChange: null or graphChanged == false");
        }

    }

}
