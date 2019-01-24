/*
 * Last modified: 14.10.18 22:52
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FlightPlaner extends Fragment {

    private static final String TAG = "FlightPlany";

    private MapView mMapView;

    private LinkedHashMap<Double, GeoPoint> points = new LinkedHashMap<>();
    public LinkedHashMap<Double, GeoPoint> existingPoints = new LinkedHashMap<>();

    private SharedPreferences sp;
    private FloatingActionButton saveFAB;
    private FloatingActionButton removeFAB;
    private boolean canAddMarker = true;
    private Location location;

    private boolean mRequestingLocationUpdates = false;
    private boolean hasAlreadyCentered = false;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private Polyline line = new Polyline();

    private Rect outRect = new Rect();
    private int[] locationAr = new int[2];

    private MotionEvent lastEvent;

    private List<Marker> markers = new LinkedList<>();

    private int draggedPosition = -1;

    public FlightPlaner() {
        // Required empty public constructor
        points = new LinkedHashMap<>();
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        mMapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        drawExistingPoints();
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mMapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        } catch (Exception ex) {
            Log.e("flightplany", "error", ex);
        }
    }

    private boolean checkGPSPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Only call this method when you have GPS permissions
     * starts the location updates with a callback and a locationrequest
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case OpenDroneUtils.RQ_GPS: {
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initWithCurrentPosition();
                } else {
                    setCenter(OpenDroneUtils.DEFAULT_LAT, OpenDroneUtils.DEFAULT_LNG);
                }
                return;
            }
            default: {
                Toast.makeText(getContext(), "Didn't grant all Permissions", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestPermissionAndSetLocation() {
        if (!checkGPSPermission()) {
            setCenter(OpenDroneUtils.DEFAULT_LAT, OpenDroneUtils.DEFAULT_LNG);
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    OpenDroneUtils.RQ_GPS);
        } else {
            initWithCurrentPosition();
        }
    }


    private void initWithCurrentPosition() {
        initLocationManager();
        createLocationRequest();
        initLocationCallback();
        mRequestingLocationUpdates = true;
        setLastKnownLocation();
    }

    /**
     * Only call this method when you have GPS permissions
     * Sets the last known position.
     */
    @SuppressLint("MissingPermission")
    private void setLastKnownLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        Log.i(TAG, location.getLatitude() + "/ " + location.getLongitude());
                        setCoordinates(location);
                        hasAlreadyCentered = true;
                    } else {
                        setCenter(OpenDroneUtils.DEFAULT_LAT, OpenDroneUtils.DEFAULT_LNG);
                        Toast.makeText(getActivity(), getResources().getString(R.string.exception_no_location), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), getResources().getString(R.string.exception_sorry), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initMapView() {
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        line = new Polyline(mMapView);

        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);

        //Manually entered a point -> jump to that location
        if (points.size() > 0) {
            List<GeoPoint> pointList = new LinkedList<>(points.values());
            GeoPoint p = pointList.get(0);
            setCenter(p.getLatitude(), p.getLongitude());
        } else {
            requestPermissionAndSetLocation();
        }
        addEventListener();
    }

    private void setCenter(double lat, double lng) {
        IMapController mapController = mMapView.getController();
        mapController.setZoom(17);
        GeoPoint startPoint = new GeoPoint(lat, lng);
        mapController.setCenter(startPoint);
    }

    private void setCoordinates(Location l) {
        this.location = l;
        setCenter(location.getLatitude(), location.getLongitude());
    }

    @SuppressLint("RestrictedApi")
    private void showFAB() {
        saveFAB.setVisibility(View.VISIBLE);
    }

    private void addMarker(GeoPoint p, double position){
        Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(p);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setEnableTextLabelsWhenNoImage(true);

        Object index = points.size()+1;

        if( points.size()+1 <= 1){
            index = getString(R.string.flight_plan_start_txt).toUpperCase();
        }

        if ((position != Math.floor(position))) {
            if(Math.floor(position)== 0){
                index = getString(R.string.flight_plan_start_txt)+"&"+getString(R.string.flight_plan_end_txt);
            }else{
                index = index.toString()+"&"+getString(R.string.flight_plan_end_txt);
            }
            Log.i(TAG, "Cant add marker");
            canAddMarker = false;
        }

        startMarker.setIcon(getIconDrawable(index));
        addListenersToMarker(startMarker);
        mMapView.getOverlays().add(startMarker);
        markers.add(startMarker);
        addToLine(position, p);
        drawLine();
        showFAB();
    }

    private void addMarker(GeoPoint p) {
        addMarker(p, points.size()+0.0d);
        /*Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(p);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setEnableTextLabelsWhenNoImage(true);

        Object index = points.size()+1;

        if( points.size()+1 <= 1){
            index = getString(R.string.flight_plan_start_txt).toUpperCase();
        }

        startMarker.setIcon(getIconDrawable(index));

        addListenersToMarker(startMarker);
        mMapView.getOverlays().add(startMarker);
        markers.add(startMarker);
        addToLine(points.size(), startMarker.getPosition());
        drawLine();
        showFAB();*/
    }

    private void drawLine() {
        List<GeoPoint> pointList = new LinkedList<GeoPoint>((points.values()));
        line.setPoints(new ArrayList<>(pointList));
        //Do nothing on click
        line.setOnClickListener((polyline, mapView, eventPos) -> false);
        mMapView.getOverlayManager().remove(line);
        mMapView.getOverlayManager().add(0,line);
        mMapView.invalidate();
    }

    private void addToLine(double index, GeoPoint p) {
        Log.i(TAG, "Adding: "+index);
        points.put(index + 0d, p);
    }

    private void addEventListener() {
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (canAddMarker) {
                    addMarker(p);
                }

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };


        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getContext(), mReceive);
        mMapView.getOverlays().add(OverlayEvents);
    }

    private void initLocationManager() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    private void initLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location bestLocation = null;
                for (Location location : locationResult.getLocations()) {
                    if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                        bestLocation = location;
                    }
                }
                if (!hasAlreadyCentered) {
                    initWithCurrentPosition();
                }

            }
        };
    }

    private void loadConfig() {
        Context ctx = getActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
    }

    private void addListeners() {
        saveFAB.setOnClickListener(view -> savePointsAndAddInfo());

    }

    private void savePointsAndAddInfo() {
        FlightPlanSaveFragment fp = new FlightPlanSaveFragment(points);
        fp.setFlightPlaner(this);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout_FragmentContainer, fp);
        ft.commit();
    }

    private void initSP() {
        sp = getActivity().getSharedPreferences("at.opendrone.opendrone", Context.MODE_PRIVATE);
    }

    private void findViews(View view) {
        saveFAB = view.findViewById(R.id.fpFAB);
        removeFAB = view.findViewById(R.id.deleteFab);
        mMapView = view.findViewById(R.id.map);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadConfig();
        View view = inflater.inflate(R.layout.fragment_flightplaner, container, false);
        findViews(view);
        initSP();
        addListeners();
        initMapView();
        showFAB();
        return view;
    }

    public void setExistingPoints(LinkedHashMap<Double, GeoPoint> existingPoints) {
        this.existingPoints = new LinkedHashMap<>(existingPoints);
    }

    private double getKeyAtIndex(int index) {
        List<Double> keys = new ArrayList<Double>(points.keySet());
        Log.i(TAG, ""+keys.size());
        return keys.get(index);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addListenersToMarker(Marker marker) {
        marker.setDraggable(true);
        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
                moveMarker(marker);
                drawLine();
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (lastEvent != null && isViewInBounds(removeFAB, (int) lastEvent.getX(), (int) lastEvent.getY() + (int) (marker.getIcon().getIntrinsicHeight() * 2))) {
                    removeMarker(marker);
                } else {
                    moveMarker(marker);
                }
                removeFAB.setVisibility(View.GONE);
                saveFAB.setVisibility(View.VISIBLE);
                drawLine();
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onMarkerDragStart(Marker marker) {
                LinkedList<GeoPoint> pointList = new LinkedList<>(points.values());
                draggedPosition = pointList.lastIndexOf(marker.getPosition());
                removeFAB.setVisibility(View.VISIBLE);
                saveFAB.setVisibility(View.GONE);

            }
        });
        marker.setOnMarkerClickListener((marker1, mapView) -> {
            if(!canAddMarker){
                return true;
            }
            canAddMarker = false;
            LinkedList<GeoPoint> pointList = new LinkedList<>(points.values());
            int index = pointList.lastIndexOf(marker1.getPosition());
            double key = getKeyAtIndex(index);

            Object text = (index+1)+"&"+getString(R.string.flight_plan_end_txt).toUpperCase();
            if(index == 0){
                text = getString(R.string.flight_plan_start_txt)+"&"+getString(R.string.flight_plan_end_txt).toUpperCase();
            }
            marker1.setIcon(getIconDrawable(text));

            addToLine(key + 0.1d, marker1.getPosition());
            markers.add(marker1);
            drawLine();
            return true;
        });

        mMapView.setOnTouchListener((view, motionEvent) -> {
            lastEvent = motionEvent;
            return false;
        });
    }

    private void moveMarker(Marker marker) {
        if (!canAddMarker) {
            int lastIndex = points.size() - 1;
            double originalPosition = Math.floor(getKeyAtIndex(lastIndex));

            if (draggedPosition == originalPosition || draggedPosition == lastIndex) {
                double lastKey = getKeyAtIndex(lastIndex);
                points.put(originalPosition, marker.getPosition());
                points.put(lastKey, marker.getPosition());
                Log.i(TAG, ""+lastIndex+" / "+originalPosition+" / "+draggedPosition + " / "+lastKey);
            } else {
                points.put(draggedPosition + 0d, marker.getPosition());
            }
        } else {
            points.put(draggedPosition + 0d, marker.getPosition());
        }
    }

    private void removeMarker(Marker marker) {
        int lastIndex = points.size() - 1;
        Double originalPosition = Math.floor(getKeyAtIndex(lastIndex));
        if(!canAddMarker){
            if(draggedPosition == originalPosition || draggedPosition == lastIndex){
                double lastKey = getKeyAtIndex(lastIndex);
                removeFromMarkers(lastIndex);
                removeFromMarkers(originalPosition.intValue());
                removeFromPoints(lastKey);
                removeFromPoints(originalPosition);
                updateKeys(originalPosition);
                canAddMarker = true;
            }else{
                removeFromMarkers(draggedPosition);
                removeFromPoints(draggedPosition+0d);
                updateKeys(draggedPosition+0d);
            }
        }else{
            removeFromMarkers(draggedPosition);
            removeFromPoints(draggedPosition+0d);
            updateKeys(draggedPosition+0d);
        }
        updateIcons();
        drawLine();
    }

    private void updateIcons(){
        HashMap<GeoPoint, String> alreadyUpdated = new HashMap();
        for(int i = 0; i<markers.size(); i++){
            Marker m = markers.get(i);
            Object text = i+1;

            if(alreadyUpdated.containsKey(m.getPosition())){
                text = alreadyUpdated.get(m.getPosition())+"&"+getString(R.string.flight_plan_end_txt);
            }

            if(i == 0){
                text = getString(R.string.flight_plan_start_txt).toUpperCase();
            }
            m.setIcon(getIconDrawable(text));
            alreadyUpdated.put(m.getPosition(), text.toString());
        }
    }

    private void updateKeys(double start){
        LinkedHashMap<Double, GeoPoint> newPoints = new LinkedHashMap<>();
        for(Map.Entry<Double, GeoPoint> entry : points.entrySet()){
            double key = entry.getKey();
            if(key > start){
                key = key -1;
            }
            newPoints.put(key, entry.getValue());
        }
        points = newPoints;
    }

    private void removeFromMarkers(int position) {
        Marker tmp = markers.remove(position);
        mMapView.getOverlayManager().remove(tmp);
        mMapView.invalidate();
    }

    private void removeFromPoints(double key){
        points.remove(key);
        mMapView.invalidate();
    }

    private boolean isViewInBounds(View view, int x, int y) {
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(locationAr);
        outRect.offset(locationAr[0], locationAr[1]);
        return outRect.contains(x, y);
    }

    private void clearLists(){
        points.clear();
        markers.clear();
    }

    private void drawExistingPoints() {
        clearLists();
        /*List<GeoPoint> pointList = new LinkedList<>(existingPoints.values());
        for (int i = 0; i < pointList.size(); i++) {
            addMarker(pointList.get(i));
        }*/
        for(Map.Entry<Double, GeoPoint> entry: existingPoints.entrySet()){
            Log.i(TAG, "Existing: "+entry.getKey());
            addMarker(entry.getValue(), entry.getKey());
        }
    }

    private Drawable getIconDrawable(Object number){
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .width(100)
                .height(100)
                .endConfig()
                .buildRound(number.toString(), getResources().getColor(R.color.primaryColor, getActivity().getTheme()));
        return drawable;
    }
}
