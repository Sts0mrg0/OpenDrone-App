/*
 * Last modified: 30.10.18 13:47
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone.flightplan;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.osmdroid.util.GeoPoint;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import at.opendrone.opendrone.utils.AndroidUtils;
import at.opendrone.opendrone.utils.OpenDroneUtils;
import at.opendrone.opendrone.R;


public class FlightPlanSaveFragment extends Fragment {
    private static final String TAG = "FlightPlanSavy";

    private RecyclerView flightPlanContainer;
    private EditText nameTxt;
    private EditText descTxt;
    private FloatingActionButton add;
    private FloatingActionButton save;
    private FloatingActionButton returnToMap;

    private String name = "";
    private String desc = "";
    private LinkedList<GeoPoint> points = new LinkedList<>();
    private List<Flightplan> flightplans = new LinkedList<>();
    private FlightPlaner planer;

    private View view;

    private SharedPreferences sp;
    private Gson gson = new Gson();

    public FlightPlanSaveFragment() {

    }

    @SuppressLint("ValidFragment")
    public FlightPlanSaveFragment(LinkedList<GeoPoint> points) {
        Log.i(TAG, "Points in Constructor: "+points.size());
        this.points = points;
    }

    @SuppressLint("ValidFragment")
    public FlightPlanSaveFragment(String name, String desc, LinkedList<GeoPoint> points) {
        this.name = name;
        this.desc = desc;
        this.points = points;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtils.hideKeyboard(flightPlanContainer, getActivity());
        lockOrientation();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void findViews() {
        nameTxt = view.findViewById(R.id.flightPlan_Name);
        descTxt = view.findViewById(R.id.flightPlan_Description);
        save = view.findViewById(R.id.btn_SaveFlightPlan);
        flightPlanContainer = view.findViewById(R.id.flightPlan_Coordinates);
        returnToMap = view.findViewById(R.id.btn_returnToMap);
        add = view.findViewById(R.id.fpAFAB);

        nameTxt.setText(name);
        descTxt.setText(desc);

        flightPlanContainer.setHasFixedSize(true);
        flightPlanContainer.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setAdapter() {
        GeoPointRecyclerViewAdapter adapter = new GeoPointRecyclerViewAdapter(points, getActivity(), this);
        flightPlanContainer.setAdapter(adapter);
    }

    private void checkShowAddFAB(){
        if (points.size() <= 0) {
            showAddFAB();
        } else {
            showFAB();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_flight_plan_save, container, false);

        sp = getActivity().getSharedPreferences("at.opendrone.opendrone", Context.MODE_PRIVATE);

        findViews();
        setAdapter();
        setListeners();
        setAttributes();
        checkShowAddFAB();
        return view;
    }

    @SuppressLint("RestrictedApi")
    private void showFAB() {
        save.setVisibility(View.VISIBLE);
        add.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("RestrictedApi")
    private void showAddFAB() {
        save.setVisibility(View.INVISIBLE);
        add.setVisibility(View.VISIBLE);
    }

    private void setSensorOrientation(){
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    private void lockOrientation(){
        getActivity().setRequestedOrientation(getActivity().getResources().getConfiguration().orientation);
    }

    private void setListeners() {
        save.setOnClickListener(v -> saveFlightPlan());
        add.setOnClickListener(v -> displayAddDialog());
        save.setOnLongClickListener(view -> {
            displayAddDialog();
            return true;
        });
        returnToMap.setOnClickListener(v -> returnToMap());
    }

    private void returnToMap() {
        sp.edit().putString(OpenDroneUtils.SP_FLIGHTPLAN_NAME, nameTxt.getText().toString()).apply();
        sp.edit().putString(OpenDroneUtils.SP_FLIGHTPLAN_DESC, descTxt.getText().toString()).apply();

        if(planer == null){
            this.planer = new FlightPlaner();
        }
        planer.setExistingPoints(points);
        updateFragment(planer);
    }

    private void saveFlightPlan() {
        Log.i(TAG, nameTxt.getText().toString());
        if(nameTxt.getText().toString().equals("")){
            nameTxt.setError(getString(R.string.flight_plan_save_error_name_empty));
            return;
        }
        String savedFPString = sp.getString(OpenDroneUtils.SP_FLIGHTPLANS, "");
        if(!savedFPString.equals("")){
            Flightplan[] flightPlanAr = gson.fromJson(savedFPString, Flightplan[].class);
            flightplans = new LinkedList<>(Arrays.asList(flightPlanAr));
        }
        Flightplan fp = new Flightplan();
        fp.setName(nameTxt.getText().toString());
        fp.setDescription(descTxt.getText().toString());
        fp.setCoordinates(points);

        int position = sp.getInt(OpenDroneUtils.SP_FLIGHTPLAN_POSITION, -1);

        if(position != -1){
            flightplans.remove(position);
            flightplans.add(position, fp);
        }else{
            flightplans.add(fp);
        }

        String serialized = gson.toJson(flightplans.toArray());
        Log.i(TAG, serialized);
        sp.edit().putString(OpenDroneUtils.SP_FLIGHTPLANS, serialized).apply();

        FlightPlanListFragment fragment = new FlightPlanListFragment();
        updateFragment(fragment);
        setSensorOrientation();
    }

    private void updateFragment(Fragment fragment){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout_FragmentContainer, fragment);
        ft.commit();
    }

    private int getPositionAtGeoPointForwards(GeoPoint p){
        for(int i = 0; i<points.size(); i++){
            if(points.get(i).distanceToAsDouble(p) <= 0){
                return i;
            }
        }
        return -1;
    }

    private int getPositionAtGeoPointBackwards(GeoPoint p){
        for(int i = points.size()-1; i>=0; i--){
            if(points.get(i).distanceToAsDouble(p) <= 0){
                return i;
            }
        }
        return -1;
    }

    private boolean isAlsoEnd(GeoPoint p){
        return getPositionAtGeoPointForwards(p) != getPositionAtGeoPointBackwards(p);
    }

    public void setAttributes() {
        this.nameTxt.setText(sp.getString(OpenDroneUtils.SP_FLIGHTPLAN_NAME, ""));
        this.descTxt.setText(sp.getString(OpenDroneUtils.SP_FLIGHTPLAN_DESC, ""));
    }

    public void updatePoint(int position, GeoPoint p){
        //double key = getKeyFromPosition(position);
        int lastIndex = points.size()-1;
        if(isAlsoEnd(p)){//the route is closed
            points.set(lastIndex, p);
            points.set(getPositionAtGeoPointForwards(p), p);
        }else{
            points.set(getPositionAtGeoPointForwards(p), p);
        }
        setAdapter();
    }

    public void removePoint(int position){
        //double key = getKeyFromPosition(position);
        GeoPoint p = points.get(position);
        if(isAlsoEnd(p)){//the route is closed
            points.remove(getPositionAtGeoPointBackwards(p));
            points.remove(getPositionAtGeoPointForwards(p));
        }else{
            points.remove(position);
        }
        Log.i(TAG, points.toString());
        setAdapter();
    }

    public void setFlightPlaner(FlightPlaner fp) {
        this.planer = fp;
    }

    private void displayAddDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.fragment_edit_geopoint, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);
        TextView view = promptView.findViewById(R.id.txt_HeaderEdit);
        view.setText(getString(R.string.flight_plan_save_add_coord));
        // setup a dialog window
        alertDialogBuilder.setPositiveButton(getString(R.string.flight_plan_save_add_coord_pos), (dialog, which) -> {
            EditText txt_latitude = promptView.findViewById(R.id.txt_lat);
            EditText txt_longitude = promptView.findViewById(R.id.txt_long);
            String latitude = txt_latitude.getText().toString();
            String longitude = txt_longitude.getText().toString();
            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);
                GeoPoint p = new GeoPoint(lat, lon);
                points.add(p);
                showFAB();
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), getString(R.string.flight_plan_save_invalid_coord), Toast.LENGTH_LONG).show();
            }

        });
        alertDialogBuilder.setNegativeButton(getString(R.string.flight_plan_save_add_coord_neg), (dialog, which) -> dialog.cancel());
        alertDialogBuilder.show();
    }
}
