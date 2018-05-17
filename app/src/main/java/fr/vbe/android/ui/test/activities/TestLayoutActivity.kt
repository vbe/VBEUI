package fr.vbe.android.ui.test.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import fr.vbe.android.ui.layout.Layout

class TestLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TestLayout(this).build())
    }

}

class TestLayout(context: Context) : Layout(context, {
    textView {
        text = "Coucou"
    }
})