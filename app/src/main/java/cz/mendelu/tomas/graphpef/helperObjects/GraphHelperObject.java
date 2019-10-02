package cz.mendelu.tomas.graphpef.helperObjects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.activities.GraphControllerActivity;

/**
 * Created by tomas on 12.08.2018.
 */

public class GraphHelperObject implements Serializable{
    private GraphControllerActivity.GraphEnum graphEnum;
    private HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> series;
    private String title, labelX, labelY;
    private Boolean calculateEqulibrium;
    private ArrayList<GraphControllerActivity.LineEnum> equilibriumCurves;
    private ArrayList<GraphControllerActivity.LineEnum> dependantCurveOnEquilibrium;
    private HashMap<GraphControllerActivity.LineEnum, ArrayList<GraphControllerActivity.LineEnum>> dependantCurveOnCurve;
    private HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> lineChangeIdentificator;
    private HashMap<GraphControllerActivity.LineEnum, PositionPair> lineLabelPosition;
    private Boolean showEquilibrium = false;



    public GraphHelperObject() {
        graphEnum = null;
        series = new HashMap<>();
        title = "";
        labelX = "";
        labelY = "";
        lineChangeIdentificator = new HashMap<>();
        lineLabelPosition = new HashMap<>();
    }

    public GraphControllerActivity.GraphEnum getGraphEnum() {
        return graphEnum;
    }

    public void setGraphEnum(GraphControllerActivity.GraphEnum graphEnum) {
        this.graphEnum = graphEnum;
    }

    public ArrayList<Integer> getSeriesByLine(GraphControllerActivity.LineEnum line) {
        return series.get(line);
    }

    public HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> getSeries() {
        return series;
    }

    public void setSeries(HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> series) {
        this.series = series;
    }

    public void addToSeries(GraphControllerActivity.LineEnum lineEnum, ArrayList<Integer> array) {
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
        showEquilibrium = calculateEqulibrium;
    }

    public ArrayList<GraphControllerActivity.LineEnum> getEquilibriumCurves() {
        return equilibriumCurves;
    }

    public void setEquilibriumCurves(ArrayList<GraphControllerActivity.LineEnum> equilibriumCurves) {
        this.equilibriumCurves = equilibriumCurves;
    }

    public ArrayList<Integer> getLineChangeIdentificatorByLineEnum(GraphControllerActivity.LineEnum lineEnum) {
        return lineChangeIdentificator.get(lineEnum);
    }

    public void addLineChangeIdentificator(GraphControllerActivity.LineEnum lineEnum, ArrayList<Integer> identificators) {
        lineChangeIdentificator.put(lineEnum,identificators);
    }

    public void clearLineChangeIdentificator() {
        lineChangeIdentificator.clear();
    }

    public void changeLineChangeIdentificator(GraphControllerActivity.LineEnum lineEnum, ArrayList<Integer> identificators) {
        lineChangeIdentificator.get(lineEnum).clear();
        lineChangeIdentificator.put(lineEnum,identificators);
    }

    public Boolean isLineChangeIdentEmpty(){
        return lineChangeIdentificator.isEmpty();
    }

    public ArrayList<GraphControllerActivity.LineEnum> getDependantCurveOnEquilibrium() {
        return dependantCurveOnEquilibrium;
    }

    public void setDependantCurveOnEquilibrium(ArrayList<GraphControllerActivity.LineEnum> dependantCurveOnequilibrium) {
        this.dependantCurveOnEquilibrium = dependantCurveOnequilibrium;
    }

    public HashMap<GraphControllerActivity.LineEnum, ArrayList<GraphControllerActivity.LineEnum>> getDependantCurveOnCurve() {
        return dependantCurveOnCurve;
    }

    public void setDependantCurveOnCurve(HashMap<GraphControllerActivity.LineEnum, ArrayList<GraphControllerActivity.LineEnum>> dependantCurveOnCurve) {
        this.dependantCurveOnCurve = dependantCurveOnCurve;
    }

    public HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> getLineChangeIdentificator() {
        return lineChangeIdentificator;
    }

    public void setLineChangeIdentificator(HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> lineChangeIdentificator) {
        this.lineChangeIdentificator = lineChangeIdentificator;
    }

    public PositionPair getLineLabelPosition(GraphControllerActivity.LineEnum line) {
        return lineLabelPosition.get(line);
    }

    public void setLineLabelPosition(GraphControllerActivity.LineEnum line, PositionPair position) {
        lineLabelPosition.remove(line);
        lineLabelPosition.put(line,position);
    }

    public void setShowEquilibrium(Boolean showEquilibrium) {
        this.showEquilibrium = showEquilibrium;
    }

    public Boolean getShowEquilibrium() {
        return showEquilibrium;
    }
}
