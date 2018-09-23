package cz.mendelu.tomas.graphpef.graphs;

import android.content.Context;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;

import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.AverageCost;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.AverageVariableCost;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.MarginalCost;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.PriceLevel;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.Quantity;

/**
 * Created by tomas on 22.09.2018.
 */

public class CostCurves extends DefaultGraph implements Serializable{
    private static final String TAG = "PerfectMarketFirm";

    public CostCurves(ArrayList<String> graphTexts, ArrayList<MainScreenControllerActivity.LineEnum> movableObjects, MainScreenControllerActivity.LineEnum movableEnum, HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(graphTexts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);
        setMovableDirections(new ArrayList<>(Arrays.asList(MainScreenControllerActivity.Direction.left, MainScreenControllerActivity.Direction.right)));
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color) {
        if (getLineGraphSeries().get(line) == null) {
            double precision = MainScreenControllerActivity.getPrecision();
            int maxDataPoints = MainScreenControllerActivity.getMaxDataPoints();
            double x, y;
            x = 1;
            if (line == PriceLevel){
                x = 0;
            }
            int x0, x1, x2, x3, x_1;
            HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();

            x_1 = seriesSource.get(line).get(4);
            x0 = seriesSource.get(line).get(3);
            x1 = seriesSource.get(line).get(2);
            x2 = seriesSource.get(line).get(1);
            x3 = seriesSource.get(line).get(0);

            LineGraphSeriesSerialisable seriesLocal = new LineGraphSeriesSerialisable();

            for (int i = 0; i < maxDataPoints; i++) {
                x = x + precision;
                y = 0;
                if (line == AverageCost || line == AverageVariableCost) {
                    if (i == 0)
                        Log.d(TAG, "y = (" + x3 + "x^3/3 + " + x2 + "x^2 +" + x1 + "x + " + x0 + " )/" + x_1 + "x");

                    y = ((x3 * x * x * x) / 3 + x2 * x * x + x1 * x + x0) / (x_1 * x);
                } else if (line == MarginalCost) {
                    if (i == 0)
                        Log.d(TAG, "y = (" + x2 + " + x)^2 +" + x1 + "x + " + x0 + " )");
                    y = ((x * x)  + x1 * x + x0);

                } else if (line == Quantity) {
                    x = x0;
                    y = 0;
                    seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
                    calculateLabel(line,x,y);
                    y = 15;
                    seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
                    x = 200;
                    y = 200;
                    i = maxDataPoints;
                }
                if ( i==0 ){
                    calculateLabel(line,x,y);
                }
                if (y < 13 && y > 0 && x > 0 && x < 13) {
                    seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
                }
            }
            Log.d(TAG, "MinY [" + seriesLocal.getLowestValueY() + "] maxY[" + seriesLocal.getHighestValueY() + "]");
            Log.d(TAG, "MinX [" + seriesLocal.getLowestValueX() + "] maxX[" + seriesLocal.getHighestValueX() + "]");
            getLineGraphSeries().put(line, seriesLocal);
            return seriesLocal;
        }else{
            return getLineGraphSeries().get(line);
        }
    }

    @Override
    public void moveObject(MainScreenControllerActivity.Direction dir) {
        super.moveObject(dir);
        if (getMovableEnum() == MainScreenControllerActivity.LineEnum.AverageCost){
            if (dir == MainScreenControllerActivity.Direction.up){
                super.moveObject(MainScreenControllerActivity.Direction.right);
                super.moveObject(MainScreenControllerActivity.Direction.up,MarginalCost, 1);
                super.moveObject(MainScreenControllerActivity.Direction.right,MarginalCost, 1);
                super.moveObject(MainScreenControllerActivity.Direction.up,AverageVariableCost, 1);
                super.moveObject(MainScreenControllerActivity.Direction.right,AverageVariableCost, 1);
            }else if (dir == MainScreenControllerActivity.Direction.down){
                super.moveObject(MainScreenControllerActivity.Direction.left);
                super.moveObject(MainScreenControllerActivity.Direction.down,MarginalCost, 1);
                super.moveObject(MainScreenControllerActivity.Direction.left,MarginalCost, 1);
                super.moveObject(MainScreenControllerActivity.Direction.down,AverageVariableCost, 1);
                super.moveObject(MainScreenControllerActivity.Direction.left,AverageVariableCost, 1);
            }
        }
    }

    @Override
    public ArrayList<String> getSituationInfoTexts() {
        //https://stackoverflow.com/questions/9290651/make-a-hyperlink-textview-in-android
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(getResources().getString(R.string.perfect_market_firm_info_text_1));
        arrayList.add(getResources().getString(R.string.perfect_market_firm_info_text_mc));
        arrayList.add(getResources().getString(R.string.perfect_market_firm_info_text_ac));
        arrayList.add(getResources().getString(R.string.perfect_market_firm_info_text_avc));
        return arrayList;
    }

    private void populateTexts(boolean equilibriumExists, ArrayList<Double> equilibrium){
        Log.d(TAG,"populateTexts");
        refreshInfoTexts();
        ArrayList texts = new ArrayList();
        if (equilibriumExists){
            texts.add(getResources().getString(R.string.equilibrium_is) + " " + getStringFromLineEnum(getGraphHelperObject().getDependantCurveOnEquilibrium().get(0)) + " = " + String.format( "%.1f", equilibrium.get(1) ));
        }else{
            texts.add("Firma odchází");
            texts.add(getResources().getString(R.string.equilibrium_cannot));
        }

        for(MainScreenControllerActivity.LineEnum line:getMovableObjects()){
            if(getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line) != null )
                texts.add(getStringFromLineEnum(line) + " " + getResources().getString(R.string.changed_by) + " " + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0));
        }
        setGraphTexts(texts);
    }

    @Override
    public ArrayList<Double> calculateEqulibrium() {
        Log.d(TAG,"calculateEqulibrium");
        ArrayList<Double> retVal;
        retVal = super.calculateEqulibrium();
        if (!retVal.isEmpty()){
            if (retVal.get(1) < getLineGraphSeries().get(AverageVariableCost).getLowestValueY()){
                retVal = new ArrayList<>();
                populateTexts(false,retVal);
            }else{
                populateTexts(true,retVal);
            }
        }else{
            populateTexts(false,retVal);

        }
        return retVal;
    }



}
