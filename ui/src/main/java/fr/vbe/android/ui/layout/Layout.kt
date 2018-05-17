package fr.vbe.android.ui.layout

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

abstract class Layout(context: Context, val content: LayoutBuilder.() -> Unit) {
    val builder = LayoutBuilder(context)

    fun build(): View {
        content(builder)
        return builder.view ?: throw IllegalStateException("Layout $this is empty.")
    }
}

open class LayoutBuilder(val context: Context) {
    var view: View? = null

    open fun addView(view: View) {
        this.view = view
    }

    fun textView(config: TextView.() -> Unit) {
        addView(TextView(context).also(config))
    }

    fun linearLayout(config: LinearLayout.() -> Unit, content: GroupLayoutBuilder.() -> Unit) {
        val linear = LinearLayout(context).also(config)
        val groupLayoutBuilder = GroupLayoutBuilder(context).also(content)
        groupLayoutBuilder.views.forEach { linear.addView(it) }
        addView(linear)
    }
}

class GroupLayoutBuilder(context: Context) : LayoutBuilder(context) {
    val views = mutableListOf<View>()

    override fun addView(view: View) {
        views.add(view)
    }
}