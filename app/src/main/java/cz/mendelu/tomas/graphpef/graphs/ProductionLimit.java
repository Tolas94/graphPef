package cz.mendelu.tomas.graphpef.graphs;

import android.view.View;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.GraphHelperObject;
import cz.mendelu.tomas.graphpef.MainScreenController;

/**
 * Created by tomas on 01.09.2018.
 */

public class ProductionLimit extends DefaultGraph {
    public ProductionLimit(ArrayList<String> texts, ArrayList<MainScreenController.LineEnum> movableObjects, MainScreenController.LineEnum movableEnum, HashMap<MainScreenController.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(texts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);

        setMovableDirections(new ArrayList<>(Arrays.asList(
                MainScreenController.Direction.up,
                MainScreenController.Direction.down,
                MainScreenController.Direction.left,
                MainScreenController.Direction.right)));


    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenController.LineEnum line, int color) {
        double precision = MainScreenController.getPrecision();
        int maxDataPoints = MainScreenController.getMaxDataPoints();
        double x,y = 1;

        LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<DataPoint>();

        if (getLineGraphSeries() != null)
            getLineGraphSeries().remove(line);

        for (double t = 0.5 * Math.PI; y>=0; t -= precision ) { // <- or different step
            x = (8 + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0)) * Math.cos(t);
            y = (8 + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(1)) * Math.sin(t);
            seriesLocal.appendData( new DataPoint(x,y), true, maxDataPoints );
        }
        seriesLocal.setColor(color);
        getLineGraphSeries().put(line, seriesLocal);
        return seriesLocal;
    }

    @Override
    public void moveObject(MainScreenController.Direction dir) {
        //Log.d(TAG, "moveObject");
        ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(getMovableEnum());
        switch(dir){
            case up:    identChanges.set(1,identChanges.get(1) + 1);
                break;
            case down:  identChanges.set(1,identChanges.get(1) - 1);
                break;
            case left:  identChanges.set(0,identChanges.get(0) + 1);
                break;
            case right: identChanges.set(0,identChanges.get(0) - 1);
                break;
        }
    }
}