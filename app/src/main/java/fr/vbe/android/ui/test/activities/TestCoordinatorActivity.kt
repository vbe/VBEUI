package fr.vbe.android.ui.test.activities

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import android.widget.TextView
import fr.vbe.android.ui.coordinator.Coordinator
import fr.vbe.android.ui.coordinator.Down
import fr.vbe.android.ui.coordinator.Movement
import fr.vbe.android.ui.coordinator.Up

class TestCoordinatorActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val configuration = intent.extras

        var topView: View? = null
        var bottomView: View? = null
        var scrollView: ScrollView? = null

        setContentView(LinearLayout(this).also {
            it.orientation = LinearLayout.VERTICAL

            if (configuration.getBoolean(EXTRA_HAS_TOP_VIEW)) {
                topView = TextView(this).also {
                    it.textSize = 20F
                    it.gravity = Gravity.CENTER
                    it.setBackgroundColor(Color.RED)
                    it.text = "TOP VIEW"
                }
                it.addView(topView, LayoutParams(LayoutParams.MATCH_PARENT, 200))
            }

            scrollView = ScrollView(this).also {
                it.addView(LinearLayout(this).also {
                    it.orientation = LinearLayout.VERTICAL
                    for (i in 0..100) {
                        it.addView(TextView(this).also { it.text = "Coucou $i" })
                    }
                })
            }
            it.addView(scrollView, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

            if (configuration.getBoolean(EXTRA_HAS_BOTTOM_VIEW)) {
                bottomView = TextView(this).also {
                    it.textSize = 20F
                    it.gravity = Gravity.CENTER
                    it.setBackgroundColor(Color.RED)
                    it.text = "BOTTOM VIEW"
                }
                it.addView(bottomView, LayoutParams(LayoutParams.MATCH_PARENT, 200))
            }
        })

        scrollView?.let {
            val coordinatorBuilder = Coordinator.Builder(it)
            topView?.let {  }
            bottomView?.let { coordinatorBuilder.addBottomView(it) { when(it) {
                is Down -> Coordinator.Action.Hide()
                is Up -> Coordinator.Action.Show()
            }}}
            coordinatorBuilder.build()
        }




    }





    companion object {
        const val EXTRA_HAS_TOP_VIEW = "EXTRA_HAS_TOP_VIEW"
        const val EXTRA_HAS_BOTTOM_VIEW = "EXTRA_HAS_BOTTOM_VIEW"
    }


}