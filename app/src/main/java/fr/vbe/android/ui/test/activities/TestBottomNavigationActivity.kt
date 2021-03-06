package fr.vbe.android.ui.test.activities

import android.view.MenuItem
import android.widget.TextView
import fr.vbe.android.ui.activities.VBEBottomNavigationActivity
import fr.vbe.android.ui.test.R

class TestBottomNavigationActivity : VBEBottomNavigationActivity() {

    /**
     * Don't forget to put ids in the menu items otherwise the selection callback won't be called
     */
    override fun menuRes() = R.menu.test_bottom_navigation_activity

    override fun itemTextColor() = R.color.bottom_tab_text_color
    override fun itemIconTint() = R.color.bottom_tab_text_color
    override fun itemBackgroundResource() = R.drawable.bottom_tabs_backround

    override fun onNavigationItemSelected(item: MenuItem, userAction: Boolean): Boolean {
        when (item.itemId) {
            R.id.menu_item_tata -> displayTata()
            R.id.menu_item_titi -> displayTiti()
            R.id.menu_item_toto -> displayToto()
        }
        return true
    }

    override val toolbarTitle = "TestBottomNavigationActivity"


    fun displayTata() {
        setContent(TextView(this).also { it.text = "TATA" })
    }

    fun displayTiti() {
        setContent(TextView(this).also { it.text = "TITI" })
    }

    fun displayToto() {
        setContent(TextView(this).also { it.text = "TOTO" })
    }

}