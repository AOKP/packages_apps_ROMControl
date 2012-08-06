package com.nostra13.universalimageloader.core.assist;

/**
 * A convenience class to extend when you only want to listen for a subset of all the image loading events. This
 * implements all methods in the {@link ImageLoadingListener} but does nothing.
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class SimpleImageLoadingListener implements ImageLoadingListener {
	public void onLoadingStarted() {
		// Empty implementation
	}

	public void onLoadingFailed(FailReason failReason) {
		// Empty implementation
	}

	public void onLoadingComplete() {
		// Empty implementation
	}

	public void onLoadingCancelled() {
		// Empty implementation
	}
}
