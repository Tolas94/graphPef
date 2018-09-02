package cz.mendelu.tomas.graphpef.graphs;

import java.util.ArrayList;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.GraphHelperObject;
import cz.mendelu.tomas.graphpef.MainScreenController;

/**
 * Created by tomas on 02.09.2018.
 */

public class PerfectMarketFirm extends DefaultGraph {
    public PerfectMarketFirm(ArrayList<String> graphTexts, ArrayList<MainScreenController.LineEnum> movableObjects, MainScreenController.LineEnum movableEnum, HashMap<MainScreenController.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(graphTexts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);

    }
}
