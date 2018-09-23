package cz.mendelu.tomas.graphpef.helperObjects;

import android.util.Pair;

import java.io.Serializable;

/**
 * Created by tomas on 23.09.2018.
 */

public class PositionPair extends Pair<Double,Double> implements Serializable{

    public PositionPair(Double first, Double second) {
        super(first, second);
    }

}
