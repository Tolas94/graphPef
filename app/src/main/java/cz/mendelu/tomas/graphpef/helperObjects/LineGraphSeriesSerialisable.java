package cz.mendelu.tomas.graphpef.helperObjects;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;

/**
 * Created by tomas on 23.09.2018.
 */

public class LineGraphSeriesSerialisable extends LineGraphSeries<DataPoint> implements Serializable {
    public LineGraphSeriesSerialisable(DataPoint[] data) {
        super(data);
    }

    public LineGraphSeriesSerialisable() {
        super();
    }
}
