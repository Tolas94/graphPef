package cz.mendelu.tomas.graphpef;

import android.content.ClipData;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static java.lang.Math.abs;

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

        //todo get precision from activity
        precision = 0.02;

        up.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: Clicked button Up");
                moveCurve(true, MainScreenController.getChosenLine(),0,Color.GREEN);
            }
        });

        down.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: Clicked button Down");
                moveCurve(false,MainScreenController.getChosenLine(),0,Color.RED);
            }
        });

        HashMap<MainScreenController.LineEnum, ArrayList<Integer>> seriesSource = graphHelperObject.getSeries();
        Log.d(TAG, "Title " + graphHelperObject.getTitle() + " Size " + seriesSource.size());
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(graphHelperObject.getTitle());

        //Graph styling settings
        graph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.BOTH );
        graph.getGridLabelRenderer().setHorizontalAxisTitle(graphHelperObject.getLabelX());
        graph.getGridLabelRenderer().setHumanRounding(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle(graphHelperObject.getLabelY());
        graph.getGridLabelRenderer().setHighlightZeroLines(true);
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);
        graph.getGridLabelRenderer().setNumVerticalLabels(4);
        graph.getViewport().setMaxX(15);
        graph.getViewport().setMaxY(15);
        graph.getViewport().setXAxisBoundsManual(true);
        lineGraphSeriesMap = new HashMap<>();

        for (MainScreenController.LineEnum line:seriesSource.keySet()) {
            calculateData(line, Color.BLACK);
        }

        menu = toolbar.getMenu();
        updateMenuTitles();
        recalculateEquilibrium();

        return view;
    }

    private void moveCurve(Boolean boolUp, MainScreenController.LineEnum seriesID,int identificator, int color) {

        int change = graphHelperObject.getLineChangeIdentificatorByLineEnum(seriesID).get(identificator);
        //Log.d(TAG, "Line: " + seriesID.toString() + " Change before: " + change + " on x" + identificator);
        ArrayList<Integer> arrayList = new ArrayList<>();
        for(int i = 0; i<4; i++ ){
            if (i == identificator){
                if (boolUp){
                    arrayList.add( change + 1 );
                    //Log.d(TAG,"posunKrivku:  hore, new change: " + arrayList.get(identificator) );
                } else {
                    arrayList.add( change - 1 );
                    //Log.d(TAG, "posunKrivku: dole, new change: " + arrayList.get(identificator) );
                }
            }else{
                arrayList.add(graphHelperObject.getLineChangeIdentificatorByLineEnum(seriesID).get(i));
            }
        }
        graphHelperObject.changeLineChangeIdentificator(seriesID,arrayList);
        calculateData(seriesID,color);
        recalculateEquilibrium();
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

        if(graphHelperObject.isLineChangeIdentEmpty())
        {
            initializeChangeIdentificator();
        }
        ArrayList<Integer> identChanges = graphHelperObject.getLineChangeIdentificatorByLineEnum(line);
        //Log.d(TAG,  identChanges.get(3) + " * " + x3 + " * x^3 +"
                //+ identChanges.get(2) + " * " + x2 + " * x^2 +"
                //+ identChanges.get(1) + " * " + x1 + " * x^1 +"
               // + x0 + " + " + identChanges.get(0) );

        if( line == MainScreenController.LineEnum.ProductionCapabilities){
            for (double t = 0.5 * Math.PI; t > 0; t -= 0.05 ) { // <- or different step
                x = 8 * Math.cos(t);
                y = 8 * Math.sin(t);
                seriesLocal.appendData( new DataPoint(x,y), true, 500 );
            }
        }else{
            for( int i=0; i<500; i++){
                x = x + precision;
                y = identChanges.get(3) * x3 * x * x * x + identChanges.get(2) * x2 * x * x + identChanges.get(1) * x1 * x + x0 + identChanges.get(0);
                seriesLocal.appendData( new DataPoint(x,y), true, 500 );
            }
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

    private ArrayList<Double> calculateEqulibrium(MainScreenController.LineEnum curve1,MainScreenController.LineEnum curve2){
        ArrayList<Double> equiPoints = new ArrayList<>();
        LineGraphSeries<DataPoint> data1 = lineGraphSeriesMap.get(curve1);
        LineGraphSeries<DataPoint> data2 = lineGraphSeriesMap.get(curve2);
        double pointX, pointY, diff;
        int x3, x2, x1, x0,x3_2, x2_2, x1_2, x0_2;
        HashMap<MainScreenController.LineEnum,ArrayList<Integer>> seriesSource = graphHelperObject.getSeries();
        ArrayList<Integer> identChanges1 = graphHelperObject.getLineChangeIdentificatorByLineEnum(curve1);
        ArrayList<Integer> identChanges2 = graphHelperObject.getLineChangeIdentificatorByLineEnum(curve2);

        // check if they intersect each other
        if (data1.getHighestValueX() > data2.getLowestValueX()
                && data2.getHighestValueX() > data1.getLowestValueX())
        {
            if (data1.getHighestValueY() > data2.getLowestValueY()
                    && data2.getHighestValueY() > data1.getLowestValueY())
            {
                double minX, maxX, x, y1,y2;
                if (data1.getLowestValueX() > data2.getLowestValueX()){
                    minX = data1.getLowestValueX();
                }else{
                    minX = data2.getLowestValueX();
                }
                if (data1.getHighestValueX() < data2.getHighestValueX()){
                    maxX = data1.getHighestValueX();
                }else{
                    maxX = data2.getHighestValueX();
                }

                int counter = (int) ((maxX - minX)/precision);

                //Log.d(TAG, "calculateEqulibrium: minX[" + minX + "] maxX[" + maxX + "] counter[" + counter + "]");
                x = minX;
                pointX = minX;
                pointY = 0;
                diff = 10000;
                x3 = seriesSource.get(curve1).get(0);
                x2 = seriesSource.get(curve1).get(1);
                x1 = seriesSource.get(curve1).get(2);
                x0 = seriesSource.get(curve1).get(3);
                x3_2 = seriesSource.get(curve2).get(0);
                x2_2 = seriesSource.get(curve2).get(1);
                x1_2 = seriesSource.get(curve2).get(2);
                x0_2 = seriesSource.get(curve2).get(3);
                for (int i = 0; i < counter; i++ ){
                    x = x + precision;
                    y1 = identChanges1.get(3) * x3 * x * x * x + identChanges1.get(2) * x2 * x * x + identChanges1.get(1) * x1 * x + x0 + identChanges1.get(0);
                    y2 = identChanges2.get(3) * x3_2 * x * x * x + identChanges2.get(2) * x2_2 * x * x + identChanges2.get(1) * x1_2 * x + x0_2 + identChanges2.get(0);
                    //Log.d(TAG, "calculateEqulibrium: abs(y1-y2)[" + Math.abs( y1 - y2 ) + "]");
                    if (diff > abs( y1 - y2 )){
                        diff = abs( y1 - y2 );
                        pointX = x;
                        pointY = (y1+y2)/2;
                        //Log.d(TAG, "calculateEqulibrium: pointX[" + pointX + "] pointY[" + pointY + "]");
                    }
                }
                ArrayList<Double> arrayList = new ArrayList<>();
                if (diff < precision){
                    equiPoints.add(pointX);
                    equiPoints.add(pointY);
                    //Log.d(TAG, "calculateEqulibrium: calculated!");
                    return equiPoints;
                }else{
                    //Log.d(TAG, "calculateEqulibrium: not found!");
                }
            }
        }
        return equiPoints;
    }

    private void createShape(ArrayList<DataPoint> arrayList){
        //TODO create shape for to be shown to user
        // probably will not be able to do so
        // possible solution is to draw backgroung with color on curve and white on the lower one
    }

    private void updateMenuTitles() {
        //Log.d(TAG,"UpdateMenuTitles");
        menu.clear();
        for (final MainScreenController.LineEnum line:graphHelperObject.getSeries().keySet()) {
            MenuItem menuItem = menu.add(line.toString());
            menuItem.setIcon(R.drawable.ic_multiline_chart_black_24dp);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    //Log.d(TAG, "onMenuItemClick: " + line.toString());
                    MainScreenController.setChosenLine(line);
                    return false;
                }
            });
        }
    }

    private void initializeChangeIdentificator(){
        for (final MainScreenController.LineEnum line:graphHelperObject.getSeries().keySet()) {
            ArrayList<Integer> arrayList = new ArrayList<>();
            for (int i=0; i < 4; i++){
                arrayList.add(1);
            }
            graphHelperObject.addLineChangeIdentificator(line,arrayList);
        }
    }

    private void recalculateEquilibrium(){
        if (graphHelperObject.getCalculateEqulibrium())
        {
            ArrayList<Double> equiPoints,equiPoints2;
            //Log.d(TAG,"Equilibrium being calculated");
            equiPoints = calculateEqulibrium(graphHelperObject.getEquilibriumCurves().get(0),graphHelperObject.getEquilibriumCurves().get(1));
            if( !equiPoints.isEmpty() ){
                text1.setText("EQ point " + graphHelperObject.getLabelX() + " = " + String.format( "%.2f", equiPoints.get(0)));
                text2.setText("EQ point " + graphHelperObject.getLabelY() + " = " + String.format( "%.2f", equiPoints.get(1)));
            }
            for (MainScreenController.LineEnum keySetLine:graphHelperObject.getSeries().keySet()) {
                for (MainScreenController.LineEnum dependantLine:graphHelperObject.getDependantCurveOnequilibrium()){
                    //Log.d(TAG,"recalculateEquilibrium: " + keySetLine.toString() + " " + " " + dependantLine.toString());
                    if (keySetLine == dependantLine){
                        equiPoints2 = calculateEqulibrium(keySetLine,graphHelperObject.getEquilibriumCurves().get(1));
                        if (equiPoints2.isEmpty()){
                            Log.d(TAG,"recalculateEquilibrium: error");
                        }else if ( compareDoubleWithPrecision(equiPoints2.get(0),equiPoints.get(0)) &&
                                    compareDoubleWithPrecision(equiPoints2.get(1),equiPoints.get(1))){
                            text3.setText("State is stable");
                        }else{
                            text3.setText("State is NOT stable");
                            //Log.d(TAG,"equiPoints2.get(0) == equiPoints.get(0) && equiPoints2.get(1) == equiPoints.get(1))"
                             //       + equiPoints2.get(0)+ " " + equiPoints.get(0)+ " " + equiPoints2.get(1)  + " " + equiPoints.get(1) );
                        }
                    }
                }
            }
        }
    }

    private Boolean compareDoubleWithPrecision(Double firstValue, Double secondValue){
        //Log.d(TAG,"compareDoubleWithPrecision: " + precision + " > " + abs(firstValue-secondValue));
        if ( precision > abs(firstValue-secondValue)){
            //Log.d(TAG,"compareDoubleWithPrecision: return true");
            return true;
        }
        return false;
    }
}
