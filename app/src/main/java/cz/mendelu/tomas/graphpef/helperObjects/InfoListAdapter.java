package cz.mendelu.tomas.graphpef.helperObjects;

import android.os.Build;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import cz.mendelu.tomas.graphpef.R;

/**
 * Created by tomas on 19.08.2018.
 */

public class InfoListAdapter extends RecyclerView.Adapter<InfoListAdapter.MyViewHolder> implements Serializable {

    private final static String TAG = "MenuListAdapter";

    private List<ArrayList<String>> itemsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView infoTitleName,infoTitleText,infoText;

        public MyViewHolder(View view) {
            super(view);
            infoTitleName = view.findViewById(R.id.info_item_title_name);
            infoTitleText = view.findViewById(R.id.info_item_title_text);
            infoText = view.findViewById(R.id.info_item_text);
        }
    }
    public InfoListAdapter(List<ArrayList<String>> infoList) {
        this.itemsList = infoList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.info_item_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        ArrayList<String> item = itemsList.get(position);
        holder.infoTitleName.setText(item.get(0));
        holder.infoTitleText.setText(item.get(1));
        if (Build.VERSION.SDK_INT >= 26){
            // supported from API 26  - otherwise crash
            holder.infoText.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        } else if (Build.VERSION.SDK_INT >= 23) {
            holder.infoText.setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY);
        }
        holder.infoText.setText(item.get(2));
    }


    @Override
    public int getItemCount() {
        return itemsList.size();
    }

}

