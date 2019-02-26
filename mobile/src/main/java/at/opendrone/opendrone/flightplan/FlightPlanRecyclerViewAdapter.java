/*
 * Last modified: 14.10.18 22:47
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone.flightplan;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import at.opendrone.opendrone.utils.OpenDroneUtils;
import at.opendrone.opendrone.R;

public class FlightPlanRecyclerViewAdapter extends RecyclerView.Adapter<FlightPlanRecyclerViewAdapter.ViewHolder> {

    private List<Flightplan> flightplans;
    private AppCompatActivity activity;
    private FlightPlanListFragment fragment;
    private SharedPreferences sp;

    public FlightPlanRecyclerViewAdapter(List<Flightplan> flightplans, AppCompatActivity activity, FlightPlanListFragment fragment) {
        this.flightplans = flightplans;
        this.activity = activity;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_single_flight_plan, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Flightplan fp = flightplans.get(position);
        holder.flightplan_name.setText(fp.getName());
        holder.flightplan_description.setText(fp.getDescription());
        holder.flightplan = fp;
        holder.flightplan.setId(position);
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return this.flightplans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView mCardView;
        private TextView flightplan_name;
        private TextView flightplan_description;
        private ImageView btn_deleteFlightplan;
        private Button btn_editFlightplan;
        private Flightplan flightplan;
        private int position = -1;

        public ViewHolder(View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.single_flightplan_card);
            flightplan_name = itemView.findViewById(R.id.text_FlightPlanName);
            flightplan_description = itemView.findViewById(R.id.textView_FlightPlan);
            btn_deleteFlightplan = itemView.findViewById(R.id.imgBtn_Delete_Flightplan);
            btn_editFlightplan = itemView.findViewById(R.id.btn_Settings_flightplan);
            btn_editFlightplan.setOnClickListener(v -> {
                SharedPreferences sp = activity.getSharedPreferences("at.opendrone.opendrone", Context.MODE_PRIVATE);
                sp.edit().putString(OpenDroneUtils.SP_FLIGHTPLAN_NAME, flightplan.getName()).apply();
                sp.edit().putString(OpenDroneUtils.SP_FLIGHTPLAN_DESC, flightplan.getDescription()).apply();
                sp.edit().putInt(OpenDroneUtils.SP_FLIGHTPLAN_POSITION, position).apply();

                FlightPlanSaveFragment fp = new FlightPlanSaveFragment(flightplan.getName(), flightplan.getDescription(), flightplan.getCoordinates());
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout_FragmentContainer, fp);
                ft.commit();
            });
            btn_deleteFlightplan.setOnClickListener(v -> {
                Toast.makeText(activity, position + "", Toast.LENGTH_LONG).show();
                fragment.deletePosition(position);
                FlightPlanListFragment fp = new FlightPlanListFragment();
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout_FragmentContainer, fp);
                ft.commit();
            });
        }
    }
}
