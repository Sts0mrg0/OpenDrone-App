/*
 * Last modified: 30.10.18 12:49
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

import java.util.LinkedList;
import java.util.List;

public class GeoPointRecyclerViewAdapter extends RecyclerView.Adapter<GeoPointRecyclerViewAdapter.ViewHolder> {

    private List<GeoPoint> points = new LinkedList<>();
    private Activity activity;
    private FlightPlanSaveFragment fragment;

    public GeoPointRecyclerViewAdapter(List<GeoPoint> points, Activity activity, FlightPlanSaveFragment fragment) {
        this.points = points;
        this.activity = activity;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_geopoint, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        GeoPoint p = points.get(position);
        String txt = String.format(activity.getString(R.string.flightplan_coord_list_element), p.getLatitude(), p.getLongitude());
        holder.coordsTxt.setText(txt);
        holder.position = position;

        holder.latitude = p.getLatitude();
        holder.longitude = p.getLongitude();
    }

    @Override
    public int getItemCount() {
        return this.points.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView coordsTxt;
        private ImageView btn_EditCoords;
        private ImageView btn_DeleteCoords;

        private double latitude;
        private double longitude;

        private int position;

        public ViewHolder(View itemView) {
            super(itemView);

            coordsTxt = itemView.findViewById(R.id.txt_Coords);
            btn_DeleteCoords = itemView.findViewById((R.id.btn_DeleteGeoPoint));
            btn_EditCoords = itemView.findViewById(R.id.btn_EditGeoPoint);

            addListeners();
        }

        private void addListeners() {
            btn_EditCoords.setOnClickListener(v -> displayAddDialog(latitude, longitude));
            btn_DeleteCoords.setOnClickListener(v -> deleteCoords());
        }

        private void deleteCoords() {
            fragment.removePoint(position);
        }

        private void displayAddDialog(double latitude, double longitude) {
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            View promptView = layoutInflater.inflate(R.layout.fragment_edit_geopoint, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            alertDialogBuilder.setView(promptView);

            final EditText txt_latitude = promptView.findViewById(R.id.txt_lat);
            final EditText txt_longitude = promptView.findViewById(R.id.txt_long);
            // setup a dialog window

            txt_latitude.setText(latitude+"");
            txt_longitude.setText(longitude+"");

            alertDialogBuilder.setPositiveButton("Save", (dialog, which) -> {
                savePoint(txt_latitude, txt_longitude);
            });
            alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            alertDialogBuilder.show();
        }

        private void savePoint(EditText txt_latitude, EditText txt_longitude){
            String latitude1 = txt_latitude.getText().toString();
            String longitude1 = txt_longitude.getText().toString();
            GeoPoint p = new GeoPoint(Double.parseDouble(latitude1), Double.parseDouble(longitude1));
            points.set(position, p);
            //String txt = String.format(activity.getString(R.string.flightplan_coord_list_element), p.getLatitude(), p.getLongitude());
            //coordsTxt.setText(txt);
            fragment.updatePoint(position, p);

        }
    }
}
