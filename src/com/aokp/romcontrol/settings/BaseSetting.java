package com.aokp.romcontrol.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aokp.romcontrol.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Base class from which all other layouts inherit from. Subclasses must
 * <p/>
 * <ul><b>Supported attributes (all are optional)</b>
 * <li>android:key - the key to look up the value
 * <li>android:title - a @string reference to display as the title
 * <li>android:summary - a @string reference to display as the summary
 * <li>android:defaultValue - a string (NOT a reference) which is stored as the default value.
 * <li>table - refers to which table the setting key/value are referenced in. Currently only two tables are
 * supported: "aokp", and "system". Defaults to "aokp" if none is specified. Note that there is no namespace on this attribute.
 * </ul>
 * <p/>
 * Sub classes' implementation of setValue() should eventually call {@link #setValue(String)},
 * which will actually set the key and call the registered listener, if one exists.
 * <p/>
 *
 * @see OnSettingChangedListener OnSettingChangedListener
 * an interface which will allow the callback receiver to see any changes in this setting.
 */
public class BaseSetting extends LinearLayout {

    public static final String TAG = BaseSetting.class.getSimpleName();

    public static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
    public static final String NAMESPACE_RC = "http://schemas.android.com/apk/res/res-auto";

    // values obtained from attributes
    private String aKey, aTable, aTitle, aSummary, aDefaultValue;

    // separate in case we want to query whether one was supplied
    private String mSummary;

    protected TextView mTitleTextView, mDescriptionTextView;

    private OnSettingChangedListener mOnSettingChangedListener;

    /**
     * Sub classes should attach their inflated views to this view.
     */
    protected ViewGroup mRootView;

    private final ArrayList<OnClickListener> mRegisteredClickListeners = new ArrayList<OnClickListener>();
    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            for (OnClickListener clickListener : mRegisteredClickListeners) {
                clickListener.onClick(view);
            }
        }
    };

    public final void setOnClickListener(OnClickListener listener) {
        mRegisteredClickListeners.add(listener);
    }

    /**
     * Interface to allow classes to receive callbacks when the user has modified the value of the setting.
     */
    public interface OnSettingChangedListener {
        public void onSettingChanged(String table, String key, String oldValue, String value);
    }

    public BaseSetting(Context context) {
        this(context, null);
    }

    public BaseSetting(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseSetting(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            aKey = attrs.getAttributeValue(NAMESPACE_ANDROID, "key");
            aDefaultValue = attrs.getAttributeValue(NAMESPACE_ANDROID, "defaultValue");

            Resources r = context.getResources();
            aTitle = readAttrStringResource(r, attrs.getAttributeResourceValue(NAMESPACE_ANDROID, "title", 0));
            aSummary = readAttrStringResource(r, attrs.getAttributeResourceValue(NAMESPACE_ANDROID, "summary", 0));

            aTable = attrs.getAttributeValue(null, "table");
            if (aTable == null) {
                aTable = "aokp";
            }
        }

        mRootView = (ViewGroup) View.inflate(context, R.layout.setting_base, null);
        mTitleTextView = (TextView) mRootView.findViewById(R.id.title);
        mDescriptionTextView = (TextView) mRootView.findViewById(R.id.summary);

        setTitle(aTitle);
        setSummary(aSummary);
        super.setOnClickListener(mOnClickListener);
    }



    /**
     * @param s the new setting to apply to this table/key. Null strings are considered empty strings.
     * @throws UnsupportedOperationException if no key is set.
     */
    protected final void setValue(String s) {
        if (aKey == null) {
            // assume it's handled some other way
            return;
        }
        // accept null strings - just set them to empty
        if (s == null) {
            s = "";
        }
        String currentVal;
        try {
            currentVal = getValue();
        } catch (Exception e) {
            currentVal = "";
        }
        String key = getKey();
        // Log.d(TAG, "Attempting to set key " + key + " to value: " + s + ", in table: " + getTable());

        // dirty dirty! use reflection to allow compilation via gradle/android studio
        try {
            String className = "android.provider.Settings$";
            if ("system".equalsIgnoreCase(getTable())) {
                className += "System";
            } else {
                className += "AOKP";
            }
            Class<?> clazz = Class.forName(className);

            Class[] params = new Class[3];
            params[0] = ContentResolver.class;
            params[1] = String.class;
            params[2] = String.class;

            Object[] paramObjects = new Object[3];
            paramObjects[0] = getContext().getContentResolver();
            paramObjects[1] = getKey();
            paramObjects[2] = s;

            Method method = clazz.getDeclaredMethod("putString", params);
            method.setAccessible(true);

            Boolean result = (Boolean) method.invoke(null, paramObjects);
            // Log.d(TAG, "result: " + result.toString());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        if (mOnSettingChangedListener != null) {
            mOnSettingChangedListener.onSettingChanged(getTable(), key, currentVal, s);
        }
    }

    /**
     * @return the string value of the setting.
     */
    protected String getValue() {
        if (aKey == null) {
            return null;
        }

        // dirty dirty! use reflection to allow compilation via gradle/android studio
        try {
            String className = "android.provider.Settings$";
            if ("system".equalsIgnoreCase(getTable())) {
                className += "System";
            } else {
                className += "AOKP";
            }
            Class<?> clazz = Class.forName(className);
            Class[] params = new Class[2];
            params[0] = ContentResolver.class;
            params[1] = String.class;

            Object[] paramObjects = new Object[2];
            paramObjects[0] = getContext().getContentResolver();
            paramObjects[1] = getKey();

            Method method = clazz.getDeclaredMethod("getString", params);
            method.setAccessible(true);
            Object result = method.invoke(null, paramObjects);
            //Log.d(TAG, "result: " + result != null ? result : "null");
            return (String) result;
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        return null;
    }


    /**
     * @return the table which the setting will be saved to. Currently only 'aokp' and 'system' are supported
     */
    protected final String getTable() {
        return aTable;
    }

    /**
     * @return the key value which this preference is supposed to represent
     */
    public final String getKey() {
        return aKey;
    }

    /**
     * Used to assign or change a key value
     */
    public final void setKey(String key) {
        aKey = key;
    }

    public void setDefaultValue(String defaultValue) {
        aDefaultValue = defaultValue;
    }

    /**
     * @return returns the supplied default value. null if none was provided.
     */
    protected final String getDefaultValue() {
        return aDefaultValue;
    }

    /**
     * @return returns the current summary;
     */
    protected final String getCurrentSummary() {
        return mSummary;
    }

    protected final String getDefaultSummary() {
        return aSummary;
    }

    protected void setSummary(String summary) {
        mSummary = summary;
        if (mDescriptionTextView != null) {
            mDescriptionTextView.setText(summary);
            if (summary == null) {
                mDescriptionTextView.setVisibility(View.GONE);
            } else {
                mDescriptionTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    protected final String getTitle() {
        return aTitle;
    }

    protected void setTitle(String title) {
        aTitle = title;
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
    }

    public void setOnSettingChangedListener(OnSettingChangedListener listener) {
        this.mOnSettingChangedListener = listener;
        if(listener != null) {
            // set initial value
            listener.onSettingChanged(getTable(), getKey(), null, getValue());
        }
    }

    /**
     * Helper method which attempts to read in a String resource, and get its value.
     *
     * @param r        Resources from which to do the lookup
     * @param resource R.string identifier
     * @return the looked-up String, or null if it wasn't found
     */
    public static String readAttrStringResource(Resources r, int resource) {
        try {
            String string = r.getString(resource);
            return string;
        } catch (NotFoundException e) {
            return null;
        }
    }

}
