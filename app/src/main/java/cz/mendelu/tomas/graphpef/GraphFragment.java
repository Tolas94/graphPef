package cz.mendelu.tomas.graphpef;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.graphs.DefaultGraph;

import static java.lang.Math.abs;

/**
 * Created by tomas on 12.08.2018.
 */

public class GraphFragment extends Fragment{
    private static final String TAG = "GraphFragment";

    private Menu menu;
    private GraphView graph;
    private BottomNavigationView toolbar;
    private AppCompatTextView text1, text2, text3, text4, text5;

    private GraphIfc graphIfc;
    private final static String GRAPH_KEY = "GRAPH_KEY";
    private static boolean init = false;


    public static GraphFragment newInstance(DefaultGraph defaultGraph){
        if (!init){
            //Log.d(TAG,"change init to true");
            init = true;
        }
        GraphFragment graphFragment = new GraphFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("GRAPH_KEY",defaultGraph);
        graphFragment.setArguments(bundle);
        return  graphFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstances) {

        View view = inflater.inflate(R.layout.graph_fragment,container,false);

        if(getArguments() != null){
            ImageButton up = view.findViewById(R.id.buttonUp);
            ImageButton down = view.findViewById(R.id.buttonDown);
            ImageButton left = view.findViewById(R.id.buttonLeft);
            ImageButton right = view.findViewById(R.id.buttonRight);
            graph = view.findViewById(R.id.graphComponent);
            toolbar = view.findViewById(R.id.toolbarBottom);
            BottomNavigationViewHelper.disableShiftMode(toolbar);


            text1 = view.findViewById(R.id.graphText1);
            text2 = view.findViewById(R.id.graphText2);
            text3 = view.findViewById(R.id.graphText3);
            text4 = view.findViewById(R.id.graphText4);
            text5 = view.findViewById(R.id.graphText5);

            graphIfc = (GraphIfc) getArguments().getSerializable(GRAPH_KEY);

            if (graphIfc == null)
                return view;

            for(MainScreenController.Direction dir: graphIfc.getMovableDirections()){
                switch(dir){
                    case up: up.setVisibility(View.VISIBLE);
                        break;
                    case down: down.setVisibility(View.VISIBLE);
                        break;
                    case left: left.setVisibility(View.VISIBLE);
                        break;
                    case right: right.setVisibility(View.VISIBLE);
                        break;
                }
            }

            up.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button Up");
                    moveCurve(MainScreenController.Direction.up, Color.BLACK);
                }
            });


            down.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button Down");
                    moveCurve(MainScreenController.Direction.down, Color.BLACK);
                }
            });

            left.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button Up");
                    moveCurve(MainScreenController.Direction.left, Color.BLACK);
                }
            });

            right.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button Down");
                    moveCurve(MainScreenController.Direction.right, Color.BLACK);
                }
            });


            HashMap<MainScreenController.LineEnum, ArrayList<Integer>> seriesSource = graphIfc.getSeries();
            //Log.d(TAG, "Title " + graphIfc.getTitle() + " Size " + seriesSource.size());
            TextView title = view.findViewById(R.id.title);
            title.setText(graphIfc.getTitle());

            //Graph styling settings
            graph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.BOTH );
            graph.getGridLabelRenderer().setHorizontalAxisTitle(graphIfc.getLabelX());
            //graph.getGridLabelRenderer().setHumanRounding(true);
            graph.getGridLabelRenderer().setVerticalAxisTitle(graphIfc.getLabelY());
            graph.getGridLabelRenderer().setHighlightZeroLines(true);
            graph.getGridLabelRenderer().setNumHorizontalLabels(1);
            graph.getGridLabelRenderer().setNumVerticalLabels(1);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(false);


            //graph.getGridLabelRenderer().setNumVerticalLabels(4);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxX(15);
            graph.getViewport().setMaxY(15);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.setVisibility(View.VISIBLE);

            for (MainScreenController.LineEnum line:seriesSource.keySet()) {
                calculateData(line, Color.BLACK);
            }

            menu = toolbar.getMenu();
            updateMenuTitles();

            calculateEquilibrium();
        }else{
            Log.d(TAG,"return clean View");
        }
        return view;
    }

    private void moveCurve(MainScreenController.Direction dir, int color) {
        graphIfc.moveObject(dir);
        calculateData(graphIfc.getMovableEnum(),color);
        calculateEquilibrium();
    }

    private void calculateData(MainScreenController.LineEnum line, int color) {
        if (graphIfc.getLineGraphSeries().get(line) != null){
            graph.removeSeries(graphIfc.getLineGraphSeries().get(line));
        }
        LineGraphSeries lineSeries = graphIfc.calculateData(line,color);
        graph.addSeries(lineSeries);
        updateTexts();
    }

    private void createShape(ArrayList<DataPoint> arrayList){
        //TODO create shape for to be shown to user
        // probably will not be able to do so
        // possible solution is to draw backgroung with color on curve and white on the lower one
    }

    private void updateMenuTitles() {
        Log.d(TAG,"UpdateMenuTitles");
        menu.clear();
        for (final MainScreenController.LineEnum line:graphIfc.getMovableObjects()) {
            Log.d(TAG, "updateMenuTitles: " + line);
            MenuItem menuItem = menu.add(line.toString());
            menuItem.setIcon(R.drawable.ic_multiline_chart_black_24dp);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Log.d(TAG, "onMenuItemClick: " + line.toString());
                    graphIfc.setMovable(line);
                    return false;
                }
            });
        }
    }

    private void updateTexts(){
        Log.d(TAG, "updateTexts: ");
        text1.setVisibility(View.INVISIBLE);
        text2.setVisibility(View.INVISIBLE);
        text3.setVisibility(View.INVISIBLE);
        text4.setVisibility(View.INVISIBLE);
        text5.setVisibility(View.INVISIBLE);


        for (int i = 0; i < graphIfc.getGraphTexts().size(); ++i){
            Log.d(TAG, "setText: " + i);
            switch(i){
                case 0:
                    text1.setText(graphIfc.getGraphTexts().get(0));
                    text1.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    text2.setText(graphIfc.getGraphTexts().get(1));
                    text2.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    text3.setText(graphIfc.getGraphTexts().get(2));
                    text3.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    text4.setText(graphIfc.getGraphTexts().get(3));
                    text4.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    text5.setText(graphIfc.getGraphTexts().get(4));
                    text5.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void calculateEquilibrium(){
        //Log.d(TAG, "calculateEquilibrium: ");
        for(MainScreenController.LineEnum line: graphIfc.getEqDependantCurves()){
            if (graphIfc.getLineGraphSeries().get(line) != null){
                Log.d(TAG, "calculateEquilibrium: delete[" + line.toString() + "]");
                graph.removeSeries(graphIfc.getLineGraphSeries().get(line));
                graphIfc.getLineGraphSeries().remove(line);
            }
        }

        graphIfc.calculateEqulibrium();
        updateTexts();

        for(MainScreenController.LineEnum line: graphIfc.getEqDependantCurves()){
            //Log.d(TAG, "calculateEquilibrium: " + line.toString());
            if (graphIfc.getLineGraphSeries().get(line) != null){
                Log.d(TAG, "calculateEquilibrium: create[" + line.toString() + "]");
                graph.addSeries(graphIfc.getLineGraphSeries().get(line));
            }
        }
    }

}
