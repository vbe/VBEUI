package fr.vbe.android.ui.test.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import fr.vbe.android.ui.layout.Layout

class TestLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TestLayout(this).build())
    }

}

class TestLayout(context: Context) : Layout(context, {
    linearLayout({orientation = LinearLayout.VERTICAL}) {
        linearLayout({orientation = LinearLayout.HORIZONTAL}) {
            textView { text = "tab 1" }
            textView { text = "tab 2" }
            textView { text = "tab 3" }
            textView { text = "tab 4" }
        }

        textView {
            text = "SOME CONTENT"
        }
    }
})