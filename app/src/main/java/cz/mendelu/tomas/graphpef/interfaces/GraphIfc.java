package cz.mendelu.tomas.graphpef.interfaces;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;

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

    HashMap<MainScreenControllerActivity.LineEnum,LineGraphSeries<DataPoint>> getLineGraphSeries();

    ArrayList<MainScreenControllerActivity.LineEnum> getEqDependantCurves();

    ArrayList<MainScreenControllerActivity.LineEnum> getDependantCurves(MainScreenControllerActivity.LineEnum line);

    ArrayList<String> getOptionsLabels();

    ArrayList<String> getGraphInfoTexts();

    ArrayList<String> getSituationInfoTexts();

    String getMovableLabel();

    void setMovable(MainScreenControllerActivity.LineEnum movableEnum);

    String getTitle();

    String getLabelX();

    String getLabelY();

    MainScreenControllerActivity.LineEnum getMovableEnum();

    ArrayList<Double> getEquiPoints();

    void refreshInfoTexts();


}
