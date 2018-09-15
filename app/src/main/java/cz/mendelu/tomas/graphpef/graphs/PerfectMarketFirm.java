package cz.mendelu.tomas.graphpef.graphs;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;

import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.MarginalCost;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.PriceLevel;

/**
 * Created by tomas on 02.09.2018.
 */

public class PerfectMarketFirm extends DefaultGraph {
    private static final String TAG = "PerfectMarketFirm";

    public PerfectMarketFirm(ArrayList<String> graphTexts, ArrayList<MainScreenControllerActivity.LineEnum> movableObjects, MainScreenControllerActivity.LineEnum movableEnum, HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(graphTexts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);

        setMovableDirections(new ArrayList<>(Arrays.asList(MainScreenControllerActivity.Direction.up, MainScreenControllerActivity.Direction.down)));
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color) {
        if (getLineGraphSeries().get(line) == null) {
            double precision = MainScreenControllerActivity.getPrecision();
            int maxDataPoints = MainScreenControllerActivity.getMaxDataPoints();
            double x, y;
            x = 1;
            if (line == PriceLevel){
                x = 0;
            }
            int x0, x1, x2, x3, x_1;
            HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();

            x_1 = seriesSource.get(line).get(4);
            x0 = seriesSource.get(line).get(3);
            x1 = seriesSource.get(line).get(2);
            x2 = seriesSource.get(line).get(1);
            x3 = seriesSource.get(line).get(0);

            LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<>();

            for (int i = 0; i < maxDataPoints; i++) {
                x = x + precision;
                if (line == MainScreenControllerActivity.LineEnum.AverageCost) {
                    if (i == 0)
                        Log.d(TAG, "y = (" + x3 + "x^3/3 + " + x2 + "x^2 +" + x1 + "x + " + x0 + " )/" + x_1 + "x");
                    y = ((x3 * x * x * x) / 3 + x2 * x * x + x1 * x + x0) / (x_1 * x);
                } else if (line == MainScreenControllerActivity.LineEnum.MarginalCost) {
                    if (i == 0)
                        Log.d(TAG, "y = (" + x2 + " + x)^2 +" + x1 + "x + " + x0 + " )");
                    y = ((x + x2) * (x + x2) + x1 * x + x0);
                } else if (line == PriceLevel){
                    if (i == 0)
                        Log.d(TAG, "y = " + x0 + "  ");
                    y = x0;
                }else{
                    y=0;
                }
                seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
            }
            Log.d(TAG, "MinY [" + seriesLocal.getLowestValueY() + "] maxY[" + seriesLocal.getHighestValueY() + "]");
            Log.d(TAG, "MinX [" + seriesLocal.getLowestValueX() + "] maxX[" + seriesLocal.getHighestValueX() + "]");
            getLineGraphSeries().put(line, seriesLocal);
            return seriesLocal;
        }else{
            return getLineGraphSeries().get(line);
        }
    }

    @Override
    public void moveObject(MainScreenControllerActivity.Direction dir) {
        super.moveObject(dir);
        if (getMovableEnum() == MainScreenControllerActivity.LineEnum.AverageCost){
            if (dir == MainScreenControllerActivity.Direction.up){
                super.moveObject(MainScreenControllerActivity.Direction.right);
                super.moveObject(MainScreenControllerActivity.Direction.up,MarginalCost, 1);
                super.moveObject(MainScreenControllerActivity.Direction.right,MarginalCost, 1);
            }else if (dir == MainScreenControllerActivity.Direction.down){
                super.moveObject(MainScreenControllerActivity.Direction.left);
                super.moveObject(MainScreenControllerActivity.Direction.down,MarginalCost, 1);
                super.moveObject(MainScreenControllerActivity.Direction.left,MarginalCost, 1);
            }
        }
    }

    @Override
    public ArrayList<String> getSituationInfoTexts() {
        //https://stackoverflow.com/questions/9290651/make-a-hyperlink-textview-in-android
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Dokonalý trh je takový trh, na kterém mají všichni kupující dokonalé " +
                "informace o všech prodávajících a cenách, které nabízejí, při přechodu od " +
                "jednoho prodávajícího k jinému mají nulové náklady a " +
                "obchodovaný statek je homogenní.");

        arrayList.add("TODO");


        arrayList.add("TODO ");

        arrayList.add("will add this later");
        return arrayList;
    }
}
