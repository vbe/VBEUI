package fr.vbe.android.ui.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import fr.vbe.android.ui.R
import fr.vbe.android.ui.databinding.VbeActivityToolbarBinding

/**
 * Created by Vincent on 4/20/2018.
 */
abstract class VBEToolbarActivity : AppCompatActivity() {

    private lateinit var binding: VbeActivityToolbarBinding

    private val _container by lazy {
        binding.vbeActivityToolbarContainer
    }

    val toolbar by lazy {
        binding.vbeActivityToolbarToolbar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.vbe_activity_toolbar)

        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(this, toolbarBackgroundColor))
        toolbar.setTitleTextColor(ContextCompat.getColor(this, toolbarTitleTextColor))
        toolbar.setSubtitleTextColor(ContextCompat.getColor(this, toolbarSubtitleTextColor))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toolbarTitle?.let { toolbar.title = it }
    }

    open fun getContainer(): ViewGroup = _container

    open fun setContent(view: View) {
        getContainer().removeAllViews()
        getContainer().addView(view)
    }

    @ColorRes open val toolbarBackgroundColor = R.color.vbeuiToolbarBackgroundColor
    @ColorRes val toolbarTitleTextColor = R.color.vbeuiToolbarTitleTextColor
    @ColorRes val toolbarSubtitleTextColor = R.color.vbeuiToolbarSubtitleTextColor
    open val toolbarTitle: String? = null
}