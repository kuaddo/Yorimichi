package jp.shiita.yorimichi

import android.content.Context
import android.support.multidex.MultiDex
import com.chibatching.kotpref.Kotpref
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import jp.shiita.yorimichi.di.DaggerAppComponent

class YorimichiApp : DaggerApplication() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        AndroidThreeTen.init(this)
        FirebaseAuth.getInstance().signInAnonymously()
        MobileAds.initialize(this, getString(R.string.admob_app_id))
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
                .application(this)
                .build()
    }
}