package jp.shiita.yorimichi.ui.tutorial

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.stephentuso.welcome.WelcomePage
import com.stephentuso.welcome.WelcomeUtils
import jp.shiita.yorimichi.R


class TutorialFragment : Fragment(), WelcomePage.OnChangeListener {
    private lateinit var root: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.frag_tutorial, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root.findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.chick)
    }

    override fun onWelcomeScreenPageScrolled(pageIndex: Int, offset: Float, offsetPixels: Int) {
        WelcomeUtils.applyParallaxEffect(root, true, offsetPixels, 0.3f, 0.2f)
    }

    override fun onWelcomeScreenPageSelected(pageIndex: Int, selectedPageIndex: Int) {
        //Not used
    }

    override fun onWelcomeScreenPageScrollStateChanged(pageIndex: Int, state: Int) {
        //Not used
    }
}