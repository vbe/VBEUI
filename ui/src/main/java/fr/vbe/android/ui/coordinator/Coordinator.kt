package fr.vbe.android.ui.coordinator

import android.view.View

class Coordinator internal constructor(scrollingView: View) {




    class Builder internal constructor(val scrollingView: View){

        fun build(): Coordinator {
            return Coordinator(scrollingView)
        }

        companion object {
            fun with(scrollingView: View) = Builder(scrollingView)
        }

    }
}