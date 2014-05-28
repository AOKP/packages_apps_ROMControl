/*
 * Copyright (C) 2013 The Android Open Kang Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aokp.romcontrol.fragments.toggles;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.internal.telephony.PhoneConstants;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.SingleChoiceSetting;
import com.aokp.romcontrol.settings.MultiChoiceSetting;
import java.util.ArrayList;

/**
 * Created by roman on 12/30/13.
 */
public class ToggleSettingsFragment extends Fragment implements OnSettingChangedListener, OnClickListener {

    private final int PICK_CONTACT = 200;

    BaseSetting mTogglesFast, mSwipeToSwitch, mFavContact;
    SingleChoiceSetting mTogglesPerRow, mToggleStyle, mToggleSide;
    MultiChoiceSetting mNetworkModes;

    protected Context mContext;
    private TelephonyManager mTelephonyManager;

    public ToggleSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_toggle_setup, container, false);

        mContext = getActivity();
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        mTogglesFast = (BaseSetting) v.findViewById(R.id.toggles_fast_toggle);
        mSwipeToSwitch = (BaseSetting) v.findViewById(R.id.toggles_swipe_to_switch);
        mTogglesPerRow = (SingleChoiceSetting) v.findViewById(R.id.toggles_per_row);
        mToggleStyle = (SingleChoiceSetting) v.findViewById(R.id.toggles_style);
        mToggleSide = (SingleChoiceSetting) v.findViewById(R.id.toggles_fast_side);
        mFavContact = (BaseSetting) v.findViewById(R.id.toggles_fav_contact);

        mNetworkModes = (MultiChoiceSetting) v.findViewById(R.id.network_modes_toggle);
        setModes();

        mToggleStyle.setOnSettingChangedListener(this);
        mFavContact.setOnClickListener(this);

        return v;
    }


    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
        if (table.equals("aokp") && key.equals(mToggleStyle.getKey())) {
            if (value == null || value.isEmpty()) {
                // defualt state
                mTogglesPerRow.setVisibility(View.VISIBLE);
                mTogglesFast.setVisibility(View.VISIBLE);
                mToggleSide.setVisibility(View.VISIBLE);
                mSwipeToSwitch.setVisibility(View.VISIBLE);
            } else {
                mTogglesPerRow.setVisibility(value.equals("0" /* 0 is the tile */)
                        ? View.VISIBLE : View.GONE);
                mTogglesFast.setVisibility(value.equals("0" /* 0 is the tile */)
                        ? View.VISIBLE : View.GONE);
                mToggleSide.setVisibility(value.equals("0" /* 0 is the tile */)
                        ? View.VISIBLE : View.GONE);
                mSwipeToSwitch.setVisibility(value.equals("0" /* 0 is the tile */)
                        ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggles_fav_contact:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
                break;
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_CONTACT) {
                Uri contactData = data.getData();
                String[] projection = new String[]{
                        ContactsContract.Contacts.LOOKUP_KEY
                };
                String selection = ContactsContract.Contacts.DISPLAY_NAME + " IS NOT NULL";
                CursorLoader cursorLoader = new CursorLoader(getActivity().getBaseContext(),
                        contactData, projection, selection, null, null);
                Cursor cursor = cursorLoader.loadInBackground();
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            String lookup_key = cursor.getString(cursor
                                    .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                            Settings.AOKP.putString(getActivity().getContentResolver(),
                                    Settings.AOKP.QUICK_TOGGLE_FAV_CONTACT, lookup_key);
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setModes() {
        if (isDeviceGSM() || isDeviceCDMA()) {
            ArrayList<String> defaultValues = new ArrayList<String>();
            ArrayList<String> defaultEntries = new ArrayList<String>();
            String default_modes = "";
            if (isDeviceGSM()) {
                if (deviceSupportsLTE()) {
                    defaultValues.add("9");
                    defaultValues.add("12");
                    defaultValues.add("11");
                    defaultEntries.add(mContext.getResources().getString(R.string.network_mode_4g));
                    defaultEntries.add(mContext.getResources().getString(R.string.network_mode_4g_3g));
                    defaultEntries.add(mContext.getResources().getString(R.string.network_mode_4g_only));
                    default_modes = "9|0|1";
                } else {
                    default_modes = "0|1";
                }
                defaultValues.add("0");
                defaultValues.add("3");
                defaultValues.add("2");
                defaultValues.add("1");
                defaultEntries.add(mContext.getResources().getString(R.string.network_mode_3g));
                defaultEntries.add(mContext.getResources().getString(R.string.network_mode_3g_auto));
                defaultEntries.add(mContext.getResources().getString(R.string.network_mode_3g_only));
                defaultEntries.add(mContext.getResources().getString(R.string.network_mode_2g));
            } else if (isDeviceCDMA()) {
                if (deviceSupportsLTE()) {
                    defaultValues.add("8");
                    defaultEntries.add(mContext.getResources().getString(R.string.network_mode_4g));
                    default_modes = "8|4|5";
                } else {
                    default_modes = "4|5";
                }
                defaultValues.add("4");
                defaultValues.add("5");
                defaultValues.add("6");
                defaultEntries.add(mContext.getResources().getString(R.string.network_mode_3g));
                defaultEntries.add(mContext.getResources().getString(R.string.network_mode_2g_cdma));
                defaultEntries.add(mContext.getResources().getString(R.string.network_mode_2g_evdo));
            }
            mNetworkModes.setDefaultValue(default_modes);
            mNetworkModes.setEntryValues(defaultValues.toArray(new String[defaultValues.size()]));
            mNetworkModes.setEntries(defaultEntries.toArray(new String[defaultEntries.size()]));
        } else {
            mNetworkModes.setVisibility(View.GONE);
        }
    }

    private boolean isDeviceCDMA() {
        return (mTelephonyManager.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA);
    }

    private boolean isDeviceGSM() {
        return (mTelephonyManager.getPhoneType() == PhoneConstants.PHONE_TYPE_GSM);
    }

    private boolean deviceSupportsLTE() {
        return (mTelephonyManager.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE
                    || mTelephonyManager.getLteOnGsmMode() != 0);
    }
}
