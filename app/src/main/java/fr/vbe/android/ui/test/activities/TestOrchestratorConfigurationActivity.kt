package fr.vbe.android.ui.test.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import fr.vbe.android.ui.test.create

class TestOrchestratorConfigurationActivity : AppCompatActivity() {

    val configuration = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(LinearLayout(this).also {
            it.orientation = LinearLayout.VERTICAL

            it.addView(checkBoxLine("Top view") {
                configuration.putBoolean(TestOrchestratorActivity.EXTRA_HAS_TOP_VIEW, it)
            })

            it.addView(checkBoxLine("Bottom view") {
                configuration.putBoolean(TestOrchestratorActivity.EXTRA_HAS_BOTTOM_VIEW, it)
            })

            it.addView(Button(this).also {
                it.text = "Start"
                it.setOnClickListener { TestOrchestratorActivity::class.create(this, configuration) }
            })
        })

        TestOrchestratorActivity::class.create(this, configuration)

    }

    fun checkBoxLine(text: String, onToggled: (Boolean) -> Unit) = LinearLayout(this).also {
        it.orientation = LinearLayout.HORIZONTAL

        val checkBox = CheckBox(this).also { it.isClickable = false }

        it.addView(checkBox)
        it.addView(TextView(this).also { it.text = text })

        it.setOnClickListener {
            checkBox.toggle()
            onToggled(checkBox.isChecked)
        }
    }



}