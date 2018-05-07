package fr.vbe.android.ui.coordinator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver

class Coordinator internal constructor(
        val scrollingView: View,
        val bottomViews: List<View>,
        val behaviors: Map<View, (Movement) -> (Action?)>

) : ViewTreeObserver.OnScrollChangedListener {
    var previousScroll = -1
    val viewStates = mutableMapOf<View, State>()

    init {
        scrollingView.viewTreeObserver.addOnScrollChangedListener(this)
        bottomViews.forEach { viewStates[it] = State.VISIBLE }
    }



    override fun onScrollChanged() {
        val scrollY = scrollingView.scrollY

        if (scrollY < 0) return
        if (previousScroll == -1) {
            previousScroll = scrollY
            return
        }

        when {
            previousScroll < scrollY -> handleMovement(Down(scrollY - previousScroll))
            previousScroll > scrollY -> handleMovement(Up(previousScroll - scrollY))
        }
        previousScroll = scrollY
    }

    private fun handleMovement(movement: Movement) {
        bottomViews.forEach { view ->
            val action = behaviors[view]?.invoke(movement)
            if (action != null) {
                executeAction(action, view, Position.BOTTOM)
            }
        }
    }

    private fun executeAction(action: Action, view: View, position: Position) {
        when (action) {
            is Action.Hide -> executeHide(view, position)
        }
    }

    private fun executeHide(view: View, position: Position) {
        if (viewStates[view] == State.HIDDEN) return
        view.animate()
                .translationYBy(view.height.toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        scrollingView.requestLayout()
                    }
                })
                .start()
        viewStates[view] = State.HIDDEN
    }


    class Builder internal constructor(val scrollingView: View){
        private val bottomViews = mutableListOf<View>()
        private val behaviors = mutableMapOf<View, (Movement) -> (Action?)>()

        fun build(): Coordinator {
            return Coordinator(scrollingView, bottomViews, behaviors)
        }

        fun addBottomView(view: View, behavior: (Movement) -> (Action?)): Builder {
            bottomViews.add(view)
            behaviors[view] = behavior
            return this
        }

        companion object {
            fun with(scrollingView: View) = Builder(scrollingView)
        }

    }


    enum class Position {
        BOTTOM, TOP
    }

    enum class State {
        HIDDEN, VISIBLE
    }



//    sealed class Speed(factor: Float) {
//        TimedSpeed()
//    }


    sealed class Action {
        class Hide : Action()
    }
}



sealed class Movement(open val distance: Int)
data class Down(override val distance: Int) : Movement(distance)
data class Up(override val distance: Int) : Movement(distance)