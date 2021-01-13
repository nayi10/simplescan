package com.bandughana.simplescan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;

import io.objectbox.Box;

public class HistoryActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recyclerView = findViewById(R.id.my_scans);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        AdView mAdView = findViewById(R.id.adViewHistory);
        if (!sharedPreferences.getBoolean("isUpgraded", false)) {
            MobileAds.initialize(this, initializationStatus -> {
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        }

        Box<ScanItem> scanItemBox = ObjectBox.get().boxFor(ScanItem.class);
        List<ScanItem> itemList;
        itemList = scanItemBox.getAll();

        if (itemList.size() == 0){
            ScanItem item = new ScanItem("No scan results has been saved");
            itemList.add(item);
        }

        ScanItemAdapter adapter = new ScanItemAdapter(itemList, this);

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
