package cz.mendelu.tomas.graphpef.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.graphs.DefaultGraph;
import cz.mendelu.tomas.graphpef.interfaces.GraphIfc;

/**
 * Created by tomas on 12.08.2018.
 */

public class InfoFragment extends Fragment{
    private static final String TAG = "InfoFragment";
    private final static String GRAPH_KEY = "GRAPH_KEY";
    private static InfoFragment instance = null;

    private TextView text2,text3,text4,title;
    private GraphIfc graphIfc;

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

            graphIfc = (GraphIfc) getArguments().getSerializable(GRAPH_KEY);
            text2 = view.findViewById(R.id.textView2);
            text3 = view.findViewById(R.id.textView3);
            text4 = view.findViewById(R.id.textView4);
            title = view.findViewById(R.id.titleTextInfo);

            populateTexts();
        }

        instance = this;
        return view;
    }

    public static InfoFragment getInstance(){
        return instance;
    }

    public void populateTexts(){
        Log.d(TAG, "populateTexts");
        if(graphIfc != null){
            Log.d(TAG, "populateTexts graphifc not null");
            ArrayList<String> texts = graphIfc.getSituationInfoTexts();
            title.setText(graphIfc.getTitle());
            text2.setText(texts.get(0));
            text2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            text3.setText(texts.get(1));
            text3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            text4.setText(texts.get(2));
            Log.d(TAG, "populateTexts text4 =" + texts.get(2));
            text4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        }
    }
}
