package cz.mendelu.tomas.graphpef;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tomas on 19.08.2018.
 */

class MenuListAdapter extends ArrayAdapter<String>{

    private Context mContext;
    private int mResource;

    public MenuListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        TextView textView = (TextView) convertView.findViewById(R.id.menu_item_text);
        textView.setText(getItem(position));
        return convertView;
    }
}
