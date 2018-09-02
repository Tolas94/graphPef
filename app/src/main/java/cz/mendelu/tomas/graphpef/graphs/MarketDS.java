package cz.mendelu.tomas.graphpef.graphs;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.GraphHelperObject;
import cz.mendelu.tomas.graphpef.MainScreenController;
import cz.mendelu.tomas.graphpef.R;

import static java.lang.Math.abs;

/**
 * Created by tomas on 25.08.2018.
 */

public class MarketDS extends DefaultGraph {
    private static final String TAG = "MarketDS";

    public MarketDS(ArrayList<String> texts, ArrayList<MainScreenController.LineEnum> movableObjects, MainScreenController.LineEnum movableEnum, HashMap<MainScreenController.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(texts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);

        setMovableDirections(new ArrayList<MainScreenController.Direction>(Arrays.asList(MainScreenController.Direction.up,MainScreenController.Direction.down)));
    }

    @Override
    public void moveObject(MainScreenController.Direction dir) {
        //Log.d(TAG, "moveObject");
        ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(getMovableEnum());
        if (dir == MainScreenController.Direction.up){
            identChanges.set(0,identChanges.get(0) + 1);
        }else if (dir == MainScreenController.Direction.down){
            identChanges.set(0,identChanges.get(0) - 1);
        }
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenController.LineEnum line, int color) {
        double precision = MainScreenController.getPrecision();
        int maxDataPoints = MainScreenController.getMaxDataPoints();
        double x,y;
        x = 1;
        int x3, x2, x1, x0;
        HashMap<MainScreenController.LineEnum,ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();

        x3 = seriesSource.get(line).get(0);
        x2 = seriesSource.get(line).get(1);
        x1 = seriesSource.get(line).get(2);
        x0 = seriesSource.get(line).get(3);

        LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<DataPoint>();
        if (getLineGraphSeries() != null)
            getLineGraphSeries().remove(line);

        ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line);
        /*Log.d(TAG,  identChanges.get(3) + " * " + x3 + " * x^3 +"
        + identChanges.get(2) + " * " + x2 + " * x^2 +"
        + identChanges.get(1) + " * " + x1 + " * x^1 +"
         + x0 + " + " + identChanges.get(0) );*/

        for( int i=0; i<maxDataPoints; i++){
            x = x + precision;
            y = identChanges.get(3) * x3 * x * x * x + identChanges.get(2) * x2 * x * x + identChanges.get(1) * x1 * x + x0 + identChanges.get(0);
            seriesLocal.appendData( new DataPoint(x,y), true, maxDataPoints );
        }
        seriesLocal.setColor(color);
        getLineGraphSeries().put(line, seriesLocal);
        return seriesLocal;
    }

    @Override
    public ArrayList<Double> calculateEqulibrium() {
        Log.d(TAG,"calculateEqulibrium");
        ArrayList<Double> retVal;
        retVal = super.calculateEqulibrium();
        if (!retVal.isEmpty()){
            ArrayList<MainScreenController.LineEnum> lineEnumArrayList = getGraphHelperObject().getDependantCurveOnEquilibrium();
            calculateData(lineEnumArrayList.get(0),Color.BLACK,retVal.get(0),false,retVal);
            calculateData(lineEnumArrayList.get(1),Color.BLACK,retVal.get(1),true,retVal);
        }
        return retVal;
    }

    public void calculateData(MainScreenController.LineEnum line, int color, Double limit, boolean vertical, ArrayList<Double> equilibrium) {

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
}
