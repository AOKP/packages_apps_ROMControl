
package com.aokp.romcontrol;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

public class NavBarWidget {
    
    private static final String TAG = "Widget";

    ComponentName mProvider;
    ImageView mView = null;
    Context mContext;
    AppWidgetProviderInfo mAppWidgetInfo;
    int mAppWidgetId;
    boolean isAddWidget = false;

    public NavBarWidget(Context c) {
        // extra +1 widget
        mContext = c;
        isAddWidget = true;
    }

    public NavBarWidget(Context c, AppWidgetProviderInfo i, int appWidgetId) {
        mContext = c;
        mAppWidgetInfo = i;
        mAppWidgetId = appWidgetId;
        Log.d(TAG,"Info:" + i +" ID:" + appWidgetId);
    }
    
    public boolean isAddWidget() {
        return isAddWidget;
    }

    public ImageView getView() {
        if (mView == null) {
            mView = new ImageView(mContext);
            mView.setImageDrawable(getWidgetPreviewDrawable());
        }
        return mView;
    }

    public int getWidgetId() {
        if (isAddWidget)
            return -1;
        return mAppWidgetId;
    }

    public int getWidgetResId() {
        if (isAddWidget)
            return 0;
        return mAppWidgetInfo.previewImage;
    }

    public int getHeight() {
        if (isAddWidget)
            return mContext.getResources().getDrawable(R.drawable.widget_na).getMinimumHeight();
        return mAppWidgetInfo.minHeight;
    }

    public Drawable getWidgetPreviewDrawable() {
        if (isAddWidget)
            return mContext.getResources().getDrawable(R.drawable.widget_add);

        Drawable d = mContext.getPackageManager().getDrawable(
                mAppWidgetInfo.provider.getPackageName(),
                getWidgetResId(), null);

        if (d == null)
            return mContext.getResources().getDrawable(R.drawable.widget_na);

        return d;
    }

}
