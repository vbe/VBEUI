package fr.vbe.android.ui.test.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import fr.vbe.android.ui.activities.VBEBottomNavigationActivity
import fr.vbe.android.ui.coordinator.Coordinator
import fr.vbe.android.ui.coordinator.Down
import fr.vbe.android.ui.coordinator.Up
import fr.vbe.android.ui.test.R

class TestBottomNavigationActivity : VBEBottomNavigationActivity() {

    /**
     * Don't forget to put ids in the menu items otherwise the selection callback won't be called
     */
    override fun menuRes() = R.menu.test_bottom_navigation_activity

    override fun itemTextColor() = R.color.bottom_tab_text_color
    override fun itemIconTint() = R.color.bottom_tab_text_color
    override fun itemBackgroundResource() = R.drawable.bottom_tabs_backround

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)

        scrollView.addView(LinearLayout(this).also {
            it.orientation = LinearLayout.VERTICAL
            for (i in 0..100) {
                it.addView(TextView(this@TestBottomNavigationActivity).also {
                    it.text = "Coucou $i"
                })
            }
        })

        setContent(scrollView)

        Coordinator.Builder(scrollView)
                .addBottomView(navigation, { when (it) {
                    is Down -> Coordinator.Action.Hide()
                    is Up -> Coordinator.Action.Show()
                }})
                .build()
    }


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