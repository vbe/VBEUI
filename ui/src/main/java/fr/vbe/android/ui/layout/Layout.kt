package fr.vbe.android.ui.layout

import android.content.Context
import android.view.View
import android.widget.TextView

abstract class Layout(context: Context, val content: LayoutBuilder.() -> Unit) {
    val builder = LayoutBuilder(context)

    fun build(): View {
        content(builder)
        return builder.view ?: throw IllegalStateException("Layout $this is empty.")
    }
}

class LayoutBuilder(val context: Context) {
    var view: View? = null

    fun textView(config: TextView.() -> Unit) {
        view = TextView(context).also(config)
    }
}