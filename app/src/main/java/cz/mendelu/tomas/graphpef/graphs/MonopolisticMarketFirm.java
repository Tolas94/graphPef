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

import cz.mendelu.tomas.graphpef.MainAppClass;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.MainActivity;
import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;

import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.AverageCost;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.AverageVariableCost;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.Demand;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.Equilibrium;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.MarginalCost;
import static cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity.LineEnum.PriceLevel;

/**
 * Created by tomas on 02.09.2018.
 */

public class MonopolisticMarketFirm extends DefaultGraph  implements Serializable {
    private static final String TAG = "MonopolisticMarketFirm";

    public MonopolisticMarketFirm(ArrayList<String> graphTexts, ArrayList<MainScreenControllerActivity.LineEnum> movableObjects, MainScreenControllerActivity.LineEnum movableEnum, HashMap<MainScreenControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject ) {
        super(graphTexts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);

        setLabelOnstartOfCurve(false);
        setMovableDirections(new ArrayList<>(Arrays.asList(MainScreenControllerActivity.Direction.up, MainScreenControllerActivity.Direction.down)));
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(MainScreenControllerActivity.LineEnum line, int color) {
        if (getLineGraphSeries().get(line) == null) {
            double precision = MainScreenControllerActivity.getPrecision();
            int maxDataPoints = MainScreenControllerActivity.getMaxDataPoints();
            double x, y;
            x = 1;
            y = 0;
            if (line == PriceLevel){
                x = 0;
            }
            Log.d(TAG,"calculateData start");
            LineGraphSeriesSerialisable seriesLocal = new LineGraphSeriesSerialisable();

            for (int i = 0; i < maxDataPoints; i++) {
                x = x + precision;
                y = 0;
                if (line == AverageCost ) {
                    Log.d(TAG, "AverageCost");
                    y = (( 0.5 ) * (( x - 8 )*( x - 8 ))) + 4;
                    Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
                } else if (line == MarginalCost) {
                    Log.d(TAG, "MarginalCost");
                    y = (( 0.5 ) * (( x - 6 )*( x - 6 ))) + 2;
                    Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
                } else if (line == PriceLevel){
                    Log.d(TAG, "PriceLevel");
                    y = 8;
                    Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
                } else if (line == MainScreenControllerActivity.LineEnum.Demand){
                    Log.d(TAG, "Demand");
                    y = ( 0.06 ) * ((x-20)*(x-20))+2;
                    Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
                } else if (line == MainScreenControllerActivity.LineEnum.MarginalRevenue) {
                    Log.d(TAG, "MarginalRevenue");
                    y = (0.07) * ((x - 17) * (x - 20));
                    Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
                }

                if (y < 13 && y > 0 && x > 0 && x < 13){
                    seriesLocal.appendData(new DataPoint(x, y), true, maxDataPoints);
                }
            }
            calculateLabel(line,x,y);
            Log.d(TAG, "MinY [" + seriesLocal.getLowestValueY() + "] maxY[" + seriesLocal.getHighestValueY() + "]");
            Log.d(TAG, "MinX [" + seriesLocal.getLowestValueX() + "] maxX[" + seriesLocal.getHighestValueX() + "]");
            seriesLocal.setColor(color);
            getLineGraphSeries().put(line, seriesLocal);
            Log.d(TAG,"calculateData end");
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
        } else if (getMovableEnum() == MainScreenControllerActivity.LineEnum.MarginalRevenue){
            if (dir == MainScreenControllerActivity.Direction.up){
                super.moveObject(MainScreenControllerActivity.Direction.right);
                super.moveObject(MainScreenControllerActivity.Direction.up,Demand, 1);
                super.moveObject(MainScreenControllerActivity.Direction.right,Demand, 1);
            }else if (dir == MainScreenControllerActivity.Direction.down){
                super.moveObject(MainScreenControllerActivity.Direction.left);
                super.moveObject(MainScreenControllerActivity.Direction.down,Demand, 1);
                super.moveObject(MainScreenControllerActivity.Direction.left,Demand, 1);
            }
        }
    }

    @Override
    public List<ArrayList<String>> getSituationInfoTexts() {
        //https://stackoverflow.com/questions/9290651/make-a-hyperlink-textview-in-android
        List<ArrayList<String>> arrayList = new ArrayList<>();
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.monopolistic_market_firm_info_text_1_title),"",getResources().getString(R.string.monopolistic_market_firm_info_text_1))));
        arrayList.add(new ArrayList<String>(Arrays.asList(getResources().getString(R.string.monopolistic_market_firm_info_text_2_title),"",getResources().getString(R.string.monopolistic_market_firm_info_text_2))));

        return arrayList;
    }

    private void populateTexts(boolean equilibriumExists, ArrayList<Double> equilibrium){
        Log.d(TAG,"populateTexts");
        refreshInfoTexts();
        ArrayList texts = new ArrayList();
        if (equilibriumExists){
            if (getProfit() > 0){
                texts.add("Zisk je " + String.format( "%.1f",getProfit()));
            }else if (getProfit() < 0){
                texts.add("Ztráta je " + String.format( "%.1f",getProfit()));
            }else{
                //texts.add("Firma je v dlouhodobe rovnováze");
            }
//            texts.add(getResources().getString(R.string.equilibrium_is) + " " + getStringFromLineEnum(getGraphHelperObject().getDependantCurveOnEquilibrium().get(1)) + " = " + String.format( "%.1f", equilibrium.get(0) ));
  //          texts.add(getResources().getString(R.string.equilibrium_is) + " " + getStringFromLineEnum(getGraphHelperObject().getDependantCurveOnEquilibrium().get(0)) + " = " + String.format( "%.1f", equilibrium.get(1) ));
        }else{
            texts.add("Firma odchází");
            texts.add(getResources().getString(R.string.equilibrium_cannot));
        }

        for(MainScreenControllerActivity.LineEnum line:getMovableObjects()){
            texts.add(getStringFromLineEnum(line) + " " + getResources().getString(R.string.changed_by) + " " + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0));
        }
        setGraphTexts(texts);
    }

    private double getProfit(){
        ArrayList<Double> eqPoints = getEquiPoints();
        double cost = 0, price = 0, retVal = 0;
        if (!eqPoints.isEmpty()){
            if (getLineGraphSeries().get(Demand).getValues(eqPoints.get(0)-0.5,eqPoints.get(0)+0.5).hasNext()){
                price = getLineGraphSeries().get(Demand).getValues(eqPoints.get(0)-0.05,eqPoints.get(0)+0.05).next().getY();
            }
            if (getLineGraphSeries().get(AverageCost).getValues(eqPoints.get(0)-0.5,eqPoints.get(0)+0.5).hasNext()){
                cost = getLineGraphSeries().get(AverageCost).getValues(eqPoints.get(0)-0.05,eqPoints.get(0)+0.05).next().getY();
            }
            retVal = price - cost;
        }
        Log.d(TAG,"price - cost " + price + " - " + cost + " = " + retVal);
        return retVal;
    }

    @Override
    public ArrayList<Double> calculateEqulibrium() {
        Log.d(TAG,"calculateEqulibrium");
        ArrayList<Double> retVal;
        retVal = super.calculateEqulibrium();
        if (!retVal.isEmpty()){
            if (retVal.size() == 4){
                Double x = retVal.get(2);
                Double y = retVal.get(3);
                retVal.clear();
                retVal.add(x);
                retVal.add(y);
            }
            retVal.remove(1);
            if (getLineGraphSeries().get(Demand).getValues(retVal.get(0)-0.5,retVal.get(0)+0.5).hasNext()){
                retVal.add(getLineGraphSeries().get(Demand).getValues(retVal.get(0)-0.05,retVal.get(0)+0.05).next().getY());
            }
            getGraphHelperObject().setShowEquilibrium(true);
            populateTexts(true,retVal);
            showCurvesDependantOnEquilibrium(retVal.get(0),retVal.get(1));

        }else{
            getGraphHelperObject().setShowEquilibrium(false);
            populateTexts(false,retVal);
        }
        return retVal;
    }

    @Override
    public int getColorOf(MainScreenControllerActivity.LineEnum lineEnum) {
        if (lineEnum == Equilibrium){
            double profit = round(getProfit(),1);
            if (profit > 0){
                return MainAppClass.getContext().getColor(R.color.colorGreenComplementary);
            }else if (profit < 0){
                return MainAppClass.getContext().getColor(R.color.red);
            }else{
                return MainAppClass.getContext().getColor(R.color.black);
            }
        }
        return super.getColorOf(lineEnum);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
