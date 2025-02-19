package com.flipp.example.app;

import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * For more information on parsing coupon JSON - see FlyerKit API.
 */
public class LoyaltyProgramCoupon {

  /**
   * Callback when clipping a coupon is completed.
   */
  public interface ClipCallback {
    void clipComplete(boolean clipped);
  }

  /**
   * Callback when the list of clipped coupons is returned.
   */
  public interface ClippedCouponsCallback {
    void clippedCoupons(List<Integer> coupons);
  }

  private int mFlyerItemId;
  private int mCouponId;
  private int mLoyaltyProgramCouponId;
  private int mLoyaltyProgramId;
  private String mExternalId;
  private String mImageUrl;
  private String mPromotion;
  private String mLegal;
  private String mStory;

  public LoyaltyProgramCoupon(JSONObject json) throws JSONException {
    if (json != null) {
      mFlyerItemId = json.getInt("flyer_item_id");
      mCouponId = json.getInt("coupon_id");
      mLoyaltyProgramCouponId = json.getInt("loyalty_program_coupon_id");
      mLoyaltyProgramId = json.getInt("loyalty_program_id");
      mExternalId = json.getString("external_id");
      mImageUrl = json.getString("image");
      mStory = json.getString("sale_story");
      mPromotion = json.getString("promotion_text");
      mLegal = json.getString("disclaimer_text");
    }
  }

  public void clip(final FlyerKitApplication global, final ClipCallback callback) {
    // Require the Loyalty Card ID to be set
    if (TextUtils.isEmpty(global.loyaltyCardId) || TextUtils.isEmpty(global.merchantId)) {
      // needs card ID and merchant ID to clip a coupon
      return;
    }

    // send request to clip this coupon
    String clipUrl = global.rootUrl + "flyerkit/" + global.apiVersion
        + "/coupons/clip?access_token=" + global.accessToken;

    // Post params to be sent to the server
    JSONObject body = new JSONObject();

    try {
      // ALL FIELDS MANDATORY
      // These 3 fields should come from the coupon itself
      body.put("external_id", getExternalId());
      body.put("coupon_id", getCouponId());
      body.put("loyalty_program_coupon_id", getLoyaltyProgramCouponId());
      body.put("loyalty_program_id", getLoyaltyProgramId());

      // These 2 fields should be provided by user / retailer
      body.put("card_id", global.loyaltyCardId);
      body.put("merchant_id", Integer.valueOf(global.merchantId));
    } catch (JSONException e) {
      e.printStackTrace();
      return;
    }

    JsonObjectRequest req = new JsonObjectRequest(clipUrl, body,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            if (response != null) {
              // check response - document it
              ClippingsManager.getInstance().clipCoupon(getCouponId());
              callback.clipComplete(true);
              return;
            }
            callback.clipComplete(false);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            callback.clipComplete(false);
          }
        });

    // add the request object to the queue to be executed
    VolleyRequestQueue.getInstance(global).addToRequestQueue(req);
  }

  public static void getClippedCoupons(
      final FlyerKitApplication global, RequestQueue queue, final ClippedCouponsCallback callback) {
    // Require the Loyalty Card ID to be set
    if (TextUtils.isEmpty(global.loyaltyCardId)) {
      // needs card ID
      return;
    }

    // send request to clip this coupon
    String clipUrl = global.rootUrl + "flyerkit/" + global.apiVersion
        + "/coupons/get_clipped_coupons?access_token="
        + global.accessToken + "&postal_code" + global.postalCode;

    // Post params to be sent to the server
    JSONObject body = new JSONObject();

    try {
      // ALL FIELDS MANDATORY
      // These 3 fields should be provided by user / retailer
      body.put("card_id", global.loyaltyCardId);
      body.put("loyalty_program_id", global.loyaltyCardProgramId);
      body.put("merchant_id", Integer.valueOf(global.merchantId));
    } catch (JSONException e) {
      return;
    }

    JsonObjectRequest req = new JsonObjectRequest(clipUrl, body,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            if (response != null) {
              List<Integer> clippedCoupons = new ArrayList<>();

              try {
                JSONArray lpcArray = response.getJSONArray("loyalty_program_coupons");
                for (int index=0; index < lpcArray.length(); index++) {
                  JSONObject lpc = lpcArray.getJSONObject(index);
                  if (lpc.has("id")) {
                    clippedCoupons.add(lpc.getInt("id"));
                  }
                }
                callback.clippedCoupons(clippedCoupons);
                return;
              } catch (JSONException e) {
                e.printStackTrace();
              }
            }
            callback.clippedCoupons(null);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            callback.clippedCoupons(null);
          }
        });

    // add the request object to the queue to be executed
    queue.add(req);
  }

  public int getCouponId() {
    return mCouponId;
  }

  public String getExternalId() {
    return mExternalId;
  }

  public int getFlyerItemId() {
    return mFlyerItemId;
  }

  public int  getLoyaltyProgramCouponId() {
    return mLoyaltyProgramCouponId;
  }

  public int getLoyaltyProgramId() {
    return mLoyaltyProgramId;
  }

  public String getLegal() {
    return mLegal;
  }

  public String getImageUrl() {
    return mImageUrl;
  }

  public String getPromotion() {
    return mPromotion;
  }

  public String getStory() {
    return mStory;
  }
}
