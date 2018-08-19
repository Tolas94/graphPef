package cz.mendelu.tomas.graphpef;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tomas on 12.08.2018.
 */

public class GraphHelperObject implements Serializable{
    private MainScreenController.GraphEnum graphEnum;
    private HashMap<MainScreenController.LineEnum,ArrayList<Integer>> series;
    private String title, labelX, labelY;
    private Boolean calculateEqulibrium;
    private ArrayList<MainScreenController.LineEnum> equilibriumCurves;



    public GraphHelperObject() {
        graphEnum = null;
        series = null;
        title = "";
        labelX = "";
        labelY = "";

    }

    public MainScreenController.GraphEnum getGraphEnum() {
        return graphEnum;
    }

    public void setGraphEnum(MainScreenController.GraphEnum graphEnum) {
        this.graphEnum = graphEnum;
    }

    public ArrayList<Integer> getSeriesByLine(MainScreenController.LineEnum line) {
        return series.get(line);
    }

    public HashMap<MainScreenController.LineEnum, ArrayList<Integer>> getSeries() {
        return series;
    }

    public void setSeries(HashMap<MainScreenController.LineEnum,ArrayList<Integer>> series) {
        this.series = series;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabelX() {
        return labelX;
    }

    public void setLabelX(String labelX) {
        this.labelX = labelX;
    }

    public String getLabelY() {
        return labelY;
    }

    public void setLabelY(String labelY) {
        this.labelY = labelY;
    }

    public Boolean getCalculateEqulibrium() {
        return calculateEqulibrium;
    }

    public void setCalculateEqulibrium(Boolean calculateEqulibrium) {
        this.calculateEqulibrium = calculateEqulibrium;
    }

    public ArrayList<MainScreenController.LineEnum> getEquilibriumCurves() {
        return equilibriumCurves;
    }

    public void setEquilibriumCurves(ArrayList<MainScreenController.LineEnum> equilibriumCurves) {
        this.equilibriumCurves = equilibriumCurves;
    }

}
