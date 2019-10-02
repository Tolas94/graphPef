package cz.mendelu.tomas.graphpef.graphs;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.GraphControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;

import static cz.mendelu.tomas.graphpef.activities.GraphControllerActivity.LineEnum.BudgetLine;

/**
 * Created by tomas on 12.09.2018.
 */

public class IndifferentAnalysis extends DefaultGraph  implements Serializable {
    private static final String TAG = "IndifferentAnalysis";
    private int numOfEqPoints;

    public IndifferentAnalysis(ArrayList<String> graphTexts, ArrayList<GraphControllerActivity.LineEnum> movableObjects, GraphControllerActivity.LineEnum movableEnum, HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(graphTexts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);
        setMovableDirections(new ArrayList<>(Arrays.asList(
                GraphControllerActivity.Direction.up,
                GraphControllerActivity.Direction.down,
                GraphControllerActivity.Direction.left,
                GraphControllerActivity.Direction.right)));
        numOfEqPoints = 0;
    }


    @Override
    public LineGraphSeries<DataPoint> calculateData(GraphControllerActivity.LineEnum line, int color) {
        if (getLineGraphSeries().get(line) == null) {
            double precision = GraphControllerActivity.getPrecision();
            int maxDataPoints = GraphControllerActivity.getMaxDataPoints();
            Double x,y;
            x = 0.0;
            y = 0.0;
            int arg0,arg1, counter;
            counter = 0;
            HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();


            arg0 = seriesSource.get(line).get(0);
            arg1 = seriesSource.get(line).get(1);

            LineGraphSeriesSerialisable seriesLocal = new LineGraphSeriesSerialisable();

            if (line == GraphControllerActivity.LineEnum.IndifferentCurve) {
                precision = precision/50;
                maxDataPoints = maxDataPoints*50;
                for (int i = 0; i < maxDataPoints; i++) {
                    x = x + precision; // 3 3 1
                    //Log.d(TAG,"calculateData y = 1 / (sqrt( x +" + arg1 + ") + " + arg0);
                    y = 1 /  (Math.sqrt(x + arg1) ) + arg0 + 0.2;
                    if (Double.isNaN(y)){
                        y = 200.0;
                    }
                    //Log.d(TAG,"calculateData x = " + x + " y = " + y);
                    if (y < 13 && y > 0 && x > 0 && x < 13){
                        seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
                        counter++;
                    }
                }
                Log.d(TAG,"calculateData: IndifferentCurve points - " + counter);
            } else if (line == GraphControllerActivity.LineEnum.BudgetLine) {

                //http://www.coolmath.com/algebra/08-lines/12-finding-equation-two-points-01
                float m = ( - arg0 )/arg1;

                for (int i = 0; i < maxDataPoints; i++) {
                    x = x + precision;
                    y = m * (x - arg1);
                    seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
                    counter++;
                }
                Log.d(TAG,"calculateData: BudgetLine points - " + counter);
            }

            Log.d(TAG, "calculateData: MinY [" + seriesLocal.getLowestValueY() + "] maxY[" + seriesLocal.getHighestValueY() + "]");
            Log.d(TAG, "calculateData: MinX [" + seriesLocal.getLowestValueX() + "] maxX[" + seriesLocal.getHighestValueX() + "]");
            getLineGraphSeries().put(line, seriesLocal);
            calculateLabel(line,x,y);
            seriesLocal.setColor(color);
            return seriesLocal;
        }else{
            return getLineGraphSeries().get(line);
        }
    }

    @Override
    public void moveObject(GraphControllerActivity.Direction dir) {

        if (getMovableEnum() == GraphControllerActivity.LineEnum.IndifferentCurve) {
            if (dir == GraphControllerActivity.Direction.up
                    || dir == GraphControllerActivity.Direction.right) {
                super.moveObject(GraphControllerActivity.Direction.up, getMovableEnum(), 100);
                super.moveObject(GraphControllerActivity.Direction.right, getMovableEnum(), 100);
            } else if (dir == GraphControllerActivity.Direction.down
                    || dir == GraphControllerActivity.Direction.left) {
                super.moveObject(GraphControllerActivity.Direction.down, getMovableEnum(), 100);
                super.moveObject(GraphControllerActivity.Direction.left, getMovableEnum(), 100);
            }
        } else if (getMovableEnum() == GraphControllerActivity.LineEnum.BudgetLine) {
            LineGraphSeriesSerialisable seriesLocal = new LineGraphSeriesSerialisable();
            ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(getMovableEnum());
            float arg0 = getSeries().get(BudgetLine).get(0);
            float arg1 = getSeries().get(BudgetLine).get(1);
            if (dir == GraphControllerActivity.Direction.up) {
                identChanges.set(0,identChanges.get(0) + 1);
            } else if (dir == GraphControllerActivity.Direction.down) {
                identChanges.set(0,identChanges.get(0) - 1);

            } else if (dir == GraphControllerActivity.Direction.left) {
                identChanges.set(1,identChanges.get(1) - 1);

            } else if (dir == GraphControllerActivity.Direction.right) {
                identChanges.set(1,identChanges.get(1) + 1);
            }
            Log.d(TAG,"moveObject: arg0["+arg0+"] arg1[" + arg1 + "]");
            Log.d(TAG,"moveObject: ident0["+identChanges.get(0)+"] ident1[" + identChanges.get(1) + "]");

            double precision = GraphControllerActivity.getPrecision() / 100;
            int maxDataPoints = GraphControllerActivity.getMaxDataPoints() * 100;
            float m = ( -(float)identChanges.get(0) - arg0 )/(arg1 + (float)identChanges.get(1));
            double x = 0, y;
            Log.d(TAG,"moveObject: m =" + m);

            for (int i = 0; i < maxDataPoints; i++) {
                x = x + precision;
                y = m * (x - arg1 - identChanges.get(1));
                seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
            }
            getLineGraphSeries().put(getMovableEnum(), seriesLocal);
        }
    }

    @Override
    public ArrayList<Double> calculateEqulibrium() {
        ArrayList<Double> retVal = super.calculateEqulibrium();
        Log.d(TAG,"calculateEqulibrium arraySize["+retVal.size()+"]" );
        if (retVal.size() != 0){
            if (retVal.size() == 2){
                numOfEqPoints = 1;
            } else if (retVal.size() == 4){
                numOfEqPoints = 2;
            }
        }else{
            numOfEqPoints = 0;
        }
        populateTexts(numOfEqPoints,retVal);
        return retVal;
    }

    private void populateTexts(int  numberOfEquilibriums, ArrayList<Double> equilibrium){
        Log.d(TAG,"populateTexts");
        refreshInfoTexts();
        ArrayList texts = new ArrayList();
        for (GraphControllerActivity.LineEnum line : getMovableObjects()) {
            texts.add(getStringFromLineEnum(line) + " " + getResources().getString(R.string.changed_by) + " " + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0));
            if (line == BudgetLine){
                texts.add(getStringFromLineEnum(line) + " " + getResources().getString(R.string.changed_by) + " " + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(1));
            }
        }
        if(numberOfEquilibriums == 1){
            texts.add(getResources().getString(R.string.equilibrium_is) + " [" + String.format( "%.1f",equilibrium.get(0)) + "][" + String.format( "%.1f",equilibrium.get(1))+ "]");
        }else if (numberOfEquilibriums == 2){
            texts.add(getResources().getString(R.string.equilibrium_are) + " [" + String.format( "%.1f",equilibrium.get(0)) + "][" + String.format( "%.1f",equilibrium.get(1))+ "] " +
                    getResources().getString(R.string.and) + " [" + String.format( "%.1f",equilibrium.get(2)) + "][" + String.format( "%.1f",equilibrium.get(3)) + "]" );
        }else if (numberOfEquilibriums == 0){
            texts.add(getResources().getString(R.string.equilibrium_cannot));
        }

        setGraphTexts(texts);
    }

    @Override
    public List<ArrayList<String>> getSituationInfoTexts() {
        Log.d(TAG,"getSituationInfoTexts");
        //https://stackoverflow.com/questions/9290651/make-a-hyperlink-textview-in-android
        List<ArrayList<String>> arrayList = new ArrayList<>();

        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.indifferent_analysis_info_budget_line_title),"",getResources().getString(R.string.indifferent_analysis_info_budget_line))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.indifferent_analysis_info_indifferent_curve_title),"",getResources().getString(R.string.indifferent_analysis_info_indifferent_curve))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.indifferent_analysis_info_indifferent_curve_title),"",getResources().getString(R.string.indifferent_analysis_info_text_2))));
        if (numOfEqPoints == 0){
            //0 eq
            arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.indifferent_analysis_info_text_situation_title),"",getResources().getString(R.string.indifferent_analysis_info_text_situation_1))));
        }else if (numOfEqPoints == 1){
            //1 eq
            arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.indifferent_analysis_info_text_situation_title),"",getResources().getString(R.string.indifferent_analysis_info_text_situation_2))));
        }else if (numOfEqPoints == 2) {
            //2 eq
            arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.indifferent_analysis_info_text_situation_title),"",getResources().getString(R.string.indifferent_analysis_info_text_situation_3))));
        }
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.indifferent_analysis_info_text_3_title),"",getResources().getString(R.string.indifferent_analysis_info_text_3))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.indifferent_analysis_info_text_4_title),"",getResources().getString(R.string.indifferent_analysis_info_text_4))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.indifferent_analysis_info_text_5_title),"",getResources().getString(R.string.indifferent_analysis_info_text_5))));

        return arrayList;
    }
}
