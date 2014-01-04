package com.aokp.romcontrol.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.aokp.romcontrol.R;

/**
 * Setting toggle which represents an image
 * <p/>
 * <ul><b>Supported attributes (in addition to {@link BaseSetting} attributes)</b>
 * <li>aokp:imagename - name of the saved image
 * </ul>
 */
public class ImageSetting extends BaseSetting implements OnClickListener {

    private ImageView mImageView;

    public ImageSetting(Context context) {
        this(context, null);
    }

    public ImageSetting(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageSetting(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        addView(View.inflate(context, R.layout.setting_image, mRootView));
        setFocusable(true);

        mImageView = (ImageView) findViewById(R.id.image_preview);

        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
    }

    public void setImageDrawable(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }

}
