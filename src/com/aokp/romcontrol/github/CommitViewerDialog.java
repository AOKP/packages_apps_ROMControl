
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.github.objects.CommitObject;
import com.aokp.romcontrol.github.objects.GithubObject;
import com.aokp.romcontrol.github.tasks.FindMissingCommitDataTask;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

/**
 * Shows extended infomation about a selected commit
 * in an AlertDialog
 */
public class CommitViewerDialog extends AlertDialog.Builder implements UrlImageViewCallback {
    private String TAG = getClass().getSimpleName();
    private boolean DEBUG = false;
    private View mRoot;
    private Context mContext;
    private static CommitObject mCommit;

    /**
     * Initialize the dialog but do not show
     * @param context application context used to this bind dialog
     */
    public CommitViewerDialog(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * sets the CommitObject we pull all dialog info from
     * and shows the dialog
     * @param commit commit to display
     */
    public void setCommitAndShow(CommitObject commit) {
        mCommit = commit;
        showDialog();
    }

    /**
     * This should never be called directly
     * instead use this.setCommitAndShow(CommitObject)
     */
    private void showDialog() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflater.inflate(R.layout.extended_commit_info_layout, null);
        setup();

        this.setTitle(R.string.commit_extended_info_title);
        this.setView(mRoot);
        this.setNegativeButton(R.string.button_close, null);
        this.setPositiveButton(R.string.button_webview,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    String webviewUrl = "https://github.com/" + Config.ORGANIZATION
                        + mCommit.getPath() + "/commit/"
                        + mCommit.getCommitHash();
                    i.setData(Uri.parse(webviewUrl));
                    if (DEBUG) Log.d(TAG, mCommit.toString());
                    mContext.startActivity(i);
                }
            });
        this.create().show();
    }

    private void setup() {
        // references for our objects
        ScrollView scroller = (ScrollView) mRoot.findViewById
            (R.id.extended_commit_info_layout_scrollview);
        // so we scroll smoothly if commit message is large
        scroller.setSmoothScrollingEnabled(true);
        scroller.fling(Config.StaticVars.DEFAULT_FLING_SPEED);

        // gain object references
        LinearLayout committerContainer = (LinearLayout) mRoot.findViewById
            (R.id.committer_container);
        final ImageView authorAvatar = (ImageView) mRoot.findViewById
            (R.id.author_avatar);
        final ImageView committerAvatar = (ImageView) mRoot.findViewById
            (R.id.committer_avatar);
        TextView committer_header = (TextView) mRoot.findViewById
            (R.id.committer_header);
        TextView author_tv = (TextView) mRoot.findViewById
            (R.id.commit_author);
        TextView committer_tv = (TextView) mRoot.findViewById
            (R.id.commit_committer);
        TextView message_tv = (TextView) mRoot.findViewById
            (R.id.commit_message);
        TextView date_tv = (TextView) mRoot.findViewById
            (R.id.commit_date);
        TextView sha_tv = (TextView) mRoot.findViewById
            (R.id.commit_sha);

        String commitPath = "https://api.github.com/repos/"
            + Config.ORGANIZATION + mCommit.getPath()
            + "/commits/" + mCommit.getCommitHash().trim();
        // if we have a url for image get the image if not
        // look the commit up on github and attempt to repopulate
        // gravatar url from there
        if (mCommit.getAuthorGravatar() != null) {
            UrlImageViewHelper.setUrlDrawable(authorAvatar,
                    mCommit.getAuthorGravatar(),
                    R.drawable.ic_null,
                    UrlImageViewHelper.CACHE_DURATION_ONE_WEEK,
                    this);
        } else {
            FindMissingCommitDataTask getMoreInfoTask = new FindMissingCommitDataTask() {
                /**
                 * called when the task is finished to
                 * apply our new missing values
                 *
                 * @param result GithubObject with all possible information
                 *        (with all fields the possibility of null exists
                 *         along with the possibility we failed and the
                 */
                @Override
                protected void onPostExecute(GithubObject result) {
                    // if one image is null we attempt to populate both assuming
                    // the second will also fail
                    if (result != null) {
                        mCommit = result;
                        String aImagUrl = mCommit.getAuthorGravatar();
                        String cImagUrl = mCommit.getCommitterGravatar();
                        // handle author image
                        if (aImagUrl != null && !"".equals(aImagUrl.trim())) {
                            UrlImageViewHelper.setUrlDrawable(authorAvatar,
                                    mCommit.getAuthorGravatar(),
                                    R.drawable.ic_null,
                                    UrlImageViewHelper.CACHE_DURATION_ONE_WEEK,
                                    getThis());
                        } else {
                            Config.StaticVars.REMOVE_AUTHOR_LAYOUT = true;
                        }

                        // handle committer image
                        if (cImagUrl != null && !"".equals(cImagUrl.trim())) {
                            UrlImageViewHelper.setUrlDrawable(committerAvatar,
                                    mCommit.getCommitterGravatar(),
                                    R.drawable.ic_null,
                                    UrlImageViewHelper.CACHE_DURATION_ONE_WEEK,
                                    getThis());
                        } else {
                            Config.StaticVars.REMOVE_COMMITTER_LAYOUT = true;
                        }
                    }
                }
            };
            // execute with path as parameter
            getMoreInfoTask.execute(commitPath);
        }

        Log.d(TAG, "hash: " + mCommit.getCommitHash().trim());
        Log.d(TAG, "path: " + commitPath);
        if (Config.StaticVars.REMOVE_COMMITTER_LAYOUT) {
            committerContainer.setVisibility(View.GONE);
            committer_header.setVisibility(View.GONE);
            // reset our watcher
            Config.StaticVars.REMOVE_COMMITTER_LAYOUT = false;
        } else {
            // try to populate the image from gravatar
            // @link http://stackoverflow.com/a/9288544
            if (mCommit.getCommitterGravatar() != null
                    && !mCommit.getCommitterGravatar().equals("")) {
                UrlImageViewHelper.setUrlDrawable(committerAvatar,
                        mCommit.getCommitterGravatar(),
                        R.drawable.ic_null,
                        UrlImageViewHelper.CACHE_DURATION_ONE_WEEK,
                        this);
            } else {
                Config.StaticVars.REMOVE_COMMITTER_LAYOUT = true;
            }
        }

        // setText for TextViews
        author_tv.setText(mCommit.getAuthorName());
        committer_tv.setText(mCommit.getCommitterName());
        message_tv.setText(mCommit.getBody());
        date_tv.setText(mCommit.getCommitterDate());

        // we split the sha-1 hash into two strings because
        // it looks horrible by default display and smaller
        // size text is hard to read
        int halfHashLength = mCommit.getCommitHash().length() / 2;
        StringBuilder splitHash = new StringBuilder();
        splitHash.append(mCommit.getCommitHash().substring(0, halfHashLength));
        splitHash.append("-\n"); // to separate the strings
        splitHash.append(mCommit.getCommitHash().substring(halfHashLength));
        splitHash.trimToSize();
        // set the text from our StringBuilder
        sha_tv.setText(splitHash.toString());
    }

    private CommitViewerDialog getThis() {
        return this;
    }

    @Override
    public void onLoaded(ImageView imageView, Drawable loadedDrawable, String url, boolean loadedFromCache) {
        Log.d(TAG, "Avatar loaded from " + (loadedFromCache ? "cache" : url));
        imageView.setImageDrawable(loadedDrawable);
        imageView.setVisibility(View.VISIBLE);
    }
}