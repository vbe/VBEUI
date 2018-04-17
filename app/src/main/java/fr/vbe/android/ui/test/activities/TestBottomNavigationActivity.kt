package fr.vbe.android.ui.test.activities

import android.os.Bundle
import android.widget.TextView
import fr.vbe.android.ui.activities.VBEBottomNavigationActivity
import fr.vbe.android.ui.test.R

class TestBottomNavigationActivity : VBEBottomNavigationActivity() {

    override fun menuRes() = R.menu.test_bottom_navigation_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container.addView(TextView(this).also { it.text = "CONTENT" })
    }

}