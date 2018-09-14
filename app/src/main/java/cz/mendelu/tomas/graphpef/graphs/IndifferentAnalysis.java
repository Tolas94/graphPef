package cz.mendelu.tomas.graphpef.graphs;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;

import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.BudgetLine;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.PriceLevel;
import static java.lang.Double.NaN;

/**
 * Created by tomas on 12.09.2018.
 */

public class IndifferentAnalysis extends DefaultGraph{
    private static final String TAG = "IndifferentAnalysis";

    public IndifferentAnalysis(ArrayList<String> graphTexts, ArrayList<MainScreenControllerActivity.LineEnum> movableObjects, MainScreenControllerActivity.LineEnum movableEnum, HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(graphTexts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);
        setMovableDirections(new ArrayList<>(Arrays.asList(
                MainScreenControllerActivity.Direction.up,
                MainScreenControllerActivity.Direction.down,
                MainScreenControllerActivity.Direction.left,
                MainScreenControllerActivity.Direction.right)));
    }


    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color) {
        if (getLineGraphSeries().get(line) == null) {
            double precision = MainScreenControllerActivity.getPrecision() / 100;
            int maxDataPoints = MainScreenControllerActivity.getMaxDataPoints() * 100;
            Double x,y;
            x = 0.0;
            int arg0,arg1;
            HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();


            arg0 = seriesSource.get(line).get(0);
            arg1 = seriesSource.get(line).get(1);

            LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<>();

                if (line == MainScreenControllerActivity.LineEnum.IndifferentCurve) {
                    for (int i = 0; i < maxDataPoints; i++) {
                        x = x + precision; // 3 3 1
                        //Log.d(TAG,"calculateData y = 1 / (sqrt( x +" + arg1 + ") + " + arg0);
                        y = 1 /  (Math.sqrt(x + arg1) ) + arg0 + 0.2;
                        if (Double.isNaN(y)){
                            y = 200.0;
                        }
                        //Log.d(TAG,"calculateData x = " + x + " y = " + y);
                        seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
                    }
                } else if (line == MainScreenControllerActivity.LineEnum.BudgetLine) {

                    //http://www.coolmath.com/algebra/08-lines/12-finding-equation-two-points-01
                    float m = ( - arg0 )/arg1;

                    for (int i = 0; i < maxDataPoints; i++) {
                        x = x + precision;
                        y = m * (x - arg1);
                        seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
                    }
                }

            Log.d(TAG, "calculateData: MinY [" + seriesLocal.getLowestValueY() + "] maxY[" + seriesLocal.getHighestValueY() + "]");
            Log.d(TAG, "calculateData: MinX [" + seriesLocal.getLowestValueX() + "] maxX[" + seriesLocal.getHighestValueX() + "]");
            getLineGraphSeries().put(line, seriesLocal);
            return seriesLocal;
        }else{
            return getLineGraphSeries().get(line);
        }
    }

    @Override
    public void moveObject(MainScreenControllerActivity.Direction dir) {

        if (getMovableEnum() == MainScreenControllerActivity.LineEnum.IndifferentCurve){
            if (dir == MainScreenControllerActivity.Direction.up){
                super.moveObject(dir);
                super.moveObject(MainScreenControllerActivity.Direction.right);
            }else if (dir == MainScreenControllerActivity.Direction.down){
                super.moveObject(dir);
                super.moveObject(MainScreenControllerActivity.Direction.left);
            }
        }else if (getMovableEnum() == MainScreenControllerActivity.LineEnum.BudgetLine) {
            LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<>();
            ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(getMovableEnum());
            float arg0 = getSeries().get(BudgetLine).get(0);
            float arg1 = getSeries().get(BudgetLine).get(1);
            if (dir == MainScreenControllerActivity.Direction.up){
                identChanges.set(0,identChanges.get(0) - 1);
            } else if (dir == MainScreenControllerActivity.Direction.down){
                identChanges.set(0,identChanges.get(0) + 1);

            } else if (dir == MainScreenControllerActivity.Direction.left){
                identChanges.set(1,identChanges.get(1) - 1);

            } else if (dir == MainScreenControllerActivity.Direction.right){
                identChanges.set(1,identChanges.get(1) + 1);
            }
            Log.d(TAG,"moveObject: arg0["+arg0+"] arg1[" + arg1 + "]");
            Log.d(TAG,"moveObject: ident0["+identChanges.get(0)+"] ident1[" + identChanges.get(1) + "]");

            double precision = MainScreenControllerActivity.getPrecision() /100;
            int maxDataPoints = MainScreenControllerActivity.getMaxDataPoints()*100;
            float m = ( (float)identChanges.get(0) - arg0 )/(arg1 + (float)identChanges.get(1));
            double x = 0, y;
            Log.d(TAG,"moveObject: m =" + m);

            for (int i = 0; i < maxDataPoints; i++) {
                x = x + precision;
                y = m * (x - arg1 - identChanges.get(1));
                seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
            }
            getLineGraphSeries().put(getMovableEnum(), seriesLocal);
        }
    }

}
