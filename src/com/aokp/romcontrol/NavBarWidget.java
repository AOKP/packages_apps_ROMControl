
package com.aokp.romcontrol;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class NavBarWidget {

    ComponentName mProvider;
    ImageView mView;
    Context mContext;
    AppWidgetProviderInfo mAppWidgetInfo;

    public NavBarWidget(Context c, AppWidgetProviderInfo i) {
        mContext = c;
        mAppWidgetInfo = i;
    }

    public ImageView getView() {
        if (mView == null) {
            mView = new ImageView(mContext);
            mView.setImageDrawable(getWidgetPreviewDrawable());
        }
        return mView;
    }

    public int getWidgetResId() {
        return mAppWidgetInfo.previewImage;
    }
    
    public int getHeight() {
        return mAppWidgetInfo.minHeight;
    }

    public Drawable getWidgetPreviewDrawable() {
        return mContext.getPackageManager().getDrawable(
                mAppWidgetInfo.provider.getPackageName(),
                getWidgetResId(), null);
    }

}
