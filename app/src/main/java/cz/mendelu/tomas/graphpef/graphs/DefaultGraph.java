package cz.mendelu.tomas.graphpef.graphs;

import android.app.FragmentManager;
import android.graphics.Color;
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

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.fragments.InfoFragment;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.interfaces.GraphIfc;

import static java.lang.Math.abs;

/**
 * Created by tomas on 25.08.2018.
 */

public abstract class DefaultGraph implements GraphIfc,Serializable{
    private static final String TAG = "DeafultGraph";
    private ArrayList<String> graphTexts;
    private ArrayList<MainScreenControllerActivity.LineEnum> movableObjects;
    private MainScreenControllerActivity.LineEnum movableEnum;
    private HashMap<MainScreenControllerActivity.LineEnum,ArrayList<Integer> > series;
    private ArrayList<String> optionsLabels;
    private ArrayList<String> infoTexts;
    private GraphHelperObject graphHelperObject;
    private HashMap<MainScreenControllerActivity.LineEnum,LineGraphSeries<DataPoint>> lineGraphSeriesMap;
    private ArrayList<MainScreenControllerActivity.Direction> movableDirections;
    private ArrayList<Double> equiPoints;

    public DefaultGraph(ArrayList<String> graphTexts, ArrayList<MainScreenControllerActivity.LineEnum> movableObjects, MainScreenControllerActivity.LineEnum movableEnum, HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer> > series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        this.graphTexts = graphTexts;
        this.movableObjects = movableObjects;
        this.movableEnum = movableEnum;
        this.series = series;
        this.optionsLabels = optionsLabels;
        this.graphHelperObject = graphHelperObject;
        this.lineGraphSeriesMap = new HashMap<>();
        for (MainScreenControllerActivity.LineEnum line:graphHelperObject.getSeries().keySet()) {
            graphHelperObject.addLineChangeIdentificator(line,new ArrayList<>(Arrays.asList(0,0)));
            if (graphHelperObject.getDependantCurveOnCurve() != null){
                if (graphHelperObject.getDependantCurveOnCurve().get(line) != null) {
                    for (int i = 0; i < graphHelperObject.getDependantCurveOnCurve().get(line).size(); i++) {
                        graphHelperObject.addLineChangeIdentificator(graphHelperObject.getDependantCurveOnCurve().get(line).get(i), new ArrayList<>(Arrays.asList(0, 0)));
                    }
                }
            }
        }
        if (graphHelperObject.getDependantCurveOnEquilibrium() != null) {
            for (MainScreenControllerActivity.LineEnum line: graphHelperObject.getDependantCurveOnEquilibrium()) {
                graphHelperObject.addLineChangeIdentificator(line, new ArrayList<>(Arrays.asList(0, 0)));
            }
        }
    }


    @Override
    public ArrayList<String> getGraphTexts() {
        return graphTexts;
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color) {
        return new LineGraphSeries<>();
    }

    @Override
    public ArrayList<Double> calculateEqulibrium(){
        Log.d(TAG, "calculateEqulibrium:");
        ArrayList<Double> equiPoints = new ArrayList<>();
        if(graphHelperObject.getCalculateEqulibrium() ){
            MainScreenControllerActivity.LineEnum curve1 = graphHelperObject.getEquilibriumCurves().get(0);
            MainScreenControllerActivity.LineEnum curve2 = graphHelperObject.getEquilibriumCurves().get(1);
            Log.d(TAG, "calculateEqulibrium: curve 1 [" + curve1 + "] curve2{" + curve2 + "]");
            LineGraphSeries<DataPoint> data1 = getLineGraphSeries().get(curve1);
            LineGraphSeries<DataPoint> data2 = getLineGraphSeries().get(curve2);
            if(data1 != null &&
               data2 != null){
                Log.d(TAG, "calculateEqulibrium: data not null");

                double precision = MainScreenControllerActivity.getPrecision() ;
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

                            //Log.d(TAG, "data 2 x[" + dataPoint2.getX() + "] data 1 x[" +dataPoint1.getX()+ "]");
                            //Log.d(TAG, "data 2 y[" + dataPoint2.getY() + "] data 1 y[" +dataPoint1.getY()+ "]");
                            //Log.d(TAG, "calculateEqulibrium: diff [" + diff + "] abs [" + abs( dataPoint1.getY() - dataPoint2.getY() ) + "]");

                            if ( abs( dataPoint1.getY() - dataPoint2.getY()) < precision*2) {
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

                ArrayList<MainScreenControllerActivity.LineEnum> lineEnumArrayList = getGraphHelperObject().getDependantCurveOnEquilibrium();
                if (lineEnumArrayList != null){
                    calculateData(lineEnumArrayList.get(0),Color.BLACK,equiPoints.get(0),false,equiPoints);
                    calculateData(lineEnumArrayList.get(1),Color.BLACK,equiPoints.get(1),true,equiPoints);
                }
            }
        }
        this.equiPoints = equiPoints;
        return equiPoints;
    }

    @Override
    public ArrayList<MainScreenControllerActivity.LineEnum> getMovableObjects() {
        return movableObjects;
    }

    @Override
    public void moveObject(MainScreenControllerActivity.Direction dir) {
        moveObject(dir,movableEnum, 1);
    }

    @Override
    public void moveObject(MainScreenControllerActivity.Direction dir, MainScreenControllerActivity.LineEnum line, int precisionModificator) {
        Log.d(TAG,"line " + line.toString() + " dir " + dir.toString());
        int changeX = 0;
        int changeY = 0;
        int maxDataPoints = MainScreenControllerActivity.getMaxDataPoints() * precisionModificator;
        double originX;
        double originY;
        ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line);

        if(dir == MainScreenControllerActivity.Direction.up){
            changeY++;
            identChanges.set(0,identChanges.get(0) + 1);
        }else if(dir == MainScreenControllerActivity.Direction.down){
            changeY--;
            identChanges.set(0,identChanges.get(0) - 1);
        }else if (dir == MainScreenControllerActivity.Direction.left){
            changeX--;
        }else if (dir == MainScreenControllerActivity.Direction.right){
            changeX++;
        }

        LineGraphSeries<DataPoint> newLineGraphSeries = new LineGraphSeries<>();
        Iterator<DataPoint> iterator = lineGraphSeriesMap.get(line).getValues(lineGraphSeriesMap.get(line).getLowestValueX(),lineGraphSeriesMap.get(line).getHighestValueX());

        while (iterator.hasNext()){
            DataPoint dataPoint = iterator.next();
            originX = dataPoint.getX();
            originY = dataPoint.getY();

            newLineGraphSeries.appendData(new DataPoint( originX + changeX ,originY + changeY ),true, maxDataPoints);
        }

        lineGraphSeriesMap.remove(line);
        lineGraphSeriesMap.put(line,newLineGraphSeries);
        refreshInfoTexts();

    }

    @Override
    public HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer> > getSeries() {
        return series;
    }

    @Override
    public HashMap<MainScreenControllerActivity.LineEnum,LineGraphSeries<DataPoint>> getLineGraphSeries(){
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
    public void setMovable(MainScreenControllerActivity.LineEnum movableEnum) {
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

    public void setLineGraphSeriesMap(HashMap<MainScreenControllerActivity.LineEnum, LineGraphSeries<DataPoint>> lineGraphSeriesMap) {
        this.lineGraphSeriesMap = lineGraphSeriesMap;
    }

    @Override
    public MainScreenControllerActivity.LineEnum getMovableEnum() {
        return movableEnum;
    }

    @Override
    public ArrayList<MainScreenControllerActivity.Direction> getMovableDirections() {
        return movableDirections;
    }

    void setMovableDirections(ArrayList<MainScreenControllerActivity.Direction> movableDir){
        movableDirections = movableDir;
    }

    void setOptionsLabels(ArrayList<String> optionsLabels) {
        this.optionsLabels = optionsLabels;
    }

    public void setGraphTexts(ArrayList<String> graphTexts) {
        this.graphTexts = graphTexts;
    }

    @Override
    public ArrayList<MainScreenControllerActivity.LineEnum> getEqDependantCurves() {
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
    public ArrayList<String> getSituationInfoTexts() {
        return null;
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color, Double limit, boolean vertical, ArrayList<Double> equilibrium) {

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
        seriesLocal.setTitle(line.toString());
        getLineGraphSeries().put(line,seriesLocal);
        return seriesLocal;
    }

    @Override
    public ArrayList<MainScreenControllerActivity.LineEnum> getDependantCurves(MainScreenControllerActivity.LineEnum line) {
        if (graphHelperObject.getDependantCurveOnCurve() != null){
            return graphHelperObject.getDependantCurveOnCurve().get(line);
        }
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Double> getEquiPoints() {
        return equiPoints;
    }

    @Override
    public void refreshInfoTexts(){
        if( InfoFragment.getInstance() != null){
            Log.d(TAG,"refreshInfoTexts InfoFragment.getInstance().populateTexts()");
            InfoFragment.getInstance().populateTexts();
        }
    }
}
