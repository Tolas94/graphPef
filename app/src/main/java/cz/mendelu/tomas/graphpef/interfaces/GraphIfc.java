package cz.mendelu.tomas.graphpef.interfaces;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.mendelu.tomas.graphpef.activities.GraphControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;
import cz.mendelu.tomas.graphpef.helperObjects.PositionPair;

/**
 * Created by tomas on 25.08.2018.
 */

public interface GraphIfc{

    ArrayList<String> getGraphTexts();

    LineGraphSeries<DataPoint> calculateData(GraphControllerActivity.LineEnum line, int color);

    LineGraphSeries<DataPoint> calculateData(GraphControllerActivity.LineEnum line, int color, Double limit, boolean vertical, ArrayList<Double> equilibrium);

    ArrayList<Double> calculateEqulibrium();

    ArrayList<GraphControllerActivity.LineEnum> getMovableObjects();

    ArrayList<GraphControllerActivity.Direction> getMovableDirections();

    void moveObject(GraphControllerActivity.Direction dir);

    void moveObject(GraphControllerActivity.Direction dir, GraphControllerActivity.LineEnum line, int precisionModificator);

    HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> getSeries();

    HashMap<GraphControllerActivity.LineEnum, LineGraphSeriesSerialisable> getLineGraphSeries();

    ArrayList<GraphControllerActivity.LineEnum> getEqDependantCurves();

    ArrayList<GraphControllerActivity.LineEnum> getDependantCurves(GraphControllerActivity.LineEnum line);

    ArrayList<String> getOptionsLabels();

    ArrayList<String> getGraphInfoTexts();

    List<ArrayList<String>> getSituationInfoTexts();

    String getMovableLabel();

    void setMovable(GraphControllerActivity.LineEnum movableEnum);

    String getTitle();

    String getLabelX();

    String getLabelY();

    GraphControllerActivity.LineEnum getMovableEnum();

    ArrayList<Double> getEquiPoints();

    void refreshInfoTexts();

    PositionPair getLineLabelPosition(GraphControllerActivity.LineEnum line);

    int getColorOf(GraphControllerActivity.LineEnum lineEnum);

    void showCurvesDependantOnEquilibrium(double x,double y);


}
