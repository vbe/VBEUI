package fr.vbe.android.ui.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import fr.vbe.android.ui.extensions.setNegativeBtn
import fr.vbe.android.ui.extensions.setNeutralBtn
import fr.vbe.android.ui.extensions.setPositiveBtn
import fr.vbe.android.ui.test.activities.TestBottomNavigationActivity
import fr.vbe.android.ui.test.activities.TestLayoutActivity
import kotlin.reflect.KClass

/**
 * Created by Vincent on 4/15/2018.
 */
class HomeActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        TestLayoutActivity::class.create(this)
    }
}

fun <A: Activity> KClass<A>.create(context: Context) {
    context.startActivity(Intent(context, this.java))
}