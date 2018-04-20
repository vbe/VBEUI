package fr.vbe.android.ui.activities

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.MenuRes
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import fr.vbe.android.ui.databinding.VbeActivityBottomNavigationBinding

abstract class VBEBottomNavigationActivity : VBEToolbarActivity() {

    private lateinit var binding: VbeActivityBottomNavigationBinding

    private val _container by lazy {
        binding.vbeActivityBottomNavigationContainer
    }
    val navigation by lazy {
        binding.vbeActivityBottomNavigationNavigation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VbeActivityBottomNavigationBinding.inflate(layoutInflater, super.getContainer(), false)
        super.getContainer().addView(binding.root)
        navigation.inflateMenu(menuRes())

        // Look and feel
        itemBackgroundResource()?.let {
            navigation.itemBackgroundResource = it
        }
        itemTextColor()?.let {
            navigation.itemTextColor = ContextCompat.getColorStateList(this, it)
        }
        itemIconTint()?.let {
            navigation.itemIconTintList = ContextCompat.getColorStateList(this, it)
        }


        // When only setting the item selected listener, the callback will be called every time
        // the user taps on the tab. But usually this activity will display one page per
        // navigation tab, and in that case we don't want the page to be recreated each time (and
        // having to manage a flag to handle that is a bit tedious)
        // So we also set the item reselected listener, doing nothing by default
        navigation.setOnNavigationItemSelectedListener { onNavigationItemSelected(it, true) }
        navigation.setOnNavigationItemReselectedListener { onNavigationItemReselected(it) }
        // indicating which one is the first selected tab
        onNavigationItemSelected(navigation.menu.findItem(navigation.selectedItemId), false)
    }

    override fun getContainer() = _container

    @MenuRes abstract fun menuRes(): Int

    @DrawableRes open fun itemBackgroundResource(): Int? = null

    /**
     * Note: selected tab state is state_checked="true"
     */
    @ColorRes open fun itemTextColor(): Int? = null
    /**
     * Note: selected tab state is state_checked="true"
     */
    @ColorRes open fun itemIconTint(): Int? = null

    abstract fun onNavigationItemSelected(item: MenuItem, userAction: Boolean): Boolean
    open fun onNavigationItemReselected(item: MenuItem) {}
}