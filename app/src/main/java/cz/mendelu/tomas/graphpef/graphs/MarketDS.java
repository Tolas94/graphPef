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

import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.BudgetLine;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.Demand;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.IndifferentCurve;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.Supply;
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
                seriesLocal.setColor(color);
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
            populateTexts(true,retVal);
        }else{
            populateTexts(false,retVal);
        }
        return retVal;
    }

    private void populateTexts(boolean equilibriumExists,ArrayList<Double> equilibrium){
        Log.d(TAG,"populateTexts");
        refreshInfoTexts();
        ArrayList texts = new ArrayList();
        if(equilibriumExists){
            texts.add("Eq " + getGraphHelperObject().getDependantCurveOnEquilibrium().get(0) + " = " + String.format( "%.1f", equilibrium.get(0) ));
            texts.add("Eq " + getGraphHelperObject().getDependantCurveOnEquilibrium().get(1) + " = " + String.format( "%.1f", equilibrium.get(1) ));
        }else{
            texts.add("Eq cannot be calculated");
        }
        for(MainScreenControllerActivity.LineEnum line:getMovableObjects()){
            texts.add(line.toString() + " changed by " + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0));
        }
        setGraphTexts(texts);
    }

    @Override
    public ArrayList<String> getSituationInfoTexts() {
        Log.d(TAG,"getSituationInfoTexts");
        //https://stackoverflow.com/questions/9290651/make-a-hyperlink-textview-in-android
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Trh je v ekonomice prostor, kde dochází ke směně statků a služeb. Na trhu " +
                "se setkávají nabízející, kteří chtějí směnit za peníze, a poptávající, kteří za " +
                "ně chtějí získat nějaké nové zboží. Cílem prodejců je maximalizace ceny, zatímco " +
                "kupující si přejí pravý opak, cenu co nejnižší.");
        if (getMovableEnum() == Demand ){
            arrayList.add("Poptávka (značí se D, z anglického demand) je křivka, jež vyjadřuje " +
                    "závislost mezi množstvím zboží, které je kupující ochoten koupit, a cenou, " +
                    "jakou je ochoten za zboží zaplatit v určitý čas na určitém místě.");
        }else if (getMovableEnum() == Supply){
            arrayList.add("Nabídka (značí se S, z anglického supply) je ekonomický pojem" +
                    "vyjadřující objem výstupu výroby, který chce vyrábějící subjekt na " +
                    "trhu prodat za určitou cenu.");
        }

        arrayList.add("wll add this later");
        return arrayList;
    }
}
