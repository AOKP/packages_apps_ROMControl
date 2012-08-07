package com.baked.romcontrol;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import com.baked.romcontrol.R;

public class DeveloperPreference extends Preference implements OnClickListener {
    private Context mContext;

    private Drawable mDevIcon;

    private ImageView mAvatar;
    private ImageView mBtnDonate;
    private ImageView mBtnEmail;
    private ImageView mBtnMarket;
    private LinearLayout mTwitterRes;
    private ProgressBar mLoading;
    private TextView mTitleRes;

    private String mDevUrl;
    private String mDonate;
    private String mEmail;
    private String mMarket;
    private String mTwitter;

    public DeveloperPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.DeveloperPreference, 0, 0);
        mDevIcon = attributes.getDrawable(R.styleable.DeveloperPreference_devIcon);
        mDevUrl = attributes.getString(R.styleable.DeveloperPreference_devUrl);
        mDonate = attributes.getString(R.styleable.DeveloperPreference_donate);
        mEmail = attributes.getString(R.styleable.DeveloperPreference_email);
        mMarket = attributes.getString(R.styleable.DeveloperPreference_market);
        mTwitter = attributes.getString(R.styleable.DeveloperPreference_twitter);
        attributes.recycle();
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View developer = View.inflate(mContext, R.layout.widget_developer, null);

        mAvatar = (ImageView) developer.findViewById(R.id.widget_developer_photo);
        mBtnDonate = (ImageView) developer.findViewById(R.id.widget_developer_btn_donate);
        mBtnEmail = (ImageView) developer.findViewById(R.id.widget_developer_btn_email);
        mBtnMarket = (ImageView) developer.findViewById(R.id.widget_developer_btn_market);
        mTwitterRes = (LinearLayout) developer.findViewById(R.id.widget_developer_title);
        mLoading = (ProgressBar) developer.findViewById(R.id.widget_developer_loading);
        mTitleRes = (TextView) developer.findViewById(R.id.widget_developer_name);

        return developer;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        if (mDevIcon != null) {
            mAvatar.setImageDrawable(mDevIcon);
        } else if (mDevUrl != null) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));
            imageLoader.displayImage(mDevUrl, mAvatar, new ImageLoadingListener() {

                public void onLoadingStarted() {
                    mLoading.setVisibility(View.VISIBLE);
                }

                public void onLoadingFailed(FailReason failReason) {
                    mLoading.setVisibility(View.INVISIBLE);
                }

                public void onLoadingComplete() {
                    mLoading.setVisibility(View.INVISIBLE);
                }

                public void onLoadingCancelled() {
                    // Do nothing here unless you want to create a log report
                }
            });
        }

        if (mDonate != null) {
            mBtnDonate.setOnClickListener(this);
        } else mBtnDonate.setVisibility(View.GONE);

        if (mEmail != null) {
            mBtnEmail.setOnClickListener(this);
        } else mBtnEmail.setVisibility(View.GONE);

        if (mMarket != null) {
            mBtnMarket.setOnClickListener(this);
        } else mBtnMarket.setVisibility(View.GONE);

        if (mTwitter != null) {
            mTitleRes.setText("@" + mTwitter);
            this.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Uri uriUrl = Uri.parse("http://twitter.com/#!/" + mTwitter);
                    Intent twitter = new Intent(Intent.ACTION_VIEW, uriUrl);
                    getContext().startActivity(twitter);
                    return true;
                }
            });
        } else mTwitterRes.setVisibility(View.GONE);
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.widget_developer_btn_donate:
                Uri uriUrl = Uri.parse(mDonate);
                Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
                getContext().startActivity(donate);
                break;
            case R.id.widget_developer_btn_email:
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {mEmail});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                getContext().startActivity(emailIntent);
                break;
            case R.id.widget_developer_btn_market:
                Uri playStore = Uri.parse(mMarket);
                Intent shop = new Intent(Intent.ACTION_VIEW, playStore);
                getContext().startActivity(shop);
                break;
        }
    }
}
