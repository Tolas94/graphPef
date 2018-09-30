package cz.mendelu.tomas.graphpef.graphs;

import android.graphics.DashPathEffect;
import android.graphics.Paint;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;

/**
 * Created by tomas on 01.09.2018.
 */

public class ProductionLimit extends DefaultGraph  implements Serializable {
    public ProductionLimit(ArrayList<String> texts, ArrayList<MainScreenControllerActivity.LineEnum> movableObjects, MainScreenControllerActivity.LineEnum movableEnum, HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject ) {
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
        double x = 1,y = 1;

        int x0, y0;
        LineGraphSeriesSerialisable seriesLocal = new LineGraphSeriesSerialisable();

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
        calculateLabel(line,x,y);
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
            text4 = "P(X) " + getResources().getString(R.string.value_extended);
        }else if ((int) Math.round(getMaxX()) == 8 ){
            text4 = "P(X) " + getResources().getString(R.string.on_default_values);
        }else if ((int) Math.round(getMaxX()) < 8 ){
            text4 = "P(X) " + getResources().getString(R.string.value_lowered);
        }
        if ((int) Math.round(getMaxY()) > 8 ){
            text5 = "P(Y) " + getResources().getString(R.string.value_extended);
        }else if ((int) Math.round(getMaxY()) == 8 ){
            text5 = "P(Y) " + getResources().getString(R.string.on_default_values);
        }else if ((int) Math.round(getMaxY()) < 8 ){
            text5 = "P(Y) " + getResources().getString(R.string.value_lowered);
        }
        setGraphTexts(new ArrayList<>(Arrays.asList(
                "Max " + getLabelX() + " = " + (int) Math.round(getMaxX()),
                "Max " + getLabelY() + " = " + (int) Math.round(getMaxY()),
                getResources().getString(R.string.default_word) + " " + getResources().getString(R.string.max_production) + " [8,8]",
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
    public List<ArrayList<String>> getSituationInfoTexts() {
        //https://stackoverflow.com/questions/9290651/make-a-hyperlink-textview-in-android
        List<ArrayList<String>> arrayList = new ArrayList<>();
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.production_limit_info_text_1_title),"",getResources().getString(R.string.production_limit_info_text_1))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.production_limit_info_text_2_title),"",getResources().getString(R.string.production_limit_info_text_2))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.production_limit_info_text_3_title),"",getResources().getString(R.string.production_limit_info_text_3))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.production_limit_info_text_4_title),"",getResources().getString(R.string.production_limit_info_text_4))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.production_limit_info_text_5_title),"",getResources().getString(R.string.production_limit_info_text_5))));
        return arrayList;
    }
}
