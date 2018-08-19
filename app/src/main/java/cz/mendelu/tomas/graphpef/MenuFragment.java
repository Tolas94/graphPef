package cz.mendelu.tomas.graphpef;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by tomas on 12.08.2018.
 */

public class MenuFragment extends Fragment{
    private static final String TAG = "MenuFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstances) {
        View view = inflater.inflate(R.layout.menu_fragment,container,false);
        Log.d(TAG,"onCreateView init");


        ListView graphMenu = view.findViewById(R.id.listOfGraphs);

        ArrayList<String> graphNames = new ArrayList<>();
        Log.d(TAG,"graphNames init");
        for(MainScreenController.GraphEnum graphEnum: MainScreenController.GraphEnum.values()){
            graphNames.add(graphEnum.toString());
        }
        Log.d(TAG,"graphNames size[" + graphNames.size() + "] e.g " /*+ graphNames.get(0)*/);
        MenuListAdapter adapter = new MenuListAdapter(getActivity(), R.layout.graph_menu_item_layout, graphNames);
        graphMenu.setAdapter(adapter);
        return view;
    }

}
