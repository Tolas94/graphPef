package cz.mendelu.tomas.graphpef.activities;

import android.content.Context;
import android.graphics.PaintFlagsDrawFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import cz.mendelu.tomas.graphpef.R;
import cz.mendelu.tomas.graphpef.helperObjects.StringIntegerPair;

/**
 * Created by tomas on 19.08.2018.
 */

class MenuListAdapter extends ArrayAdapter<StringIntegerPair> implements Serializable {

    private Context mContext;
    private int mResource;

    public MenuListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<StringIntegerPair> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        TextView textView = convertView.findViewById(R.id.menu_item_text);
        textView.setText(getItem(position).first);
        ImageView imageView = convertView.findViewById(R.id.menu_item_image);
        imageView.setBackgroundResource(getItem(position).second);
        return convertView;
    }
}
