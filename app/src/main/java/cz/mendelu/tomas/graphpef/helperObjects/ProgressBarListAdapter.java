package cz.mendelu.tomas.graphpef.helperObjects;

import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cz.mendelu.tomas.graphpef.R;

public class ProgressBarListAdapter extends RecyclerView.Adapter<ProgressBarListAdapter.MyViewHolder> implements Serializable {

    private final static String TAG = "ProgressBarListAdapter";

    private List<String> stringsList;
    private List<ArrayList<Integer>> priceList;
    private String neededPoints;

    public ProgressBarListAdapter(List<String> stringsList, List<ArrayList<Integer>> priceList) {
        this.stringsList = stringsList;
        this.priceList = priceList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_progress_bar_button, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ArrayList<Integer> item = priceList.get(position);
        startCountAnimation(holder.price, item.get(0), neededPoints + ":");
        startCountAnimationProgressBar(holder.progressBar, item.get(1));
        //holder.price.setText(neededPoints + ":" + item.get(0).toString());
        holder.progressBar.setProgress(item.get(1));
        holder.category.setText(stringsList.get(position));
    }

    @Override
    public int getItemCount() {
        return stringsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        TextView category, price;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            category = itemView.findViewById(R.id.progressBarNameOfCategory);
            price = itemView.findViewById(R.id.progressBarPrice);
            neededPoints = itemView.getResources().getString(R.string.mainScreenPointsNeededToUnlock);
        }
    }

    private void startCountAnimation(TextView textView, Integer maxPoints, String preText) {
        ValueAnimator animator = ValueAnimator.ofInt(0, maxPoints);
        animator.setDuration(1500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText(preText + animation.getAnimatedValue().toString());
            }
        });
        animator.start();
    }

    private void startCountAnimationProgressBar(ProgressBar progressBar, Integer maxPoints) {
        ValueAnimator animator = ValueAnimator.ofInt(0, maxPoints);
        animator.setDuration(1500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                progressBar.setProgress(Integer.valueOf(animation.getAnimatedValue().toString()));
            }
        });
        animator.start();
    }

}
