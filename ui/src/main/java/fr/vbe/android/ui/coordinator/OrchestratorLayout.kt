package fr.vbe.android.ui.coordinator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.support.annotation.CallSuper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import fr.vbe.android.ui.R

class OrchestratorLayout /*constructor(
        val scrollingView: View,
        val bottomViews: List<View>,
        val behaviors: Map<View, (Movement) -> (Action?)>

)*/ : FrameLayout, ViewTreeObserver.OnScrollChangedListener {

    // Scrolling content
    private var contentId: Int = -1
    private val content by lazy {
        findViewById<View>(contentId).also {
            it.viewTreeObserver.addOnScrollChangedListener(this)
        }
    }

    // Views reacting to scrolling content movements
    private val reactingViews = Movement.allClasses().associate { Pair(it, mutableSetOf<View>()) }

    private val viewInfos = mutableMapOf<View, ViewInfo>()


    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {

        if (attributeSet != null) {
            val array = context.theme.obtainStyledAttributes(attributeSet, R.styleable.OrchestratorLayout, defStyleAttr, 0)
            contentId = array.getResourceId(R.styleable.OrchestratorLayout_Orchestrator_content, -1)
            array.recycle()
        }

        if (contentId == -1) throw IllegalArgumentException("Must specify a content")
    }


    // ===============
    //region Layouting

    var isFirstLayoutPass = true

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        logIfDebug("onMeasure|==========| w=$widthMeasureSpec h=$heightMeasureSpec")

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        logIfDebug("onLayout|==========| chd?$changed l=$left t=$top r=$right b=$bottom")

        if (isFirstLayoutPass) doFirstPass()

        val totalHeight = bottom - top
        // first computing the height of the scrolling content
        // which is the total height minus the aggregate height of all other children
        val contentHeight = totalHeight - children().filter { it != content }.sumBy { it.height }

        var childTop = top
        for (i in 0 until childCount) {
            val child = getChildAt(i)
//          logIfDebug("onLayout|| ch=${child::class.java.simpleName} ch.h=${child.height}")
            // computing the bottom position of the child, layouting the child, then updating
            // the top position of the next child
            val childBottom = childTop + if (child == content) contentHeight else child.height
            child.layout(left, childTop, right, childBottom)
            childTop = childBottom
        }
    }

    private fun doFirstPass() {
        logIfDebug("doFirstPass|==========|")
        for (child in children()) {
            if (child == content) continue

            val lp = child.layoutParams as LayoutParams

            lp.scrollDownAction?.let {
                reactingViews[Down::class]?.add(child)
            }

            lp.scrollUpAction?.let {
                reactingViews[Up::class]?.add(child)
            }

            viewInfos[child] = ViewInfo(State.VISIBLE, -1f, child.layoutParams.height)
        }
        isFirstLayoutPass = false
    }

    // TODO KTX
    fun children() = (0 until childCount).map { getChildAt(it) }

    //endregion Layouting
    // ==================


    //==========================
    //region Actions, Animations

    var previousScroll = 0
    var isDoingAnimation = false

    override fun onScrollChanged() {
        val scrollY = content.scrollY
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
        reactingViews[movement::class]?.forEach { view ->
            view.myLayoutParams().getAction(movement)?.let { executeAction(it, view) }
        }

//        bottomViews.forEach { view ->
//            val action = behaviors[view]?.invoke(movement)
//            if (action != null) {
//                executeAction(action, view, Position.BOTTOM)
//            }
//        }
    }



    private fun executeAction(action: Action, view: View) {
        if (DEBUG) Log.d(LOG_TAG, "[executeAction] ${action::class.java.simpleName} on ${view::class.java.simpleName}")
        when (action) {
            is Action.Hide -> executeHide(view)
            is Action.Show -> executeShow(view)
        }
    }

    private fun executeHide(view: View) {
        val viewInfo = viewInfos[view]
        if (viewInfo == null || viewInfo.state == State.HIDDEN) return
        if (DEBUG) Log.d(LOG_TAG, "[executeHide] on ${view::class.java.simpleName}")
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

    private fun executeShow(view: View) {
        val viewInfo = viewInfos[view]
        if (viewInfo == null || viewInfo.state == State.VISIBLE) return
        if (DEBUG) Log.d(LOG_TAG, "[executeShow] on ${view::class.java.simpleName}")

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


    //endregion Actions, Animations
    //=============================


    // ==================
    //region LayoutParams

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?) = p is LayoutParams

    private fun View.myLayoutParams() = this.layoutParams as LayoutParams

    class LayoutParams : FrameLayout.LayoutParams {
        // private raw attributes extracted from the layout
        private var scrollDownBehavior: Int = NOTHING
        private var scrollUpBehavior: Int = NOTHING
        // computed attributes
        val scrollDownAction by lazy { getAction(scrollDownBehavior) }
        val scrollUpAction by lazy { getAction(scrollUpBehavior) }


        constructor(width: Int, height: Int) : super(width, height)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            if (attrs != null) {
                val array = context.theme.obtainStyledAttributes(attrs, R.styleable.OrchestratorLayout_Layout, 0, 0)

                scrollDownBehavior = array.getInt(R.styleable.OrchestratorLayout_Layout_layout_scrollDown_behavior, NOTHING)
                scrollUpBehavior = array.getInt(R.styleable.OrchestratorLayout_Layout_layout_scrollUp_behavior, NOTHING)

                array.recycle()
            }
        }

        fun getAction(movement: Movement) = when(movement) {
            is Down -> scrollDownAction
            is Up -> scrollUpAction
        }

        private fun getAction(behavior: Int) = when (behavior) {
            BEHAVIOR_SHOW -> Action.Show()
            BEHAVIOR_HIDE -> Action.Hide()
            else -> null
        }

        companion object {
            const val NOTHING = -1
            const val BEHAVIOR_HIDE = 0
            const val BEHAVIOR_SHOW = 1
        }
    }

    //endregion LayoutParams
    // =====================


    fun logIfDebug(log: String) = if (DEBUG) Log.d(LOG_TAG, log) else 0












    init {
//        bottomViews.forEach { viewInfos[it] = ViewInfo(State.VISIBLE, -1f, it.layoutParams.height) }
    }


//    class Builder constructor(val scrollingView: View){
//        private val bottomViews = mutableListOf<View>()
//        private val behaviors = mutableMapOf<View, (Movement) -> (Action?)>()
//
//        fun build(): OrchestratorLayout {
//            return OrchestratorLayout(scrollingView, bottomViews, behaviors)
//        }
//
//        fun addBottomView(view: View, behavior: (Movement) -> (Action?)): Builder {
//            bottomViews.add(view)
//            behaviors[view] = behavior
//            return this
//        }
//    }


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

        companion object {
            fun all() = listOf(Hide(), Show())
        }
    }

    companion object {
        const val DEBUG = true
        const val LOG_TAG = "OrchestratorLayout"
        const val SCROLLING_THRESHOLD = 20
    }
}


sealed class Movement(open val distance: Int) {

    companion object {
        fun allClasses() = listOf(Down::class, Up::class)
    }
}
data class Down(override val distance: Int) : Movement(distance)
data class Up(override val distance: Int) : Movement(distance)