package cz.mendelu.tomas.graphpef.graphs;

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

/**
 * Created by tomas on 22.09.2018.
 */

public class CostCurves extends PerfectMarketFirm implements Serializable{
    private static final String TAG = "CostCurves";

    public CostCurves(ArrayList<String> graphTexts, ArrayList<GraphControllerActivity.LineEnum> movableObjects, GraphControllerActivity.LineEnum movableEnum, HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(graphTexts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);
        setMovableDirections(new ArrayList<>(Arrays.asList(GraphControllerActivity.Direction.left, GraphControllerActivity.Direction.right)));
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(GraphControllerActivity.LineEnum line, int color) {
        if (line == GraphControllerActivity.LineEnum.PriceLevel || line == GraphControllerActivity.LineEnum.Quantity) {
            Log.d(TAG,"calculateData: return null");
            line = GraphControllerActivity.LineEnum.PriceLevel;
            super.calculateData(line, color);
            return null;
        }else{
            Log.d(TAG,"calculateData: return super()");
            return super.calculateData(line, color);
        }
    }

    @Override
    public void moveObject(GraphControllerActivity.Direction dir) {
        if (getMovableEnum() == GraphControllerActivity.LineEnum.AverageCost) {
            if (dir == GraphControllerActivity.Direction.right) {
                super.moveObject(GraphControllerActivity.Direction.up);
            } else if (dir == GraphControllerActivity.Direction.left) {
                super.moveObject(GraphControllerActivity.Direction.down);
            }
        } else if (getMovableEnum() == GraphControllerActivity.LineEnum.Quantity) {
            if (dir == GraphControllerActivity.Direction.right) {
                super.moveObject(GraphControllerActivity.Direction.up, GraphControllerActivity.LineEnum.PriceLevel, 1);
            } else if (dir == GraphControllerActivity.Direction.left) {
                super.moveObject(GraphControllerActivity.Direction.down, GraphControllerActivity.LineEnum.PriceLevel, 1);
            }
        }
    }

    @Override
    public List<ArrayList<String>> getSituationInfoTexts() {
        //https://stackoverflow.com/questions/9290651/make-a-hyperlink-textview-in-android
        List<ArrayList<String>> arrayList = new ArrayList<>();

        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.cost_curves_info_text_1_title),"",getResources().getString(R.string.cost_curves_info_text_1))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.cost_curves_info_text_2_title),getResources().getString(R.string.cost_curves_info_text_2_subtitle),getResources().getString(R.string.cost_curves_info_text_2))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.cost_curves_info_text_3_title),getResources().getString(R.string.cost_curves_info_text_3_subtitle),getResources().getString(R.string.cost_curves_info_text_3))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.cost_curves_info_text_4_title),getResources().getString(R.string.cost_curves_info_text_4_subtitle),getResources().getString(R.string.cost_curves_info_text_4))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.cost_curves_info_text_5_title),getResources().getString(R.string.cost_curves_info_text_5_subtitle),getResources().getString(R.string.cost_curves_info_text_5))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.cost_curves_info_text_6_title),getResources().getString(R.string.cost_curves_info_text_6_subtitle),getResources().getString(R.string.cost_curves_info_text_6))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.cost_curves_info_text_7_title),"",getResources().getString(R.string.cost_curves_info_text_7))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.cost_curves_info_text_8_title),"",getResources().getString(R.string.cost_curves_info_text_8))));

        return arrayList;
    }
}
