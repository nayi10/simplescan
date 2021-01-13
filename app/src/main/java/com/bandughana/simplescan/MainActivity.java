package com.bandughana.simplescan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.objectbox.Box;
import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever;

import static com.bandughana.simplescan.Helper.copyText;
import static com.bandughana.simplescan.Helper.openSearch;
import static com.bandughana.simplescan.Helper.openUrl;
import static com.bandughana.simplescan.Helper.shareContent;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements BarcodeRetriever, PurchasesUpdatedListener {
    private View mContentControlView;
    private TextView barcodeResult;
    private Button btnOpenUrl;
    private TextView switchView;
    private SwitchCompat aSwitch;
    private Box<ScanItem> itemBox;
    private boolean autoOpenLinks;
    private boolean storeScans;
    private boolean multiScan;
    private long counter;
    private boolean forceSearchUrls;
    SharedPreferences sharedPreferences;
    InterstitialAd mInterstitialAd;
    BillingClient billingClient;
    PopupMenu popupMenu;
    String price;
    String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAka5uVRObh/sBdfGVyKy+Az8mRhT+3+oazjFV1Z/TX3SEUwVeS8bgNZwIJXMFFH6ktmVRMAPHb67t4u3AO9PvRMc6k/Kq6r8zKdhcTXEzqu6X8umbcOztY6SAZcUn4Zw/NqaWl1SJdDSGRmB/QYtb5W7WelIIKyf676BpbYI7WEqMTr8WTMMVYmkD+iPLHU+xa1/oW2HKtbn+RicuLOe/KpYL+cZABzMSZcRoZ5fKeDV6tQekSN0Z2f+sGi5AJl4hWZvOl22h4SyUWxdbZ/sNWyNyOZPVPCC9pHiAApb5MMwwDpl3kEoQMZgCg2Zr+ViJI8/CkEhXDCOZLoC5KCyUZwIDAQAB";
    private static final String TAG = "IABUtil/Security";
    List<String> skuList = new ArrayList<>();
    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        AdView mAdView = findViewById(R.id.adView);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isUpgraded = sharedPreferences.getBoolean("isUpgraded", false);

        if (!isUpgraded) {
            MobileAds.initialize(this, initializationStatus -> {
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        } else {
            mAdView.setVisibility(View.GONE);
        }
        hide();
        Button btnCopy = findViewById(R.id.btnCopy);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnMenu = findViewById(R.id.btnMenu);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnSearch = findViewById(R.id.btnSearch);
        ImageView viewFinder = findViewById(R.id.viewFinder);

        mContentControlView = findViewById(R.id.fullscreen_content_controls);
        barcodeResult = findViewById(R.id.barcodeResult);
        btnOpenUrl = findViewById(R.id.btnOpenUrl);
        switchView = findViewById(R.id.flashStart);
        aSwitch = findViewById(R.id.flashToggle);

        billingClient = BillingClient.newBuilder(getBaseContext())
                .setListener(this)
                .enablePendingPurchases()
                .build();
        skuList.add("ss_upgrade");

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    loadSkus();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });

        final BarcodeCapture barcodeCapture = (BarcodeCapture)
                getSupportFragmentManager().findFragmentById(R.id.barcode);

        boolean disableViewFinder = sharedPreferences.getBoolean("disable_view_finder", false);
        autoOpenLinks = sharedPreferences.getBoolean("auto_open_links", false);
        storeScans = sharedPreferences.getBoolean("store_scans", false);
        forceSearchUrls = sharedPreferences.getBoolean("force_search_urls", false);
        multiScan = sharedPreferences.getBoolean("multiple_scan", false);
        counter = sharedPreferences.getLong("usedCounter", 0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs1, key) -> {
            if (key != null) {
                recreate();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

        Objects.requireNonNull(barcodeCapture).setRetrieval(this);
        barcodeCapture.setShowFlash(false);

        if (disableViewFinder){
            viewFinder.setVisibility(View.GONE);
            barcodeCapture.shouldAutoFocus(true);
        }
        aSwitch.setTrackTintList(ColorStateList.valueOf(getResources()
                .getColor(R.color.colorPrimaryDark)));
        aSwitch.setThumbTintList(ColorStateList.valueOf(Color.LTGRAY));

        itemBox = ObjectBox.get().boxFor(ScanItem.class);

        btnSettings.setOnClickListener(v -> startActivity(
                new Intent(MainActivity.this, SettingsActivity.class)));
        popupMenu = new PopupMenu(this, btnMenu);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());
        popupMenu.getMenu().findItem(R.id.action_upgrade).setVisible(!isUpgraded);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.action_about:
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                    break;
                case R.id.action_rate_us:
                    Helper.rateUs(MainActivity.this);
                    break;
                case R.id.action_share:
                    Helper.shareApp(MainActivity.this, null);
                    break;
                default:
                    break;
            }
            return true;
        });

        btnMenu.setOnClickListener(v -> popupMenu.show());
        btnHistory.setOnClickListener(v -> startActivity(
                new Intent(MainActivity.this, HistoryActivity.class)));

        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Helper.supportsFlashLight(this)) {
                if (isChecked) {
                    barcodeCapture.setShowFlash(true);
                    aSwitch.setThumbTintList(ColorStateList.valueOf(getResources()
                            .getColor(R.color.color_green)));
                    switchView.setText(getResources().getString(R.string.flash_off));
                } else {
                    barcodeCapture.setShowFlash(false);
                    aSwitch.setThumbTintList(ColorStateList.valueOf(Color.LTGRAY));
                    switchView.setText(getResources().getString(R.string.flash_on));
                }
                barcodeCapture.refresh(true);
            } else {
                Toast.makeText(this, "Device doesn't support flash light",
                        Toast.LENGTH_LONG).show();
                aSwitch.setChecked(!isChecked);
            }
        });

        btnOpenUrl.setOnClickListener(v -> {
            if (URLUtil.isNetworkUrl(barcodeResult.getText().toString())){
                openUrl(barcodeResult.getText().toString(), MainActivity.this);
            } else if (barcodeResult.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "Nothing to navigate to",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "The text is not a link",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnSearch.setOnClickListener(v -> {
            if (barcodeResult.getText().toString().isEmpty()){
                return;
            }

            if (URLUtil.isNetworkUrl(barcodeResult.getText().toString())){
                if (forceSearchUrls) {
                    openSearch(barcodeResult.getText().toString(), MainActivity.this);
                } else {
                    openUrl(barcodeResult.getText().toString(), MainActivity.this);
                }
            } else {
                openSearch(barcodeResult.getText().toString(), MainActivity.this);
            }
        });

        btnCopy.setOnClickListener(v -> {
            if (!barcodeResult.getText().toString().isEmpty()) {
                copyText(MainActivity.this, barcodeResult.getText().toString());
                Toast.makeText(MainActivity.this, "Text copied successfully",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnShare.setOnClickListener(v -> shareContent(barcodeResult.getText().toString(), MainActivity.this));
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onRetrieved(final Barcode barcode) {
        if (storeScans) {
            ScanItem item = new ScanItem(barcode.displayValue);
            itemBox.put(item);
        }

        runOnUiThread(() -> {
            ++counter;
            sharedPreferences.edit().putLong("usedCounter", counter).apply();

            if (counter % 4 == 0){
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }

            barcodeResult.setText(barcode.displayValue);
            barcodeResult.setVisibility(View.VISIBLE);
            mContentControlView.setVisibility(View.VISIBLE);
            if (URLUtil.isNetworkUrl(barcode.displayValue)){
                btnOpenUrl.setEnabled(true);
                if (autoOpenLinks){
                    openUrl(barcode.displayValue, MainActivity.this);
                }
            }
        });
    }

    @Override
    public void onRetrievedMultiple(Barcode closetToClick, List<BarcodeGraphic> barcode) {

        if (multiScan) {
            final StringBuilder readValues = new StringBuilder();
            Box<ScanItem> itemBox = ObjectBox.get().boxFor(ScanItem.class);

            for (BarcodeGraphic barcodeGraphic : barcode) {
                if (storeScans) {
                    List<ScanItem> already = itemBox.getAll();
                    ScanItem item = new ScanItem(barcodeGraphic.getBarcode().displayValue);
                    for (ScanItem i : already) {
                        if (!i.equals(item)) {
                            itemBox.put(item);
                        }
                    }
                }
                readValues.append(barcodeGraphic.getBarcode().displayValue).append("\n");
            }
            runOnUiThread(() -> {
                ++counter;
                sharedPreferences.edit().putLong("usedCounter", counter).apply();

                if (counter % 4 == 0){
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    }
                }

                mContentControlView.setVisibility(View.VISIBLE);
                barcodeResult.setText(readValues);
                barcodeResult.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onRetrievedFailed(String reason) {
        Toast.makeText(this,
                "Cannot parse QRCode/Barcode. Try again or change an image",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionRequestDenied() {
        Toast.makeText(this,
                "Permission must be granted. Closing app now.",
                Toast.LENGTH_LONG).show();
        finish();
    }

    private void loadSkus() {
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    // Process the result.
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                        for (SkuDetails skuDetails : skuDetailsList) {
                            String sku = skuDetails.getSku();
                            price = skuDetails.getPrice();
                            if ("ss_upgrade".equals(sku)) {

                                popupMenu.getMenu().findItem(R.id.action_upgrade)
                                        .setOnMenuItemClickListener(item -> {
                                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                            .setSkuDetails(skuDetails)
                                            .build();
                                    billingClient.launchBillingFlow(MainActivity.this, flowParams);
                                    return true;
                                });
                            }
                        }
                    }
                });
    }

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            Log.e(TAG, "Got an exception trying to validate a purchase: " + e);
            return false;
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                    Log.i(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
                    return;
                } else {
                    handlePurchase(purchase);
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(this, "Transaction cancelled, please try again",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "An error occurred, please try again",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
            // Grant entitlement to the user.
            sharedPreferences.edit().putBoolean("isUpgraded", true)
                    .apply();
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
            Toast.makeText(this, getString(R.string.purchase_success_msg),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
