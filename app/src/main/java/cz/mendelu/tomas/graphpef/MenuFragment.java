package cz.mendelu.tomas.graphpef;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * Created by tomas on 12.08.2018.
 */

public class MenuFragment extends Fragment{
    private static final String TAG = "MenuFragment";

    private Button btnTest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstances) {
        View view = inflater.inflate(R.layout.menu_fragment,container,false);
        btnTest = (Button) view.findViewById(R.id.BTNtest1);

        btnTest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(getActivity(), "TESTING BUTTON",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}
