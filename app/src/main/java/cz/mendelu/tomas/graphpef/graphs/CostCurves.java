package cz.mendelu.tomas.graphpef.graphs;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;

import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.AverageCost;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.MarginalCost;

/**
 * Created by tomas on 22.09.2018.
 */

public class CostCurves extends PerfectMarketFirm implements Serializable{
    private static final String TAG = "CostCurves";

    public CostCurves(ArrayList<String> graphTexts, ArrayList<MainScreenControllerActivity.LineEnum> movableObjects, MainScreenControllerActivity.LineEnum movableEnum, HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(graphTexts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);
        setMovableDirections(new ArrayList<>(Arrays.asList(MainScreenControllerActivity.Direction.left, MainScreenControllerActivity.Direction.right)));
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color) {
        if (line == MainScreenControllerActivity.LineEnum.PriceLevel || line == MainScreenControllerActivity.LineEnum.Quantity){
            Log.d(TAG,"calculateData: return null");
            line = MainScreenControllerActivity.LineEnum.PriceLevel;
            super.calculateData(line, color);
            return null;
        }else{
            Log.d(TAG,"calculateData: return super()");
            return super.calculateData(line, color);
        }
    }

    @Override
    public void moveObject(MainScreenControllerActivity.Direction dir) {
        if (getMovableEnum() == MainScreenControllerActivity.LineEnum.AverageCost){
            if (dir == MainScreenControllerActivity.Direction.right){
                super.moveObject(MainScreenControllerActivity.Direction.up);
            }else if (dir == MainScreenControllerActivity.Direction.left){
                super.moveObject(MainScreenControllerActivity.Direction.down);
            }
        }else if ( getMovableEnum() == MainScreenControllerActivity.LineEnum.Quantity){
            if (dir == MainScreenControllerActivity.Direction.right){
                super.moveObject(MainScreenControllerActivity.Direction.up,MainScreenControllerActivity.LineEnum.PriceLevel,1);
            }else if (dir == MainScreenControllerActivity.Direction.left){
                super.moveObject(MainScreenControllerActivity.Direction.down,MainScreenControllerActivity.LineEnum.PriceLevel,1);
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
