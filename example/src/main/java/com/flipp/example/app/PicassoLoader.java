package com.flipp.example.app;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flipp.sfml.helpers.ImageLoader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;

/**
 * Created by johnstemberger on 2018-05-22.
 */

public class PicassoLoader implements ImageLoader.Loader {
  private HashMap<ImageLoader.ImageTarget, Target> mTargetMap;

  private static PicassoLoader mPicassoLoader;

  public static synchronized ImageLoader.Loader getPicassoLoader() {
    if (mPicassoLoader == null) {
      mPicassoLoader = new PicassoLoader();
    }
    return mPicassoLoader;
  }


  private PicassoLoader() {
    mTargetMap = new HashMap<>();
  }

  @Override
  public void loadInto(String url, @NonNull final ImageLoader.ImageTarget target) {
    final Target picassoTarget = new Target() {
      @Override
      public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        target.onBitmapLoaded(bitmap);
        mTargetMap.remove(target);
      }

      @Override
      public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        target.onBitmapFailed();
        mTargetMap.remove(target);
      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {

      }
    };
    mTargetMap.put(target, picassoTarget);
    Picasso.get()
           .load(url)
           .into(picassoTarget);
  }

  @Override
  public void cancelTarget(@Nullable ImageLoader.ImageTarget target) {
    Target picassoTarget = mTargetMap.get(target);
    if (picassoTarget == null) {
      return;
    }
    Picasso.get()
           .cancelRequest(picassoTarget);
  }

}