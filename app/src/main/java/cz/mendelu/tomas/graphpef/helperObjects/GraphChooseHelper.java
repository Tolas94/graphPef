package cz.mendelu.tomas.graphpef.helperObjects;

import android.app.Activity;
import android.util.Log;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;

/**
 * Created by tomas on 25.08.2018.
 */

public class GraphChooseHelper {
    private static final String TAG = "GraphChooseHelper";

    private MainScreenControllerActivity activity;
    private String nameOfGraph;

    public GraphChooseHelper(Activity activity, String nameOfGraph) {
        this.activity = (MainScreenControllerActivity) activity;
        this.nameOfGraph = nameOfGraph;
    }

    public void setChoosenGraph(){
        Log.d(TAG, "setChoosenGraph");
        MainScreenControllerActivity.setChosenGraph(nameOfGraph);
        activity.onChosenGraphChange();
        //activity.getViewPager().setCurrentItem(1,true);
    }
}
