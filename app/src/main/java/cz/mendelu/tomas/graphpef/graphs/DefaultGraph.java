package cz.mendelu.tomas.graphpef.graphs;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.GraphHelperObject;
import cz.mendelu.tomas.graphpef.GraphIfc;
import cz.mendelu.tomas.graphpef.MainScreenController;

import static java.lang.Math.abs;

/**
 * Created by tomas on 25.08.2018.
 */

public class DefaultGraph implements GraphIfc,Serializable{
    private ArrayList<String> texts;
    private ArrayList<String> movableObjects;
    private int movableIndex;
    private HashMap<MainScreenController.LineEnum,ArrayList<Integer> > series;
    private ArrayList<String> optionsLabels;
    private GraphHelperObject graphHelperObject;
    private HashMap<MainScreenController.LineEnum,LineGraphSeries<DataPoint>> lineGraphSeriesMap;

    public DefaultGraph(ArrayList<String> texts, ArrayList<String> movableObjects, int movableIndex, HashMap<MainScreenController.LineEnum, ArrayList<Integer> > series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        this.texts = texts;
        this.movableObjects = movableObjects;
        this.movableIndex = movableIndex;
        this.series = series;
        this.optionsLabels = optionsLabels;
        this.graphHelperObject = graphHelperObject;
        this.lineGraphSeriesMap = new HashMap<>();
    }

    @Override
    public Integer getTextCount() {
        return texts.size();
    }

    @Override
    public ArrayList<String> getTexts() {
        return texts;
    }

    @Override
    public LineGraphSeries<DataPoint> CalculateData(MainScreenController.LineEnum line, int color) {
        return new LineGraphSeries<>();
    }

    @Override
    public ArrayList<Double> calculateEqulibrium(MainScreenController.LineEnum curve1,MainScreenController.LineEnum curve2){
        ArrayList<Double> doubles = new ArrayList<>();
        doubles.add(0.0);
        doubles.add(0.0);
        return doubles;

    }

    @Override
    public ArrayList<String> getMovableObjects() {
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
        return movableObjects.get(movableIndex);
    }

    @Override
    public void setMovable(String movableName) {
        if (movableObjects != null)
            movableIndex = movableObjects.indexOf(movableName);
    }

    @Override
    public String getTitle() {
        return graphHelperObject.getTitle();
    }

    @Override
    public String getLabelX() {
        return null;
    }

    @Override
    public String getLabelY() {
        return null;
    }

    private Boolean compareDoubleWithPrecision(Double firstValue, Double secondValue, Double precision){
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

}
