package com.aokp.romcontrol.models;

import android.annotation.ManagedSetting;
import android.app.Fragment;
import com.google.gson.annotations.SerializedName;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jbird on 12/29/13.
 */
public class AokpSetting {
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String TITLE = "title";
    private static final String CATEGORY = "category";
    private static final String REQUIRES_REBOOT = "requires_reboot";

    @SerializedName(NAME)
    private String mName;

    @SerializedName(VALUE)
    private String mValue;

    @SerializedName(TITLE)
    private int mTitle;

    @SerializedName(CATEGORY)
    private int mCategory;

    @SerializedName(REQUIRES_REBOOT)
    private boolean mRequiresReboot;

    public AokpSetting(String name, String value,
                       ManagedSetting managedSetting) {
        mName = name;
        mValue = value;
        mTitle = managedSetting.title();
        mCategory = managedSetting.category();
        mRequiresReboot = managedSetting.requiresReboot();
    }

    public AokpSetting(JSONObject jsonObject) {
        try {
            mName = jsonObject.getString(NAME);
            mValue = jsonObject.getString(VALUE);
            mTitle = jsonObject.getInt(TITLE);
            mCategory = jsonObject.getInt(CATEGORY);
            mRequiresReboot = jsonObject.getBoolean(REQUIRES_REBOOT);
        } catch (JSONException e) {
            try {
                throw new Fragment.InstantiationException("Failed to parse " +
                        "AokpSetting from " + jsonObject.toString(4), e);
            } catch (JSONException e1) {
                // all the things failed
                // TODO?
            }
        }
    }

    public String getName() {
        return mName;
    }

    public String getValue() {
        return mValue;
    }

    public int getTitle() {
        return mTitle;
    }

    public int getCategory() {
        return mCategory;
    }

    public boolean isRequiresReboot() {
        return mRequiresReboot;
    }
}
