package fr.vbe.android.ui.coordinator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.CallSuper
import android.support.annotation.IdRes
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
    private val overChildrenOrganization = mutableListOf<List<View>>()


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
        // which is the total height minus the aggregate height of all other enclosing children
        val contentHeight = totalHeight - children()
                .filter { it != content && it.myLayoutParams().relation == LayoutParams.Relation.ENCLOSES }
                .sumBy { it.height }

        // first, layouting enclosing children and content
        var childTop = top
        var childBottom = 0
        val overChildren = mutableListOf<View>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.myLayoutParams().relation == LayoutParams.Relation.OVER) {
                // will draw the over children later on
                overChildren.add(child)
            }
            else {
//          logIfDebug("onLayout|| ch=${child::class.java.simpleName} ch.h=${child.height}")
                // computing the bottom position of the child, layouting the child, then updating
                // the top position of the next child
                childBottom = childTop + if (child == content) contentHeight else child.height
                child.layout(left, childTop, right, childBottom)
                childTop = childBottom
            }
        }
    }

    private fun doFirstPass() {
        logIfDebug("doFirstPass|==========|")
        val overChildren = mutableListOf<View>()
        val overChildrenById = mutableMapOf<Int, View>()
        // independent means the positioning is not linked to any other over view
        fun isIndependent(pos: LayoutParams.Positioning) = pos is LayoutParams.Positioning.None || pos is LayoutParams.Positioning.RelativeToContent
        val dealtOverChildren = mutableListOf<View>()
        val remainingOverChildren = mutableListOf<View>()

        for (child in children()) {
            if (child == content){
                continue
            }

            val lp = child.layoutParams as LayoutParams

            lp.scrollDownAction?.let {
                reactingViews[Down::class]?.add(child)
            }

            lp.scrollUpAction?.let {
                reactingViews[Up::class]?.add(child)
            }

            if (lp.relation == LayoutParams.Relation.OVER) {
                overChildren.add(child)
                overChildrenById[child.id] = child
                if (
                        isIndependent(lp.topPositioning) &&
                        isIndependent(lp.rightPositioning) &&
                        isIndependent(lp.leftPositioning) &&
                        isIndependent(lp.bottomPositioning)
                ) {
                    dealtOverChildren.add(child)
                }
                else {
                    remainingOverChildren.add(child)
                }
            }

            viewInfos[child] = ViewInfo(State.VISIBLE, -1f, child.layoutParams.height, Position.TOP)
        }

        // dealt with positioning means the positioning is either independent (not linked to another over view)
        // or the linked view's full positioning has been dealt with
        fun isPositioningDependencyDealtWith(pos: LayoutParams.Positioning) = isIndependent(pos) ||
                (pos is LayoutParams.Positioning.RelativeToView && dealtOverChildren.contains(overChildrenById[pos.ref]))
        // first layer of over children is the one that are fully independent
        overChildrenOrganization.add(List(dealtOverChildren.size, {dealtOverChildren[it]}))
        var security = 0
        while (remainingOverChildren.isNotEmpty() && security < 100) {
            val layer = mutableListOf<View>()
            remainingOverChildren.forEach { overChild ->
                val lp = overChild.myLayoutParams()
                if (
                        isPositioningDependencyDealtWith(lp.topPositioning) &&
                        isPositioningDependencyDealtWith(lp.rightPositioning) &&
                        isPositioningDependencyDealtWith(lp.bottomPositioning) &&
                        isPositioningDependencyDealtWith(lp.leftPositioning)
                ) {
                    layer.add(overChild)
                }
            }

            remainingOverChildren.removeAll(layer)
            overChildrenOrganization.add(layer)
            dealtOverChildren.addAll(layer)
            security++
        }

        if (remainingOverChildren.isNotEmpty()) throw IllegalArgumentException("Could not layout over children properly.")

        isFirstLayoutPass = false
    }

    // TODO KTX
    fun children() = (0 until childCount).map { getChildAt(it) }

    //endregion Layouting
    // ==================


    //==========================
    //region Actions, Animations

    var previousScroll = 0
    var animationCounter = 0
    fun isDoingAnimation() = animationCounter > 0

    override fun onScrollChanged() {
        val scrollY = content.scrollY
        if (scrollY < 0) {
            if (DEBUG) Log.d(LOG_TAG, "[onScrollChanged] Scrolling ignored (negative) $scrollY")
            return
        }
        if (isDoingAnimation()) {
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

        val factor = if (viewInfo.position == Position.TOP) -1 else 1

        val animation = ValueAnimator.ofFloat(0f, factor * viewInfo.initialHeight)
        animation.addUpdateListener { updatedAnimation ->
            val animatedValue = updatedAnimation.animatedValue as Float
            view.translationY = animatedValue
            view.layoutParams = view.layoutParams.also { it.height = (viewInfo.initialHeight - factor * animatedValue).toInt() }
        }
        animation.addListener(CoordinatorAnimatorListenerAdapter())
        animationCounter++
        animation.start()

        viewInfo.state = State.HIDDEN
    }

    private fun executeShow(view: View) {
        val viewInfo = viewInfos[view]
        if (viewInfo == null || viewInfo.state == State.VISIBLE) return
        if (DEBUG) Log.d(LOG_TAG, "[executeShow] on ${view::class.java.simpleName}")

        val factor = if (viewInfo.position == Position.TOP) -1 else 1

        val animation = ValueAnimator.ofFloat(factor * viewInfo.initialHeight, 0f)
        animation.addUpdateListener { updatedAnimation ->
            val animatedValue = updatedAnimation.animatedValue as Float
            view.translationY = animatedValue
            view.layoutParams = view.layoutParams.also { it.height = (viewInfo.initialHeight - factor * animatedValue).toInt() }
        }
        animation.addListener(object : CoordinatorAnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                view.layoutParams = view.layoutParams.also { it.height = viewInfo.initialHeightParam }
            }
        })
        animationCounter++
        animation.start()

        viewInfo.state = State.VISIBLE
    }

    open inner class CoordinatorAnimatorListenerAdapter : AnimatorListenerAdapter() {
        @CallSuper
        override fun onAnimationEnd(animation: Animator?) {
            animationCounter--
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
        private var relationToContent: Int = RELATION_ENCLOSES
        private var scrollDownBehavior: Int = NOTHING
        private var scrollUpBehavior: Int = NOTHING
        private var toTopOf: Positioning = Positioning.None()
        private var toLeftOf: Positioning = Positioning.None()
        private var toBottomOf: Positioning = Positioning.None()
        private var toRightOf: Positioning = Positioning.None()

        // computed attributes
        val scrollDownAction by lazy { getAction(scrollDownBehavior) }
        val scrollUpAction by lazy { getAction(scrollUpBehavior) }
        val relation by lazy {
            when (relationToContent) {
                RELATION_OVER -> Relation.OVER
                else -> Relation.ENCLOSES
            }
        }
        val topPositioning by lazy { toTopOf }
        val leftPositioning by lazy { toLeftOf }
        val bottomPositioning by lazy { toBottomOf }
        val rightPositioning by lazy { toRightOf }


        constructor(width: Int, height: Int) : super(width, height)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            if (attrs != null) {
                val array = context.theme.obtainStyledAttributes(attrs, R.styleable.OrchestratorLayout_Layout, 0, 0)

                relationToContent = array.getInt(R.styleable.OrchestratorLayout_Layout_relationToContent, RELATION_ENCLOSES)
                scrollDownBehavior = array.getInt(R.styleable.OrchestratorLayout_Layout_whenContentScrollsDown, NOTHING)
                scrollUpBehavior = array.getInt(R.styleable.OrchestratorLayout_Layout_whenContentScrollsUp, NOTHING)

                fun getPositioning(array: TypedArray, index: Int): Positioning {
                    val resId = array.getResourceId(index, NOTHING)
                    return when {
                        resId != NOTHING -> Positioning.RelativeToView(resId)
                        array.getInt(index, NOTHING) == POSITION_RELATIVE_TO_CONTENT -> Positioning.RelativeToContent()
                        else -> Positioning.None()
                    }
                }

                toTopOf = getPositioning(array, R.styleable.OrchestratorLayout_Layout_toTopOf)
                toLeftOf = getPositioning(array, R.styleable.OrchestratorLayout_Layout_toLeftOf)
                toBottomOf = getPositioning(array, R.styleable.OrchestratorLayout_Layout_toBottomOf)
                toRightOf = getPositioning(array, R.styleable.OrchestratorLayout_Layout_toRightOf)

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

            const val RELATION_ENCLOSES = 0
            const val RELATION_OVER = 1

            const val BEHAVIOR_HIDE = 0
            const val BEHAVIOR_SHOW = 1

            const val POSITION_RELATIVE_TO_CONTENT = 1
        }

        sealed class Positioning {
            class None : Positioning()
            class RelativeToContent : Positioning()
            class RelativeToView(@IdRes val ref: Int) : Positioning()
        }

        enum class Relation {
            ENCLOSES, OVER
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
            var initialHeightParam: Int,
            val position: Position)

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