package com.vinaykpro.deathnotes

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

const val AD_ID = "ca-app-pub-2813592783630195/7076877373"

class AppOpenAdManager {
    private var loadTime: Long = 0
    var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) return

        isLoadingAd = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_ID,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(p0: AppOpenAd) {
                    Log.i("AppOpenAd", "AD loaded syccessfully")
                    super.onAdLoaded(p0)
                    appOpenAd = p0
                    isLoadingAd = false
                    loadTime = Date().time
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.i("AppOpenAd", p0.message)
                    isLoadingAd = false
                }
            }
        )
    }
    private fun loadTimeLessThanHour( n: Long): Boolean {
        val diff: Long = Date().time - loadTime
        val millisPerHour: Long = 3_600_000
        return diff < millisPerHour * n
    }
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && loadTimeLessThanHour(4)
    }
    fun showAd(activity: MainActivity) {
        if(isShowingAd) return
        Log.i("AppOpenAd", "Trying to show ad")
        if(!isAdAvailable()) {
            loadAd(activity)
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                Log.i("AppOpenAd", p0.message)
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                Log.i("AppOpenAd", "Ad shown")
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
    }
}