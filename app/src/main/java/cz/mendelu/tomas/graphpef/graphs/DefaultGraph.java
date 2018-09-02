package cz.mendelu.tomas.graphpef.graphs;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.GraphHelperObject;
import cz.mendelu.tomas.graphpef.GraphIfc;
import cz.mendelu.tomas.graphpef.InfoHelper;
import cz.mendelu.tomas.graphpef.MainScreenController;

import static java.lang.Math.abs;

/**
 * Created by tomas on 25.08.2018.
 */

public abstract class DefaultGraph implements GraphIfc,Serializable{
    private ArrayList<String> graphTexts;
    private ArrayList<MainScreenController.LineEnum> movableObjects;
    private MainScreenController.LineEnum movableEnum;
    private HashMap<MainScreenController.LineEnum,ArrayList<Integer> > series;
    private ArrayList<String> optionsLabels;
    private ArrayList<String> infoTexts;
    private GraphHelperObject graphHelperObject;
    private HashMap<MainScreenController.LineEnum,LineGraphSeries<DataPoint>> lineGraphSeriesMap;
    private ArrayList<MainScreenController.Direction> movableDirections;

    public DefaultGraph(ArrayList<String> graphTexts, ArrayList<MainScreenController.LineEnum> movableObjects, MainScreenController.LineEnum movableEnum, HashMap<MainScreenController.LineEnum, ArrayList<Integer> > series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        this.graphTexts = graphTexts;
        this.movableObjects = movableObjects;
        this.movableEnum = movableEnum;
        this.series = series;
        this.optionsLabels = optionsLabels;
        this.graphHelperObject = graphHelperObject;
        this.lineGraphSeriesMap = new HashMap<>();
        for (MainScreenController.LineEnum line:graphHelperObject.getSeries().keySet()) {
            graphHelperObject.addLineChangeIdentificator(line,new ArrayList<>(Arrays.asList(0,0)));
        }
    }


    @Override
    public ArrayList<String> getGraphTexts() {
        return graphTexts;
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenController.LineEnum line, int color) {
        return new LineGraphSeries<>();
    }

    @Override
    public ArrayList<Double> calculateEqulibrium(){
        double precision = MainScreenController.getPrecision();
        MainScreenController.LineEnum curve1 = graphHelperObject.getEquilibriumCurves().get(0);
        MainScreenController.LineEnum curve2 = graphHelperObject.getEquilibriumCurves().get(1);

        ArrayList<Double> equiPoints = new ArrayList<>();

        LineGraphSeries<DataPoint> data1 = getLineGraphSeries().get(curve1);
        LineGraphSeries<DataPoint> data2 = getLineGraphSeries().get(curve2);
        double pointX, pointY, diff;
        int x3, x2, x1, x0,x3_2, x2_2, x1_2, x0_2;
        HashMap<MainScreenController.LineEnum,ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();
        ArrayList<Integer> identChanges1 = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(curve1);
        ArrayList<Integer> identChanges2 = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(curve2);

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
                    y1 = /*x3   * x * x * x +  x2   * x * x +*/ x1   * x + x0   + identChanges1.get(0);
                    y2 = /*x3_2 * x * x * x +  x2_2 * x * x +*/ x1_2 * x + x0_2 + identChanges2.get(0);
                    //Log.d(TAG, "calculateEqulibrium: abs(y1-y2)[" + Math.abs( y1 - y2 ) + "]");
                    if (diff > abs( y1 - y2 )){
                        diff = abs( y1 - y2 );
                        pointX = x;
                        pointY = (y1+y2)/2;
                        //Log.d(TAG, "calculateEqulibrium: pointX[" + pointX + "] pointY[" + pointY + "]");
                    }
                }
                ArrayList<Double> arrayList = new ArrayList<>();
                if (diff < precision){
                    equiPoints.add(pointX);
                    equiPoints.add(pointY);
                    //Log.d(TAG, "calculateEqulibrium: calculated!");
                    return equiPoints;
                }else{
                    //Log.d(TAG, "calculateEqulibrium: not found!");
                }
            }
        }
        return equiPoints;
    }

    @Override
    public ArrayList<MainScreenController.LineEnum> getMovableObjects() {
        return movableObjects;
    }

    @Override
    public void moveObject(MainScreenController.Direction dir) {

    }

    @Override
    public HashMap<MainScreenController.LineEnum, ArrayList<Integer> > getSeries() {
        return series;
    }

    @Override
    public HashMap<MainScreenController.LineEnum,LineGraphSeries<DataPoint>> getLineGraphSeries(){
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
    public void setMovable(MainScreenController.LineEnum movableEnum) {
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

    public void setLineGraphSeriesMap(HashMap<MainScreenController.LineEnum, LineGraphSeries<DataPoint>> lineGraphSeriesMap) {
        this.lineGraphSeriesMap = lineGraphSeriesMap;
    }

    @Override
    public MainScreenController.LineEnum getMovableEnum() {
        return movableEnum;
    }

    @Override
    public ArrayList<MainScreenController.Direction> getMovableDirections() {
        return movableDirections;
    }

    void setMovableDirections(ArrayList<MainScreenController.Direction> movableDir){
        movableDirections = movableDir;
    }

    void setOptionsLabels(ArrayList<String> optionsLabels) {
        this.optionsLabels = optionsLabels;
    }

    public void setGraphTexts(ArrayList<String> graphTexts) {
        this.graphTexts = graphTexts;
    }

    @Override
    public ArrayList<MainScreenController.LineEnum> getEqDependantCurves() {
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
