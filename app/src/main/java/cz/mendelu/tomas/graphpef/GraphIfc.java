package cz.mendelu.tomas.graphpef;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tomas on 25.08.2018.
 */

public interface GraphIfc{

    Integer getTextCount();

    ArrayList<String> getTexts();

    LineGraphSeries<DataPoint> CalculateData(MainScreenController.LineEnum line, int color);

    ArrayList<Double> calculateEqulibrium(MainScreenController.LineEnum curve1,MainScreenController.LineEnum curve2);

    ArrayList<String> getMovableObjects();

    void moveObject(MainScreenController.Direction dir);

    HashMap<MainScreenController.LineEnum,ArrayList<Integer> > getSeries();

    HashMap<MainScreenController.LineEnum,LineGraphSeries<DataPoint>> getLineGraphSeries();

    ArrayList<String> getOptionsLabels();

    String getMovableLabel();

    void setMovable(String movableName);

    String getTitle();

    String getLabelX();

    String getLabelY();

}
