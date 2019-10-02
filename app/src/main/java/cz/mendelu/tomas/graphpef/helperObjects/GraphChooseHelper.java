package cz.mendelu.tomas.graphpef.helperObjects;

import android.app.Activity;
import android.util.Log;

import java.io.Serializable;

import cz.mendelu.tomas.graphpef.activities.GraphControllerActivity;

/**
 * Created by tomas on 25.08.2018.
 */

public class GraphChooseHelper  implements Serializable {
    private static final String TAG = "GraphChooseHelper";

    private GraphControllerActivity activity;
    private String nameOfGraph;

    public GraphChooseHelper(Activity activity, String nameOfGraph) {
        this.activity = (GraphControllerActivity) activity;
        this.nameOfGraph = nameOfGraph;
    }

    public void setChoosenGraph(){
        Log.d(TAG, "setChoosenGraph");
        GraphControllerActivity.setChosenGraph(nameOfGraph);
        activity.onChosenGraphChange();
        //activity.getViewPager().setCurrentItem(1,true);
    }
}
