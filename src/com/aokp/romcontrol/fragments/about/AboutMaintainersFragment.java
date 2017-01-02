package com.aokp.romcontrol.fragments.about;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;

import java.util.Random;

public class AboutMaintainersFragment extends Fragment {

    public AboutMaintainersFragment() {
        // empty fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about_aokp_maintainers, container, false);

        Random rng = new Random();

        ViewGroup maintainersGroup = (ViewGroup) root.findViewById(R.id.maintainers);

        // remove all developers from the view randomize them, add em back
        int N = maintainersGroup.getChildCount();
        while (N > 0) {
            View removed = maintainersGroup.getChildAt(rng.nextInt(N));
            maintainersGroup.removeView(removed);
            maintainersGroup.addView(removed);
            N -= 1;
        }
        return root;
    }

}
