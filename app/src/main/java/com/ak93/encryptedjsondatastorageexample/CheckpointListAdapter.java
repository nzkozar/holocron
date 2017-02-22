package com.ak93.encryptedjsondatastorageexample;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Anže Kožar on 3.11.2016.
 * Adapted and modified from official android documentation
 */

public class CheckpointListAdapter extends RecyclerView.Adapter<CheckpointListAdapter.ViewHolder>{

    private OnListItemChildClickListener onItemChildClickListener;

    //Dataset
    private ArrayList<Object> mDataset = new ArrayList<>();
    //This tag is used to identify the adapter in interface callbacks
    // where multiple adapters are reporting to a single callback method
    String adapterTAG;

    private static final String TAG = "CheckpointListAdapter";

    /** Provide a reference to the views for each data item
     *  Complex data items may need more than one view per item, and
     *  you provide access to all the views for a data item in a view holder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        View mView;
        TextView id_text, name_text, latitude_text, longitude_text;
        Button deleteButton;
        ViewHolder(View v) {
            super(v);
            mView = v;
            id_text = (TextView) v.findViewById(R.id.id_text);
            name_text = (TextView) v.findViewById(R.id.name_text);
            latitude_text = (TextView) v.findViewById(R.id.latitude_text);
            longitude_text = (TextView) v.findViewById(R.id.longitude_text);
            deleteButton = (Button) v.findViewById(R.id.delete_button);
        }
    }

    /**
     * Construct a new adapter
     * @param checkpoints Checkpoints to populate the list with
     * @param tag String TAG to identify the adapter in interface callbacks
     */
    public CheckpointListAdapter(ArrayList<Object> checkpoints, String tag) {
        mDataset = checkpoints;
        adapterTAG = tag;
    }

    @Override
    public CheckpointListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_checkpoint, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CheckpointListAdapter.ViewHolder holder, final int position) {

        Checkpoint checkpoint = (Checkpoint) mDataset.get(position);

        holder.id_text.setText(String.valueOf(checkpoint.getId()));
        holder.name_text.setText(String.valueOf(checkpoint.getName()));
        holder.latitude_text.setText(String.valueOf(checkpoint.getLatitude()));
        holder.longitude_text.setText(String.valueOf(checkpoint.getLongitude()));


        if(this.onItemChildClickListener !=null){
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemChildClickListener.onListItemChildClick(view,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
            return mDataset.size();
    }

    public void setOnItemChildClickListener(OnListItemChildClickListener listener){
        onItemChildClickListener = listener;
    }
}
