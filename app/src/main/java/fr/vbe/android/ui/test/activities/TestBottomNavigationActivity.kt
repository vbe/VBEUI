package fr.vbe.android.ui.test.activities

import android.os.Bundle
import android.widget.TextView
import fr.vbe.android.ui.activities.BottomNavigationActivity

class TestBottomNavigationActivity : BottomNavigationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container.addView(TextView(this).also { it.text = "CONTENT" })
    }

}