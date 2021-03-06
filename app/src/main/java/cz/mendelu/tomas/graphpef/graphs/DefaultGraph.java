package cz.mendelu.tomas.graphpef.graphs;

import android.content.res.Resources;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import androidx.core.content.ContextCompat;
import cz.mendelu.tomas.graphpef.MainAppClass;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.GraphControllerActivity;
import cz.mendelu.tomas.graphpef.fragments.InfoFragment;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;
import cz.mendelu.tomas.graphpef.helperObjects.PositionPair;
import cz.mendelu.tomas.graphpef.interfaces.GraphIfc;

import static java.lang.Math.abs;

/**
 * Created by tomas on 25.08.2018.
 */

public abstract class DefaultGraph implements GraphIfc,Serializable{
    private static final String TAG = "DeafultGraph";

    private ArrayList<String> graphTexts;
    private ArrayList<GraphControllerActivity.LineEnum> movableObjects;
    private GraphControllerActivity.LineEnum movableEnum;
    private HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> series;
    private ArrayList<String> optionsLabels;
    private ArrayList<String> infoTexts;
    private GraphHelperObject graphHelperObject;
    private HashMap<GraphControllerActivity.LineEnum, LineGraphSeriesSerialisable> lineGraphSeriesMap;
    private ArrayList<GraphControllerActivity.Direction> movableDirections;
    private ArrayList<Double> equiPoints;
    private Boolean labelOnstartOfCurve;

    public DefaultGraph(ArrayList<String> graphTexts, ArrayList<GraphControllerActivity.LineEnum> movableObjects, GraphControllerActivity.LineEnum movableEnum, HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        this.graphTexts = graphTexts;
        this.movableObjects = movableObjects;
        this.movableEnum = movableEnum;
        this.series = series;
        this.optionsLabels = optionsLabels;
        this.graphHelperObject = graphHelperObject;
        this.lineGraphSeriesMap = new HashMap<>();
        labelOnstartOfCurve = false;
        for (GraphControllerActivity.LineEnum line : graphHelperObject.getSeries().keySet()) {
            graphHelperObject.addLineChangeIdentificator(line,new ArrayList<>(Arrays.asList(0,0)));
            if (graphHelperObject.getDependantCurveOnCurve() != null){
                if (graphHelperObject.getDependantCurveOnCurve().get(line) != null) {
                    for (int i = 0; i < graphHelperObject.getDependantCurveOnCurve().get(line).size(); i++) {
                        Log.d(TAG, "getDependantCurveOnCurve: " + graphHelperObject.getDependantCurveOnCurve().get(line).get(i).toString());
                        graphHelperObject.addLineChangeIdentificator(graphHelperObject.getDependantCurveOnCurve().get(line).get(i), new ArrayList<>(Arrays.asList(0, 0)));
                    }
                }
            }
            getGraphHelperObject().setLineLabelPosition(line,new PositionPair(0.0,0.0));
        }
        if (graphHelperObject.getDependantCurveOnEquilibrium() != null) {
            for (GraphControllerActivity.LineEnum line : graphHelperObject.getDependantCurveOnEquilibrium()) {
                graphHelperObject.addLineChangeIdentificator(line, new ArrayList<>(Arrays.asList(0, 0)));
            }
        }
    }


    @Override
    public ArrayList<String> getGraphTexts() {
        return graphTexts;
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(GraphControllerActivity.LineEnum line, int color) {
        return new LineGraphSeries<>();
    }

    @Override
    public ArrayList<Double> calculateEqulibrium(){

        Log.d(TAG, "calculateEqulibrium:");
        ArrayList<Double> equiPoints = new ArrayList<>();
        if(graphHelperObject.getCalculateEqulibrium() ){
            GraphControllerActivity.LineEnum curve1 = graphHelperObject.getEquilibriumCurves().get(0);
            GraphControllerActivity.LineEnum curve2 = graphHelperObject.getEquilibriumCurves().get(1);
            Log.d(TAG, "calculateEqulibrium: curve 1 [" + curve1 + "] curve2{" + curve2 + "]");
            LineGraphSeries<DataPoint> data1 = getLineGraphSeries().get(curve1);
            LineGraphSeries<DataPoint> data2 = getLineGraphSeries().get(curve2);
            if(data1 != null &&
               data2 != null){
                Log.d(TAG, "calculateEqulibrium: data not null");

                double precision = GraphControllerActivity.getPrecision();
                double pointX, pointY, diff, diff2, pointX2, pointY2;
                Log.d(TAG, "calculateEqulibrium: " + data1.getHighestValueX()
                        + " " + data1.getLowestValueX()
                        + " " + data2.getHighestValueX()
                        + " " + data2.getLowestValueX());
                Log.d(TAG, "calculateEqulibrium: " + data1.getHighestValueY()
                        + " " + data1.getLowestValueY()
                        + " " + data2.getHighestValueY()
                        + " " + data2.getLowestValueY());
                if (data1.getHighestValueX() > data2.getLowestValueX()
                        && data2.getHighestValueX() > data1.getLowestValueX())
                {
                    if (data1.getHighestValueY() > data2.getLowestValueY()
                            && data2.getHighestValueY() > data1.getLowestValueY())
                    {
                        double minX, maxX;
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

                        Iterator<DataPoint> dataIt1 = data1.getValues(minX,maxX);
                        Iterator<DataPoint> dataIt2 = data2.getValues(minX,maxX);
                        diff = 10000;
                        diff2 = 10001;
                        pointX = 0;
                        pointY = 0;
                        pointX2 = 0;
                        pointY2 = 0;
                        Log.d(TAG, "minX[" + minX + "] maxX[" + maxX + "]");

                        while ( dataIt1.hasNext() && dataIt2.hasNext()) {
                            DataPoint dataPoint1 = dataIt1.next();
                            DataPoint dataPoint2 = dataIt2.next();
                            if (dataPoint1.getX() != dataPoint2.getX()){
                                Log.d(TAG, "calculatedata: data 2 x[" + dataPoint2.getX() + "] data 1 x[" +dataPoint1.getX()+ "]");
                                if(dataPoint1.getX() > dataPoint2.getX()){
                                    while (dataIt2.hasNext() && (dataPoint1.getX() - dataPoint2.getX()) > precision){
                                        dataPoint2 = dataIt2.next();
                                        Log.d(TAG, "calculatedata inner while: data 2 x[" + dataPoint2.getX() + "] data 1 x[" +dataPoint1.getX()+ "]");
                                    }
                                }else{
                                    while (dataIt1.hasNext() && (dataPoint2.getX() - dataPoint1.getX()) > precision){
                                        dataPoint1 = dataIt1.next();
                                        Log.d(TAG, "calculatedata inner while: data 2 x[" + dataPoint2.getX() + "] data 1 x[" +dataPoint1.getX()+ "]");
                                    }
                                }
                            }

                            //Log.d(TAG, "data 2 y[" + dataPoint2.getY() + "] data 1 y[" +dataPoint1.getY()+ "]");
                            //Log.d(TAG, "calculateEqulibrium: diff [" + diff + "] abs [" + abs( dataPoint1.getY() - dataPoint2.getY() ) + "]");

                            if ( abs( dataPoint1.getY() - dataPoint2.getY()) < precision*2.5) {
                                //Log.d(TAG, "calculateEqulibrium: pointX [" + dataPoint1.getX() + "] pointY [" + dataPoint1.getY() + "]");
                                if (diff2 > abs(dataPoint1.getY() - dataPoint2.getY())) {
                                    if (diff > abs(dataPoint1.getY() - dataPoint2.getY())) {
                                        if (abs(dataPoint1.getX() - pointX) > 0.3) {
                                            diff2 = diff;
                                            pointX2 = pointX;
                                            pointY2 = pointY;
                                        }
                                        diff = abs(dataPoint1.getY() - dataPoint2.getY());
                                        pointX = dataPoint2.getX();
                                        pointY = (dataPoint1.getY() + dataPoint2.getY()) / 2;
                                        Log.d(TAG, "calculateEqulibrium: pointX[" + pointX + "] pointY[" + pointY + "]");
                                    } else {
                                        if (abs(dataPoint1.getX() - pointX) > 0.5) {
                                            diff2 = abs(dataPoint1.getY() - dataPoint2.getY());
                                            pointX2 = dataPoint2.getX();
                                            pointY2 = (dataPoint1.getY() + dataPoint2.getY()) / 2;
                                        }
                                    } // end diff 1

                                } //end diff 2
                                //Log.d(TAG, "calculateEqulibrium: diff [" + diff + "] diff2 [" + diff2 + "]");
                            } //end if precision
                        } // end while
                        //Log.d(TAG, "calculateEqulibrium: pointX[" + pointX + "] pointY[" + pointY + "]");
                        //Log.d(TAG, "calculateEqulibrium: calculated!");
                        if ( pointX != 0 ){
                            if ( pointX2 != 0 ) {
                                if (pointX < pointX2) {
                                    equiPoints.add(pointX);
                                    equiPoints.add(pointY);
                                    equiPoints.add(pointX2);
                                    equiPoints.add(pointY2);
                                } else {
                                    equiPoints.add(pointX2);
                                    equiPoints.add(pointY2);
                                    equiPoints.add(pointX);
                                    equiPoints.add(pointY);
                                }
                            }else{
                                equiPoints.add(pointX);
                                equiPoints.add(pointY);
                            }
                            Log.d(TAG, "calculateEqulibrium: pointX [" + pointX + "] pointY [" + pointY + "]");
                            Log.d(TAG, "calculateEqulibrium: pointX2[" + pointX2 + "] pointY2[" + pointY2 + "]");
                            //Log.d(TAG, "calculateEqulibrium: pointX [" + pointX + "] pointY [" + pointY + "]");
                            //Log.d(TAG, "calculateEqulibrium: pointX2[" + pointX2 + "] pointY2[" + pointY2 + "]");
                            Log.d(TAG, "calculateEqulibrium: calculated!");
                        }else{
                            Log.d(TAG, "calculateEqulibrium: not found!");
                        }
                    }
                }
            }

            if (!equiPoints.isEmpty()){
                Log.d(TAG, "calculateEqulibrium: equiPoints not empty!");
                showCurvesDependantOnEquilibrium(equiPoints.get(0),equiPoints.get(1));
            }
        }
        this.equiPoints = equiPoints;
        return equiPoints;
    }

    @Override
    public ArrayList<GraphControllerActivity.LineEnum> getMovableObjects() {
        return movableObjects;
    }

    @Override
    public void moveObject(GraphControllerActivity.Direction dir) {
        moveObject(dir,movableEnum, 1);
    }

    @Override
    public void moveObject(GraphControllerActivity.Direction dir, GraphControllerActivity.LineEnum line, int precisionModificator) {
        Log.d(TAG,"line " + line.toString() + " dir " + dir.toString());
        int changeX = 0;
        int changeY = 0;
        int maxDataPoints = GraphControllerActivity.getMaxDataPoints() * precisionModificator;
        double originX = 0;
        double originY = 0;
        ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line);

        if (dir == GraphControllerActivity.Direction.up) {
            changeY++;
            identChanges.set(0,identChanges.get(0) + 1);
        } else if (dir == GraphControllerActivity.Direction.down) {
            changeY--;
            identChanges.set(0,identChanges.get(0) - 1);
        } else if (dir == GraphControllerActivity.Direction.left) {
            changeX--;
            identChanges.set(0,identChanges.get(0) - 1);
        } else if (dir == GraphControllerActivity.Direction.right) {
            changeX++;
            identChanges.set(0,identChanges.get(0) + 1);
        }

        LineGraphSeriesSerialisable newLineGraphSeries = new LineGraphSeriesSerialisable();
        Iterator<DataPoint> iterator = lineGraphSeriesMap.get(line).getValues(lineGraphSeriesMap.get(line).getLowestValueX(),lineGraphSeriesMap.get(line).getHighestValueX());
        boolean labelchanged = false;
        while (iterator.hasNext()){
            DataPoint dataPoint = iterator.next();
            originX = dataPoint.getX();
            originY = dataPoint.getY();

            if (!labelchanged && labelOnstartOfCurve){
                labelchanged = true;
                calculateLabel(line,originX + changeX,originY + changeY);
            }
            newLineGraphSeries.appendData(new DataPoint( originX + changeX ,originY + changeY ),true, maxDataPoints);
        }
        if (!labelOnstartOfCurve){
            calculateLabel(line,originX + changeX,originY + changeY);
        }
        newLineGraphSeries.setColor(getColorOf(line));
        lineGraphSeriesMap.remove(line);
        lineGraphSeriesMap.put(line,newLineGraphSeries);
        refreshInfoTexts();
    }

    @Override
    public HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> getSeries() {
        return series;
    }

    @Override
    public HashMap<GraphControllerActivity.LineEnum, LineGraphSeriesSerialisable> getLineGraphSeries() {
        return lineGraphSeriesMap;
    }
    @Override
    public ArrayList<String> getOptionsLabels() {
        return optionsLabels;
    }

    @Override
    public String getMovableLabel() {
        return movableEnum.toString();
    }

    @Override
    public void setMovable(GraphControllerActivity.LineEnum movableEnum) {
        this.movableEnum = movableEnum;
    }

    @Override
    public String getTitle() {
        return graphHelperObject.getTitle();
    }

    @Override
    public String getLabelX() {
        return graphHelperObject.getLabelX();
    }

    @Override
    public String getLabelY() {
        return graphHelperObject.getLabelY();
    }

    protected Boolean compareDoubleWithPrecision(Double firstValue, Double secondValue, Double precision){
        //Log.d(TAG,"compareDoubleWithPrecision: " + precision + " > " + abs(firstValue-secondValue));
        if ( precision > abs(firstValue-secondValue)){
            //Log.d(TAG,"compareDoubleWithPrecision: return true");
            return true;
        }
        return false;
    }


    public GraphHelperObject getGraphHelperObject() {
        return graphHelperObject;
    }

    public void setGraphHelperObject(GraphHelperObject graphHelperObject) {
        this.graphHelperObject = graphHelperObject;
    }

    public void setLineGraphSeriesMap(HashMap<GraphControllerActivity.LineEnum, LineGraphSeriesSerialisable> lineGraphSeriesMap) {
        this.lineGraphSeriesMap = lineGraphSeriesMap;
    }

    @Override
    public GraphControllerActivity.LineEnum getMovableEnum() {
        return movableEnum;
    }

    @Override
    public ArrayList<GraphControllerActivity.Direction> getMovableDirections() {
        return movableDirections;
    }

    void setMovableDirections(ArrayList<GraphControllerActivity.Direction> movableDir) {
        movableDirections = movableDir;
    }

    void setOptionsLabels(ArrayList<String> optionsLabels) {
        this.optionsLabels = optionsLabels;
    }

    public void setGraphTexts(ArrayList<String> graphTexts) {
        this.graphTexts = graphTexts;
    }

    @Override
    public ArrayList<GraphControllerActivity.LineEnum> getEqDependantCurves() {
        if (graphHelperObject.getCalculateEqulibrium())
            return graphHelperObject.getDependantCurveOnEquilibrium();
        else
            return new ArrayList<>();
    }

    @Override
    public ArrayList<String> getGraphInfoTexts() {
        return null;
    }

    @Override
    public List<ArrayList<String>> getSituationInfoTexts() {
        return null;
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(GraphControllerActivity.LineEnum line, int color, Double limit, boolean vertical, ArrayList<Double> equilibrium) {

        LineGraphSeriesSerialisable seriesLocal = new LineGraphSeriesSerialisable();
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
        seriesLocal.setColor(color);
        getLineGraphSeries().put(line,seriesLocal);
        return seriesLocal;
    }

    @Override
    public ArrayList<GraphControllerActivity.LineEnum> getDependantCurves(GraphControllerActivity.LineEnum line) {
        if (graphHelperObject.getDependantCurveOnCurve() != null){
            return graphHelperObject.getDependantCurveOnCurve().get(line);
        }
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Double> getEquiPoints() {
        if (graphHelperObject.getShowEquilibrium()){
            return equiPoints;
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    public void refreshInfoTexts(){
        if( InfoFragment.getInstance() != null){
            //Log.d(TAG,"refreshInfoTexts InfoFragment.getInstance().populateTexts()");
            InfoFragment.getInstance().populateTexts();
        }
    }

    public Resources getResources() {
        return MainAppClass.getContext().getResources();
    }

    public String getStringFromLineEnum(GraphControllerActivity.LineEnum lineEnum) {
        return getResources().getString(getResources().getIdentifier(lineEnum.toString(),"string", MainAppClass.getContext().getPackageName()));
    }

    public String getStringFromGraphEnum(GraphControllerActivity.GraphEnum graphEnum) {
        return getResources().getString(getResources().getIdentifier(graphEnum.toString(),"string", MainAppClass.getContext().getPackageName()));
    }

    @Override
    public PositionPair getLineLabelPosition(GraphControllerActivity.LineEnum line) {
        return graphHelperObject.getLineLabelPosition(line);
    }

    protected void calculateLabel(GraphControllerActivity.LineEnum line, double x, double y) {
        Log.d(TAG, "calculateLabel: line [" + line + "] x[" + x + "] y[" + y + "]");
        if (x < 0){
            x = 0;
        }
        if (x > 14){
            x = 14;
        }
        if (y < 0){
            y = 0;
        }
        if (y > 14){
            y = 14;
        }
        getGraphHelperObject().setLineLabelPosition(line,new PositionPair(x,y));
    }

    @Override
    public int getColorOf(GraphControllerActivity.LineEnum lineEnum) {
        if (lineEnum == GraphControllerActivity.LineEnum.Equilibrium) {
            return ContextCompat.getColor(MainAppClass.getContext(), R.color.colorPrimary);
        } else if ( lineEnum == getMovableEnum() ){
            return ContextCompat.getColor(MainAppClass.getContext(), R.color.colorPrimary);
        }
        return ContextCompat.getColor(MainAppClass.getContext(), R.color.black);
    }

    public Boolean getLabelOnstartOfCurve() {
        return labelOnstartOfCurve;
    }

    public void setLabelOnstartOfCurve(Boolean labelOnstartOfCurve) {
        this.labelOnstartOfCurve = labelOnstartOfCurve;
    }

    @Override
    public void showCurvesDependantOnEquilibrium(double x,double y) {
        ArrayList<GraphControllerActivity.LineEnum> lineEnumArrayList = getGraphHelperObject().getDependantCurveOnEquilibrium();
        if (lineEnumArrayList != null){
            calculateData(lineEnumArrayList.get(0),getColorOf(lineEnumArrayList.get(0)),x,false,new ArrayList<>(Arrays.asList(x,y)));
            if(lineEnumArrayList.size()>1){
                calculateData(lineEnumArrayList.get(1),getColorOf(lineEnumArrayList.get(1)),y,true,new ArrayList<>(Arrays.asList(x,y)));
            }
        }
    }
}
