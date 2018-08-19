package cz.mendelu.tomas.graphpef;

import android.content.ClipData;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tomas on 12.08.2018.
 */

public class GraphFragment extends Fragment{
    private static final String TAG = "GraphFragment";
    //line name and datapoints
    private HashMap<MainScreenController.LineEnum,LineGraphSeries<DataPoint>> lineGraphSeriesMap;


    private Menu menu;
    private GraphView graph;
    private BottomNavigationView toolbar;
    private AppCompatTextView text1, text2, text3, text4, text5;

    private GraphHelperObject graphHelperObject;
    private final static String GRAPH_KEY = "GRAPH_KEY";

    double precision;

    int zmena;

    public static GraphFragment newInstance(GraphHelperObject graphHelperObject){
        GraphFragment graphFragment = new GraphFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("GRAPH_KEY",graphHelperObject);
        graphFragment.setArguments(bundle);
        return  graphFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstances) {

        View view = inflater.inflate(R.layout.graph_fragment,container,false);
        ImageButton up = (ImageButton) view.findViewById(R.id.buttonUp);
        ImageButton down = (ImageButton) view.findViewById(R.id.buttonDown);
        graph = (GraphView) view.findViewById(R.id.graphComponent);
        toolbar = (BottomNavigationView) view.findViewById(R.id.toolbarBottom);
        BottomNavigationViewHelper.disableShiftMode(toolbar);


        text1 = view.findViewById(R.id.graphText1);
        text2 = view.findViewById(R.id.graphText2);
        text3 = view.findViewById(R.id.graphText3);
        text4 = view.findViewById(R.id.graphText4);
        text5 = view.findViewById(R.id.graphText5);

        graphHelperObject = (GraphHelperObject) getArguments().getSerializable(GRAPH_KEY);
        double y,x;
        x = 1;
        zmena = 0;


        //todo get precision from activity
        precision = 0.02;

        up.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: Clicked button Up");
                moveCurve(true, MainScreenController.getChosenLine(),Color.GREEN);
            }
        });

        down.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: Clicked button Down");
                moveCurve(false,MainScreenController.getChosenLine(),Color.RED);
            }
        });

        HashMap<MainScreenController.LineEnum, ArrayList<Integer>> seriesSource = graphHelperObject.getSeries();
        Log.d(TAG, "Title " + graphHelperObject.getTitle() + " Size " + seriesSource.size());

        //Graph styling settings
        graph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.BOTH );
        graph.getGridLabelRenderer().setHorizontalAxisTitle(graphHelperObject.getLabelX());
        graph.getGridLabelRenderer().setHumanRounding(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle(graphHelperObject.getLabelY());
        graph.getGridLabelRenderer().setHighlightZeroLines(true);
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);
        graph.getGridLabelRenderer().setNumVerticalLabels(4);
        graph.getViewport().setMaxX(12);
        graph.getViewport().setXAxisBoundsManual(true);
        lineGraphSeriesMap = new HashMap<>();

        for (MainScreenController.LineEnum line:seriesSource.keySet()) {
            calculateData(line, Color.BLACK);
        }


        if (graphHelperObject.getCalculateEqulibrium())
        {
            Log.d(TAG,"Equilibrium being calculated");
            text1.setText(graphHelperObject.getEquilibriumCurves().get(0).toString() + " = ");
            text2.setText(graphHelperObject.getEquilibriumCurves().get(1).toString() + " = ");
            calculateEqulibrium(graphHelperObject.getEquilibriumCurves().get(0),graphHelperObject.getEquilibriumCurves().get(1));
        }
        menu = toolbar.getMenu();
        updateMenuTitles();

        return view;
    }

    private void moveCurve(Boolean boolUp, MainScreenController.LineEnum seriesID, int color) {
        double x,y;
        x = 1;
        if (boolUp){
            Log.d(TAG,"posunKrivku:  hore");
            zmena+= + 1;
        }else {
            Log.d(TAG, "posunKrivku: dole");
            zmena += -1;
        }
        calculateData(seriesID,color);
    }

    private void calculateData(MainScreenController.LineEnum line, int color) {
        double x,y;
        x = 1;
        int x3, x2, x1, x0;
        HashMap<MainScreenController.LineEnum,ArrayList<Integer>> seriesSource = graphHelperObject.getSeries();

        x3 = seriesSource.get(line).get(0);
        x2 = seriesSource.get(line).get(1);
        x1 = seriesSource.get(line).get(2);
        x0 = seriesSource.get(line).get(3);

        if (lineGraphSeriesMap.get(line) != null){
            graph.removeSeries(lineGraphSeriesMap.get(line));
        }
        LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<DataPoint>();
        lineGraphSeriesMap.remove(line);

        Log.d(TAG, x3 + " * x * x * x + "+ x2 +" * x * x + "+x1+" * x + "+x0+" + " + zmena );

        for( int i=0; i<500; i++){
            x = x + precision;
            y = x3 * x * x * x + x2 * x * x + x1 * x + x0 + zmena;
            seriesLocal.appendData( new DataPoint(x,y), true, 500 );
        }
        seriesLocal.setColor(color);
        graph.addSeries(seriesLocal);

        lineGraphSeriesMap.put(line, seriesLocal);
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    private void calculateEqulibrium(MainScreenController.LineEnum curve1,MainScreenController.LineEnum curve2){

        //TODO calculate equi point
        LineGraphSeries<DataPoint> data1 = lineGraphSeriesMap.get(curve1);
        LineGraphSeries<DataPoint> data2 = lineGraphSeriesMap.get(curve2);

        // check if they intersect each other
        if (data1.getHighestValueX() > data2.getLowestValueX()
                && data2.getHighestValueX() > data1.getLowestValueX())
        {
            if (data1.getHighestValueY() > data2.getLowestValueY()
                    && data2.getHighestValueY() > data1.getLowestValueY())
            {

            }
        }



    }

    private void createShape(ArrayList<DataPoint> arrayList){
        //TODO create shape for are to be shown to user
    }

    private void updateMenuTitles() {
        Log.d(TAG,"UpdateMenuTitles");
        menu.clear();
        for (final MainScreenController.LineEnum line:graphHelperObject.getSeries().keySet()) {
            MenuItem menuItem = menu.add(line.toString());
            menuItem.setIcon(R.drawable.ic_multiline_chart_black_24dp);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Log.d(TAG, "onMenuItemClick: " + line.toString());
                    MainScreenController.setChosenLine(line);
                    return false;
                }
            });
        }
    }
}
