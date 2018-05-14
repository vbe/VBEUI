package fr.vbe.android.ui.coordinator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.support.annotation.CallSuper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver

class Coordinator internal constructor(
        val scrollingView: View,
        val bottomViews: List<View>,
        val behaviors: Map<View, (Movement) -> (Action?)>

) : ViewTreeObserver.OnScrollChangedListener {
    var previousScroll = 0
    var isDoingAnimation = false
    val viewInfos = mutableMapOf<View, ViewInfo>()

    init {
        scrollingView.viewTreeObserver.addOnScrollChangedListener(this)
        bottomViews.forEach { viewInfos[it] = ViewInfo(State.VISIBLE, -1f, it.layoutParams.height) }
    }



    override fun onScrollChanged() {
        val scrollY = scrollingView.scrollY
        if (scrollY < 0) {
            if (DEBUG) Log.d(LOG_TAG, "[onScrollChanged] Scrolling ignored (negative) $scrollY")
            return
        }
        if (isDoingAnimation) {
            if (DEBUG) Log.d(LOG_TAG, "[onScrollChanged] Animation in progress")
            previousScroll = scrollY
        }
        if (Math.abs(scrollY - previousScroll) < SCROLLING_THRESHOLD) {
            if (DEBUG) Log.d(LOG_TAG, "[onScrollChanged] Scrolling ignored (insufficient) $scrollY - $previousScroll = ${Math.abs(scrollY - previousScroll)}")
            return
        }

        if (DEBUG) Log.d(LOG_TAG, "[onScrollChanged] New scroll:$scrollY / previous:$previousScroll")
        when {
            previousScroll < scrollY -> handleMovement(Down(scrollY - previousScroll))
            previousScroll > scrollY -> handleMovement(Up(previousScroll - scrollY))
        }
        previousScroll = scrollY
    }

    private fun handleMovement(movement: Movement) {
        if (DEBUG) Log.d(LOG_TAG, "[handleMovement] $movement")
        bottomViews.forEach { view ->
            val action = behaviors[view]?.invoke(movement)
            if (action != null) {
                executeAction(action, view, Position.BOTTOM)
            }
        }
    }

    private fun executeAction(action: Action, view: View, position: Position) {
        if (DEBUG) Log.d(LOG_TAG, "[executeAction] ${action::class.java.simpleName} on ${view::class.java.simpleName} at $position")
        when (action) {
            is Action.Hide -> executeHide(view, position)
            is Action.Show -> executeShow(view, position)
        }
    }

    private fun executeHide(view: View, position: Position) {
        val viewInfo = viewInfos[view]
        if (viewInfo == null || viewInfo.state == State.HIDDEN) return
        if (DEBUG) Log.d(LOG_TAG, "[executeHide] on ${view::class.java.simpleName} at $position")
        // view infos initial height initialization
        if (viewInfo.initialHeight == -1f) {
            viewInfo.initialHeight = view.height.toFloat()
        }

        val animation = ValueAnimator.ofFloat(0f, viewInfo.initialHeight)
        animation.addUpdateListener { updatedAnimation ->
            isDoingAnimation = true
            val animatedValue = updatedAnimation.animatedValue as Float
            view.translationY = animatedValue
            view.layoutParams = view.layoutParams.also { it.height = (viewInfo.initialHeight - animatedValue).toInt() }
        }
        animation.addListener(CoordinatorAnimatorListenerAdapter())
        animation.start()

        viewInfo.state = State.HIDDEN
    }

    private fun executeShow(view: View, position: Position) {
        val viewInfo = viewInfos[view]
        if (viewInfo == null || viewInfo.state == State.VISIBLE) return
        if (DEBUG) Log.d(LOG_TAG, "[executeShow] on ${view::class.java.simpleName} at $position")

        val animation = ValueAnimator.ofFloat(viewInfo.initialHeight, 0f)
        animation.addUpdateListener { updatedAnimation ->
            isDoingAnimation = true
            val animatedValue = updatedAnimation.animatedValue as Float
            view.translationY = animatedValue
            view.layoutParams = view.layoutParams.also { it.height = (viewInfo.initialHeight - animatedValue).toInt() }
        }
        animation.addListener(object : CoordinatorAnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                view.layoutParams = view.layoutParams.also { it.height = viewInfo.initialHeightParam }
            }
        })
        animation.start()

        viewInfo.state = State.VISIBLE
    }

    open inner class CoordinatorAnimatorListenerAdapter : AnimatorListenerAdapter() {
        @CallSuper
        override fun onAnimationEnd(animation: Animator?) {
            isDoingAnimation = false
        }

        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
            isDoingAnimation = false
        }
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

    data class ViewInfo(
            var state: State,
            var initialHeight: Float,
            var initialHeightParam: Int)

    enum class State {
        HIDDEN, VISIBLE
    }



//    sealed class Speed(factor: Float) {
//        TimedSpeed()
//    }


    sealed class Action {
        class Hide : Action()
        class Show : Action()
    }

    companion object {
        const val DEBUG = true
        const val LOG_TAG = "Coordinator"
        const val SCROLLING_THRESHOLD = 20
    }
}



sealed class Movement(open val distance: Int)
data class Down(override val distance: Int) : Movement(distance)
data class Up(override val distance: Int) : Movement(distance)