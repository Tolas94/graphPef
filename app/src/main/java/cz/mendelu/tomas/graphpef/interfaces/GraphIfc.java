package cz.mendelu.tomas.graphpef.interfaces;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;
import cz.mendelu.tomas.graphpef.helperObjects.PositionPair;

/**
 * Created by tomas on 25.08.2018.
 */

public interface GraphIfc{

    ArrayList<String> getGraphTexts();

    LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color);

    LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color, Double limit, boolean vertical, ArrayList<Double> equilibrium);

    ArrayList<Double> calculateEqulibrium();

    ArrayList<MainScreenControllerActivity.LineEnum> getMovableObjects();

    ArrayList<MainScreenControllerActivity.Direction> getMovableDirections();

    void moveObject(MainScreenControllerActivity.Direction dir);

    void moveObject(MainScreenControllerActivity.Direction dir, MainScreenControllerActivity.LineEnum line, int precisionModificator);

    HashMap<MainScreenControllerActivity.LineEnum,ArrayList<Integer> > getSeries();

    HashMap<MainScreenControllerActivity.LineEnum, LineGraphSeriesSerialisable> getLineGraphSeries();

    ArrayList<MainScreenControllerActivity.LineEnum> getEqDependantCurves();

    ArrayList<MainScreenControllerActivity.LineEnum> getDependantCurves(MainScreenControllerActivity.LineEnum line);

    ArrayList<String> getOptionsLabels();

    ArrayList<String> getGraphInfoTexts();

    List<ArrayList<String>> getSituationInfoTexts();

    String getMovableLabel();

    void setMovable(MainScreenControllerActivity.LineEnum movableEnum);

    String getTitle();

    String getLabelX();

    String getLabelY();

    MainScreenControllerActivity.LineEnum getMovableEnum();

    ArrayList<Double> getEquiPoints();

    void refreshInfoTexts();

    PositionPair getLineLabelPosition(MainScreenControllerActivity.LineEnum line);

    int getColorOf(MainScreenControllerActivity.LineEnum lineEnum);

    void showCurvesDependantOnEquilibrium(double x,double y);


}
