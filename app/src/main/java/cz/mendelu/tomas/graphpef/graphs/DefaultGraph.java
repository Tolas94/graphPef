package cz.mendelu.tomas.graphpef.graphs;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.interfaces.GraphIfc;

import static java.lang.Math.abs;

/**
 * Created by tomas on 25.08.2018.
 */

public abstract class DefaultGraph implements GraphIfc,Serializable{
    private static final String TAG = "Deaful;Graph";
    private ArrayList<String> graphTexts;
    private ArrayList<MainScreenControllerActivity.LineEnum> movableObjects;
    private MainScreenControllerActivity.LineEnum movableEnum;
    private HashMap<MainScreenControllerActivity.LineEnum,ArrayList<Integer> > series;
    private ArrayList<String> optionsLabels;
    private ArrayList<String> infoTexts;
    private GraphHelperObject graphHelperObject;
    private HashMap<MainScreenControllerActivity.LineEnum,LineGraphSeries<DataPoint>> lineGraphSeriesMap;
    private ArrayList<MainScreenControllerActivity.Direction> movableDirections;

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
        ArrayList<Double> equiPoints = new ArrayList<>();
        MainScreenControllerActivity.LineEnum curve1 = graphHelperObject.getEquilibriumCurves().get(0);
        MainScreenControllerActivity.LineEnum curve2 = graphHelperObject.getEquilibriumCurves().get(1);
        LineGraphSeries<DataPoint> data1 = getLineGraphSeries().get(curve1);
        LineGraphSeries<DataPoint> data2 = getLineGraphSeries().get(curve2);
        if(graphHelperObject.getCalculateEqulibrium() &&
                data1 != null &&
                data2 != null){

            double precision = MainScreenControllerActivity.getPrecision();




            double pointX, pointY, diff;
            //int x3, x2, x1, x0,x3_2, x2_2, x1_2, x0_2;
            //HashMap<MainScreenControllerActivity.LineEnum,ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();
            //ArrayList<Integer> identChanges1 = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(curve1);
            //ArrayList<Integer> identChanges2 = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(curve2);

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

                    Iterator<DataPoint> dataIt = data1.getValues(minX,maxX);

                    diff = 10000;
                    x = 0;
                    pointX = minX;
                    pointY = 0;
                    while ( dataIt.hasNext() ) {
                        DataPoint dataPoint1 = dataIt.next();

                        float dataX = (float)dataPoint1.getX();
                        Log.d(TAG, "datax[" + dataX + "] getX [" +dataPoint1.getX()+ "]");
                        DataPoint dataPoint2 = data2.findDataPointAtX(dataX);
                        if (dataPoint2 != null){
                            Log.d(TAG, "calculateEqulibrium: diff [" + diff + "] abs [" + abs( dataPoint1.getY() - dataPoint2.getY() ) + "]");
                            if (diff > abs( dataPoint1.getY() - dataPoint2.getY() )){
                                diff = abs( dataPoint1.getY() - dataPoint2.getY() );
                                pointX = x;
                                pointY = (dataPoint1.getY() + dataPoint2.getY())/2;
                                Log.d(TAG, "calculateEqulibrium: pointX[" + pointX + "] pointY[" + pointY + "]");
                            }
                        }else{
                            Log.d(TAG, "datapoint 2 is null");
                        }
                    }
                    /*
                    int counter = (int) ((maxX - minX)/precision);

                    //Log.d(TAG, "calculateEqulibrium: minX[" + minX + "] maxX[" + maxX + "] counter[" + counter + "]");
                    x = minX;
                    pointX = minX;
                    pointY = 0;
                    diff = 10000;
                    //x3 = seriesSource.get(curve1).get(0);
                    //x2 = seriesSource.get(curve1).get(1);
                    x1 = seriesSource.get(curve1).get(0);//2);
                    x0 = seriesSource.get(curve1).get(1);//3);
                    //x3_2 = seriesSource.get(curve2).get(0);
                    //x2_2 = seriesSource.get(curve2).get(1);
                    x1_2 = seriesSource.get(curve2).get(0);//2);
                    x0_2 = seriesSource.get(curve2).get(1);//3);
                    for (int i = 0; i < counter; i++ ){
                        x = x + precision;
                        y1 = /*x3   * x * x * x +  x2   * x * x +*/ //x1   * x + x0   + identChanges1.get(0);
                        //y2 = /*x3_2 * x * x * x +  x2_2 * x * x +*/ x1_2 * x + x0_2 + identChanges2.get(0);
                        //Log.d(TAG, "calculateEqulibrium: abs(y1-y2)[" + Math.abs( y1 - y2 ) + "]");
                        /*if (diff > abs( y1 - y2 )){
                            diff = abs( y1 - y2 );
                            pointX = x;
                            pointY = (y1+y2)/2;
                            Log.d(TAG, "calculateEqulibrium: pointX[" + pointX + "] pointY[" + pointY + "]");
                        }
                    }*/
                    if (diff < precision){
                        equiPoints.add(pointX);
                        equiPoints.add(pointY);
                        Log.d(TAG, "calculateEqulibrium: pointX[" + pointX + "] pointY[" + pointY + "]");
                        Log.d(TAG, "calculateEqulibrium: calculated!");
                        return equiPoints;
                    }else{
                        Log.d(TAG, "calculateEqulibrium: not found!");
                    }
                }
            }
        }
        return equiPoints;
    }

    @Override
    public ArrayList<MainScreenControllerActivity.LineEnum> getMovableObjects() {
        return movableObjects;
    }

    @Override
    public void moveObject(MainScreenControllerActivity.Direction dir) {
        int changeX = 0;
        int changeY = 0;
        int maxDataPoints = MainScreenControllerActivity.getMaxDataPoints();
        double originX = 0;
        double originY = 0;
        ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(getMovableEnum());

        if(dir == MainScreenControllerActivity.Direction.up){
            changeY++;
            //identChanges.set(0,identChanges.get(0) + 1);
        }else if(dir == MainScreenControllerActivity.Direction.down){
            changeY--;
            //identChanges.set(0,identChanges.get(0) - 1);
        }else if (dir == MainScreenControllerActivity.Direction.left){
            changeX--;
        }else if (dir == MainScreenControllerActivity.Direction.right){
            changeX++;
        }

        LineGraphSeries<DataPoint> newLineGraphSeries = new LineGraphSeries<>();
        Iterator<DataPoint> iterator = lineGraphSeriesMap.get(movableEnum).getValues(lineGraphSeriesMap.get(movableEnum).getLowestValueX(),lineGraphSeriesMap.get(movableEnum).getHighestValueX());

        while (iterator.hasNext()){
            DataPoint dataPoint = iterator.next();
            originX = dataPoint.getX();
            originY = dataPoint.getY();

            newLineGraphSeries.appendData(new DataPoint( originX + changeX ,originY+changeY ),true, maxDataPoints);
        }

        lineGraphSeriesMap.remove(movableEnum);
        lineGraphSeriesMap.put(movableEnum,newLineGraphSeries);

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
    public ArrayList<String> getInfoTexts() {
        return null;
    }
}
