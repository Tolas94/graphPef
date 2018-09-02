package cz.mendelu.tomas.graphpef;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tomas on 25.08.2018.
 */

public interface GraphIfc{

    ArrayList<String> getGraphTexts();

    LineGraphSeries<DataPoint> calculateData(MainScreenController.LineEnum line, int color);

    ArrayList<Double> calculateEqulibrium();

    ArrayList<MainScreenController.LineEnum> getMovableObjects();

    ArrayList<MainScreenController.Direction> getMovableDirections();

    void moveObject(MainScreenController.Direction dir);

    HashMap<MainScreenController.LineEnum,ArrayList<Integer> > getSeries();

    HashMap<MainScreenController.LineEnum,LineGraphSeries<DataPoint>> getLineGraphSeries();

    ArrayList<MainScreenController.LineEnum> getEqDependantCurves();

    ArrayList<String> getOptionsLabels();

    String getMovableLabel();

    void setMovable(MainScreenController.LineEnum movableEnum);

    String getTitle();

    String getLabelX();

    String getLabelY();

    MainScreenController.LineEnum getMovableEnum();

    InfoHelper getInfoHelper();

}
