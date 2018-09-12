package cz.mendelu.tomas.graphpef.graphs;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;

import static java.lang.Math.abs;

/**
 * Created by tomas on 25.08.2018.
 */

public class MarketDS extends DefaultGraph {
    private static final String TAG = "MarketDS";

    public MarketDS(ArrayList<String> texts, ArrayList<MainScreenControllerActivity.LineEnum> movableObjects, MainScreenControllerActivity.LineEnum movableEnum, HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(texts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);

        setMovableDirections(new ArrayList<>(Arrays.asList(MainScreenControllerActivity.Direction.up, MainScreenControllerActivity.Direction.down)));
    }
/*
    @Override
    public void moveObject(MainScreenControllerActivity.Direction dir) {
        //Log.d(TAG, "moveObject");
        ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(getMovableEnum());
        if (dir == MainScreenControllerActivity.Direction.up){
            identChanges.set(0,identChanges.get(0) + 1);
        }else if (dir == MainScreenControllerActivity.Direction.down){
            identChanges.set(0,identChanges.get(0) - 1);
        }
    }
*/
    @Override
        public LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color) {
        if (getLineGraphSeries().get(line) == null) {
            double precision = MainScreenControllerActivity.getPrecision();
            int maxDataPoints = MainScreenControllerActivity.getMaxDataPoints();
            double x,y;
            x = 1;
            int x0,x1;
            HashMap<MainScreenControllerActivity.LineEnum,ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();

            x0 = seriesSource.get(line).get(1);
            x1 = seriesSource.get(line).get(0);

            LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<DataPoint>();
            if (getLineGraphSeries() != null)
                getLineGraphSeries().remove(line);

            ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line);

            for( int i=0; i<maxDataPoints; i++){
                x = x + precision;
                y = x1 * x + x0 + identChanges.get(0);
                seriesLocal.appendData( new DataPoint(x,y), true, maxDataPoints );
            }
            if (line == MainScreenControllerActivity.LineEnum.SupplyDefault || line == MainScreenControllerActivity.LineEnum.DemandDefault){
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                paint.setPathEffect(new DashPathEffect(new float[]{1,1},0));
                seriesLocal.setDrawAsPath(true);
                seriesLocal.setCustomPaint(paint);
                seriesLocal.setThickness(1);
                seriesLocal.setColor(Color.BLUE);
            }else{
                seriesLocal.setThickness(5);
                seriesLocal.setColor(color);
            }
            getLineGraphSeries().put(line, seriesLocal);
            return seriesLocal;
        }else{
            return getLineGraphSeries().get(line);
        }
    }

    @Override
    public ArrayList<Double> calculateEqulibrium() {
        Log.d(TAG,"calculateEqulibrium");
        ArrayList<Double> retVal;
        retVal = super.calculateEqulibrium();
        if (!retVal.isEmpty()){
            ArrayList<MainScreenControllerActivity.LineEnum> lineEnumArrayList = getGraphHelperObject().getDependantCurveOnEquilibrium();
            calculateData(lineEnumArrayList.get(0),Color.BLACK,retVal.get(0),false,retVal);
            calculateData(lineEnumArrayList.get(1),Color.BLACK,retVal.get(1),true,retVal);
            populateTexts(true,retVal);
        }else{
            populateTexts(false,retVal);
        }
        return retVal;
    }

    public void calculateData(MainScreenControllerActivity.LineEnum line, int color, Double limit, boolean vertical, ArrayList<Double> equilibrium) {

        LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<DataPoint>();
        Log.d(TAG,"calculateData eq line[" + line.toString() + "] limit [" + limit + "]");

        if (vertical){
            seriesLocal.appendData(new DataPoint(equilibrium.get(0),0),false,2);
            seriesLocal.appendData(new DataPoint(equilibrium.get(0),limit),false,2);
        }else{
            seriesLocal.appendData(new DataPoint(0,equilibrium.get(1)),false,2);
            seriesLocal.appendData(new DataPoint(limit,equilibrium.get(1)),false,2);
        }

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setPathEffect(new DashPathEffect(new float[]{3,3},0));
        seriesLocal.setDrawAsPath(true);
        seriesLocal.setCustomPaint(paint);
        seriesLocal.setThickness(1);
        getLineGraphSeries().put(line,seriesLocal);
    }

    private void populateTexts(boolean equilibriumExists,ArrayList<Double> equilibrium){
        Log.d(TAG,"populateTexts");
        ArrayList texts = new ArrayList();
        if(equilibriumExists){
            texts.add("Eq " + getGraphHelperObject().getDependantCurveOnEquilibrium().get(0) + " = " + String.format( "%.1f", equilibrium.get(0) ));
            texts.add("Eq " + getGraphHelperObject().getDependantCurveOnEquilibrium().get(1) + " = " + String.format( "%.1f", equilibrium.get(1) ));
        }else{
            texts.add("Eq cannot be calculated");
        }
        for(MainScreenControllerActivity.LineEnum line:getMovableObjects()){
            texts.add("Line " + line.toString() + " changed by " + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0));
        }

        setGraphTexts(texts);
    }
}
