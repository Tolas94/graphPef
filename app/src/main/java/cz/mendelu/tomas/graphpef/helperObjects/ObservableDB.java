package cz.mendelu.tomas.graphpef.helperObjects;

import android.util.Log;

import java.util.Observable;

public class ObservableDB extends Observable {
    public static String TAG = "ObservableDB";

    public ObservableDB() {

    }

    public void updateUI() {
        Log.d(TAG, " notifyObservers ");
        setChanged();
        notifyObservers();
    }

    ;

}
