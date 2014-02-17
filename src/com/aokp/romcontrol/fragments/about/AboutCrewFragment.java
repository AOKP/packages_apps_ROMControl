package com.aokp.romcontrol.fragments.about;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.DeveloperPreference;

import java.util.ArrayList;
import java.util.Random;

public class AboutCrewFragment extends Fragment {

    public AboutCrewFragment() {
        // empty fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about_aokp_crew, container, false);

        Random rng = new Random();

        ViewGroup crewGroup = (ViewGroup) root.findViewById(R.id.crew);
        ArrayList<DeveloperPreference> prefs = new ArrayList<DeveloperPreference>();

        // remove all developers from the view randomize them, add em back
        while(crewGroup.getChildCount() != 0) {
            View removed = crewGroup.getChildAt(rng.nextInt(crewGroup.getChildCount()));
            if (removed instanceof DeveloperPreference) {
                prefs.add((DeveloperPreference) removed);
            }
            crewGroup.removeView(removed);
        }
        for (int i = 0; i < prefs.size(); i++) {
            crewGroup.addView(prefs.get(i));
        }

        return root;
    }

}
