package cz.mendelu.tomas.graphpef.helperObjects;

import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tomas on 23.09.2018.
 */

public class StringIntegerPair extends Pair<ArrayList<String>,Integer> implements Serializable {
    public StringIntegerPair(ArrayList<String> first, Integer second) {
        super(first, second);
    }

}
