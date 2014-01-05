package com.aokp.romcontrol.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.models.AokpSetting;
import com.aokp.romcontrol.service.BackupService;
import com.aokp.romcontrol.settings.BackupSetting;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.aokp.romcontrol.service.BackupService.UpdateFrequency;

/**
 * Created by jbird on 12/29/13.
 */
public final class BackupFragment extends PreferenceFragment {
    public static final String TAG = BackupFragment.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final String LOADING_TAG = "loading_tag";
    private TextView mEmpty;
    private ViewGroup mRoot;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String error = intent.getStringExtra(BackupService.ERROR);
            UpdateFrequency frequency = UpdateFrequency.valueOf(intent.getStringExtra(
                    BackupService.UPDATE_FREQUENCY));
            if (error != null) {
                Log.e(TAG, "Getting Settings produced an error: " + error);
                // TODO Do something with an error
            } else {
                String response = intent.getStringExtra(BackupService.BACKUP_RESPONSE);
                switch (frequency) {
                    default:
                    case OnCompletion:
                        List<AokpSetting> settings = parseResponse(response);
                        updateUi(settings);
                        break;
                    case Immediate:
                        try {
                            mRoot.removeView(mEmpty);
                            AokpSetting setting = parseSetting(new JSONObject(
                                    response));
                            mRoot.addView(generateCheckPreference(setting));
                        } catch (JSONException e) {
                            Log.e(TAG,
                                    "Parsing Immediate response to json failed!");
                        }
                        break;
                }
            }
        }
    };

    private List<AokpSetting> parseResponse(String response) {
        List<AokpSetting> settings = new ArrayList<AokpSetting>(0);
        try {
            JSONArray jsonArray = new JSONArray(response);
            int length = jsonArray.length();
            for (int i = 0; i > length; i++) {
                settings.add(parseSetting(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to generate settings list");
        }
        return settings;
    }

    private AokpSetting parseSetting(JSONObject jsonObject) {
        return new AokpSetting(jsonObject);
    }

    public BackupFragment() {
        // do nothing
    }

    @Override
    public void onStart() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mReceiver,
                new IntentFilter(BackupService.NOTIFICATION));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                mReceiver);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_backup, null);
        mEmpty = new TextView(getActivity());
        mEmpty.setTag(LOADING_TAG);
        mEmpty.setText(R.string.loading_please_wait);
        mRoot.addView(mEmpty);
        launchService();
        return mRoot;
    }

    private void launchService() {
        Intent intent = new Intent(BackupService.ACTION_LOAD_SETTINGS);
        intent.putExtra(BackupService.UPDATE_FREQUENCY,
                UpdateFrequency.OnCompletion);
        //noinspection ConstantConditions
        getActivity().startService(intent);
    }

    private void updateUi(List<AokpSetting> response) {
        sortList(response);
        for (AokpSetting setting : response) {
            mRoot.addView(generateCheckPreference(setting));
        }
    }

    private void sortList(List<AokpSetting> response) {
        Collections.sort(response, new Comparator<AokpSetting>() {
            long startTime = System.currentTimeMillis();
            @Override public int compare (
                    AokpSetting first, AokpSetting second) {
                String firstCat = getString(first.getCategory()).toLowerCase();
                String secondCat = getString(second.getCategory()).toLowerCase();
                // if the categories are the same return secondary sorting
                // method; if they are not the same then place second after
                // the first
                // TODO Sort categories alphabetically
                int sortStatus = firstCat.equalsIgnoreCase(secondCat)
                        ? doSecondarySory(first, second)
                        : 1;
                if (DEBUG) {
                    Log.d(TAG, "Sorting took: "
                            + (System.currentTimeMillis() - startTime) + " ms");
                }
                return sortStatus;
            }
            public int doSecondarySory(AokpSetting first, AokpSetting second) {
                String firstTitle = getString(first.getTitle()).toLowerCase();
                String secondTitle = getString(second.getTitle()).toLowerCase();
                char[] charArray = firstTitle.toCharArray();
                for (int i = 0, charArrayLength = charArray.length;
                     i < charArrayLength;
                     i++) {
                    char letter = charArray[i];
                    // if chars are the same move on to the next
                    // Optimization so we don't do unneeded comparisons
                    if (letter == secondTitle.charAt(i)) {
                        continue;
                    // if letter of first setting is before the same letter
                    // in second return as before
                    } else if (letter > secondTitle.charAt(i)) {
                        return -1;
                    // if letter of first setting is before the same letter
                    // in second return as after
                    } else if (letter < secondTitle.charAt(i)) {
                        return 1;
                    }
                }
                // all the letters were the same return as equals
                return 0;
            }
        });
    }

    private View generateCheckPreference(AokpSetting setting) {
        return new BackupSetting(getActivity(), setting);
    }
}