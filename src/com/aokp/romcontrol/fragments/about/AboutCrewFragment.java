package com.aokp.romcontrol.fragments.about;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;

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

        // remove all developers from the view randomize them, add em back
        int N = crewGroup.getChildCount();
        while (N > 0) {
            View removed = crewGroup.getChildAt(rng.nextInt(N));
            crewGroup.removeView(removed);
            crewGroup.addView(removed);
            N -= 1;
        }
        return root;
    }

}
