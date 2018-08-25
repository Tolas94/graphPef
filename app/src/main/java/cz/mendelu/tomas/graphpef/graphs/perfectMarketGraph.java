package cz.mendelu.tomas.graphpef.graphs;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;

import cz.mendelu.tomas.graphpef.GraphHelperObject;
import cz.mendelu.tomas.graphpef.GraphIfc;
import cz.mendelu.tomas.graphpef.MainScreenController;

import static java.lang.Math.abs;

/**
 * Created by tomas on 25.08.2018.
 */

public class perfectMarketGraph extends DefaultGraph {
    private static final String TAG = "perfectMarketGraph";

    public perfectMarketGraph(ArrayList<String> texts, ArrayList<String> movableObjects, int movableIndex, HashMap<MainScreenController.LineEnum, LineGraphSeries<DataPoint>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(texts, movableObjects, movableIndex, series, optionsLabels, graphHelperObject);
    }

    @Override
    public ArrayList<Double> calculateEqulibrium(MainScreenController.LineEnum curve1, MainScreenController.LineEnum curve2) {
        double precision = MainScreenController.getPrecision();

        ArrayList<Double> equiPoints = new ArrayList<>();

        LineGraphSeries<DataPoint> data1 = getSeries().get(curve1);
        LineGraphSeries<DataPoint> data2 = getSeries().get(curve2);;
        double pointX, pointY, diff;
        int x3, x2, x1, x0,x3_2, x2_2, x1_2, x0_2;
        HashMap<MainScreenController.LineEnum,ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();
        ArrayList<Integer> identChanges1 = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(curve1);
        ArrayList<Integer> identChanges2 = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(curve2);

        // check if they intersect each other
        if (data1.getHighestValueX() > data2.getLowestValueX()
                && data2.getHighestValueX() > data1.getLowestValueX())
        {
            if (data1.getHighestValueY() > data2.getLowestValueY()
                    && data2.getHighestValueY() > data1.getLowestValueY())
            {
                double minX, maxX, x, y1,y2;
                if (data1.getLowestValueX() > data2.getLowestValueX()){
                    minX = data1.getLowestValueX();
                }else{
                    minX = data2.getLowestValueX();
                }
                if (data1.getHighestValueX() < data2.getHighestValueX()){
                    maxX = data1.getHighestValueX();
                }else{
                    maxX = data2.getHighestValueX();
                }

                int counter = (int) ((maxX - minX)/precision);

                //Log.d(TAG, "calculateEqulibrium: minX[" + minX + "] maxX[" + maxX + "] counter[" + counter + "]");
                x = minX;
                pointX = minX;
                pointY = 0;
                diff = 10000;
                x3 = seriesSource.get(curve1).get(0);
                x2 = seriesSource.get(curve1).get(1);
                x1 = seriesSource.get(curve1).get(2);
                x0 = seriesSource.get(curve1).get(3);
                x3_2 = seriesSource.get(curve2).get(0);
                x2_2 = seriesSource.get(curve2).get(1);
                x1_2 = seriesSource.get(curve2).get(2);
                x0_2 = seriesSource.get(curve2).get(3);
                for (int i = 0; i < counter; i++ ){
                    x = x + precision;
                    y1 = x3   * x * x * x +  x2   * x * x + x1   * x + x0   + identChanges1.get(0);
                    y2 = x3_2 * x * x * x +  x2_2 * x * x + x1_2 * x + x0_2 + identChanges2.get(0);
                    //Log.d(TAG, "calculateEqulibrium: abs(y1-y2)[" + Math.abs( y1 - y2 ) + "]");
                    if (diff > abs( y1 - y2 )){
                        diff = abs( y1 - y2 );
                        pointX = x;
                        pointY = (y1+y2)/2;
                        //Log.d(TAG, "calculateEqulibrium: pointX[" + pointX + "] pointY[" + pointY + "]");
                    }
                }
                ArrayList<Double> arrayList = new ArrayList<>();
                if (diff < precision){
                    equiPoints.add(pointX);
                    equiPoints.add(pointY);
                    //Log.d(TAG, "calculateEqulibrium: calculated!");
                    return equiPoints;
                }else{
                    //Log.d(TAG, "calculateEqulibrium: not found!");
                }
            }
        }
        return equiPoints;
    }

    @Override
    public LineGraphSeries<DataPoint> CalculateData(MainScreenController.LineEnum line, int color) {
        double precision = MainScreenController.getPrecision();
        int maxDataPoints = MainScreenController.getMaxDataPoints();
        double x,y;
        x = 1;
        int x3, x2, x1, x0;
        HashMap<MainScreenController.LineEnum,ArrayList<Integer>> seriesSource = getGraphHelperObject().getSeries();

        x3 = seriesSource.get(line).get(0);
        x2 = seriesSource.get(line).get(1);
        x1 = seriesSource.get(line).get(2);
        x0 = seriesSource.get(line).get(3);

        LineGraphSeries<DataPoint> seriesLocal = new LineGraphSeries<DataPoint>();
        getLineGraphSeriesMap().remove(line);

        ArrayList<Integer> identChanges = getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line);
        //Log.d(TAG,  identChanges.get(3) + " * " + x3 + " * x^3 +"
        //+ identChanges.get(2) + " * " + x2 + " * x^2 +"
        //+ identChanges.get(1) + " * " + x1 + " * x^1 +"
        // + x0 + " + " + identChanges.get(0) );

        if( line == MainScreenController.LineEnum.ProductionCapabilities){
            for (double t = 0.5 * Math.PI; t > 0; t -= 0.05 ) { // <- or different step
                x = 8 * Math.cos(t);
                y = 8 * Math.sin(t);
                seriesLocal.appendData( new DataPoint(x,y), true, maxDataPoints );
            }
        }else{
            for( int i=0; i<maxDataPoints; i++){
                x = x + precision;
                y = identChanges.get(3) * x3 * x * x * x + identChanges.get(2) * x2 * x * x + identChanges.get(1) * x1 * x + x0 + identChanges.get(0);
                seriesLocal.appendData( new DataPoint(x,y), true, maxDataPoints );
            }
        }
        seriesLocal.setColor(color);
        getLineGraphSeriesMap().put(line, seriesLocal);
        return seriesLocal;
    }

    private void recalculateEquilibrium(){
        if (getGraphHelperObject().getCalculateEqulibrium())
        {
            ArrayList<Double> equiPoints,equiPoints2;
            //Log.d(TAG,"Equilibrium being calculated");
            equiPoints = calculateEqulibrium(getGraphHelperObject().getEquilibriumCurves().get(0),getGraphHelperObject().getEquilibriumCurves().get(1));

            //TODO create texts
            /*
            if( !equiPoints.isEmpty() ){
                text1.setText("EQ point " + graphHelperObject.getLabelX() + " = " + String.format( "%.2f", equiPoints.get(0)));
                text2.setText("EQ point " + graphHelperObject.getLabelY() + " = " + String.format( "%.2f", equiPoints.get(1)));
            }

            for (MainScreenController.LineEnum keySetLine:graphHelperObject.getSeries().keySet()) {
                for (MainScreenController.LineEnum dependantLine:graphHelperObject.getDependantCurveOnEquilibrium()){
                    //Log.d(TAG,"recalculateEquilibrium: " + keySetLine.toString() + " " + " " + dependantLine.toString());
                    if (keySetLine == dependantLine){
                        equiPoints2 = calculateEqulibrium(keySetLine,graphHelperObject.getEquilibriumCurves().get(1));
                        if (equiPoints2.isEmpty()){
                            Log.d(TAG,"recalculateEquilibrium: error");
                        }else if ( compareDoubleWithPrecision(equiPoints2.get(0),equiPoints.get(0)) &&
                                compareDoubleWithPrecision(equiPoints2.get(1),equiPoints.get(1))){
                            text3.setText("State is stable");
                        }else{
                            text3.setText("State is NOT stable");
                            //Log.d(TAG,"equiPoints2.get(0) == equiPoints.get(0) && equiPoints2.get(1) == equiPoints.get(1))"
                            //       + equiPoints2.get(0)+ " " + equiPoints.get(0)+ " " + equiPoints2.get(1)  + " " + equiPoints.get(1) );
                        }
                    }
                }
            }
            */
        }
    }
}
