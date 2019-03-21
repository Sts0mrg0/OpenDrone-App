/*
 * Last modified: 06.09.18 16:46
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import at.opendrone.opendrone.utils.OpenDroneUtils;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "Homy";
    private ImageView twitterLogo;
    private ImageView githubLogo;
    private ImageView instagramLogo;
    private ImageView tutorialImg;
    private ConstraintLayout upcomingEventsView;
    private ConstraintLayout eventContainer;
    private View view;

    private SharedPreferences sp;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        findViews(view);
        addClickListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startTutorial();
    }

    private void findViews(View view) {
        sp = getActivity().getSharedPreferences("at.opendrone.opendrone", Context.MODE_PRIVATE);
        twitterLogo = view.findViewById(R.id.twitterImgView);
        githubLogo = view.findViewById(R.id.githubImgView);
        instagramLogo = view.findViewById(R.id.instagramImgView);

        eventContainer = view.findViewById(R.id.EventContainer);
        upcomingEventsView = view.findViewById(R.id.upcomingEventsLayout);
        tutorialImg = view.findViewById(R.id.startTutorialImgView);
    }

    private void addClickListeners() {
        twitterLogo.setOnClickListener((view) -> openURL("https://twitter.com/OpenDroneAT"));
        githubLogo.setOnClickListener((view) -> openURL("https://github.com/OpenDroneAT"));
        instagramLogo.setOnClickListener((view) -> openURL("https://instagram.com/OpenDroneAT"));

        eventContainer.setOnClickListener((view) -> openURL("https://makerfairevienna.com/"));
    }

    private void openURL(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void startTutorial() {
        if (!sp.getBoolean(OpenDroneUtils.SP_TUTORIAL, false)) {
            tutorialImg.setVisibility(View.VISIBLE);
            showTargetPrompt(tutorialImg.getId());
            ((MainActivity) getActivity()).canOpenDrawer = false;
        } else {
            ((MainActivity) getActivity()).canOpenDrawer = true;
            tutorialImg.setVisibility(View.GONE);
        }
    }

    private void showTargetPrompt(int targetID) {
        Log.i(TAG, "target");
        new MaterialTapTargetPrompt.Builder(this)
                .setTarget(targetID)
                .setBackgroundColour(getActivity().getColor(R.color.tutorial_prompt_bg))
                .setPrimaryText("Welcome, awesome earthling")
                .setSecondaryText("Thanks for downloading the app. To get a guided and superfast tour through the app, press the circle above. ")
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                        ((MainActivity) getActivity()).initDronesFragment();
                    } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                        tutorialImg.setVisibility(View.GONE);
                        sp.edit().putBoolean(OpenDroneUtils.SP_TUTORIAL, true).apply();
                        ((MainActivity) getActivity()).canOpenDrawer = true;
                    }
                })
                .show();
    }

}
