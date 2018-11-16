package jp.shiita.yorimichi.ui.tutorial

import com.stephentuso.welcome.BasicPage
import com.stephentuso.welcome.TitlePage
import com.stephentuso.welcome.WelcomeActivity
import com.stephentuso.welcome.WelcomeConfiguration
import jp.shiita.yorimichi.R

class TutorialActivity : WelcomeActivity() {
    override fun configuration(): WelcomeConfiguration = WelcomeConfiguration.Builder(this)
            .defaultBackgroundColor(R.color.colorPrimary)
            .page(BasicPage(R.drawable.ic_tutorial1, getString(R.string.tutorial_title1), getString(R.string.tutorial_description1))
                    .background(R.color.colorTutorial1))
            .page(BasicPage(R.drawable.ic_tutorial2, getString(R.string.tutorial_title2), getString(R.string.tutorial_description2))
                    .background(R.color.colorTutorial2))
            .page(BasicPage(R.drawable.ic_tutorial3, getString(R.string.tutorial_title3), getString(R.string.tutorial_description3))
                    .background(R.color.colorTutorial3))
            .page(TitlePage(R.drawable.ic_tutorial4, getString(R.string.tutorial_title4))
                    .background(R.color.colorTutorial4))
            .swipeToDismiss(true)
            .exitAnimation(android.R.anim.fade_out)
            .build()
}