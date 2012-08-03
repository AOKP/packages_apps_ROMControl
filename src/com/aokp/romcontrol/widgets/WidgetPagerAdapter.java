
package com.aokp.romcontrol.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aokp.romcontrol.R;

public class WidgetPagerAdapter extends PagerAdapter {
    private static String TAG = "Widget";

    View[] widgetViews = new View[1];
    int[] widgetIds = new int[1];
    View[] hostViews;
    Context mContext;
    AppWidgetManager mAppWidgetManager;

    public WidgetPagerAdapter(Context c, int[] ids) {
    	if (ids != null) {
    	    widgetIds = ids;
    	} else { // got passed null id set .. create a fake one
    	    widgetIds[0] = -1;
    	}
    	Log.d(TAG,"Constructor: WidgetAdapter:"+widgetIds.length);
        mContext = c;
        mAppWidgetManager = AppWidgetManager.getInstance(c);
        hostViews = new View[widgetIds.length];
    }

    @Override
    public int getCount() {
        return widgetViews.length;
    }

    public int getHeight(int pos) {
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetIds[pos]);
        if (appWidgetInfo != null) {
            int height = appWidgetInfo.minHeight;
            setSavedHeight(pos, height);
            return height;
        } else {
            return getSavedHeight(pos);
        }
    }

    public Object getView(int pos){
        if (hostViews != null && pos < hostViews.length){
            return hostViews[pos];
        } else {
            return null;
        }
    }
    public String getLabel(int pos) {
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetIds[pos]);
    	if (appWidgetInfo != null) {
    		return appWidgetInfo.label;
    	} else 
    		return "Widget";
    	
    }
    private int getSavedHeight(int pos) {
        SharedPreferences prefs = mContext.getSharedPreferences("widget_adapter",
                Context.MODE_WORLD_WRITEABLE);
        return prefs.getInt("widget_pos_" + pos, 100);
    }

    private void setSavedHeight(int pos, int height) {
        SharedPreferences prefs = mContext.getSharedPreferences("widget_adapter",
                Context.MODE_WORLD_WRITEABLE);
        prefs.edit().putInt("widget_pos_" + pos, height).commit();
    }

    /**
     * Create the page for the given position. The adapter is responsible for
     * adding the view to the container given here, although it only must ensure
     * this is done by the time it returns from {@link #finishUpdate()}.
     * 
     * @param container The containing View in which the page will be shown.
     * @param position The page position to be instantiated.
     * @return Returns an Object representing the new page. This does not need
     *         to be a View, but can be some other container of the page.
     */
    @Override
    public Object instantiateItem(View collection, int position) {
        ImageView widgetPreview;
        int widgetId = widgetIds[position];
        Log.d(TAG,"Instantiate PagerAdapter:"+ position);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetId);
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.widget_image_preview, null);
        widgetPreview = (ImageView) vg.findViewById(R.id.widget_preview);
        Log.d(TAG,"Instantiate widgetID:"+ widgetId);
        if (widgetId == -1 || appWidgetInfo == null){
            widgetPreview.setImageResource(R.drawable.ic_sysbar_null);
        } else {
            widgetPreview.setImageResource(appWidgetInfo.previewImage);
        }       
        if (hostViews != null && position < hostViews.length){
            hostViews[position] = vg;
            ((ViewPager) collection).addView(hostViews[position], 0);
            Log.d(TAG,"Instantiate hostViews added:"+ hostViews[position]);
            return hostViews[position];
        } else {
            Log.d(TAG,"HostViews null!!");
            return null;
        }
           
    }

    /**
     * Remove a page for the given position. The adapter is responsible for
     * removing the view from its container, although it only must ensure this
     * is done by the time it returns from {@link #finishUpdate()}.
     * 
     * @param container The containing View from which the page will be removed.
     * @param position The page position to be removed.
     * @param object The same object that was returned by
     *            {@link #instantiateItem(View, int)}.
     */
    @Override
    public void destroyItem(View collection, int position, Object view) {
        ((ViewPager) collection).removeView((ViewGroup) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View) object);
    }

    /**
     * Called when the a change in the shown pages has been completed. At this
     * point you must ensure that all of the pages have actually been added or
     * removed from the container as appropriate.
     * 
     * @param container The containing View which is displaying this adapter's
     *            page views.
     */
    @Override
    public void finishUpdate(View arg0) {
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View arg0) {
    }
}
