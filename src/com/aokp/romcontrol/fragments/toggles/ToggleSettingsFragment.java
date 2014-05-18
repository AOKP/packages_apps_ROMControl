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
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;
import com.aokp.romcontrol.widgets.CategorySeparator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 12/30/13.
 */
public class ToggleSettingsFragment extends Fragment implements OnSettingChangedListener, OnClickListener {

    private final int PICK_CONTACT = 200;

    BaseSetting mTogglesFast, mSwipeToSwitch, mFavContact;
    SingleChoiceSetting mTogglesPerRow, mToggleStyle, mToggleSide;
    CategorySeparator mNetworkModesCat;
    CheckboxSetting mNetworkMode4G, mNetworkMode4G3G, mNetworkMode4Gonly, mNetworkMode4Gcdma, mNetworkMode3G,
                mNetworkMode3Gauto, mNetworkMode3Gonly, mNetworkMode3Gcdma, mNetworkMode2G,
                mNetworkMode2Gcdma, mNetworkMode2Gevdo;
    protected Context mContext;
    private TelephonyManager mTelephonyManager;

    List<Integer> mMobileNetworks = new ArrayList<Integer>();

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

        mNetworkModesCat = (CategorySeparator) v.findViewById(R.id.network_modes_category);
        mNetworkMode4G = (CheckboxSetting) v.findViewById(R.id.network_mode_4g);
        mNetworkMode4G3G = (CheckboxSetting) v.findViewById(R.id.network_mode_4g_3g);
        mNetworkMode4Gonly = (CheckboxSetting) v.findViewById(R.id.network_mode_4g_only);
        mNetworkMode3G = (CheckboxSetting) v.findViewById(R.id.network_mode_3g);
        mNetworkMode3Gauto = (CheckboxSetting) v.findViewById(R.id.network_mode_3g_auto);
        mNetworkMode3Gonly = (CheckboxSetting) v.findViewById(R.id.network_mode_3g_only);
        mNetworkMode2G = (CheckboxSetting) v.findViewById(R.id.network_mode_2g);
        mNetworkMode2Gcdma = (CheckboxSetting) v.findViewById(R.id.network_mode_2g_cdma);
        mNetworkMode2Gevdo = (CheckboxSetting) v.findViewById(R.id.network_mode_2g_evdo);

        mToggleStyle.setOnSettingChangedListener(this);
        mFavContact.setOnClickListener(this);

        mNetworkMode4G.setOnClickListener(this);
        mNetworkMode4G3G.setOnClickListener(this);
        mNetworkMode4Gonly.setOnClickListener(this);
        mNetworkMode3G.setOnClickListener(this);
        mNetworkMode3Gauto.setOnClickListener(this);
        mNetworkMode3Gonly.setOnClickListener(this);
        mNetworkMode2G.setOnClickListener(this);
        mNetworkMode2Gcdma.setOnClickListener(this);
        mNetworkMode2Gevdo.setOnClickListener(this);

        setModes();

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
            case R.id.network_mode_4g:
            case R.id.network_mode_4g_3g:
            case R.id.network_mode_4g_only:
            case R.id.network_mode_3g:
            case R.id.network_mode_3g_auto:
            case R.id.network_mode_3g_only:
            case R.id.network_mode_2g:
            case R.id.network_mode_2g_cdma:
            case R.id.network_mode_2g_evdo:
                createModesList();
                if (mMobileNetworks.size() >= 2) {
                    final String value = TextUtils.join("|", mMobileNetworks);
                    Settings.AOKP.putString(mContext.getContentResolver(),
                            Settings.AOKP.NETWORK_MODES_TOGGLE, value);
                } else {
                    Toast.makeText(mContext, R.string.network_mode_warning, Toast.LENGTH_LONG).show();
                }
                break;
        }
    };

    private void setModes() {
        String default_modes = "";
        if (isDeviceGSM()) {
            if (deviceSupportsLTE()) {
                default_modes = "9|0|1";
            } else {
                default_modes = "0|1";
                mNetworkMode4G.setVisibility(View.GONE);
                mNetworkMode4G3G.setVisibility(View.GONE);
                mNetworkMode4Gonly.setVisibility(View.GONE);
            }
            mNetworkMode2Gcdma.setVisibility(View.GONE);
            mNetworkMode2Gevdo.setVisibility(View.GONE);
        } else if (isDeviceCDMA()) {
            if (deviceSupportsLTE()) {
                default_modes = "8|4|5";
            } else {
                default_modes = "4|5";
                mNetworkMode4G.setVisibility(View.GONE);
            }
            mNetworkMode4G3G.setVisibility(View.GONE);
            mNetworkMode4Gonly.setVisibility(View.GONE);
            mNetworkMode3Gauto.setVisibility(View.GONE);
            mNetworkMode3Gonly.setVisibility(View.GONE);
            mNetworkMode2G.setVisibility(View.GONE);
        } else {
            mNetworkModesCat.setVisibility(View.GONE);
            mNetworkMode4G.setVisibility(View.GONE);
            mNetworkMode4G3G.setVisibility(View.GONE);
            mNetworkMode4Gonly.setVisibility(View.GONE);
            mNetworkMode3G.setVisibility(View.GONE);
            mNetworkMode3Gauto.setVisibility(View.GONE);
            mNetworkMode3Gonly.setVisibility(View.GONE);
            mNetworkMode2G.setVisibility(View.GONE);
            mNetworkMode2Gcdma.setVisibility(View.GONE);
            mNetworkMode2Gevdo.setVisibility(View.GONE);
        }

        if (!default_modes.equals("")) {
            String saved_toggles = Settings.AOKP.getString(mContext.getContentResolver(),
                Settings.AOKP.NETWORK_MODES_TOGGLE);
            String toggles_string = (saved_toggles != null) ? saved_toggles : default_modes;
            setCheckedModes(toggles_string);
        }
    }

    private void setCheckedModes(String toggles) {
        for (String mode : toggles.split("\\|")) {
            switch (Integer.parseInt(mode)) {
                case 9:
                case 8:
                    mNetworkMode4G.setChecked(true);
                    break;
                case 12:
                    mNetworkMode4G3G.setChecked(true);
                    break;
                case 11:
                    mNetworkMode4Gonly.setChecked(true);
                    break;
                case 0:
                case 4:
                    mNetworkMode3G.setChecked(true);
                    break;
                case 3:
                    mNetworkMode3Gauto.setChecked(true);
                    break;
                case 2:
                    mNetworkMode3Gonly.setChecked(true);
                    break;
                case 1:
                    mNetworkMode2G.setChecked(true);
                    break;
                case 5:
                    mNetworkMode2Gcdma.setChecked(true);
                    break;
                case 6:
                    mNetworkMode2Gevdo.setChecked(true);
                    break;
            }
        }
    }

    private void createModesList() {
        mMobileNetworks.clear();
        if (mNetworkMode4G.isChecked()) {
            if (deviceSupportsLTE()) {
                mMobileNetworks.add(isDeviceCDMA() ? 8 : 9);
            }
        }
        if (mNetworkMode4G3G.isChecked()) {
            mMobileNetworks.add(12);
        }
        if (mNetworkMode4Gonly.isChecked()) {
            mMobileNetworks.add(11);
        }
        if (mNetworkMode3G.isChecked()) {
            if (isDeviceCDMA()) {
                mMobileNetworks.add(4);
            } else if (isDeviceGSM()) {
                mMobileNetworks.add(0);
            }
        }
        if (mNetworkMode3Gauto.isChecked()) {
            mMobileNetworks.add(3);
        }
        if (mNetworkMode3Gonly.isChecked()) {
            mMobileNetworks.add(2);
        }
        if (mNetworkMode2G.isChecked()) {
            mMobileNetworks.add(1);
        }
        if (mNetworkMode2Gcdma.isChecked()) {
            mMobileNetworks.add(5);
        }
        if (mNetworkMode2Gevdo.isChecked()) {
            mMobileNetworks.add(6);
        }
    }

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
