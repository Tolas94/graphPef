package cz.mendelu.tomas.graphpef.helperObjects;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import java.io.Serializable;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;
import cz.mendelu.tomas.graphpef.fragments.GraphFragment;

public class GraphCurveChooseAdapter extends RecyclerView.Adapter<GraphCurveChooseAdapter.MyViewHolder> implements Serializable {

    private final static String TAG = "GraphCurveChooseAdapter";

    private List<String> curveNameList;
    private List<MainScreenControllerActivity.LineEnum> lineList;
    private Fragment parent;
    private int selected;
    private int colorPrimary, colorWhite, colorGrey, colorBlack;

    public GraphCurveChooseAdapter(List<String> curveNameList, List<MainScreenControllerActivity.LineEnum> lineList, Fragment parent, int selected) {
        this.curveNameList = curveNameList;
        this.lineList = lineList;
        this.parent = parent;
        this.selected = selected;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.graph_curve_toggle_button, parent, false);
        colorPrimary = itemView.getResources().getColor(R.color.colorPrimary, itemView.getContext().getTheme());
        colorWhite = itemView.getResources().getColor(R.color.white, itemView.getContext().getTheme());
        colorGrey = itemView.getResources().getColor(R.color.grey3, itemView.getContext().getTheme());
        colorBlack = itemView.getResources().getColor(R.color.black, itemView.getContext().getTheme());

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolderS");
        String curveName = curveNameList.get(position);
        MainScreenControllerActivity.LineEnum curveEnum = lineList.get(position);

        holder.button.setText(curveName);
        holder.button.setTextOn(curveName);
        holder.button.setTextOff(curveName);
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((GraphFragment) parent).setChosenCurve(curveEnum);
                selected = position;
                notifyDataSetChanged();
            }
        });

        if (position != selected) {
            if (holder.button.isChecked()) {
                holder.button.toggle();
            }
            holder.button.setBackgroundColor(colorGrey);
            holder.button.setTextColor(colorWhite);
        } else {
            holder.button.setBackgroundColor(colorPrimary);
            holder.button.setTextColor(colorWhite);
        }
    }

    @Override
    public int getItemCount() {
        return curveNameList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ToggleButton button;
        public MainScreenControllerActivity.LineEnum line;

        public MyViewHolder(View view) {
            super(view);
            button = view.findViewById(R.id.graphCurveToggleButton);
        }

    }

}



