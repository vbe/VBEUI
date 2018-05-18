package fr.vbe.android.ui.layout

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

abstract class Layout<out LH: LayoutHolder>(context: Context, val holder: LH, val content: LayoutBuilder.() -> Unit) {
    private val builder = LayoutBuilder(context)

    fun build(): LH {
        content(builder)
        holder.root = builder.view ?: throw IllegalStateException("Layout $this is empty.")
        return holder
    }
}

abstract class LayoutHolder {
    var root: View? = null
}

open class LayoutBuilder(val context: Context) {
    var view: View? = null

    open fun addView(view: View) {
        this.view = view
    }

    private fun <V: View> V.alsoConfigureAndAdd(config: V.() -> Unit) = this.also {
        config(it)
        addView(it)
    }

    fun textView(config: TextView.() -> Unit): TextView = TextView(context).alsoConfigureAndAdd(config)

    fun button(config: Button.() -> Unit): Button = Button(context).alsoConfigureAndAdd(config)

    fun linearLayout(config: LinearLayout.() -> Unit, content: GroupLayoutBuilder.() -> Unit): LinearLayout {
        val linear = LinearLayout(context).also(config)
        val groupLayoutBuilder = GroupLayoutBuilder(context).also(content)
        groupLayoutBuilder.views.forEach { linear.addView(it) }
        addView(linear)
        return linear
    }
}

class GroupLayoutBuilder(context: Context) : LayoutBuilder(context) {
    val views = mutableListOf<View>()

    override fun addView(view: View) {
        views.add(view)
    }
}