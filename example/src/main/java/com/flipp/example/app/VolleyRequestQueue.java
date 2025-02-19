package com.flipp.example.app;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import androidx.collection.LruCache;

/**
 * Created by johnstemberger on 2018-05-15.
 */

public class VolleyRequestQueue {
  private static VolleyRequestQueue mInstance;
  private RequestQueue mRequestQueue;
  private ImageLoader mImageLoader;
  private static Context mCtx;

  private VolleyRequestQueue(Context context) {
    mCtx = context;
    mRequestQueue = getRequestQueue();

    mImageLoader = new ImageLoader(mRequestQueue,
        new ImageLoader.ImageCache() {
          private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

          @Override
          public Bitmap getBitmap(String url) {
            return cache.get(url);
          }

          @Override
          public void putBitmap(String url, Bitmap bitmap) {
            cache.put(url, bitmap);
          }
        });
  }

  public static synchronized VolleyRequestQueue getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new VolleyRequestQueue(context);
    }
    return mInstance;
  }

  public RequestQueue getRequestQueue() {
    if (mRequestQueue == null) {
      // getApplicationContext() is key, it keeps you from leaking the
      // Activity or BroadcastReceiver if someone passes one in.
      mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
    }
    return mRequestQueue;
  }

  public <T> void addToRequestQueue(Request<T> req) {
    getRequestQueue().add(req);
  }

  public ImageLoader getImageLoader() {
    return mImageLoader;
  }
}