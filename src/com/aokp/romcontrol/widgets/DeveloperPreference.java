package com.aokp.romcontrol.widgets;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aokp.romcontrol.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class DeveloperPreference extends Preference {
    
    private static final String TAG = "DeveloperPreference";
    
    private ImageView twitterButton;
    private ImageView donateButton;
    private ImageView photoView;
    
    private TextView devName;
    
    private String nameDev;
    private String twitterName;
    private String donateLink;
    
    public DeveloperPreference (Context context, AttributeSet attrs) {
        super (context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeveloperPreference);
        nameDev = a.getString(R.styleable.DeveloperPreference_nameDev);
        twitterName = a.getString(R.styleable.DeveloperPreference_twitterHandle);
        donateLink = a.getString(R.styleable.DeveloperPreference_donateLink);
        a.recycle();
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        
        View layout = View.inflate(getContext(), R.layout.dev_card, null);

        twitterButton = (ImageView) layout.findViewById (R.id.twitter_button);
        donateButton = (ImageView) layout.findViewById (R.id.donate_button);
        devName = (TextView) layout.findViewById (R.id.name);
        photoView = (ImageView) layout.findViewById(R.id.photo);
        
        return layout;
    }
    
    private void getDevPic(String account) {
        Bitmap bitmap = null;
        final String url = "http://api.twitter.com/1/users/profile_image/" + account + "?size=original";
         
        AsyncTask<Void, Void, BitmapDrawable> downloader = new AsyncTask<Void, Void, BitmapDrawable>() {
            @Override
            protected BitmapDrawable doInBackground(Void... params) {
                AndroidHttpClient client = AndroidHttpClient.newInstance(getContext().getPackageName());
                try {
                    HttpGet get = new HttpGet(url);
                    final HttpParams httpParams = new BasicHttpParams();
                    HttpClientParams.setRedirecting(httpParams, true);
                    get.setParams(httpParams);
                    HttpResponse resp = client.execute(get);
                    int status = resp.getStatusLine().getStatusCode();
                    if(status != HttpURLConnection.HTTP_OK){
                        Log.i(TAG, "Couldn't download image from Server: " 
                            + url + " Reason: " + resp.getStatusLine().getReasonPhrase() 
                                + " / " + status);
                        return null;
                    }
                    HttpEntity entity = resp.getEntity();
                    InputStream is = entity.getContent();
                    return loadDrawableFromStream(is);
                }
                catch (Exception ex) {
                    Log.e(TAG, "Exception during Image download of " + url, ex);
                    return null;
                }
                finally {
                    client.close();
                } 
            }    
            
            protected void onPostExecute(BitmapDrawable result) {
                Drawable image = result;
                if (image != null) {
                    photoView.setImageDrawable(image);
                } else {
                    photoView.setVisibility(View.INVISIBLE);
                }
            }
        };
        downloader.execute();
    }
    
    private BitmapDrawable loadDrawableFromStream(InputStream stream) {
        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        BitmapDrawable bd = new BitmapDrawable(bitmap);
        return bd;
    }
    
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        if (donateLink != null) {
            final OnClickListener openDonate = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri donateURL = Uri.parse(donateLink);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, donateURL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);
                }
            };  
            
            donateButton.setOnClickListener(openDonate);
        } else {
            donateButton.setVisibility(View.GONE);
        }
        
        if (twitterName != null) {
            final OnPreferenceClickListener openTwitter = new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri twitterURL = Uri.parse("http://twitter.com/#!/" + twitterName);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, twitterURL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);
                    return true;
                }
            };
            
            //changed to clicking the preference to open twitter
            //it was a hit or miss to click the twitter bird
            //twitterButton.setOnClickListener(openTwitter);
            this.setOnPreferenceClickListener(openTwitter);
            getDevPic(twitterName);
        } else {
            twitterButton.setVisibility(View.INVISIBLE);
            photoView.setVisibility(View.GONE);
        }
        
        devName.setText(nameDev);
        
    }
}