/*
 * Last modified: 30.09.18 15:07
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
//MS App Center
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import at.opendrone.opendrone.drone.DroneCardListRecyclerFragment;
import at.opendrone.opendrone.flightplan.FlightPlanListFragment;
import at.opendrone.opendrone.fly.FlyManualFlight;
import at.opendrone.opendrone.network.ConnectDisconnectTasks;
import at.opendrone.opendrone.network.OpenDroneFrame;
import at.opendrone.opendrone.network.TCPHandler;
import at.opendrone.opendrone.network.TCPMessageReceiver;
import at.opendrone.opendrone.raspistats.RaspiStat;
import at.opendrone.opendrone.raspistats.RaspiStatParser;
import at.opendrone.opendrone.settings.AdjustPIDFragment;
import at.opendrone.opendrone.settings.SettingsFragment;
import at.opendrone.opendrone.utils.OpenDroneUtils;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "Mainy";
    private static boolean INEEDPIDCONTROLS = true;
    //public static TCPSend client;
    public static FragmentManager fm;
    private ConnectDisconnectTasks tasks = ConnectDisconnectTasks.getInstance();
    private SharedPreferences sp;
    boolean isRunning = true;
    public DrawerLayout drawerLayout;
    private boolean isOpened = false;
    private FrameLayout fragmentContainer;
    private int lastFragment;


    public boolean canOpenDrawer = true;

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_Main);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    public void initNavView() {
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);
        navView.getMenu().clear();
        sp = getSharedPreferences("at.opendrone.opendrone", Context.MODE_PRIVATE);
        INEEDPIDCONTROLS =  sp.getBoolean(OpenDroneUtils.SP_SETTINGS_PROMODE,false);
        if(INEEDPIDCONTROLS){
            navView.inflateMenu(R.menu.navview_dev);
        }else{
            navView.inflateMenu(R.menu.navview);
        }

        navView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCenter.start(getApplication(), "3a5cb885-3ad1-4141-a8da-f2901d36fb2f",
                Analytics.class, Crashes.class);
        findViews();

        initToolbar();
        new CheckConnectionTask().execute();
        initNavView();
        fm = getSupportFragmentManager();
        initHomeFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void findViews() {
        fragmentContainer = findViewById(R.id.frameLayout_FragmentContainer);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(!canOpenDrawer){
                    Toast.makeText(this, getString(R.string.manualflight_no_open_drawer), Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (isOpened) {
                    closeDrawer();
                } else {
                    openDrawer();
                }
                return true;
            default:
                Log.i("MainActivity", "OnOptionItemSelected default case");
                return super.onOptionsItemSelected(item);
        }
    }

    public void clearContainer() {
        for (Fragment fragment : fm.getFragments()) {
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
    }

    public void initHomeFragment() {
        lastFragment = OpenDroneUtils.LF_HOME;
        HomeFragment hf = new HomeFragment();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(fragmentContainer.getId(), hf);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
        //updateFragment(hf);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        lastFragment = savedInstanceState.getInt("LastFragment");
        switch (lastFragment) {
            case OpenDroneUtils.LF_HOME:
                initHomeFragment();
                break;
            case OpenDroneUtils.LF_DRONE:
                initDronesFragment();
                break;
            case OpenDroneUtils.LF_FP:
                initFlightplaner();
                break;
            case OpenDroneUtils.LF_FLY:
                initFlyStartFragment();
                break;
            case OpenDroneUtils.LF_SETTINGS:
                initSettingsFragment();
                break;
            case OpenDroneUtils.LF_ADJUST_PID:
                initPIDAdjustFragment();
                break;
            default:
                Log.i("Mainy", "Default onRestore");
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putInt("LastFragment", lastFragment);
        // etc.
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        setSensorOrientation();
        switch (item.getItemId()) {
            case R.id.navItem_Home:
                initHomeFragment();
                closeDrawer();
                return true;
            case R.id.navItem_Drones:
                initDronesFragment();
                closeDrawer();
                return true;
            case R.id.navItem_FlightPlanner:
                initFlightplaner();
                closeDrawer();
                return true;
            case R.id.navItem_Fly:
                initFlyStartFragment();
                closeDrawer();
                return true;
            case R.id.navItem_Libs:
                displayLibraries();
                return true;
            case R.id.navItem_Github:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/OpenDroneAT"));
                startActivity(browserIntent);
                return true;
            case R.id.navItem_PID:
                initPIDAdjustFragment();
                closeDrawer();
                return true;
            case R.id.navItem_Settings:
                initSettingsFragment();
                return true;
            default:
                Log.i("MainActivity", "OnNavigationItem Default case");
                return false;
        }
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
        isOpened = false;
    }

    private void openDrawer() {
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                isOpened = true;
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                isOpened = false;
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
        drawerLayout.openDrawer(GravityCompat.START);
        isOpened = true;
    }

    @Override
    public void onBackPressed() {
        clearContainer();
        initHomeFragment();
    }

    private void setSensorOrientation(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    private void lockOrientation(){
        setRequestedOrientation(getResources().getConfiguration().orientation);
    }

    public void initDronesFragment() {
        lastFragment = OpenDroneUtils.LF_DRONE;
        Fragment defFragment = new DroneCardListRecyclerFragment();
        updateFragment(defFragment);
    }

    private void updateFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(fragmentContainer.getId(), fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    private void initPIDAdjustFragment(){
        lastFragment = OpenDroneUtils.LF_ADJUST_PID;
        AdjustPIDFragment fragment = new AdjustPIDFragment();
        updateFragment(fragment);
    }


    public void initFlyStartFragment() {
        //FlyStart defFragment = new FlyStart();
        lastFragment = OpenDroneUtils.LF_FLY;
        FlyManualFlight defFragment = new FlyManualFlight();
        updateFragment(defFragment);
    }

    public void initFlightplaner() {
        lastFragment = OpenDroneUtils.LF_FP;
        SharedPreferences sp = getApplication().getSharedPreferences("at.opendrone.opendrone", Context.MODE_PRIVATE);
        //sp.edit().remove(OpenDroneUtils.SP_FLIGHTPLAN_HOLDER).apply();
        FlightPlanListFragment defFragment = new FlightPlanListFragment();
        updateFragment(defFragment);
    }

    public void initSettingsFragment() {
        lastFragment = OpenDroneUtils.LF_SETTINGS;
        Fragment defFragment = new SettingsFragment();
        updateFragment(defFragment);
        closeDrawer();
    }

    private void displayLibraries() {
// When the user selects an option to see the licenses:
        startActivity(new Intent(this, OssLicensesMenuActivity.class));
    }

    @SuppressLint("RestrictedApi")
    private void setConnectBtnImg(boolean isConnected) {
        ActionMenuItemView btn = findViewById(R.id.connected);
        runOnUiThread(() -> {
            if (btn != null && isConnected) {
                btn.setIcon(getResources().getDrawable(R.drawable.ic_connected));
            } else if (btn != null && !isConnected) {
                btn.setIcon(getResources().getDrawable(R.drawable.ic_disconnected));
            }
        });


    }

    private class CheckConnectionTask extends AsyncTask<String, Boolean, Void> {

        @Override
        protected void onPreExecute() {

            Thread t = new Thread(){


                @Override
                public void run() {

                    while(isRunning){
                        boolean connected = tasks.ping();
                        publishProgress(connected);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            t.start();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... voids) {

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            Log.i("TAGGY","PostExecute:");

            super.onPostExecute(nothing);
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {

            ActionMenuItemView btn = findViewById(R.id.connected);
            runOnUiThread(new Runnable() {
                @SuppressLint("RestrictedApi")
                @Override
                public void run() {
                    if(values[0] != null){
                        if(values[0] == true){
                            if(btn != null){
                                btn.setIcon(getResources().getDrawable(R.drawable.ic_disconnected));
                            }
                        }
                        if(values[0] == false){
                            if(btn != null){
                                btn.setIcon(getResources().getDrawable(R.drawable.ic_connected));
                            }
                        }

                    }else{
                        if(btn != null){
                            btn.setIcon(getResources().getDrawable(R.drawable.ic_disconnected));
                        }

                    }
                }
            });

            super.onProgressUpdate(values);
        }
    }


}
