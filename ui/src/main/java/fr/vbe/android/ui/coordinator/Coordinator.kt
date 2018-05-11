package fr.vbe.android.ui.coordinator

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewTreeObserver

class Coordinator internal constructor(
        val scrollingView: View,
        val bottomViews: List<View>,
        val behaviors: Map<View, (Movement) -> (Action?)>

) : ViewTreeObserver.OnScrollChangedListener {
    var previousScroll = -1
    val viewInfos = mutableMapOf<View, ViewInfo>()

    init {
        scrollingView.viewTreeObserver.addOnScrollChangedListener(this)
        bottomViews.forEach { viewInfos[it] = ViewInfo(State.VISIBLE, 0f) }
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
        val viewInfo = viewInfos[view]
        if (viewInfo == null || viewInfo.state == State.HIDDEN) return
        viewInfo.initialHeight = view.height.toFloat()

        val animation = ValueAnimator.ofFloat(0f, viewInfo.initialHeight)
        animation.addUpdateListener { updatedAnimation ->
            val animatedValue = updatedAnimation.animatedValue as Float
            view.translationY = animatedValue
            view.layoutParams = view.layoutParams.also { it.height = (viewInfo.initialHeight - animatedValue).toInt() }
        }
        animation.start()

        viewInfo.state = State.HIDDEN
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

    data class ViewInfo(var state: State, var initialHeight: Float)

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