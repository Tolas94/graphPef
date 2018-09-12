package cz.mendelu.tomas.graphpef.helperObjects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;

/**
 * Created by tomas on 12.08.2018.
 */

public class GraphHelperObject implements Serializable{
    private MainScreenControllerActivity.GraphEnum graphEnum;
    private HashMap<MainScreenControllerActivity.LineEnum,ArrayList<Integer>> series;
    private String title, labelX, labelY;
    private Boolean calculateEqulibrium;
    private ArrayList<MainScreenControllerActivity.LineEnum> equilibriumCurves;
    private ArrayList<MainScreenControllerActivity.LineEnum> dependantCurveOnEquilibrium;
    private HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> lineChangeIdentificator;

    //TODO add dependant LINEs connection e.g. TC MC or TR MT



    public GraphHelperObject() {
        graphEnum = null;
        series = new HashMap<>();
        title = "";
        labelX = "";
        labelY = "";
        lineChangeIdentificator = new HashMap<>();

    }

    public MainScreenControllerActivity.GraphEnum getGraphEnum() {
        return graphEnum;
    }

    public void setGraphEnum(MainScreenControllerActivity.GraphEnum graphEnum) {
        this.graphEnum = graphEnum;
    }

    public ArrayList<Integer> getSeriesByLine(MainScreenControllerActivity.LineEnum line) {
        return series.get(line);
    }

    public HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> getSeries() {
        return series;
    }

    public void setSeries(HashMap<MainScreenControllerActivity.LineEnum,ArrayList<Integer>> series) {
        this.series = series;
    }

    public void addToSeries(MainScreenControllerActivity.LineEnum lineEnum, ArrayList<Integer> array){
        series.put(lineEnum,array);
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

    public ArrayList<MainScreenControllerActivity.LineEnum> getEquilibriumCurves() {
        return equilibriumCurves;
    }

    public void setEquilibriumCurves(ArrayList<MainScreenControllerActivity.LineEnum> equilibriumCurves) {
        this.equilibriumCurves = equilibriumCurves;
    }

    public ArrayList<Integer> getLineChangeIdentificatorByLineEnum(MainScreenControllerActivity.LineEnum lineEnum) {
        return lineChangeIdentificator.get(lineEnum);
    }

    public void addLineChangeIdentificator(MainScreenControllerActivity.LineEnum lineEnum, ArrayList<Integer> identificators) {
        lineChangeIdentificator.put(lineEnum,identificators);
    }

    public void clearLineChangeIdentificator() {
        lineChangeIdentificator.clear();
    }

    public void changeLineChangeIdentificator(MainScreenControllerActivity.LineEnum lineEnum, ArrayList<Integer> identificators) {
        lineChangeIdentificator.get(lineEnum).clear();
        lineChangeIdentificator.put(lineEnum,identificators);
    }

    public Boolean isLineChangeIdentEmpty(){
        return lineChangeIdentificator.isEmpty();
    }

    public ArrayList<MainScreenControllerActivity.LineEnum> getDependantCurveOnEquilibrium() {
        return dependantCurveOnEquilibrium;
    }

    public void setDependantCurveOnEquilibrium(ArrayList<MainScreenControllerActivity.LineEnum> dependantCurveOnequilibrium) {
        this.dependantCurveOnEquilibrium = dependantCurveOnequilibrium;
    }


}
