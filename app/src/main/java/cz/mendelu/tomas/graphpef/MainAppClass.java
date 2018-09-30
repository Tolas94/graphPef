package cz.mendelu.tomas.graphpef;

import android.app.Application;
import android.content.Context;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Created by tomas on 22.09.2018.
 */

public class MainAppClass extends Application {
    protected static MainAppClass instance;

    //private static Context myContext = getContext();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }
}