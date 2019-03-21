/*
 * Last modified: 09.09.18 17:21
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone.drone;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import at.opendrone.opendrone.MainActivity;
import at.opendrone.opendrone.R;
import at.opendrone.opendrone.utils.OpenDroneUtils;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static android.content.Context.MODE_PRIVATE;

public class DroneCardListRecyclerFragment extends Fragment {
    private static final String TAG = "DraneFraggy";
    public static List<Drone> drones = new LinkedList<>();
    private RecyclerView recyclerView;
    private SharedPreferences sp;
    private ImageView tutorialImg;
    private FloatingActionButton startConfigBtn;

    public DroneCardListRecyclerFragment() {

    }

    public static Fragment newInstance() {
        return new DroneCardListRecyclerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drone_card_list_recycler, container, false);

        findViews(view);
        updateAdapter();
        getDrones();
        return view;
    }

    private void findViews(View view) {
        sp = getActivity().getSharedPreferences("at.opendrone.opendrone", MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recycler_view);
        tutorialImg = view.findViewById(R.id.droneTutorialImgView);
        startConfigBtn = view.findViewById(R.id.btn_StartConfiguration);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        addClickListenerToFAB();
    }

    private void addNewDrone() {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, DroneSettingsActivity.class);
        intent.putExtra("Position", drones.size());
        intent.putExtra("Mode", "new");
            /*Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(
                    sourceView, 0, 0, sourceView.getWidth(), sourceView.getHeight()).toBundle();*/

        //ActivityCompat.startActivity(activity, intent, options);
        startActivity(intent);
    }

    private void addClickListenerToFAB() {
        startConfigBtn.setOnClickListener(v -> addNewDrone());
    }

    public void remove(int i) {
        drones.remove(i);
        updateAdapter();
    }

    public void updateAdapterAndSaveList() {
        saveDrones();
        updateAdapter();
    }

    private void updateAdapter() {
        DroneRecyclerViewAdapter adapter = new DroneRecyclerViewAdapter(drones, this.getActivity(), DroneCardListRecyclerFragment.this, sp);
        recyclerView.setAdapter(adapter);
    }

    private void getDrones() {
        String droneJSON = sp.getString("DroneList", "");

        if (!droneJSON.equals("")) {
            Gson gson = new Gson();
            Drone[] droneAr = gson.fromJson(droneJSON, Drone[].class);

            drones = new LinkedList<>(Arrays.asList(droneAr));
        }
    }

    private void saveDrones() {
        Gson gson = new Gson();
        String serialized = gson.toJson(drones.toArray());

        sp.edit().putString("DroneList", serialized).apply();
    }

    private void startTutorial() {
        if (!sp.getBoolean(OpenDroneUtils.SP_TUTORIAL, false)) {
            tutorialImg.setVisibility(View.VISIBLE);
            showTargetPrompt(tutorialImg.getId());
            startConfigBtn.setEnabled(false);
            ((MainActivity) getActivity()).canOpenDrawer = false;
        } else {
            tutorialImg.setVisibility(View.GONE);
            startConfigBtn.setEnabled(true);
            ((MainActivity) getActivity()).canOpenDrawer = true;
        }
    }

    private void showTargetPrompt(int targetID) {
        new MaterialTapTargetPrompt.Builder(this)
                .setTarget(targetID)
                .setAutoDismiss(false)
                .setBackgroundColour(getActivity().getColor(R.color.tutorial_prompt_bg))
                .setPrimaryText("The Drone List")
                .setSecondaryText("Here you can see your configurated drones and add new drones if needed")
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                        startConfigBtn.setEnabled(true);
                        ((MainActivity) getActivity()).canOpenDrawer = true;
                        ((MainActivity) getActivity()).initFlightplaner();
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapter();
        startTutorial();
    }

}
