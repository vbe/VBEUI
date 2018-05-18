package fr.vbe.android.ui.test.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import fr.vbe.android.ui.layout.Layout
import fr.vbe.android.ui.layout.LayoutHolder

class TestLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutHolder = TestLayout(this).build()
        setContentView(layoutHolder.root)

        with(layoutHolder) {
            button.setOnClickListener {
                content.text = "CONTENT CHANGED !!!"
            }
        }
    }

}

class TestLayoutHolder : LayoutHolder() {
    lateinit var tabs: View
    lateinit var content: TextView
    lateinit var button: Button
}

class TestLayout(context: Context, holder: TestLayoutHolder = TestLayoutHolder()) : Layout<TestLayoutHolder>(context, holder, {
    linearLayout({orientation = LinearLayout.VERTICAL}) {
        holder.tabs = linearLayout({orientation = LinearLayout.HORIZONTAL}) {
            textView { text = "tab 1" }
            textView { text = "tab 2" }
            textView { text = "tab 3" }
            textView { text = "tab 4" }
        }

        holder.content = textView {
            text = "SOME CONTENT"
        }

        holder.button = button {
            text = "CHANGE TEXT"
        }
    }
})