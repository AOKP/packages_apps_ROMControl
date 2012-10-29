
package com.aokp.romcontrol.github;

/*
 * Copyright (C) 2012 The Android Open Kang Project
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
 * simple image download performed off the UI thread
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private final boolean DEBUG = false;
    private final String TAG = getClass().getSimpleName();
    // send String[] (url[0]) return Bitmap
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected void onPreExecute() {
        bmImage.setVisibility(View.GONE);
    }

    protected Bitmap doInBackground(String... urls) {
        String avatarUrl = urls[0];
        if (DEBUG) Log.d(TAG, "downloading: " + avatarUrl);
        Bitmap mAvatar = null;
        try {
            InputStream in = new URL(avatarUrl).openStream();
            mAvatar = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(TAG, "failed to download avatar", e);
        }
        return mAvatar;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
        bmImage.setVisibility(View.VISIBLE);
    }
}
