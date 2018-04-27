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
import kotlin.reflect.KClass

/**
 * Created by Vincent on 4/15/2018.
 */
class HomeActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("Do you want to start TestBottomNavigationActivity?")
                .setPositiveBtn("Yes", { TestBottomNavigationActivity::class.create(this) })
                .setNegativeBtn("Nope", { Toast.makeText(this, "Well ok", Toast.LENGTH_SHORT).show() })
                .setNeutralBtn("Hmmm", { Toast.makeText(this, "ok switzerland", Toast.LENGTH_SHORT).show() })
                .show()
    }
}

fun <A: Activity> KClass<A>.create(context: Context) {
    context.startActivity(Intent(context, this.java))
}