package fr.vbe.android.ui.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import fr.vbe.android.base.text.VBELabel
import fr.vbe.android.ui.dialog.VBEDialog
import fr.vbe.android.ui.test.activities.TestBottomNavigationActivity
import kotlin.reflect.KClass

/**
 * Created by Vincent on 4/15/2018.
 */
class HomeActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        VBEDialog(this,
                title = VBELabel(R.string.app_name),
                message = VBELabel("Do you want to start TestBottomNavigationActivity?"),
                positive = VBEDialog.ButtonConfig("Yes", { TestBottomNavigationActivity::class.create(this) }),
                negative = VBEDialog.ButtonConfig("Nope", { Toast.makeText(this, "Well ok", Toast.LENGTH_SHORT).show() }),
                neutral = VBEDialog.ButtonConfig("Hmmm", { Toast.makeText(this, "ok switzerland", Toast.LENGTH_SHORT).show() })
        ).show()
    }
}

fun <A: Activity> KClass<A>.create(context: Context) {
    context.startActivity(Intent(context, this.java))
}