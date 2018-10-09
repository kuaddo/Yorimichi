package jp.shiita.yorimichi.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.shiita.yorimichi.R
import javax.inject.Inject

/**
 * 未実装用のFragment
 */
class BlankFragment @Inject constructor() : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_blank, container, false)
    }

    companion object {
        fun newInstance() = BlankFragment()
    }
}