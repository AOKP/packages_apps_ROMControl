package com.aokp.romcontrol.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.aokp.romcontrol.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Setting toggle which represents an image
 * <p/>
 * <ul><b>Supported attributes (in addition to {@link BaseSetting} attributes)</b>
 * <li>aokp:imagename - name of the saved image
 * </ul>
 */
public class ImagePickerSetting extends BaseSetting implements OnClickListener {

    public static final int REQUEST_PICK_WALLPAPER = 199;

    private String mImagename;
    private ImageView mImageView;

    public ImagePickerSetting(Context context) {
        this(context, null);
    }

    public ImagePickerSetting(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImagePickerSetting(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {

            TypedArray typedArray = null;
            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImagePickerSetting);

                mImagename = typedArray.getString(R.styleable.ImagePickerSetting_imageName);
            } finally {
                if (typedArray != null) {
                    typedArray.recycle();
                }
            }
        }

        addView(View.inflate(context, R.layout.setting_imagepicker, mRootView));
        setFocusable(true);

        mImageView = (ImageView) findViewById(R.id.image_preview);

        if(mImagename != null) {
            setOnClickListener(this);
            setImage();
        }
    }

    @Override
    public void onClick(View v) {
        if (wallpaperExists()) {
            buildWallpaperAlert();
        } else {
            prepareAndSetWallpaper();
        }
    }

    private boolean wallpaperExists() {
        File wallpaper = new File(mContext.getFilesDir(), mImagename);
        return wallpaper.exists();
    }

    private void setImage() {
        if (wallpaperExists()) {
            mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_wallpaper_exists));
        } else {
            mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_wallpaper_none));
        }
    }

    private Uri getLockscreenExternalUri() {
        File dir = mContext.getExternalCacheDir();
        File wallpaper = new File(dir, WALLPAPER_NAME);
        return Uri.fromFile(wallpaper);
    }

    private void buildWallpaperAlert() {
        Drawable myWall = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.lockscreen_wallpaper_title);
        builder.setPositiveButton(R.string.lockscreen_wallpaper_pick,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prepareAndSetWallpaper();
                    }
                });
        builder.setNegativeButton(R.string.lockscreen_wallpaper_remove,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeWallpaper();
                        dialog.dismiss();
                    }
                });
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.dialog_shade_wallpaper, null);
        ImageView wallView = (ImageView) layout.findViewById(R.id.shade_wallpaper_preview);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        wallView.setLayoutParams(new LinearLayout.LayoutParams(size.x / 2, size.y / 2));
        File wallpaper = new File(mContext.getFilesDir(), WALLPAPER_NAME);
        myWall = new BitmapDrawable(mContext.getResources(), wallpaper.getAbsolutePath());
        wallView.setImageDrawable(myWall);
        builder.setView(layout);
        builder.show();
    }

    private void prepareAndSetWallpaper() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();

        int width = getActivity().getWallpaperDesiredMinimumWidth();
        int height = getActivity().getWallpaperDesiredMinimumHeight();
        float spotlightX = (float)display.getWidth() / width;
        float spotlightY = (float)display.getHeight() / height;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("aspectX", width);
        intent.putExtra("aspectY", height);
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);
        intent.putExtra("spotlightX", spotlightX);
        intent.putExtra("spotlightY", spotlightY);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getLockscreenExternalUri());

        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    private void removeWallpaper() {
        mContext.deleteFile(mImagename);

        /*Some "no wallpaper" image (grayed out?)*/
        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_wallpaper_none));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_WALLPAPER) {
                FileOutputStream wallpaperStream = null;
                try {
                    wallpaperStream = mContext.openFileOutput(mImagename,
                            Context.MODE_WORLD_READABLE);

                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }
                Uri selectedImageUri = getLockscreenExternalUri();
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);

                /*Some "wallpaper exists" image (full color?)*/
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_wallpaper_exists));
                buildWallpaperAlert();
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
