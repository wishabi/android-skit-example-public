package com.flipp.example.app;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CouponListView extends LinearLayout implements View.OnClickListener {

  private Context mContext;

  public CouponListView(Context context) {
    this(context, null);
  }

  public CouponListView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CouponListView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    mContext = context;
  }

  public void setCoupons(JSONArray couponJson) {
    if (couponJson == null) {
      return;
    }

    for (int index = 0; index < couponJson.length(); index++) {
      try {
        // get the items we need from the coupon
        JSONObject coupon = couponJson.getJSONObject(index);
        LoyaltyProgramCoupon lpc = new LoyaltyProgramCoupon(coupon);
        if (lpc == null) {
          return;
        }

        // build our coupon view
        View couponRoot = View.inflate(mContext, R.layout.coupon_cell, null);
        NetworkImageView couponImage =
            (NetworkImageView) couponRoot.findViewById(R.id.coupon_image);
        TextView couponStory = (TextView) couponRoot.findViewById(R.id.coupon_story);
        TextView couponPromo = (TextView) couponRoot.findViewById(R.id.coupon_promo);
        TextView couponLegal = (TextView) couponRoot.findViewById(R.id.coupon_disclaimer);
        ViewGroup clipButton = (ViewGroup) couponRoot.findViewById(R.id.clip_button);

        if (!TextUtils.isEmpty(lpc.getImageUrl())) {
          couponImage.setImageUrl(lpc.getImageUrl(),
              VolleyRequestQueue.getInstance(getContext()).getImageLoader());
        }

        if (!TextUtils.isEmpty(lpc.getStory())) {
          couponStory.setText(lpc.getStory());
        }

        if (!TextUtils.isEmpty(lpc.getPromotion())) {
          couponPromo.setText(lpc.getPromotion());
        }

        if (!TextUtils.isEmpty(lpc.getLegal())) {
          couponLegal.setText(lpc.getLegal());
        }

        boolean isClipped =
            ClippingsManager.getInstance().containsCoupon(lpc.getCouponId());
        if (isClipped) {
          clipButton.setEnabled(false);
          couponRoot.setActivated(true);
          ((TextView) clipButton.findViewById(R.id.clip_button_text)).setText("Coupon Clipped");
        }
        clipButton.setTag(R.id.tag_lpc, lpc);
        clipButton.setTag(R.id.tag_coupon_view, couponRoot);
        clipButton.setOnClickListener(this);

        addView(couponRoot);

      } catch (JSONException e) {
        // skip the item
        continue;
      }
    }
  }

  @Override
  public void onClick(final View v) {
    final View couponRoot = (View) v.getTag(R.id.tag_coupon_view);
    LoyaltyProgramCoupon lpc = (LoyaltyProgramCoupon) v.getTag(R.id.tag_lpc);
    lpc.clip(
        (FlyerKitApplication) mContext.getApplicationContext(),
        new LoyaltyProgramCoupon.ClipCallback() {
          @Override
          public void clipComplete(boolean clipped) {
            if (clipped) {
              v.setEnabled(false);
              couponRoot.setActivated(true);
              ((TextView) v.findViewById(R.id.clip_button_text)).setText("Coupon Clipped");
            }
          }
        });
  }
}
