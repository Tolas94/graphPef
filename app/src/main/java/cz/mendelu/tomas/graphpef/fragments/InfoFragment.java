package cz.mendelu.tomas.graphpef.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cz.mendelu.tomas.graphpef.R;


import cz.mendelu.tomas.graphpef.helperObjects.InfoListAdapter;
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
    private TextView title;
    private GraphIfc graphIfc;
    private RecyclerView infoTextView;
    private InfoListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<ArrayList<String>> texts;

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
            view.findViewById(R.id.RecyclerViewRelative).setVisibility(View.VISIBLE);
            textViews = new ArrayList<>();

            graphIfc = (GraphIfc) getArguments().getSerializable(GRAPH_KEY);
            title = view.findViewById(R.id.titleTextInfo);

            populateTexts();

            infoTextView = view.findViewById(R.id.listOfInfoOfGraph);

            mAdapter = new InfoListAdapter(texts);
            mLayoutManager = new LinearLayoutManager(getContext());
            infoTextView.setLayoutManager(mLayoutManager);
            infoTextView.setAdapter(mAdapter);
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
            texts = graphIfc.getSituationInfoTexts();

            title.setText(graphIfc.getTitle());
        }
    }
}
