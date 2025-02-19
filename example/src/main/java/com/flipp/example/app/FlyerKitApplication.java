package com.flipp.example.app;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.flipp.injectablehelper.ContextHelper;
import com.flipp.injectablehelper.HelperManager;

public class FlyerKitApplication extends Application {
  // Postal/ZIP code given by user (L1B9C3, 90210)
  String postalCode = "L4W1L6";
  // Store code selected by user
  String storeCode = "001";
  // Access token provided by Flipp
  final String accessToken = BuildConfig.FLYER_KIT_API_TOKEN;
  // Locale of user (en, fr, en-US, en-CA)
  final String locale = "en-CA";
  // Flipp's name identifier of merchant
  final String merchantIdentifier = "flippflyerkit";
  // Root URL of API calls
  final String rootUrl = "https://api.flipp.com/";
  // API version number (vX.X)
  final String apiVersion = "v4.0";
  // default flyer id
  final int defaultFlyerId = 788309;
  // Flipp's merchant ID
  final String merchantId = "4489";
  // Default loyalty card ID - should be provided by user
  final String loyaltyCardId = "3333";
  // Default loyalty card programID
  final String loyaltyCardProgramId = "1234567890";

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public void setStoreCode(String storeCode) {
    this.storeCode = storeCode;
  }


  @Override
  public void onCreate() {
    super.onCreate();
    Stetho.initialize(
        Stetho.newInitializerBuilder(this)
              .enableDumpapp(
                  Stetho.defaultDumperPluginsProvider(this))
              .enableWebKitInspector(
                  Stetho.defaultInspectorModulesProvider(this))
              .build());

    HelperManager.getService(ContextHelper.class).registerContext(this);
  }

}