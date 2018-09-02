package cz.mendelu.tomas.graphpef.graphs;

import android.graphics.DashPathEffect;
import android.graphics.Paint;

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

        int x0, y0;
        LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<>();

        HashMap<MainScreenController.LineEnum,ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();

        x0 = seriesSource.get(line).get(0);
        y0 = seriesSource.get(line).get(1);

        if (getLineGraphSeries() != null)
            getLineGraphSeries().remove(line);

        for (double t = 0.5 * Math.PI; y>=0; t -= precision ) { // <- or different step
            x = (x0 + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0)) * Math.cos(t);
            y = (y0 + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(1)) * Math.sin(t);
            seriesLocal.appendData( new DataPoint(x,y), true, maxDataPoints );
        }
        seriesLocal.setColor(color);

        if (line == MainScreenController.LineEnum.ProductionCapabilitiesDefault){
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setPathEffect(new DashPathEffect(new float[]{8,5},0));
            seriesLocal.setDrawAsPath(true);
            seriesLocal.setCustomPaint(paint);
            seriesLocal.setThickness(1);

        }else{
            seriesLocal.setThickness(5);
        }
        getLineGraphSeries().put(line, seriesLocal);
        updateTexts();
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

    private double getMaxX(){
        if (getLineGraphSeries().get(MainScreenController.LineEnum.ProductionCapabilities) != null)
            return getLineGraphSeries().get(MainScreenController.LineEnum.ProductionCapabilities).getHighestValueX();
        else
            return 0.0;
    }

    private double getMaxY(){
        if (getLineGraphSeries().get(MainScreenController.LineEnum.ProductionCapabilities) != null)
            return getLineGraphSeries().get(MainScreenController.LineEnum.ProductionCapabilities).getHighestValueY();
        else
            return 0.0;
    }

    private void updateTexts(){
        String text4,text5;
        text4 = "";
        text5 = "";
        if ((int) Math.round(getMaxX()) > 9 ){
            text4 = "Production capability(X) has been extended";
        }else if ((int) Math.round(getMaxX()) == 9 ){
            text4 = "Production capability(X) is on default values";
        }else if ((int) Math.round(getMaxX()) < 9 ){
            text4 = "Production capability(X) has been lowered";
        }
        if ((int) Math.round(getMaxY()) > 9 ){
            text5 = "Production capability(Y) has been extended";
        }else if ((int) Math.round(getMaxY()) == 9 ){
            text5 = "Production capability(Y) is on default values";
        }else if ((int) Math.round(getMaxY()) < 9 ){
            text5 = "Production capability(Y) has been lowered";
        }
        setGraphTexts(new ArrayList<>(Arrays.asList(
                "Max " + getLabelX() + " = " + (int) Math.round(getMaxX()),
                "Max " + getLabelY() + " = " + (int) Math.round(getMaxY()),
                "Default Max Production [9,9]",
                text4,
                text5)));
    }

    @Override
    public ArrayList<Double> calculateEqulibrium() {
        if (getGraphHelperObject().getCalculateEqulibrium())
            return super.calculateEqulibrium();
        else
            return null;
    }
}
