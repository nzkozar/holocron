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
import android.widget.ProgressBar;

import com.ak93.holocron.Holocron;
import com.ak93.holocron.HolocronResponse;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnListItemChildClickListener {

    private Holocron holocron;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private CheckpointListAdapter listAdapter;
    private ArrayList<Object> checkpoints = new ArrayList<>();

    private boolean loadAsync = true;

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        if(loadAsync){
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            holocron = new Holocron(this, new Holocron.HolocronResponseHandler() {
                @Override
                public void onHolocronResponse(int responseCode, HolocronResponse data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateCheckpointsList();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });
        }else {
            holocron = new Holocron(this);
        }

        init();
    }

    private void init(){

        Button addButton = (Button)findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

        Button removeAllButton = (Button)findViewById(R.id.remove_all_button);
        removeAllButton.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.checkpoint_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        checkpoints = new ArrayList<>();
        listAdapter = new CheckpointListAdapter(checkpoints,"CheckpointListAdapter");
        listAdapter.setOnItemChildClickListener(this);
        recyclerView.setAdapter(listAdapter);

        if(!loadAsync) updateCheckpointsList();
    }

    private void updateCheckpointsList(){
        if(holocron.isInitialized()) {
            checkpoints.clear();
            if(loadAsync){
                holocron.getAllAsync(Checkpoint.class, new Holocron.HolocronResponseHandler() {
                    @Override
                    public void onHolocronResponse(int responseCode, HolocronResponse data) {
                        checkpoints.addAll(data.getDataObjectList());
                        Log.i(TAG, "Objects retrieved: " + checkpoints.size());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAdapter.notifyDataSetChanged();
                                if(checkpoints.size()>0){
                                    recyclerView.smoothScrollToPosition(checkpoints.size()-1);
                                }
                            }
                        });
                    }
                });
            }else {
                checkpoints.addAll(holocron.getAll(Checkpoint.class));
                Log.i(TAG, "Objects retrieved: " + checkpoints.size());
                listAdapter.notifyDataSetChanged();
                if(checkpoints.size()>0){
                    recyclerView.smoothScrollToPosition(checkpoints.size()-1);
                }
            }
        }
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

                if(holocron.isInitialized())holocron.put(checkpoint, checkpoint.getId());

                field_name.setText("");
                field_longitude.setText("");
                field_latitude.setText("");

                updateCheckpointsList();
                break;
            case R.id.remove_all_button:
                if(holocron.isInitialized()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete confirmation!")
                            .setMessage("Do you really want to delete all stored instances of class Checkpoint?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    if(loadAsync){
                                        holocron.removeAllAsync(Checkpoint.class, new Holocron.HolocronResponseHandler() {
                                            @Override
                                            public void onHolocronResponse(int responseCode, HolocronResponse data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateCheckpointsList();
                                                        dialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                    }else{
                                        holocron.removeAll(Checkpoint.class);
                                        updateCheckpointsList();
                                        dialog.dismiss();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
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
