package jp.shiita.yorimichi.ui.dialog

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import dagger.android.support.DaggerAppCompatDialogFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import jp.shiita.yorimichi.ui.main.MainViewModel
import javax.inject.Inject

class PointGetDialogFragment : DaggerAppCompatDialogFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var repository: YorimichiRepository
    @Inject lateinit var scheduler: BaseSchedulerProvider
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val gotPoints by lazy { arguments!!.getInt(ARGS_GOT_POINTS) }
    private val additionalPoints by lazy { arguments!!.getInt(ARGS_ADDITIONAL_POINTS) }
    private lateinit var rewardedVideoAd: RewardedVideoAd
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context)
        rewardedVideoAd.rewardedVideoAdListener = object : RewardedVideoAdListener{

            override fun onRewarded(reward: RewardItem) {
                repository.addPoints(UserInfo.userId, additionalPoints)
                        .subscribeOn(scheduler.io())
                        .observeOn(scheduler.ui())
                        .subscribeBy(
                                onSuccess = {
                                    UserInfo.points = it.points
                                    mainViewModel.updatePoints()
                                },
                                onError = {}
                        )
                        .addTo(disposables)
            }

            override fun onRewardedVideoAdLoaded() {}
            override fun onRewardedVideoAdClosed() {}
            override fun onRewardedVideoAdLeftApplication() {}
            override fun onRewardedVideoAdOpened() {}
            override fun onRewardedVideoCompleted() {}
            override fun onRewardedVideoStarted() {}
            override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {}
        }
        rewardedVideoAd.loadAd(getString(R.string.admob_reward_ad_unit_id), AdRequest.Builder().build())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(activity!!).also { d ->
            d.window?.run {
                requestFeature(Window.FEATURE_NO_TITLE)
                setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
                setBackgroundDrawableResource(R.color.colorTransparent)
            }
            d.setContentView(R.layout.frag_point_get_dialog)
            d.findViewById<TextView>(R.id.mainTextView).text = getString(R.string.dialog_point_get_main_message, gotPoints)
            d.findViewById<TextView>(R.id.movieTextView).text = getString(R.string.dialog_point_get_movie_message, additionalPoints)
            d.findViewById<View>(R.id.closeButton).setOnClickListener { dismiss() }
            d.findViewById<View>(R.id.watchMovieButton).setOnClickListener {
                if (rewardedVideoAd.isLoaded) {
                    rewardedVideoAd.show()
                }
            }
        }
    }

    override fun onResume() {
        rewardedVideoAd.resume(context)
        super.onResume()
    }

    override fun onPause() {
        rewardedVideoAd.pause(context)
        disposables.clear()
        super.onPause()
    }

    override fun onDestroy() {
        rewardedVideoAd.destroy(context)
        super.onDestroy()
    }

    companion object {
        val TAG: String = PointGetDialogFragment::class.java.simpleName
        private const val ARGS_GOT_POINTS = "argsGotPoints"
        private const val ARGS_ADDITIONAL_POINTS = "argsAdditionalPoints"

        fun newInstance(gotPoints: Int, additionalPoints: Int) = PointGetDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARGS_GOT_POINTS, gotPoints)
                putInt(ARGS_ADDITIONAL_POINTS, additionalPoints)
            }
        }
    }
}