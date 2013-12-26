package com.aokp.romcontrol.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.SingleChoiceSetting;
import com.aokp.romcontrol.util.EasyPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 12/23/13.
 */
public class ToggleSetupFragment extends Fragment implements OnClickListener, OnSettingChangedListener {

    private static final String TAG = ToggleSetupFragment.class.getSimpleName();

    static Bundle sToggles;
    BroadcastReceiver mReceiver;
    ArrayList<String> mToggles;

    BaseSetting mEnabledToggles, mArrangeToggles;
    SingleChoiceSetting mTogglesPerRow, mToggleStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("toggle_bundle")) {
                    onTogglesUpdate(intent.getBundleExtra("toggle_bundle"));
                }
            }
        };
        getActivity().registerReceiver(mReceiver,
                new IntentFilter(
                        "com.android.systemui.statusbar.toggles.ACTION_BROADCAST_TOGGLES"));
        requestAvailableToggles();
    }

    static ArrayList<EasyPair<String, String>> buildToggleMap(Bundle toggleInfo) {
        ArrayList<String> _toggleIdents = toggleInfo.getStringArrayList("toggles");
        ArrayList<EasyPair<String, String>> _toggles = new ArrayList<EasyPair<String, String>>();
        for (String _ident : _toggleIdents) {
            _toggles.add(new EasyPair<String, String>(_ident, toggleInfo.getString(_ident)));
        }
        return _toggles;
    }

    @Override
    public void onResume() {
        super.onResume();
        requestAvailableToggles();
    }


    private void onTogglesUpdate(Bundle toggleInfo) {
        mToggles = toggleInfo.getStringArrayList("toggles");
        sToggles = toggleInfo;
    }


    private void requestAvailableToggles() {
        Intent request =
                new Intent("com.android.systemui.statusbar.toggles.ACTION_REQUEST_TOGGLES");
        getActivity().sendBroadcast(request);
    }

    static synchronized void addToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getEnabledToggles(context);
        if (enabledToggles.contains(key)) {
            enabledToggles.remove(key);
        }
        enabledToggles.add(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    static synchronized ArrayList<String> getEnabledToggles(Context context) {
        try {
            ArrayList<String> userEnabledToggles = new ArrayList<String>();
            String userToggles = Settings.AOKP.getString(context.getContentResolver(),
                    Settings.AOKP.QUICK_TOGGLES);

            String[] splitter = userToggles.split("\\|");
            for (String toggle : splitter) {
                userEnabledToggles.add(toggle);
            }
//            Collections.sort(userEnabledToggles);
            return userEnabledToggles;
        } catch (Exception e) {
            if (sToggles != null && sToggles.containsKey("default_toggles")) {
                return sToggles.getStringArrayList("default_toggles");
            }
        }
        return new ArrayList<String>();
    }

    static synchronized void setTogglesFromStringArray(Context c, List<String> enabledToggles) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < enabledToggles.size(); i++) {
            final String _toggle = enabledToggles.get(i);
            if (_toggle.isEmpty()) {
                continue;
            }
            b.append(_toggle);
            b.append("|");
        }
        if (b.length() > 0) {
            if (b.charAt(b.length() - 1) == '!') {
                b.deleteCharAt(b.length() - 1);
            }
        }
        Log.d(TAG, "saving toggles:" + b.toString());
        Settings.AOKP.putString(c.getContentResolver(), Settings.AOKP.QUICK_TOGGLES,
                b.toString());
    }

    static synchronized void removeToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getEnabledToggles(context);
        enabledToggles.remove(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    static String lookupToggle(Context c, String ident) {
        if (sToggles != null) {
            return sToggles.getString(ident.toUpperCase());
        }
        return ident;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_toggle_setup, container, false);

        mEnabledToggles = (BaseSetting) v.findViewById(R.id.enabled_toggles);
        mArrangeToggles = (BaseSetting) v.findViewById(R.id.arrange_toggles);
        mTogglesPerRow = (SingleChoiceSetting) v.findViewById(R.id.toggles_per_row);
        mToggleStyle = (SingleChoiceSetting) v.findViewById(R.id.toggles_style);

        mEnabledToggles.setOnClickListener(this);
        mArrangeToggles.setOnClickListener(this);
        mToggleStyle.setOnSettingChangedListener(this);

        return v;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.enabled_toggles:
                showEnabledTogglesDialog();
                break;

            case R.id.arrange_toggles:
                ArrangeTogglesFragment fragment = ArrangeTogglesFragment.newInstance(sToggles);
//                fragment.show(getFragmentManager(), "arrange");
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment, "arrange")
                        .addToBackStack(null)
                        .commit();
                break;

        }
    }

    private void showEnabledTogglesDialog() {
        if (mToggles == null || mToggles.isEmpty()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final ArrayList<String> userToggles = getEnabledToggles(getActivity());
        final ArrayList<String> availableToggles = new ArrayList<String>();
        for (String t : mToggles) {
            availableToggles.add(t);
        }

        // final String[] finalArray = getResources().getStringArray(
        // R.array.available_toggles_entries);
        final String[] toggleValues = new String[availableToggles.size()];
        for (int i = 0; i < availableToggles.size(); i++) {
            toggleValues[i] = ToggleSetupFragment.lookupToggle(getActivity(), availableToggles.get(i));
        }

        final boolean checkedToggles[] = new boolean[availableToggles.size()];

        boolean anyChecked = false;
        for (int i = 0; i < checkedToggles.length; i++) {
            String selectedToggle = availableToggles.get(i);
            if (userToggles.contains(selectedToggle)) {
                Log.d(TAG, "found toggle: " + selectedToggle);
                checkedToggles[i] = true;
                anyChecked = true;
            }
        }
        if (!anyChecked) {
            // no toggles are checked, wipe the setting to be sure
            Settings.AOKP.putString(getActivity().getContentResolver(), Settings.AOKP.QUICK_TOGGLES, "");
        }

        builder.setTitle(R.string.toggle_dialog_add_toggles);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.toggles_display_close,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setMultiChoiceItems(toggleValues, checkedToggles,
                new OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        String toggleKey = availableToggles.get(which);

                        if (isChecked)
                            ToggleSetupFragment.addToggle(getActivity(), toggleKey);
                        else
                            ToggleSetupFragment.removeToggle(getActivity(), toggleKey);

                        if ("FAVCONTACT".equals(toggleKey)) {
//                            mFavContact.setEnabled(isChecked);
                        }
                    }
                });

        AlertDialog d = builder.create();

        d.show();

    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
        if(table.equals("aokp") && key.equals(mToggleStyle.getKey())) {
            if(value == null || value.isEmpty()) {
                // defualt state
                mTogglesPerRow.setVisibility(View.VISIBLE);
            } else {
                mTogglesPerRow.setVisibility(value.equals("0" /* 0 is the tile */)
                                        ? View.VISIBLE : View.GONE);
            }
        }
    }
}
