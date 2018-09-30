package cz.mendelu.tomas.graphpef.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.graphs.DefaultGraph;
import cz.mendelu.tomas.graphpef.helperObjects.BottomNavigationViewHelper;
import cz.mendelu.tomas.graphpef.interfaces.GraphIfc;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static java.lang.Math.abs;

/**
 * Created by tomas on 12.08.2018.
 */

public class GraphFragment extends Fragment  implements Serializable {
    private static final String TAG = "GraphFragment";

    private Menu menu;
    private GraphView graph;
    private BottomNavigationView toolbar;
    private ImageButton up,down,left,right;
    private AppCompatTextView text1, text2, text3, text4, text5;
    private PointsGraphSeries<DataPoint> eqpoints;
    private HashMap<MainScreenControllerActivity.LineEnum,PointsGraphSeries> labelSeries;
    private RelativeLayout loadingAnimation;

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
            up = view.findViewById(R.id.buttonUp);
            down = view.findViewById(R.id.buttonDown);
            left = view.findViewById(R.id.buttonLeft);
            right = view.findViewById(R.id.buttonRight);
            graph = view.findViewById(R.id.graphComponent);
            toolbar = view.findViewById(R.id.toolbarBottom);
            //BottomNavigationViewHelper.disableShiftMode(toolbar);
            //toolbar.setBackgroundColor(getContext().getColor(R.color.colorPrimary));
            toolbar.addStatesFromChildren();
            labelSeries = new HashMap<>();
            loadingAnimation = view.findViewById(R.id.loadingPanelGraph);
            loadingAnimation.setVisibility(View.INVISIBLE);


            text1 = view.findViewById(R.id.graphText1);
            text2 = view.findViewById(R.id.graphText2);
            text3 = view.findViewById(R.id.graphText3);
            text4 = view.findViewById(R.id.graphText4);
            text5 = view.findViewById(R.id.graphText5);

            graphIfc = (GraphIfc) getArguments().getSerializable(GRAPH_KEY);

            if (graphIfc == null)
                return view;

            for(MainScreenControllerActivity.Direction dir: graphIfc.getMovableDirections()){
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
                    loadingAnimation.setVisibility(View.VISIBLE);
                    moveCurve(MainScreenControllerActivity.Direction.up);
                    loadingAnimation.setVisibility(View.INVISIBLE);
                }
            });


            down.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button Down");
                    loadingAnimation.setVisibility(View.VISIBLE);
                    moveCurve(MainScreenControllerActivity.Direction.down);
                    loadingAnimation.setVisibility(View.INVISIBLE);
                }
            });

            left.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button left");
                    loadingAnimation.setVisibility(View.VISIBLE);
                    moveCurve(MainScreenControllerActivity.Direction.left);
                    loadingAnimation.setVisibility(View.INVISIBLE);
                }
            });

            right.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button right");
                    loadingAnimation.setVisibility(View.VISIBLE);
                    moveCurve(MainScreenControllerActivity.Direction.right);
                    loadingAnimation.setVisibility(View.INVISIBLE);
                }
            });


            HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> seriesSource = graphIfc.getSeries();
            //Log.d(TAG, "Title " + graphIfc.getTitle() + " Size " + seriesSource.size());

            //Graph styling settings
            graph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.BOTH);
            graph.getGridLabelRenderer().setHorizontalAxisTitle(graphIfc.getLabelX());
            graph.getGridLabelRenderer().setHumanRounding(true);
            graph.getGridLabelRenderer().setVerticalAxisTitle(graphIfc.getLabelY());
            graph.getGridLabelRenderer().setHighlightZeroLines(true);
            graph.getGridLabelRenderer().setNumHorizontalLabels(4);
            graph.getGridLabelRenderer().setNumVerticalLabels(4);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

            //graph.getLegendRenderer().setVisible(true);
            //graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);


            //graph.getGridLabelRenderer().setNumVerticalLabels(4);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxX(15);
            graph.getViewport().setMaxY(15);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.setVisibility(View.VISIBLE);
            boolean calculateQuantityLast = false;
            for (MainScreenControllerActivity.LineEnum line:seriesSource.keySet()) {
                if (line == MainScreenControllerActivity.LineEnum.Quantity){
                    calculateQuantityLast = true;
                }else{
                    calculateData(line, graphIfc.getColorOf(line));
                }
            }
            if (calculateQuantityLast){
                calculateData(MainScreenControllerActivity.LineEnum.Quantity, graphIfc.getColorOf(MainScreenControllerActivity.LineEnum.Quantity));
            }

            menu = toolbar.getMenu();
            updateMenuTitles();

            calculateEquilibrium();
            presentShowcaseSequence();
        }else{
            Log.d(TAG,"return clean View");
        }
        return view;
    }

    private void moveCurve(MainScreenControllerActivity.Direction dir) {
        if (labelSeries != null){
            graph.removeSeries(labelSeries.get(graphIfc.getMovableEnum()));
            //labelSeries.remove(graphIfc.getMovableEnum());
        }
        if (graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()) != null){
            graph.removeSeries(graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()));
            if (graphIfc.getDependantCurves(graphIfc.getMovableEnum()) != null){
                for (MainScreenControllerActivity.LineEnum line:graphIfc.getDependantCurves(graphIfc.getMovableEnum())) {
                    graph.removeSeries(graphIfc.getLineGraphSeries().get(line));
                    graph.removeSeries(labelSeries.get(line));
                }
            }
        }
        graphIfc.moveObject(dir);
        calculateData(graphIfc.getMovableEnum(),graphIfc.getColorOf(graphIfc.getMovableEnum()));
        if (graphIfc.getDependantCurves(graphIfc.getMovableEnum()) != null){
            for (MainScreenControllerActivity.LineEnum line:graphIfc.getDependantCurves(graphIfc.getMovableEnum())) {
                calculateData(line,graphIfc.getColorOf(line));
            }
        }
        calculateEquilibrium();
    }

    private void calculateData(final MainScreenControllerActivity.LineEnum line, int color) {
        LineGraphSeries lineSeries = graphIfc.calculateData(line,color);
        if (lineSeries != null){
            PointsGraphSeries labelSeries = new PointsGraphSeries();
            labelSeries.appendData(new DataPoint(graphIfc.getLineLabelPosition(line).first.doubleValue(),graphIfc.getLineLabelPosition(line).second.doubleValue()),
                    false,
                    1);

            labelSeries.setColor(color);
            labelSeries.setCustomShape(new PointsGraphSeries.CustomShape() {
                @Override
                public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                    paint.setTextSize(50);
                    canvas.drawText(getContext().getString(getResources().getIdentifier(line.toString()+"Label","string",getContext().getPackageName())),x+20,y-20,paint);
                }
            });

            lineSeries.setTitle(getContext().getString(getResources().getIdentifier(line.toString(),"string",getContext().getPackageName())));
            graph.addSeries(lineSeries);
            this.labelSeries.put(line,labelSeries);
            graph.addSeries(labelSeries);
            updateTexts();
        }
    }

    private void createShape(ArrayList<DataPoint> arrayList){
        //TODO create shape for to be shown to user
        // probably will not be able to do so
        // possible solution is to draw backgroung with color on curve and white on the lower one
            //will overdraw grid??
    }

    private void updateMenuTitles() {
        Log.d(TAG,"UpdateMenuTitles");
        menu.clear();
        for (final MainScreenControllerActivity.LineEnum line:graphIfc.getMovableObjects()) {
            Log.d(TAG, "updateMenuTitles: " + line);
            MenuItem menuItem = menu.add(getContext().getString(getResources().getIdentifier(line.toString(),"string",getContext().getPackageName())));
            menuItem.setIcon(R.drawable.ic_multiline_chart_black_24dp);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Log.d(TAG, "onMenuItemClick: " + line.toString());

                    graph.removeSeries(graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()));
                    graph.removeSeries(labelSeries.get(graphIfc.getMovableEnum()));

                    //nastav farbu starej movable na default
                    graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()).setColor(graphIfc.getColorOf(line));
                    graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()).setThickness(5);
                    if (labelSeries.get(graphIfc.getMovableEnum()) != null){
                        labelSeries.get(graphIfc.getMovableEnum()).setColor(graphIfc.getColorOf(line));
                    }

                    graph.addSeries(graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()));
                    if (labelSeries.get(graphIfc.getMovableEnum()) != null) {
                        graph.addSeries(labelSeries.get(graphIfc.getMovableEnum()));
                    }

                    graphIfc.setMovable(line);

                    graph.removeSeries(graphIfc.getLineGraphSeries().get(line));
                    //vybratu krivku vyfarbi na modro
                    graphIfc.getLineGraphSeries().get(line).setColor(getContext().getColor(R.color.colorPrimary));
                    //vybratu krivku hrubo
                    graphIfc.getLineGraphSeries().get(line).setThickness(10);
                    LineGraphSeries lineGraphSeries = graphIfc.calculateData(line,getContext().getColor(R.color.colorPrimary));
                    if(lineGraphSeries != null){
                        graph.addSeries(lineGraphSeries);
                    }

                    graphIfc.refreshInfoTexts();
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
            //Log.d(TAG, "setText: " + i);
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
        Log.d(TAG, "calculateEquilibrium: ");
        clearBeforeCalculationOfEQ();
        graphIfc.calculateEqulibrium();
        updateTexts();
        addSeriesToGraphAfterEQCalculation();
    }

    private void clearBeforeCalculationOfEQ(){
        if( graphIfc.getEqDependantCurves() != null){
            for(MainScreenControllerActivity.LineEnum line: graphIfc.getEqDependantCurves()){
                if (graphIfc.getLineGraphSeries().get(line) != null){
                    Log.d(TAG, "calculateEquilibrium: delete[" + line.toString() + "]");
                    graph.removeSeries(graphIfc.getLineGraphSeries().get(line));
                    graphIfc.getLineGraphSeries().remove(line);
                }
            }
        }
        if (eqpoints != null){
            graph.removeSeries(eqpoints);
        }

    }

    private void addSeriesToGraphAfterEQCalculation(){
        eqpoints = new PointsGraphSeries<>();
        eqpoints.setColor(graphIfc.getColorOf(MainScreenControllerActivity.LineEnum.Equilibrium));
        eqpoints.setTitle(getContext().getString(R.string.Equilibrium));
        if (graphIfc.getEquiPoints() != null && graphIfc.getEquiPoints().size() != 0){
            Log.d(TAG, "calculateEquilibrium: create EQ points size[" + graphIfc.getEquiPoints().size() + "]");
            if (graphIfc.getEquiPoints().size() == 4 && MainScreenControllerActivity.getChosenGraph() == MainScreenControllerActivity.GraphEnum.IndifferentAnalysis){
                eqpoints.appendData(new DataPoint(graphIfc.getEquiPoints().get(0),graphIfc.getEquiPoints().get(1)),false,4);
                eqpoints.appendData(new DataPoint(graphIfc.getEquiPoints().get(2),graphIfc.getEquiPoints().get(3)),false,4);
            }else{
                eqpoints.appendData(new DataPoint(graphIfc.getEquiPoints().get(0),graphIfc.getEquiPoints().get(1)),false,2);
            }
            graph.addSeries(eqpoints);
        }
        if( graphIfc.getEqDependantCurves() != null) {
            for (MainScreenControllerActivity.LineEnum line : graphIfc.getEqDependantCurves()) {
                Log.d(TAG, "calculateEquilibrium: " + line.toString());
                if (graphIfc.getLineGraphSeries().get(line) != null) {
                    Log.d(TAG, "calculateEquilibrium: create[" + line.toString() + "]");
                    graphIfc.getLineGraphSeries().get(line).setTitle(getContext().getString(getResources().getIdentifier(line.toString(),"string",getContext().getPackageName())));
                    graph.addSeries(graphIfc.getLineGraphSeries().get(line));
                }
            }
        }
    }

    private void presentShowcaseSequence() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200); // half second between each showcase view
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this.getActivity(),TAG);
        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
            @Override
            public void onShow(MaterialShowcaseView itemView, int position) {
            }
        });
        sequence.setConfig(config);
        //sequence.addSequenceItem(graph,getString(R.string.graph_showcase),getString(R.string.dismiss_showcase_text));
        sequence.addSequenceItem(text1,getString(R.string.graph_values_showcase),getString(R.string.dismiss_showcase_text));
        sequence.addSequenceItem(up,getString(R.string.up_button_showcase),getString(R.string.dismiss_showcase_text));
        sequence.addSequenceItem(down,getString(R.string.down_button_showcase),getString(R.string.dismiss_showcase_text));
        sequence.addSequenceItem(toolbar,getString(R.string.choose_curve_showcase),getString(R.string.dismiss_showcase_text));
        sequence.start();
    }
}
