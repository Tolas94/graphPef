package cz.mendelu.tomas.graphpef.graphs;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.GraphControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;

/**
 * Created by tomas on 25.08.2018.
 */

public class MarketDS extends DefaultGraph  implements Serializable {
    private static final String TAG = "MarketDS";

    public MarketDS(ArrayList<String> texts, ArrayList<GraphControllerActivity.LineEnum> movableObjects, GraphControllerActivity.LineEnum movableEnum, HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(texts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);

        setMovableDirections(new ArrayList<>(Arrays.asList(GraphControllerActivity.Direction.up, GraphControllerActivity.Direction.down)));
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(GraphControllerActivity.LineEnum line, int color) {
        if (getLineGraphSeries().get(line) == null) {
            double precision = GraphControllerActivity.getPrecision();
            int maxDataPoints = GraphControllerActivity.getMaxDataPoints();
            double x,y;
            x = 1;
            y = 0;
            int x0,x1;
            HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();

            x0 = seriesSource.get(line).get(1);
            x1 = seriesSource.get(line).get(0);

            LineGraphSeriesSerialisable seriesLocal = new LineGraphSeriesSerialisable();
            if (getLineGraphSeries() != null)
                getLineGraphSeries().remove(line);

            ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line);

            for( int i=0; i<maxDataPoints; i++){
                x = x + precision;
                y = x1 * x + x0 + identChanges.get(0);
                seriesLocal.appendData( new DataPoint(x,y), true, maxDataPoints );
            }
            if (line == GraphControllerActivity.LineEnum.SupplyDefault || line == GraphControllerActivity.LineEnum.DemandDefault) {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                paint.setPathEffect(new DashPathEffect(new float[]{1,1},0));
                seriesLocal.setDrawAsPath(true);
                seriesLocal.setCustomPaint(paint);
                seriesLocal.setThickness(1);
            }else{
                seriesLocal.setThickness(5);
            }
            seriesLocal.setColor(color);
            getLineGraphSeries().put(line, seriesLocal);
            calculateLabel(line,x,y);
            return seriesLocal;
        }else{
            return getLineGraphSeries().get(line);
        }
    }

    @Override
    public ArrayList<Double> calculateEqulibrium() {
        Log.d(TAG,"calculateEqulibrium");
        ArrayList<Double> retVal;
        retVal = super.calculateEqulibrium();
        if (!retVal.isEmpty()){
            populateTexts(true,retVal);
        }else{
            populateTexts(false,retVal);
        }
        return retVal;
    }

    private void populateTexts(boolean equilibriumExists,ArrayList<Double> equilibrium){
        Log.d(TAG,"populateTexts");
        refreshInfoTexts();
        ArrayList texts = new ArrayList();
        if(equilibriumExists){
            texts.add(getResources().getString(R.string.equilibrium_is) + " " + getStringFromLineEnum(getGraphHelperObject().getDependantCurveOnEquilibrium().get(0)) + " = " + String.format( "%.1f", equilibrium.get(0) ));
            texts.add(getResources().getString(R.string.equilibrium_is) + " " + getStringFromLineEnum(getGraphHelperObject().getDependantCurveOnEquilibrium().get(1)) + " = " + String.format( "%.1f", equilibrium.get(1) ));
        }else{
            texts.add(getResources().getString(R.string.equilibrium_cannot));
        }
        for (GraphControllerActivity.LineEnum line : getMovableObjects()) {
            texts.add(getStringFromLineEnum(line) + " " + getResources().getString(R.string.changed_by) + " " + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0));
        }
        setGraphTexts(texts);
    }

    @Override
    public List<ArrayList<String>> getSituationInfoTexts() {
        Log.d(TAG,"getSituationInfoTexts");
        //https://stackoverflow.com/questions/9290651/make-a-hyperlink-textview-in-android
        List<ArrayList<String>> arrayList = new ArrayList<>();
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.marketDS_info_text_1_title),"",getResources().getString(R.string.marketDS_info_text_1))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.marketDS_info_text_demand_title),"",getResources().getString(R.string.marketDS_info_text_demand))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.marketDS_info_text_supply_title),"",getResources().getString(R.string.marketDS_info_text_supply))));
        //arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.marketDS_info_text_2_title),"",getResources().getString(R.string.marketDS_info_text_2))));
        //arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.marketDS_info_text_3_title),"",getResources().getString(R.string.marketDS_info_text_3))));
        //arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.marketDS_info_text_4_title),"",getResources().getString(R.string.marketDS_info_text_4))));
        //arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.marketDS_info_text_5_title),"",getResources().getString(R.string.marketDS_info_text_5))));

        return arrayList;
    }
}
