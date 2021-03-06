package cz.mendelu.tomas.graphpef.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.adapters.SectionsPagerAdapter;
import cz.mendelu.tomas.graphpef.fragments.GraphFragment;
import cz.mendelu.tomas.graphpef.fragments.InfoFragment;
import cz.mendelu.tomas.graphpef.graphs.CostCurves;
import cz.mendelu.tomas.graphpef.graphs.DefaultGraph;
import cz.mendelu.tomas.graphpef.graphs.IndifferentAnalysis;
import cz.mendelu.tomas.graphpef.graphs.MarketDS;
import cz.mendelu.tomas.graphpef.graphs.MonopolisticMarketFirm;
import cz.mendelu.tomas.graphpef.graphs.PerfectMarketFirm;
import cz.mendelu.tomas.graphpef.graphs.ProductionLimit;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;

/**
 * Created by tomas on 11.08.2018.
 */

public class GraphControllerActivity extends AppCompatActivity implements Serializable {

    private static final String TAG = "MainScreenController";

    //private SectionsPagerAdapter mSectionsPagerAdapter;

    private HashMap<GraphEnum, DefaultGraph> graphsDatabase;

    private static GraphEnum chosenGraph;

    public enum GraphEnum {
        ProductionLimit,
        MarketDS,
        PerfectMarket,
        CostCurves,
        IndifferentAnalysis,
        MonopolisticMarket,
        /*MonopolisticMarketLongTerm,
        Oligopol,
        Monopol,
        AdmMonopol,
        Utility*/
    }
    public enum LineEnum {
        Demand,
        DemandDefault,
        IndividualDemand,
        PriceLevel,
        Price,
        SupplyDefault,
        Supply,
        //TotalCost,
        MarginalCost,
        AverageCost,
        //FixedCost,
        //VariableCost,
        //TotalRevenue,
        MarginalRevenue,
        //AverageRevenue,
        Quantity,
        ProductionCapabilities,
        ProductionCapabilitiesDefault,
        //Taxes,
        Equilibrium,
        //TotalUtility,
        AverageVariableCost,
        BudgetLine,
        IndifferentCurve,
    }
    //public static HashMap<LineEnum,LineEnum> lineLabels;

    public enum Direction {
        up,
        down,
        left,
        right
    }

    static double precision = 0.05;
    static int maxDataPoints = 200;

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

        setChosenGraph(Objects.requireNonNull(getIntent().getExtras()).getString("GRAPH_KEY"));
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(Objects.requireNonNull(graphsDatabase.get(chosenGraph)).getTitle());
        //actionBar.setTitle(getIntent().getExtras().getString("GRAPH_KEY"));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);


        //TabLayout tabLayout = findViewById(R.id.tabs);
        //tabLayout.setupWithViewPager(mViewPager);

        //tabLayout.getTabAt(0).setIcon(R.drawable.ic_menu_black_24dp);
        //tabLayout.getTabAt(0).setIcon(R.drawable.ic_multiline_chart_black_24dp);
        //tabLayout.getTabAt(0).setText(R.string.graph);
        //tabLayout.setTabTextColors(getColor(R.color.colorPrimaryWhite),getColor(R.color.colorPrimaryDark));
        //tabLayout.getTabAt(1).setIcon(R.drawable.ic_info_black_24dp);
        //tabLayout.getTabAt(1).setText(R.string.info);
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
        //adapter.addFragment(InfoFragment.newInstance(graphsDatabase.get(chosenGraph)));
        viewPager.setAdapter(adapter);
    }

    private void populateGraphDatabase(){

        GraphHelperObject marketDS = new GraphHelperObject();
        GraphHelperObject productionLimit = new GraphHelperObject();
        GraphHelperObject perfectMarketFirm = new GraphHelperObject();
        GraphHelperObject indifferentAnalysis = new GraphHelperObject();
        GraphHelperObject costCurveHelper = new GraphHelperObject();
        GraphHelperObject monopolisticMarketFirm = new GraphHelperObject();

        marketDS.setTitle(getString(R.string.MarketDS));
        marketDS.setLabelX(getString(R.string.quantity) + " [" + getString(R.string.units) + "]");
        marketDS.setLabelY(getString(R.string.price)  + " [" + getString(R.string.currency) + "]");
        marketDS.setGraphEnum(GraphEnum.MarketDS);
        marketDS.addToSeries(LineEnum.Supply,   new ArrayList<>(Arrays.asList(1,2)));
        marketDS.addToSeries(LineEnum.Demand,   new ArrayList<>(Arrays.asList(-1,14)));
        marketDS.addToSeries(LineEnum.SupplyDefault,   new ArrayList<>(Arrays.asList(1,2)));
        marketDS.addToSeries(LineEnum.DemandDefault,   new ArrayList<>(Arrays.asList(-1,14)));

        marketDS.setCalculateEqulibrium(true);
        marketDS.setEquilibriumCurves(new ArrayList<>(Arrays.asList(LineEnum.Demand, LineEnum.Supply)));
        marketDS.setDependantCurveOnEquilibrium(new ArrayList<>(Arrays.asList(LineEnum.Price, LineEnum.Quantity)));

        graphsDatabase.put(GraphEnum.MarketDS, new MarketDS(
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(LineEnum.Supply,LineEnum.Demand)),
                LineEnum.Supply,
                marketDS.getSeries(),
                new ArrayList<>(),
                marketDS));

        // ProductionLimit

        productionLimit.setTitle(getString(R.string.ProductionLimit));
        productionLimit.setLabelX(getString(R.string.production_of) + " X [" + getString(R.string.units) + "]");
        productionLimit.setLabelY(getString(R.string.production_of) + " Y [" + getString(R.string.units) + "]");
        productionLimit.setGraphEnum(GraphEnum.ProductionLimit);
        productionLimit.addToSeries(LineEnum.ProductionCapabilities, new ArrayList<>(Arrays.asList(8,8)));
        productionLimit.addToSeries(LineEnum.ProductionCapabilitiesDefault, new ArrayList<>(Arrays.asList(8,8)));
        productionLimit.setCalculateEqulibrium(false);
        graphsDatabase.put(GraphEnum.ProductionLimit,new ProductionLimit(
                new ArrayList<>(),
                new ArrayList<>(Collections.singletonList(LineEnum.ProductionCapabilities)),
                LineEnum.ProductionCapabilities,
                productionLimit.getSeries(),
                new ArrayList<>(),
                productionLimit));

        // Perfect Market competition

        perfectMarketFirm.setTitle(getString(R.string.PerfectMarket));
        perfectMarketFirm.setLabelX(getString(R.string.quantity) + " [" + getString(R.string.units) + "]");
        perfectMarketFirm.setLabelY(getString(R.string.price)  + " [" + getString(R.string.currency) + "]");
        perfectMarketFirm.setGraphEnum(GraphEnum.PerfectMarket);
        perfectMarketFirm.addToSeries(LineEnum.MarginalCost, new ArrayList<>(Arrays.asList(0,0,-4,6,0)));
        perfectMarketFirm.addToSeries(LineEnum.AverageCost, new ArrayList<>(Arrays.asList(1,-2,6,15,1)));
        perfectMarketFirm.addToSeries(LineEnum.AverageVariableCost, new ArrayList<>(Arrays.asList(1,-2,6,5,1)));
        perfectMarketFirm.addToSeries(LineEnum.PriceLevel, new ArrayList<>(Arrays.asList(0,0,0,10,0)));

        perfectMarketFirm.setCalculateEqulibrium(true);
        perfectMarketFirm.setEquilibriumCurves(new ArrayList<>(Arrays.asList(LineEnum.PriceLevel, LineEnum.MarginalCost)));

        perfectMarketFirm.setDependantCurveOnEquilibrium(new ArrayList<>(Arrays.asList(LineEnum.Price, LineEnum.Quantity)));
        HashMap<GraphControllerActivity.LineEnum, ArrayList<GraphControllerActivity.LineEnum>> hashMap = new HashMap<>();
        hashMap.put(LineEnum.AverageCost, new ArrayList<>(Arrays.asList(LineEnum.MarginalCost,LineEnum.AverageVariableCost)));
        perfectMarketFirm.setDependantCurveOnCurve(hashMap);

        graphsDatabase.put(GraphEnum.PerfectMarket,new PerfectMarketFirm(
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(LineEnum.PriceLevel,LineEnum.AverageCost)),
                LineEnum.PriceLevel,
                perfectMarketFirm.getSeries(),
                new ArrayList<>(),
                perfectMarketFirm));

        //costcurves
        costCurveHelper.setTitle(getString(R.string.CostCurves));
        costCurveHelper.setLabelX(getString(R.string.quantity) + " [" + getString(R.string.units) + "]");
        costCurveHelper.setLabelY(getString(R.string.price)  + " [" + getString(R.string.currency) + "]");
        costCurveHelper.setGraphEnum(GraphEnum.CostCurves);
        costCurveHelper.addToSeries(LineEnum.MarginalCost, new ArrayList<>(Arrays.asList(0,0,-4,6,0)));
        costCurveHelper.addToSeries(LineEnum.AverageCost, new ArrayList<>(Arrays.asList(1,-2,6,15,1)));
        costCurveHelper.addToSeries(LineEnum.AverageVariableCost, new ArrayList<>(Arrays.asList(1,-2,6,5,1)));
        costCurveHelper.addToSeries(LineEnum.PriceLevel, new ArrayList<>(Arrays.asList(0,0,0,10,0)));

        costCurveHelper.setCalculateEqulibrium(true);
        costCurveHelper.setEquilibriumCurves(new ArrayList<>(Arrays.asList(LineEnum.PriceLevel, LineEnum.MarginalCost)));

        costCurveHelper.setDependantCurveOnEquilibrium(new ArrayList<>(Arrays.asList(LineEnum.Price, LineEnum.Quantity)));
        hashMap.put(LineEnum.PriceLevel, new ArrayList<>(Arrays.asList(LineEnum.MarginalCost, LineEnum.AverageVariableCost, LineEnum.AverageCost)));
        costCurveHelper.setDependantCurveOnCurve(hashMap);

        graphsDatabase.put(GraphEnum.CostCurves,new CostCurves(
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(LineEnum.Quantity,LineEnum.AverageCost)),
                LineEnum.Quantity,
                costCurveHelper.getSeries(),
                new ArrayList<>(),
                costCurveHelper));

        //indiferent analysis
        indifferentAnalysis.setTitle(getString(R.string.IndifferentAnalysis));
        indifferentAnalysis.setLabelX(getString(R.string.estate) + " X [" + getString(R.string.units) + "]");
        indifferentAnalysis.setLabelY(getString(R.string.estate) + " Y [" + getString(R.string.units) + "]");
        indifferentAnalysis.setGraphEnum(GraphEnum.IndifferentAnalysis);
        indifferentAnalysis.addToSeries(LineEnum.BudgetLine, new ArrayList<>(Arrays.asList(8, 8)));
        indifferentAnalysis.addToSeries(LineEnum.IndifferentCurve, new ArrayList<>(Arrays.asList(3, -3)));

        indifferentAnalysis.setCalculateEqulibrium(true);
        indifferentAnalysis.setEquilibriumCurves(new ArrayList<>(Arrays.asList(LineEnum.BudgetLine, LineEnum.IndifferentCurve)));

        graphsDatabase.put(GraphEnum.IndifferentAnalysis, new IndifferentAnalysis(
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(LineEnum.BudgetLine,LineEnum.IndifferentCurve)), //krivky na posun
                LineEnum.BudgetLine,
                indifferentAnalysis.getSeries(),
                new ArrayList<>(),
                indifferentAnalysis));

        //monopolistic market firm
        monopolisticMarketFirm.setTitle(getString(R.string.MonopolisticMarket));
        monopolisticMarketFirm.setLabelX(getString(R.string.quantity) + " [" + getString(R.string.units) + "]");
        monopolisticMarketFirm.setLabelY(getString(R.string.price)  + " [" + getString(R.string.currency) + "]");
        monopolisticMarketFirm.setGraphEnum(GraphEnum.MonopolisticMarket);
        monopolisticMarketFirm.addToSeries(LineEnum.AverageCost, new ArrayList<>());
        monopolisticMarketFirm.addToSeries(LineEnum.MarginalCost, new ArrayList<>());
        //monopolisticMarketFirm.addToSeries(LineEnum.PriceLevel, new ArrayList<Integer>());
        monopolisticMarketFirm.addToSeries(LineEnum.IndividualDemand, new ArrayList<>());
        monopolisticMarketFirm.addToSeries(LineEnum.MarginalRevenue, new ArrayList<>());

        monopolisticMarketFirm.setEquilibriumCurves(new ArrayList<>(Arrays.asList(LineEnum.MarginalCost,LineEnum.MarginalRevenue)));
        monopolisticMarketFirm.setCalculateEqulibrium(true);

        monopolisticMarketFirm.setDependantCurveOnEquilibrium(new ArrayList<>(Arrays.asList(LineEnum.Price, LineEnum.Quantity)));
        HashMap<GraphControllerActivity.LineEnum, ArrayList<GraphControllerActivity.LineEnum>> hashMap2 = new HashMap<>();
        hashMap2.put(LineEnum.MarginalRevenue, new ArrayList<>(Arrays.asList(LineEnum.IndividualDemand, LineEnum.AverageCost)));
        hashMap2.put(LineEnum.AverageCost, new ArrayList<>(Collections.singletonList(LineEnum.MarginalCost)));
        monopolisticMarketFirm.setDependantCurveOnCurve(hashMap2);

        graphsDatabase.put(GraphEnum.MonopolisticMarket, new MonopolisticMarketFirm(
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(LineEnum.MarginalRevenue,LineEnum.AverageCost)),
                LineEnum.MarginalRevenue,
                monopolisticMarketFirm.getSeries(),
                new ArrayList<>(),
                monopolisticMarketFirm
        ));
    }

    public static GraphEnum getChosenGraph() {
        return chosenGraph;
    }

    /*public ViewPager getViewPager() {
        return mViewPager;
    }*/

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
