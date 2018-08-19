package cz.mendelu.tomas.graphpef;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tomas on 11.08.2018.
 */

public class MainScreenController extends AppCompatActivity{

    private  SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private HashMap<GraphEnum, GraphHelperObject> graphsDatabase;

    private static GraphEnum chosenGraph = GraphEnum.MarketDS;

    public static LineEnum getChosenLine() {
        return chosenLine;
    }

    public static void setChosenLine(LineEnum chosenLine) {
        MainScreenController.chosenLine = chosenLine;
    }

    private static LineEnum chosenLine = LineEnum.Demand;

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
        TotalRevenue,
        MarginalRevenue,
        Quantity,
        ProductionCapabilities,
        Taxes,
        Equilibrium,
        TotalUtility

    }


    public GraphHelperObject getGraphByEnum(GraphEnum graphEnum){
        return graphsDatabase.get(graphEnum);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_layout);
        graphsDatabase = new HashMap<GraphEnum, GraphHelperObject>();
        populateGraphDatabase();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_menu_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_multiline_chart_black_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_info_black_24dp);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MenuFragment());
        adapter.addFragment(GraphFragment.newInstance(graphsDatabase.get(GraphEnum.MarketDS)));
        adapter.addFragment(new InfoFragment());
        viewPager.setAdapter(adapter);
    }

    private void populateGraphDatabase(){
        //MarketDS
        GraphHelperObject newGraph = new GraphHelperObject();

        newGraph.setTitle("MarketDS");
        newGraph.setLabelX("Quantity");
        newGraph.setLabelY("Price");
        newGraph.setGraphEnum(GraphEnum.MarketDS);
        ArrayList<Integer> values = new ArrayList<Integer>(), values2 = new ArrayList<Integer>(), values3 = new ArrayList<Integer>();
        HashMap<MainScreenController.LineEnum,ArrayList<Integer>> map = new HashMap<>();

        //series 1 - Supply
        values.add(0);//X^3
        values.add(0);//X^2
        values.add(1);//x^1
        values.add(0);//x^0
        map.put(LineEnum.Supply,values);

        //series 2 - Demand
        values2.add(0);//X^3
        values2.add(0);//X^2
        values2.add(-1);//x^1
        values2.add(10);//x^0
        map.put(LineEnum.Demand,values2);

        // series 3 - Price
        values3.add(0);//X^3
        values3.add(0);//X^2
        values3.add(0);//x^1
        values3.add(5);//x^0
        map.put(LineEnum.Price,values3);

        newGraph.setSeries( map );

        newGraph.setCalculateEqulibrium(true);
        ArrayList<LineEnum> arrayList = new ArrayList<>();
        arrayList.add(LineEnum.Demand);
        arrayList.add(LineEnum.Supply);
        newGraph.setEquilibriumCurves(arrayList);

        graphsDatabase.put(GraphEnum.MarketDS,newGraph);
    }

    public static GraphEnum getChosenGraph() {
        return chosenGraph;
    }

    public void setChosenGraph(GraphEnum chosenGraph) {
        MainScreenController.chosenGraph = chosenGraph;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        GraphFragment graphFragment = GraphFragment.newInstance(graphsDatabase.get(chosenGraph));
        ft.replace(R.id.graph_fragment,graphFragment);
        ft.commit();
    }

}
