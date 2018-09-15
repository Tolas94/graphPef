package cz.mendelu.tomas.graphpef.graphs;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;

import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.Demand;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.Supply;

/**
 * Created by tomas on 01.09.2018.
 */

public class ProductionLimit extends DefaultGraph {
    public ProductionLimit(ArrayList<String> texts, ArrayList<MainScreenControllerActivity.LineEnum> movableObjects, MainScreenControllerActivity.LineEnum movableEnum, HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(texts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);

        setMovableDirections(new ArrayList<>(Arrays.asList(
                MainScreenControllerActivity.Direction.up,
                MainScreenControllerActivity.Direction.down,
                MainScreenControllerActivity.Direction.left,
                MainScreenControllerActivity.Direction.right)));
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color) {
        double precision = 0.1;
        int maxDataPoints = 500;
        double x,y = 1;

        int x0, y0;
        LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<>();

        HashMap<MainScreenControllerActivity.LineEnum,ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();

        x0 = seriesSource.get(line).get(0);
        y0 = seriesSource.get(line).get(1);

        if (getLineGraphSeries() != null)
            getLineGraphSeries().remove(line);

        for (double t = 0.5 * Math.PI; y>=0; t -= precision ) { // <- or different step
            x = (x0 + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0)) * Math.cos(t);
            y = (y0 + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(1)) * Math.sin(t);
            seriesLocal.appendData( new DataPoint(x,y), true, maxDataPoints );
        }
        seriesLocal.setColor(color);

        if (line == MainScreenControllerActivity.LineEnum.ProductionCapabilitiesDefault){
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setPathEffect(new DashPathEffect(new float[]{8,5},0));
            seriesLocal.setDrawAsPath(true);
            seriesLocal.setCustomPaint(paint);
            seriesLocal.setThickness(1);
        }else{
            seriesLocal.setThickness(5);
        }
        getLineGraphSeries().put(line, seriesLocal);
        updateTexts();
        return seriesLocal;
    }

    @Override
    public void moveObject(MainScreenControllerActivity.Direction dir) {
        //Log.d(TAG, "moveObject");
        ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(getMovableEnum());
        switch(dir){
            case up:
             identChanges.set(1,identChanges.get(1) + 1);
                break;
            case down:
                if (identChanges.get(1) > -7 ) {
                    identChanges.set(1, identChanges.get(1) - 1);
                }
                break;
            case right:  identChanges.set(0,identChanges.get(0) + 1);
                break;
            case left:
            if (identChanges.get(0) > -7 ) {
                identChanges.set(0, identChanges.get(0) - 1);
            }
                break;
        }
    }

    private double getMaxX(){
        if (getLineGraphSeries().get(MainScreenControllerActivity.LineEnum.ProductionCapabilities) != null)
            return getLineGraphSeries().get(MainScreenControllerActivity.LineEnum.ProductionCapabilities).getHighestValueX();
        else
            return 0.0;
    }

    private double getMaxY(){
        if (getLineGraphSeries().get(MainScreenControllerActivity.LineEnum.ProductionCapabilities) != null)
            return getLineGraphSeries().get(MainScreenControllerActivity.LineEnum.ProductionCapabilities).getHighestValueY();
        else
            return 0.0;
    }

    private void updateTexts(){
        refreshInfoTexts();
        String text4,text5;
        text4 = "";
        text5 = "";
        if ((int) Math.round(getMaxX()) > 8 ){
            text4 = "P(X) has been extended";
        }else if ((int) Math.round(getMaxX()) == 8 ){
            text4 = "P(X) is on default values";
        }else if ((int) Math.round(getMaxX()) < 8 ){
            text4 = "P(X) has been lowered";
        }
        if ((int) Math.round(getMaxY()) > 8 ){
            text5 = "P(Y) has been extended";
        }else if ((int) Math.round(getMaxY()) == 8 ){
            text5 = "P(Y) is on default values";
        }else if ((int) Math.round(getMaxY()) < 8 ){
            text5 = "P(Y) has been lowered";
        }
        setGraphTexts(new ArrayList<>(Arrays.asList(
                "Max " + getLabelX() + " = " + (int) Math.round(getMaxX()),
                "Max " + getLabelY() + " = " + (int) Math.round(getMaxY()),
                "Default Max Production [8,8]",
                text4,
                text5)));
    }

    @Override
    public ArrayList<Double> calculateEqulibrium() {
        if (getGraphHelperObject().getCalculateEqulibrium())
            return super.calculateEqulibrium();
        else
            return null;
    }

    @Override
    public ArrayList<String> getSituationInfoTexts() {
        //https://stackoverflow.com/questions/9290651/make-a-hyperlink-textview-in-android
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Hranice produkčních možností (anglicky: \"production-possibility" +
                " frontier\" nebo-li PPF) vyjadřuje rozdílné kombinace dvou různých statků, které může" +
                " výrobce produkovat při plném využití daných zdrojů a při dané technologii." +
                " Vyjadřuje hranici mezi dosažitelnou a nedosažitelnou úrovní produkce.");

        arrayList.add("PPF je kombinace statků, které může ekonomika při svých celkově " +
                "omezených zdrojích vyrábět. Ve zjednodušeném modelu jde o kombinace dvou " +
                "statků, přičemž hranice výrobních možností je vyjádřena konkávní křivkou " +
                "v důsledku klesající mezní míry transformace produktu.");


        arrayList.add("Při rozhodování CO a JAK vyrábět si ekonomický subjekt volí míru " +
                "produkce tak, aby byla vždy dosažena hranice produkčních možností (PPF), " +
                "čímž předchází neefektivnosti výroby. PPF zobrazuje všechny maximálně dostupné" +
                " kombinace statků, které mohou být vyrobeny při 100% využití daných zdrojů ");

        arrayList.add("will add this later");
        return arrayList;
    }
}
