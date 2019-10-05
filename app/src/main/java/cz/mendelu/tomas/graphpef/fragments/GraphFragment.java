package cz.mendelu.tomas.graphpef.fragments;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.GraphControllerActivity;
import cz.mendelu.tomas.graphpef.adapters.GraphCurveChooseAdapter;
import cz.mendelu.tomas.graphpef.adapters.InfoListAdapter;
import cz.mendelu.tomas.graphpef.graphs.DefaultGraph;
import cz.mendelu.tomas.graphpef.interfaces.GraphIfc;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by tomas on 12.08.2018.
 */

public class GraphFragment extends Fragment  implements Serializable {
    private static final String TAG = "GraphFragment";

    //private Menu menu;
    private GraphView graph;
    //private BottomNavigationView toolbar;
    private ImageButton up,down,left,right;
    private AppCompatTextView text1, text2, text3, text4, text5;
    private PointsGraphSeries<DataPoint> eqpoints;
    private HashMap<GraphControllerActivity.LineEnum, PointsGraphSeries> labelSeries;
    private CardView graphChoose;
    //private RelativeLayout loadingAnimation;

    private GraphIfc graphIfc;
    private final static String GRAPH_KEY = "GRAPH_KEY";
    private static boolean init = false;


    private RecyclerView infoTextView;
    private List<ArrayList<String>> texts;
    private InfoListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView recyclerViewCurves;
    private RecyclerView.LayoutManager layoutManagerCurves;
    private GraphCurveChooseAdapter graphCurveChooseAdapter;


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

    public void populateTexts() {
        Log.d(TAG, "populateTexts");
        if (graphIfc != null) {
            //Log.d(TAG, "populateTexts graphifc not null");
            texts = graphIfc.getSituationInfoTexts();
        }
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
            //toolbar = view.findViewById(R.id.toolbarBottom);
            //BottomNavigationViewHelper.disableShiftMode(toolbar);
            //toolbar.setBackgroundColor(getContext().getColor(R.color.colorPrimary));
            //toolbar.addStatesFromChildren();
            labelSeries = new HashMap<>();
            //loadingAnimation = view.findViewById(R.id.loadingPanelGraph);
            //loadingAnimation.setVisibility(View.INVISIBLE);
            graphChoose = this.getActivity().findViewById(R.id.graphChooseCurveCardViewViewLayout);


            text1 = view.findViewById(R.id.graphText1);
            text2 = view.findViewById(R.id.graphText2);
            text3 = view.findViewById(R.id.graphText3);
            text4 = view.findViewById(R.id.graphText4);
            text5 = view.findViewById(R.id.graphText5);

            graphIfc = (GraphIfc) getArguments().getSerializable(GRAPH_KEY);

            if (graphIfc == null)
                return view;

            for (GraphControllerActivity.Direction dir : graphIfc.getMovableDirections()) {
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
                    //loadingAnimation.setVisibility(View.VISIBLE);
                    moveCurve(GraphControllerActivity.Direction.up);
                    //loadingAnimation.setVisibility(View.INVISIBLE);
                }
            });


            down.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button Down");
                    //loadingAnimation.setVisibility(View.VISIBLE);
                    moveCurve(GraphControllerActivity.Direction.down);
                    //loadingAnimation.setVisibility(View.INVISIBLE);
                }
            });

            left.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button left");
                    //loadingAnimation.setVisibility(View.VISIBLE);
                    moveCurve(GraphControllerActivity.Direction.left);
                    //loadingAnimation.setVisibility(View.INVISIBLE);
                }
            });

            right.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,"onClick: Clicked button right");
                    //loadingAnimation.setVisibility(View.VISIBLE);
                    moveCurve(GraphControllerActivity.Direction.right);
                    //loadingAnimation.setVisibility(View.INVISIBLE);
                }
            });


            HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> seriesSource = graphIfc.getSeries();
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
            for (GraphControllerActivity.LineEnum line : seriesSource.keySet()) {
                if (line == GraphControllerActivity.LineEnum.Quantity) {
                    calculateQuantityLast = true;
                }else{
                    calculateData(line, graphIfc.getColorOf(line));
                }
            }
            if (calculateQuantityLast){
                calculateData(GraphControllerActivity.LineEnum.Quantity, graphIfc.getColorOf(GraphControllerActivity.LineEnum.Quantity));
            }

            //menu = toolbar.getMenu();
            //updateMenuTitles();

            recyclerViewCurves = view.findViewById(R.id.graphChooseCurveRecyclerView);
            layoutManagerCurves = new LinearLayoutManager(getContext());
            ArrayList<String> labels = new ArrayList<>();
            Log.d(TAG, "recycler line size [" + graphIfc.getMovableObjects().size() + "]");
            for (GraphControllerActivity.LineEnum line : graphIfc.getMovableObjects()) {
                Log.d(TAG, "recycler line [" + line.toString() + "]");
                labels.add(getContext().getString(getResources().getIdentifier(line.toString(), "string", getContext().getPackageName())));
            }
            graphCurveChooseAdapter = new GraphCurveChooseAdapter(labels, graphIfc.getMovableObjects(), this, 0);
            recyclerViewCurves.setLayoutManager(layoutManagerCurves);
            recyclerViewCurves.setAdapter(graphCurveChooseAdapter);

            infoTextView = view.findViewById(R.id.listOfInfoOfGraph);


            populateTexts();
            mAdapter = new InfoListAdapter(texts);
            mLayoutManager = new LinearLayoutManager(getContext());
            infoTextView.setLayoutManager(mLayoutManager);
            infoTextView.setAdapter(mAdapter);
            infoTextView.setNestedScrollingEnabled(false);


            calculateEquilibrium();
            presentShowcaseSequence();
        }else{
            Log.d(TAG,"return clean View");
        }
        return view;
    }

    private void moveCurve(GraphControllerActivity.Direction dir) {
        if (labelSeries != null){
            graph.removeSeries(labelSeries.get(graphIfc.getMovableEnum()));
            //labelSeries.remove(graphIfc.getMovableEnum());
        }
        if (graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()) != null){
            graph.removeSeries(graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()));
            if (graphIfc.getDependantCurves(graphIfc.getMovableEnum()) != null){
                for (GraphControllerActivity.LineEnum line : graphIfc.getDependantCurves(graphIfc.getMovableEnum())) {
                    graph.removeSeries(graphIfc.getLineGraphSeries().get(line));
                    graph.removeSeries(labelSeries.get(line));
                }
            }
        }
        graphIfc.moveObject(dir);
        calculateData(graphIfc.getMovableEnum(),graphIfc.getColorOf(graphIfc.getMovableEnum()));
        if (graphIfc.getDependantCurves(graphIfc.getMovableEnum()) != null){
            for (GraphControllerActivity.LineEnum line : graphIfc.getDependantCurves(graphIfc.getMovableEnum())) {
                calculateData(line,graphIfc.getColorOf(line));
            }
        }
        calculateEquilibrium();
    }

    private void calculateData(final GraphControllerActivity.LineEnum line, int color) {
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

    public void setChosenCurve(GraphControllerActivity.LineEnum line) {
        Log.d(TAG, "setChosenCurve: " + line.toString());

        // remove old movable curve from GraphView
        graph.removeSeries(graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()));
        graph.removeSeries(labelSeries.get(graphIfc.getMovableEnum()));

        //recreate old movable to default color and thickness
        if (graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()) != null) {
            graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()).setColor(graphIfc.getColorOf(line));
            graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()).setThickness(5);
            if (labelSeries.get(graphIfc.getMovableEnum()) != null) {
                labelSeries.get(graphIfc.getMovableEnum()).setColor(graphIfc.getColorOf(line));
            }

            graph.addSeries(graphIfc.getLineGraphSeries().get(graphIfc.getMovableEnum()));
            if (labelSeries.get(graphIfc.getMovableEnum()) != null) {
                graph.addSeries(labelSeries.get(graphIfc.getMovableEnum()));
            }
        }

        graphIfc.setMovable(line);

        //remove new movable from graph to recreate it after
        graph.removeSeries(graphIfc.getLineGraphSeries().get(line));
        //set chosen curve to blue color

        graphIfc.getLineGraphSeries().get(line).setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        //chosen curve set to thickness 10
        graphIfc.getLineGraphSeries().get(line).setThickness(10);
        LineGraphSeries lineGraphSeries = graphIfc.calculateData(line, ContextCompat.getColor(getContext(), R.color.colorPrimary));
        if (lineGraphSeries != null) {
            graph.addSeries(lineGraphSeries);
        }

        graphIfc.refreshInfoTexts();
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
            for (GraphControllerActivity.LineEnum line : graphIfc.getEqDependantCurves()) {
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
        eqpoints.setColor(graphIfc.getColorOf(GraphControllerActivity.LineEnum.Equilibrium));
        eqpoints.setTitle(getContext().getString(R.string.Equilibrium));
        if (graphIfc.getEquiPoints() != null && graphIfc.getEquiPoints().size() != 0){
            Log.d(TAG, "calculateEquilibrium: create EQ points size[" + graphIfc.getEquiPoints().size() + "]");
            if (graphIfc.getEquiPoints().size() == 4 && GraphControllerActivity.getChosenGraph() == GraphControllerActivity.GraphEnum.IndifferentAnalysis) {
                eqpoints.appendData(new DataPoint(graphIfc.getEquiPoints().get(0),graphIfc.getEquiPoints().get(1)),false,4);
                eqpoints.appendData(new DataPoint(graphIfc.getEquiPoints().get(2),graphIfc.getEquiPoints().get(3)),false,4);
            }else{
                eqpoints.appendData(new DataPoint(graphIfc.getEquiPoints().get(0),graphIfc.getEquiPoints().get(1)),false,2);
            }
            graph.addSeries(eqpoints);
        }
        if( graphIfc.getEqDependantCurves() != null) {
            for (GraphControllerActivity.LineEnum line : graphIfc.getEqDependantCurves()) {
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
        ImageButton showcaseBtn;
        if (up.getVisibility() == View.VISIBLE) {
            showcaseBtn = up;
        } else {
            showcaseBtn = right;
        }
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this.getActivity())
                        .setTarget(showcaseBtn)
                        .setDismissText(getString(R.string.up_button_showcase))
                        .setContentText(getString(R.string.dismiss_showcase_text))
                        .withRectangleShape()
                        .setDismissOnTouch(true)
                        .build()
        );
        if (down.getVisibility() == View.VISIBLE) {
            showcaseBtn = down;
        } else {
            showcaseBtn = left;
        }
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this.getActivity())
                        .setTarget(showcaseBtn)
                        .setDismissText(getString(R.string.down_button_showcase))
                        .setContentText(getString(R.string.dismiss_showcase_text))
                        .withRectangleShape()
                        .setDismissOnTouch(true)
                        .build()
        );
        try {
            if (graphChoose != null) {
                sequence.addSequenceItem(
                        new MaterialShowcaseView.Builder(this.getActivity())
                                .setTarget(graphChoose)
                                .setDismissText(getString(R.string.choose_curve_showcase))
                                .setContentText(getString(R.string.dismiss_showcase_text))
                                .withRectangleShape(true)
                                .setDismissOnTouch(true)
                                .build()
                );
            }

            ScrollView scrollView = this.getActivity().findViewById(R.id.graphScrollView);
            if (text1 != null && scrollView != null) {
                int pos = text1.getTop();
                sequence.setOnItemDismissedListener((itemView, position) -> {

                    if (position == 2) {
                        scrollView.scrollTo(0, pos);
                    }

                });
                sequence.addSequenceItem(
                        new MaterialShowcaseView.Builder(this.getActivity())
                                .setTarget(text1)
                                .setDismissText(getString(R.string.graph_values_showcase))
                                .setContentText(getString(R.string.dismiss_showcase_text))
                                .withRectangleShape()
                                .setDismissOnTouch(true)
                                .build()
                );
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
        sequence.start();
    }
}
