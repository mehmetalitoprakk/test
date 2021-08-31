package com.motive.cimbomes.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.*
import com.motive.cimbomes.R

class KvkkActivity : AppCompatActivity() {
    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kvkk)

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                println("reklama tıknaldı")
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }

    }
}