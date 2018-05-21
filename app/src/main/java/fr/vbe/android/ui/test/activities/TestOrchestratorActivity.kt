package fr.vbe.android.ui.test.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import fr.vbe.android.ui.test.R

class TestOrchestratorActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val configuration = intent.extras

        var topView: View? = null
        var bottomView: View? = null
        var scrollView: ScrollView? = null


        setContentView(when {
            else -> R.layout.orchestrator_top_and_bottom
        })

        findViewById<LinearLayout>(R.id.content).let {
            for (i in 0..100) {
                it.addView(TextView(this).also { it.text = "Coucou $i"})
            }
        }

//        scrollView?.let {
//            val coordinatorBuilder = OrchestratorLayout.Builder(it)
//            topView?.let {  }
//            bottomView?.let { coordinatorBuilder.addBottomView(it) { when(it) {
//                is Down -> OrchestratorLayout.Action.Hide()
//                is Up -> OrchestratorLayout.Action.Show()
//            }}}
//            coordinatorBuilder.build()
//        }




    }





    companion object {
        const val EXTRA_HAS_TOP_VIEW = "EXTRA_HAS_TOP_VIEW"
        const val EXTRA_HAS_BOTTOM_VIEW = "EXTRA_HAS_BOTTOM_VIEW"
    }


}