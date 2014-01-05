package com.aokp.romcontrol.settings;

import android.content.Context;
import android.widget.TextView;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.models.AokpSetting;

/**
 * Created by jbird on 1/1/14.
 */
public class BackupSetting extends CheckboxSetting {
    private final String mCategory;
    private final AokpSetting mAokpSetting;
    private final TextView mCategoryTextView;

    public BackupSetting(Context context, AokpSetting setting) {
        super(context);
        mAokpSetting = setting;
        mCategory = getContext().getString(setting.getCategory());
        mCategoryTextView = (TextView) findViewById(R.id.category);
        setCheckedText(mAokpSetting.getTitle());
        setUncheckedText(mAokpSetting.getTitle());
        setCategory(mCategory);
    }

    private void setCategory(String category) {
        mCategoryTextView.setText(category);;
    }

    protected int getLayout() {
        return R.layout.backup_checkboxsetting;
    }

    public boolean requiresReboot() {
        return mAokpSetting.isRequiresReboot();
    }

    public String getSettingName() {
        return mAokpSetting.getName();
    }
}
