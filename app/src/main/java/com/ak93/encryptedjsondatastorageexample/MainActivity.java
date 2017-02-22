package com.ak93.encryptedjsondatastorageexample;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ak93.holocron.Holocron;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnListItemChildClickListener {

    private Holocron holocron;

    private CheckpointListAdapter listAdapter;
    private ArrayList<Object> checkpoints = new ArrayList<>();

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        holocron = Holocron.init(this);

        init();
    }

    private void init(){
        Button addButton = (Button)findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

        Button removeAllButton = (Button)findViewById(R.id.remove_all_button);
        removeAllButton.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.checkpoint_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        checkpoints = (ArrayList<Object>) holocron.getAll(Checkpoint.class);
        listAdapter = new CheckpointListAdapter(checkpoints,"CheckpointListAdapter");
        listAdapter.setOnItemChildClickListener(this);
        recyclerView.setAdapter(listAdapter);
    }

    private void updateCheckpointsList(){
        checkpoints.clear();
        checkpoints.addAll(holocron.getAll(Checkpoint.class));
        Log.i(TAG,"Objects retrieved: "+checkpoints.size());
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_button:
                EditText field_name = (EditText) findViewById(R.id.field_name);
                EditText field_longitude = (EditText) findViewById(R.id.field_longitude);
                EditText field_latitude = (EditText) findViewById(R.id.field_latitude);

                String name = String.valueOf(field_name.getText());
                double longitude = Double.parseDouble(String.valueOf(field_longitude.getText()));
                double latitude = Double.parseDouble(String.valueOf(field_latitude.getText()));

                Checkpoint checkpoint = new Checkpoint(
                        holocron.getConfiguration().getNextClassId(Checkpoint.class)
                        ,name,longitude,latitude);

                holocron.put(checkpoint, checkpoint.getId());

                field_name.setText("");
                field_longitude.setText("");
                field_latitude.setText("");

                updateCheckpointsList();
                break;
            case R.id.remove_all_button:
                new AlertDialog.Builder(this)
                        .setTitle("Delete confirmation!")
                        .setMessage("Do you really want to delete all stored instances of class Checkpoint?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                holocron.removeAll(Checkpoint.class);
                                updateCheckpointsList();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .show();
                break;
        }
    }

    @Override
    public void onListItemChildClick(View view, int position) {
        final Checkpoint checkpoint = (Checkpoint) checkpoints.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete confirmation")
                .setMessage("Delete Checkpoint "+checkpoint.getId()+" named '"+checkpoint.getName()+"' ?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        holocron.remove(Checkpoint.class,checkpoint.getId());
                        updateCheckpointsList();
                    }
                })
                .setNegativeButton("Cancel",null)
                .show();
    }
}
