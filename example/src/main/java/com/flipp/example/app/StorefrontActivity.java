package com.flipp.example.app;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WayfinderView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.SparseArrayCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.flipp.injectablehelper.HelperManager;
import com.flipp.injectablehelper.ViewHelper;
import com.flipp.sfml.ItemAttributes;
import com.flipp.sfml.ItemSource;
import com.flipp.sfml.SFArea;
import com.flipp.sfml.SFHead;
import com.flipp.sfml.StoreFront;
import com.flipp.sfml.Wayfinder;
import com.flipp.sfml.helpers.ImageLoader;
import com.flipp.sfml.helpers.SFMLHelper;
import com.flipp.sfml.helpers.StorefrontAnalyticsManager;
import com.flipp.sfml.helpers.StorefrontViewBuilder;
import com.flipp.sfml.net.ParseStorefrontHelper;
import com.flipp.sfml.views.StorefrontImageView;
import com.flipp.sfml.views.StorefrontItemAtomViewHolder;
import com.flipp.sfml.views.ZoomScrollView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StorefrontActivity extends AppCompatActivity
  implements ParseStorefrontHelper.ParseStorefrontLoadListener,
  StorefrontImageView.OnAreaClickListener,
  StorefrontImageView.ClipStateDelegate,
  StorefrontImageView.MatchupDelegate,
  StorefrontItemAtomViewHolder.ItemAtomClickListener,
  ZoomScrollView.OnZoomListener,
  StorefrontAnalyticsManager.AnalyticsEventsListener {

  @Override
  public void onAreaClicked(View view, SFArea sfArea) {
    if (sfArea == null) {
      return;
    }
    showItemDetails(sfArea.getItemAttributes());
  }

  @Override
  public void onAreaLongPressed(View view, SFArea sfArea) {
    if (sfArea == null) {
      return;
    }
    clipItem(sfArea.getItemAttributes());
  }

  /**
   * ParseStorefrontHelper.ParseStorefrontLoadListener
   * @param e
   *
   * Triggered when an loading/parsing SFML file throws an Exception
   */
  @Override
  public void onStorefrontParseError(Exception e) {
    Log.e(TAG, "onStorefrontParseError: $e");
    Toast.makeText(this, "onStorefrontParseError: $e", Toast.LENGTH_SHORT).show();
  }

  /**
   * ParseStorefrontHelper.ParseStorefrontLoadListener
   * @param store
   *
   * Triggered when an SFML file is successfully loaded and parsed
   */
  @Override
  public void onStorefrontParsed(StoreFront store) {
    HelperManager.getService(ImageLoader.class).setImageLoader(PicassoLoader.getPicassoLoader());

    // TODO: update your toolbar with the values from store.getTitle() and store.getSubtitle()

    mStorefront = (ZoomScrollView) new StorefrontViewBuilder(this, store)
            .setAnalyticsManager(mManager)
            .setAreaClickListener(this)
            .setClipStateDelegate(this)
            .setMatchupDelegate(this)
            .setItemAtomClickListener(this)
            .setItemAtomViewHolderStorage(mHeroItems)
            .build();
    mManager.setStorefrontView(mStorefront);
    mManager.setWayfinderView(mStorefrontWrapper);
    mStorefrontWrapper.addView(mStorefront,
            0,
            new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

    // setup wayfinder
    SFHead head = store.getHead();
    if (head == null) {
      return;
    }
    Wayfinder wayfinder = head.getWayfinder();
    if (wayfinder == null) {
      return;
    }
    List<Wayfinder.WayfinderCategory> ways = wayfinder.getCategories();
    mStorefrontWrapper.setWayfinderDelegates(ways);
  }

  public static final String TAG = StorefrontActivity.class.getSimpleName();
  public static final String STOREFRONT_URL_KEY = TAG + ".STOREFRONT_URL_KEY";
  public static final String STOREFRONT_CLIPPINGS = TAG + ".STOREFRONT_CLIPPINGS";
  public static final String PUBLICATION_ID_KEY = TAG + ".PUBLICATION_ID_KEY";

  private String mStorefrontUrl;
  private WayfinderView mStorefrontWrapper;
  private ZoomScrollView mStorefront;
  private ArrayList<StorefrontItemAtomViewHolder> mHeroItems;
  private String mPublicationId;
  private SparseArrayCompat<LoyaltyProgramCoupon> mCoupons;
  private SparseArrayCompat<JSONObject> mItems;
  private FlyerKitApplication mApp;
  private StorefrontAnalyticsManager mManager;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApp = (FlyerKitApplication) getApplicationContext();

    setContentView(R.layout.activity_storefront);
    Bundle b = savedInstanceState;
    if (b == null) {
      b = getIntent().getExtras();
    }
    if (b != null) {
      mStorefrontUrl = b.getString(STOREFRONT_URL_KEY);
      mPublicationId = b.getString(PUBLICATION_ID_KEY);
    }
    mHeroItems = new ArrayList<>();
    mStorefrontWrapper = findViewById(R.id.storefront_wrapper);
    mManager = new StorefrontAnalyticsManager();
    mManager.setAnalyticsEventListener(this);

    if (!TextUtils.isEmpty(mStorefrontUrl)) {
      // Parse the SFML from the given url using ParseStorefrontHelper
      // SDK handles fetchAndParseStorefront() on a background thread to fetch the Storefront object
      // Check onStorefrontParsed/onStorefrontParseError callbacks for response
      ParseStorefrontHelper parseStorefrontHelper = new ParseStorefrontHelper();
      if(mStorefrontUrl != null) {
        parseStorefrontHelper.fetchAndParseStorefront(mStorefrontUrl, this);
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadPublicationItems(mPublicationId);
    mManager.setStorefrontVisibility(true);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mManager.setStorefrontVisibility(false);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(STOREFRONT_URL_KEY, mStorefrontUrl);
    outState.putString(PUBLICATION_ID_KEY, mPublicationId);
  }

  private void loadPublicationItems(String publicationId) {
    mCoupons = new SparseArrayCompat<>();
    mItems = new SparseArrayCompat<>();
    String itemsUrl = mApp.rootUrl + "flyerkit/" + mApp.apiVersion + "/publication/" + publicationId + "/products?"
      + "access_token=" + mApp.accessToken
      + "&postal_code=" + mApp.postalCode
      + "&display_type=1,5,3,25,7,15";

    JsonArrayRequest itemsRequest = new JsonArrayRequest(itemsUrl,
      new Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response) {
          parseItems(response);
        }
      },
      new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          // TODO
        }
      });
    VolleyRequestQueue.getInstance(this).addToRequestQueue(itemsRequest);
  }

  private void parseItems(JSONArray response) {
    for (int i = 0, n = response.length(); i < n; ++i) {
      try {
        JSONObject item = response.getJSONObject(i);
        processPublicationItem(item);
      } catch (JSONException e) {
        // Skip item.
      }
    }
    LocalBroadcastManager
      .getInstance(this)
      .sendBroadcast(new Intent(StorefrontImageView.CLIP_STATE_CHANGE_ACTION));
  }

  private void processPublicationItem(JSONObject item) throws JSONException {
    // process the flyer item
    int id = item.getInt("id");
    mItems.put(id, item);
    // check for coupon matchups
    if (item.has("coupons")) {
      JSONArray couponArray = item.getJSONArray("coupons");
      // check for clipped coupons
      for (int index = 0; index < couponArray.length(); index++) {
        JSONObject coupon = couponArray.getJSONObject(index);
        LoyaltyProgramCoupon lpc = new LoyaltyProgramCoupon(coupon);
        mCoupons.append(lpc.getFlyerItemId(), lpc);
      }
    }
  }

  private void showItemDetails(ItemAttributes attributes) {
    if (attributes == null) {
      return;
    }
    JSONObject item = mItems.get(Integer.parseInt(attributes.getItemId()));
    if (item == null) {
      return;
    }
    try {
      switch (item.getInt("item_type")) {
        case 3: // video
          String videoUrl = item.getString("video_url");
          if (item.getInt("video_type") == 0) {
            Intent youtubeEmbeddedIntent = new Intent(Intent.ACTION_VIEW);
            youtubeEmbeddedIntent.setData(Uri.parse(videoUrl));
            startActivity(youtubeEmbeddedIntent);
          } else {
            Intent videoIntent = new Intent(this, VideoActivity.class);
            videoIntent.putExtra("videoUrl", videoUrl);
            startActivity(videoIntent);
          }
          break;
        case 5: // external link
          String itemUrl = item.getString("web_url");
          Intent browserIntent = new Intent(Intent.ACTION_VIEW);
          browserIntent.setData(Uri.parse(itemUrl));
          startActivity(browserIntent);
          break;
        case 7: // page anchor. This has been replaced by the anchor tag in sfml
          ViewHelper helper = HelperManager.getService(ViewHelper.class);
          View anchor = helper.findViewWithTag(mStorefront,
            attributes.getTargetAnchorId(),
            com.flipp.sfml.R.id.storefront_wayfinder_anchor_tag);
          if (anchor == null) {
            return;
          }
          int[] storefrontLocation = new int[2];
          HelperManager.getService(SFMLHelper.class)
            .getLocationInLayout(mStorefront, storefrontLocation);

          int[] anchorLocation = new int[2];
          HelperManager.getService(SFMLHelper.class).getLocationInLayout(anchor, anchorLocation);
          anchorLocation[0] = anchorLocation[0] - storefrontLocation[0];
          anchorLocation[1] = anchorLocation[1] - storefrontLocation[1];
          Rect location = new Rect(anchorLocation[0],
            anchorLocation[1],
            anchorLocation[0] + anchor.getWidth(),
            anchorLocation[1] + anchor.getHeight());

          mStorefront.smoothScrollToRect(location, false);
          break;
        case 15: // Iframe
          Intent iframeIntent = new Intent(this, IframeActivity.class);
          iframeIntent.putExtra("iframeUrl", item.getString("web_url"));
          startActivity(iframeIntent);
          break;
        case 25: //coupon
          Intent couponIntent = new Intent(this, CouponActivity.class);
          couponIntent.putExtra("couponId", item.getInt("id"));
          startActivity(couponIntent);
          break;
        default:
          Intent flyerItemIntent = new Intent(this, FlyerItemActivity.class);
          flyerItemIntent.putExtra("flyerItemId", item.getInt("id"));
          startActivity(flyerItemIntent);
      }
    } catch (JSONException ignored) {
    }

  }

  private void clipItem(ItemAttributes attributes) {
    if (attributes == null) {
      return;
    }
    JSONObject item = mItems.get(Integer.parseInt(attributes.getItemId()));
    if (item == null) {
      return;
    }

    try {
      HashMap<Long, JSONObject> clippings;
      if (attributes.getItemSource() == ItemSource.FLYER) {
        clippings = ClippingsManager.getInstance().getFlyerClippings();
      } else {
        clippings = ClippingsManager.getInstance().getFlyerEcomClippings();
      }
      long itemId = item.getLong("id");
      if (clippings.containsKey(itemId)) {
        clippings.remove(itemId);
      } else {
        clippings.put(itemId, item);
      }
      LocalBroadcastManager
        .getInstance(this)
        .sendBroadcast(new Intent(StorefrontImageView.CLIP_STATE_CHANGE_ACTION));
    } catch (JSONException e) {

    }
  }

  /**
   * SourceImageView.ClipStateDelegate
   */
  @Override
  public boolean isClipped(ItemAttributes itemAttributes) {
    HashMap<Long, JSONObject> clippings;
    if (itemAttributes.getItemSource() == ItemSource.FLYER) {
      clippings = ClippingsManager.getInstance().getFlyerClippings();
    } else {
      clippings = ClippingsManager.getInstance().getFlyerEcomClippings();
    }
    return clippings.containsKey(itemAttributes.getItemId());
  }

  /**
   * SourceImageView.MatchupDelegate
   */
  @Override
  public boolean hasMatchup(ItemAttributes itemAttributes) {
    LoyaltyProgramCoupon lpc = mCoupons.get(Integer.parseInt(itemAttributes.getItemId()));
    return lpc != null;
  }

  @Override
  public Drawable overrideMatchupIcon(ItemAttributes itemAttributes) {
    if (isClipped(itemAttributes)) {
      return getDrawable(R.drawable.custom_matchup);
    }
    return null;
  }

  /**
   * StorefrontItemAtomViewHolder.ItemAtomClickListener
   */
  @Override
  public void onItemAtomClick(StorefrontItemAtomViewHolder storefrontItemAtomViewHolder) {
    showItemDetails(storefrontItemAtomViewHolder.getItemAttributes());
  }

  @Override
  public boolean onItemAtomLongClick(StorefrontItemAtomViewHolder storefrontItemAtomViewHolder) {
    clipItem(storefrontItemAtomViewHolder.getItemAttributes());
    return true;
  }

  /**
   * ZoomScrollView.OnZoomListener
   */
  @Override
  public void onZoomChange(float v) {

  }

  @Override
  public void onPanChange(boolean b, boolean b1, float v, float v1, float v2, float v3) {

  }

  @Override
  public void forceRemeasure() {

  }

  /**
   * StorefrontAnalyticsManager.AnalyticsEventsListener
   */
  @Override
  public void onWayfinderCategoriesVisibilityChange(boolean b,
                                                    Wayfinder.WayfinderCategory wayfinderCategory) {
    if (b) {
      String message = "Analytic: WayfinderOpen: " + wayfinderCategory;
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
      Log.e(TAG, message);
    }
  }

  @Override
  public void onWayfinderCategorySelected(@Nullable Wayfinder.WayfinderCategory wayfinderCategory) {
    String message = "Analytic: WayfinderCategorySelect: " + wayfinderCategory;
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    Log.e(TAG, message);
  }

  @Override
  public void onEngagedVisit() {
    String message = "Analytic: EngagedVisit";
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    Log.e(TAG, message);
  }

  @Override
  public void onItemImpression(List<ItemAttributes> list) {
    String message = "Analytic: ItemImpression: " + list;
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    Log.e(TAG, message);
  }

  @Override
  public void onStorefrontOpen() {
    String message = "Analytic: StorefrontOpen";
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    Log.e(TAG, message);
  }
}
