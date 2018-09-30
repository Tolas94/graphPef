package cz.mendelu.tomas.graphpef.helperObjects;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.activities.GraphMenuListActivity;
import cz.mendelu.tomas.graphpef.activities.MainScreenControllerActivity;

/**
 * Created by tomas on 19.08.2018.
 */

public class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.MyViewHolder> implements Serializable {

    private final static String TAG = "MenuListAdapter";

    private List<StringIntegerPair> itemsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView menuItemText,menuItemTextUpper,menuItemTextLower;
        public ImageView menuItemImage;

        public MyViewHolder(View view) {
            super(view);
            menuItemText = view.findViewById(R.id.menu_item_text);
            menuItemTextUpper = view.findViewById(R.id.menu_item_text_upper);
            menuItemTextLower = view.findViewById(R.id.menu_item_text_lower);

            menuItemImage = view.findViewById(R.id.menu_item_image);
        }
    }
    public MenuListAdapter(List<StringIntegerPair> itemsList) {
        this.itemsList = itemsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.graph_menu_item_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        StringIntegerPair item = itemsList.get(position);
        holder.menuItemText.setText(item.first.get(0));
        holder.menuItemTextUpper.setText(item.first.get(1));
        holder.menuItemTextLower.setText(item.first.get(2));
        holder.menuItemImage.setImageResource(item.second);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy//hh:mm:ss");
                String ts = s.format(new Date());
                Answers.getInstance().logContentView(new ContentViewEvent()
                                                        .putContentId(GraphMenuListActivity.GraphEnum.values()[position].toString())
                                                        .putCustomAttribute("DateTime",ts)
                                                        .putContentType("Graph change"));
                Intent intent = new Intent(v.getContext(),MainScreenControllerActivity.class);
                //Log.d(TAG,"newGraph " + GraphMenuListActivity.GraphEnum.values()[position].toString());
                intent.putExtra("GRAPH_KEY", GraphMenuListActivity.GraphEnum.values()[position].toString());
                v.getContext().startActivity(intent);
            }
        });

    }



    @Override
    public int getItemCount() {
        return itemsList.size();
    }

}
