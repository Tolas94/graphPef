package cz.mendelu.tomas.graphpef;

import android.app.Activity;
import android.util.Log;

/**
 * Created by tomas on 25.08.2018.
 */

public class GraphChooseHelper {
    private static final String TAG = "GraphChooseHelper";

    private MainScreenController activity;
    private String nameOfGraph;

    public GraphChooseHelper(Activity activity, String nameOfGraph) {
        this.activity = (MainScreenController) activity;
        this.nameOfGraph = nameOfGraph;
    }

    public void setChoosenGraph(){
        Log.d(TAG, "setChoosenGraph");
        MainScreenController.setChosenGraph(nameOfGraph);
        activity.onChosenGraphChange();
        activity.getViewPager().setCurrentItem(1,true);
    }
}
