package fr.vbe.android.ui.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import fr.vbe.android.base.BaseActivity
import fr.vbe.android.ui.test.activities.TestBottomNavigationActivity
import fr.vbe.android.ui.test.activities.TestFABActivity
import kotlin.reflect.KClass

/**
 * Created by Vincent on 4/15/2018.
 */
class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestBottomNavigationActivity::class.create(this)
    }
}

fun <A: Activity> KClass<A>.create(context: Context) {
    context.startActivity(Intent(context, this.java))
}