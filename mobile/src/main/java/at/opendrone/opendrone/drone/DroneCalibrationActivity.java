/*
 * Last modified: 09.09.18 18:56
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone.drone;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import at.opendrone.opendrone.utils.OpenDroneUtils;
import at.opendrone.opendrone.R;
import at.opendrone.opendrone.network.ConnectDisconnectTasks;
import at.opendrone.opendrone.network.OpenDroneFrame;


/**
 * A simple {@link Fragment} subclass.
 */
public class DroneCalibrationActivity extends AppCompatActivity {
    private static final String TAG = "DroneCalibraty";

    private Button btn_calibration;
    private TextView txtView_calibration;
    private AVLoadingIndicatorView avi;

    private ConnectDisconnectTasks tasks = ConnectDisconnectTasks.getInstance();

    private Drone drone;
    private String mode;
    private int position;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return false;
            default:
                Log.i("DroneCalibration", "Default case");
                return super.onOptionsItemSelected(item);
        }

        //return super.onOptionsItemSelected(item);
    }

    private void initActionbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_Calibrate);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_calibration);

        Intent i = getIntent();
        if (i != null) {
            mode = i.getStringExtra("Mode");
            drone = (Drone) i.getSerializableExtra("Drone");
            position = i.getIntExtra("Position", -1);
        }

        initActionbar();

        initButtons();
        avi.hide();
    }

    private void initButtons() {

        btn_calibration = findViewById(R.id.btn_calibration);
        txtView_calibration = findViewById(R.id.txtView_calibration);
        avi = findViewById(R.id.avi);

        btn_calibration.setOnClickListener(v -> startCalibration());


    }

    public void startCalibration() {
        avi.show();
        txtView_calibration.setText(R.string.txt_calibration_inprogress);

        try {
            OpenDroneFrame frame = new OpenDroneFrame((byte) 1, new String[]{"1"}, new int[]{OpenDroneUtils.CODE_CALIBRATE});
            Log.i(TAG, frame.toString());
            tasks.sendMessage(frame.toString());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(),e);
        }
        //TODO: Calibration

        txtView_calibration.setText(R.string.txt_calibration_end);
        avi.hide();
        btn_calibration.setEnabled(false);
    }

}
