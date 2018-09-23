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

import java.io.Serializable;
import java.util.ArrayList;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.graphs.DefaultGraph;
import cz.mendelu.tomas.graphpef.interfaces.GraphIfc;

/**
 * Created by tomas on 12.08.2018.
 */

public class InfoFragment extends Fragment  implements Serializable {
    private static final String TAG = "InfoFragment";
    private final static String GRAPH_KEY = "GRAPH_KEY";
    private static InfoFragment instance = null;
    private ArrayList<TextView> textViews;
    private TextView text2,text3,text4,text5,text6,title;
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
            textViews = new ArrayList<>();

            graphIfc = (GraphIfc) getArguments().getSerializable(GRAPH_KEY);
            textViews.add( (TextView) view.findViewById(R.id.textView2));
            textViews.add( (TextView) view.findViewById(R.id.textView3));
            textViews.add( (TextView) view.findViewById(R.id.textView4));
            textViews.add( (TextView) view.findViewById(R.id.textView5));
            textViews.add( (TextView) view.findViewById(R.id.textView6));
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
            //Log.d(TAG, "populateTexts graphifc not null");
            ArrayList<String> texts = graphIfc.getSituationInfoTexts();
            for(int i=0; i<texts.size();i++){
                textViews.get(i).setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
                textViews.get(i).setText(texts.get(i));
            }
            title.setText(graphIfc.getTitle());
        }
    }
}
