package cz.mendelu.tomas.graphpef;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.zip.Inflater;

import cz.mendelu.tomas.graphpef.graphs.DefaultGraph;

/**
 * Created by tomas on 12.08.2018.
 */

public class InfoFragment extends Fragment{
    private static final String TAG = "InfoFragment";
    private TextView text2,text3,text4,title;

    private GraphIfc graphIfc;
    private final static String GRAPH_KEY = "GRAPH_KEY";

    public static InfoFragment newInstance(DefaultGraph defaultGraph){
        InfoFragment infoFragment = new InfoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("GRAPH_KEY",defaultGraph);
        infoFragment.setArguments(bundle);
        return  infoFragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstances) {
        View view = inflater.inflate(R.layout.info_fragment,container,false);

        if (getArguments() != null){
            view.findViewById(R.id.testing_relative).setVisibility(View.VISIBLE);
            Log.d(TAG, "visible");
            graphIfc = (GraphIfc) getArguments().getSerializable(GRAPH_KEY);
            text2 = view.findViewById(R.id.textView2);
            text3 = view.findViewById(R.id.textView3);
            text4 = view.findViewById(R.id.textView4);
            title = view.findViewById(R.id.titleTextInfo);

            populateTexts();
        }

        return view;
    }

    private void populateTexts(){

        //graphIfc.getInfo();
    }
}
