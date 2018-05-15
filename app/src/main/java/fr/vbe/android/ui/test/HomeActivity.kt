package fr.vbe.android.ui.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import fr.vbe.android.ui.test.activities.TestCoordinatorConfigurationActivity
import kotlin.reflect.KClass

/**
 * Created by Vincent on 4/15/2018.
 */
class HomeActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        TestCoordinatorConfigurationActivity::class.create(this)
    }
}

fun <A: Activity> KClass<A>.create(context: Context, bundle: Bundle? = null) {
    context.startActivity(Intent(context, this.java).also {
        if (bundle != null) it.putExtras(bundle)
    })
}