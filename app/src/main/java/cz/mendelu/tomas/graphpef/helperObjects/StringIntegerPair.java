package cz.mendelu.tomas.graphpef.helperObjects;

import android.util.Pair;

import java.io.Serializable;

/**
 * Created by tomas on 23.09.2018.
 */

public class StringIntegerPair extends Pair<String,Integer> implements Serializable {
    public StringIntegerPair(String first, Integer second) {
        super(first, second);
    }
}
