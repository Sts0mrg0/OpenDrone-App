/*
 * Last modified: 30.10.18 12:13
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone.flightplan;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.appcenter.analytics.Analytics;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import at.opendrone.opendrone.utils.OpenDroneUtils;
import at.opendrone.opendrone.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class FlightPlanListFragment extends Fragment {

    private View view;
    private RecyclerView recyclerView;
    private FloatingActionButton btn_AddFP;
    private List<Flightplan> plans;
    private SharedPreferences sp;

    public FlightPlanListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        sp = getActivity().getSharedPreferences("at.opendrone.opendrone", Context.MODE_PRIVATE);
        view = inflater.inflate(R.layout.fragment_flight_plan_list, container, false);

        findViews();
        getSavedFlightPlans();
        return view;
    }

    private void getSavedFlightPlans(){
        try {
            Gson gson = new Gson();
            String flightplanJSON = sp.getString(OpenDroneUtils.SP_FLIGHTPLANS, "");
            if (!flightplanJSON.equals("")) {
                Flightplan[] flightPlanAr = gson.fromJson(flightplanJSON, Flightplan[].class);
                plans = new LinkedList<>(Arrays.asList(flightPlanAr));
                setAdapter();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), getResources().getString(R.string.exception_sorry), Toast.LENGTH_LONG).show();
        }
    }

    private void findViews(){
        recyclerView = view.findViewById(R.id.flightplans);
        btn_AddFP = view.findViewById(R.id.btn_AddFlightPlan);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setAddBtnListener();
    }

    private void setAddBtnListener(){
        btn_AddFP.setOnClickListener(v -> {
            Analytics.trackEvent("NewFlightPlanAdded");
            sp.edit().putString(OpenDroneUtils.SP_FLIGHTPLAN_NAME, "").apply();
            sp.edit().putString(OpenDroneUtils.SP_FLIGHTPLAN_DESC, "").apply();
            sp.edit().putInt(OpenDroneUtils.SP_FLIGHTPLAN_POSITION, -1).apply();
            FlightPlaner fp = new FlightPlaner();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frameLayout_FragmentContainer, fp);
            ft.commit();
        });
    }

    public void deletePosition(int position) {
        plans.remove(position);
        try {

            Gson gson = new Gson();
            String serialized = gson.toJson(plans.toArray());
            sp.edit().putString(OpenDroneUtils.SP_FLIGHTPLANS, serialized).apply();
        } catch (Exception e) {
            Log.e("FlightPlanError", "Something went wrong",e);
        }

    }

    public void setAdapter() {
        FlightPlanRecyclerViewAdapter adapter = new FlightPlanRecyclerViewAdapter(plans, (AppCompatActivity) getActivity(), this);
        recyclerView.setAdapter(adapter);
    }

}
