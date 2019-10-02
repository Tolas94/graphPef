package cz.mendelu.tomas.graphpef.graphs;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.core.content.ContextCompat;
import cz.mendelu.tomas.graphpef.MainAppClass;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.GraphControllerActivity;
import cz.mendelu.tomas.graphpef.helperObjects.GraphHelperObject;
import cz.mendelu.tomas.graphpef.helperObjects.LineGraphSeriesSerialisable;

import static cz.mendelu.tomas.graphpef.activities.GraphControllerActivity.LineEnum.AverageCost;
import static cz.mendelu.tomas.graphpef.activities.GraphControllerActivity.LineEnum.Equilibrium;
import static cz.mendelu.tomas.graphpef.activities.GraphControllerActivity.LineEnum.IndividualDemand;
import static cz.mendelu.tomas.graphpef.activities.GraphControllerActivity.LineEnum.MarginalCost;
import static cz.mendelu.tomas.graphpef.activities.GraphControllerActivity.LineEnum.PriceLevel;

/**
 * Created by tomas on 02.09.2018.
 */

public class MonopolisticMarketFirm extends DefaultGraph  implements Serializable {
    private static final String TAG = "MonopolisticMarketFirm";

    public MonopolisticMarketFirm(ArrayList<String> graphTexts, ArrayList<GraphControllerActivity.LineEnum> movableObjects, GraphControllerActivity.LineEnum movableEnum, HashMap<GraphControllerActivity.LineEnum, ArrayList<Integer>> series, ArrayList<String> optionsLabels, GraphHelperObject graphHelperObject) {
        super(graphTexts, movableObjects, movableEnum, series, optionsLabels, graphHelperObject);

        setLabelOnstartOfCurve(false);
        setMovableDirections(new ArrayList<>(Arrays.asList(GraphControllerActivity.Direction.up, GraphControllerActivity.Direction.down)));
    }

    @Override
    public LineGraphSeries<DataPoint> calculateData(GraphControllerActivity.LineEnum line, int color) {
        if (getLineGraphSeries().get(line) == null) {
            double precision = GraphControllerActivity.getPrecision();
            int maxDataPoints = GraphControllerActivity.getMaxDataPoints();
            double x, y;
            x = 1;
            y = 0;
            if (line == PriceLevel){
                x = 0;
            }
            //Log.d(TAG,"calculateData start");
            LineGraphSeriesSerialisable seriesLocal = new LineGraphSeriesSerialisable();

            for (int i = 0; i < maxDataPoints; i++) {
                x = x + precision;
                y = 0;
                if (line == AverageCost ) {
                    //Log.d(TAG, "AverageCost");
                    y = (( 0.5 ) * (( x - 8 )*( x - 8 ))) + 4;
                    //Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
                } else if (line == MarginalCost) {
                    //Log.d(TAG, "MarginalCost");
                    y = (( 0.5 ) * (( x - 6 )*( x - 6 ))) + 2;
                    //Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
                } else if (line == PriceLevel){
                    //Log.d(TAG, "PriceLevel");
                    y = 8;
                    //Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
                } else if (line == IndividualDemand){
                    //Log.d(TAG, "IndividualDemand");
                    y = ( 0.06 ) * ((x-20)*(x-20))+2;
                    //Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
                } else if (line == GraphControllerActivity.LineEnum.MarginalRevenue) {
                    //Log.d(TAG, "MarginalRevenue");
                    y = (0.07) * ((x - 17) * (x - 20));
                    //Log.d(TAG, "[x,y] [" + x + ","  + y + "]" );
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
    public void moveObject(GraphControllerActivity.Direction dir) {
        super.moveObject(dir);
        if (getMovableEnum() == GraphControllerActivity.LineEnum.AverageCost) {
            if (dir == GraphControllerActivity.Direction.up) {
                super.moveObject(GraphControllerActivity.Direction.right);
                super.moveObject(GraphControllerActivity.Direction.up, MarginalCost, 1);
                super.moveObject(GraphControllerActivity.Direction.right, MarginalCost, 1);
            } else if (dir == GraphControllerActivity.Direction.down) {
                super.moveObject(GraphControllerActivity.Direction.left);
                super.moveObject(GraphControllerActivity.Direction.down, MarginalCost, 1);
                super.moveObject(GraphControllerActivity.Direction.left, MarginalCost, 1);
            }
        } else if (getMovableEnum() == GraphControllerActivity.LineEnum.MarginalRevenue) {
            if (dir == GraphControllerActivity.Direction.up) {
                super.moveObject(GraphControllerActivity.Direction.right);
                super.moveObject(GraphControllerActivity.Direction.up, IndividualDemand, 1);
                super.moveObject(GraphControllerActivity.Direction.right, IndividualDemand, 1);
            } else if (dir == GraphControllerActivity.Direction.down) {
                super.moveObject(GraphControllerActivity.Direction.left);
                super.moveObject(GraphControllerActivity.Direction.down, IndividualDemand, 1);
                super.moveObject(GraphControllerActivity.Direction.left, IndividualDemand, 1);
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

        for (GraphControllerActivity.LineEnum line : getMovableObjects()) {
            texts.add(getStringFromLineEnum(line) + " " + getResources().getString(R.string.changed_by) + " " + getGraphHelperObject().getLineChangeIdentificatorByLineEnum(line).get(0));
        }
        setGraphTexts(texts);
    }

    private double getProfit(){
        ArrayList<Double> eqPoints = getEquiPoints();
        double cost = 0, price = 0, retVal = 0;
        if (!eqPoints.isEmpty()){
            if (getLineGraphSeries().get(IndividualDemand).getValues(eqPoints.get(0)-0.5,eqPoints.get(0)+0.5).hasNext()){
                price = getLineGraphSeries().get(IndividualDemand).getValues(eqPoints.get(0)-0.05,eqPoints.get(0)+0.05).next().getY();
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
            if (getLineGraphSeries().get(IndividualDemand).getValues(retVal.get(0)-0.5,retVal.get(0)+0.5).hasNext()){
                retVal.add(getLineGraphSeries().get(IndividualDemand).getValues(retVal.get(0)-0.05,retVal.get(0)+0.05).next().getY());
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
    public int getColorOf(GraphControllerActivity.LineEnum lineEnum) {
        if (lineEnum == Equilibrium){
            double profit = round(getProfit(),1);
            if (profit > 0){
                ContextCompat.getColor(MainAppClass.getContext(), R.color.colorGreenComplementary);
                return ContextCompat.getColor(MainAppClass.getContext(), R.color.colorGreenComplementary);
            }else if (profit < 0){
                return ContextCompat.getColor(MainAppClass.getContext(), R.color.red);
            }else{
                return ContextCompat.getColor(MainAppClass.getContext(), R.color.black);
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
