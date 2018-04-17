package fr.vbe.android.ui.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.MenuRes
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import fr.vbe.android.ui.R
import fr.vbe.android.ui.databinding.VbeActivityBottomNavigationBinding

abstract class VBEBottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: VbeActivityBottomNavigationBinding

    val container by lazy {
        binding.vbeContainer
    }
    val navigation by lazy {
        binding.vbeNavigation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.vbe_activity_bottom_navigation)
        navigation.inflateMenu(menuRes())

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

    @MenuRes
    abstract fun menuRes(): Int

    abstract fun onNavigationItemSelected(item: MenuItem, userAction: Boolean): Boolean
    open fun onNavigationItemReselected(item: MenuItem) {}
}