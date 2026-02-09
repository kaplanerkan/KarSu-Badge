package com.panda.karsu_badge

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

/**
 * A custom View that displays a [BadgeDrawable] and can be used directly in XML layouts.
 *
 * XML usage:
 * ```xml
 * <com.panda.karsu_badge.BadgeView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:badgeType="oneText"
 *     app:badgeText1="VIP"
 *     app:badgeColor="#336699" />
 * ```
 *
 * Programmatic usage:
 * ```kotlin
 * val badgeView = BadgeView(context)
 * badgeView.setBadgeText1("Hello")
 * badgeView.setBadgeColor(Color.RED)
 * ```
 */
class BadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var badgeDrawable: BadgeDrawable

    init {
        val builder = BadgeDrawable.Builder()

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.BadgeView, defStyleAttr, 0)
            try {
                if (ta.hasValue(R.styleable.BadgeView_badgeType)) {
                    builder.type(ta.getInt(R.styleable.BadgeView_badgeType, BadgeDrawable.TYPE_NUMBER))
                }
                if (ta.hasValue(R.styleable.BadgeView_badgeNumber)) {
                    builder.number(ta.getInt(R.styleable.BadgeView_badgeNumber, 0))
                }
                ta.getString(R.styleable.BadgeView_badgeText1)?.let { t -> builder.text1(t) }
                ta.getString(R.styleable.BadgeView_badgeText2)?.let { t -> builder.text2(t) }
                if (ta.hasValue(R.styleable.BadgeView_badgeTextSize)) {
                    builder.textSize(ta.getDimension(R.styleable.BadgeView_badgeTextSize, 0f))
                }
                if (ta.hasValue(R.styleable.BadgeView_badgeColor)) {
                    builder.badgeColor(ta.getColor(R.styleable.BadgeView_badgeColor, 0))
                }
                if (ta.hasValue(R.styleable.BadgeView_badgeTextColor)) {
                    builder.textColor(ta.getColor(R.styleable.BadgeView_badgeTextColor, 0))
                }
                if (ta.hasValue(R.styleable.BadgeView_badgeText2Color)) {
                    builder.text2Color(ta.getColor(R.styleable.BadgeView_badgeText2Color, 0))
                }
                if (ta.hasValue(R.styleable.BadgeView_badgeCornerRadius)) {
                    builder.cornerRadius(ta.getDimension(R.styleable.BadgeView_badgeCornerRadius, 0f))
                }

                // Padding: only call builder.padding() if at least one value is specified.
                // Unset values use named-parameter defaults from the Builder (which reads Config defaults).
                val hasLeft = ta.hasValue(R.styleable.BadgeView_badgePaddingLeft)
                val hasTop = ta.hasValue(R.styleable.BadgeView_badgePaddingTop)
                val hasRight = ta.hasValue(R.styleable.BadgeView_badgePaddingRight)
                val hasBottom = ta.hasValue(R.styleable.BadgeView_badgePaddingBottom)
                val hasCenter = ta.hasValue(R.styleable.BadgeView_badgePaddingCenter)
                if (hasLeft || hasTop || hasRight || hasBottom || hasCenter) {
                    // Build a temporary config only when needed, to read defaults
                    val defaults = BadgeDrawable.Config()
                    builder.padding(
                        left = if (hasLeft) ta.getDimension(R.styleable.BadgeView_badgePaddingLeft, 0f) else defaults.paddingLeft,
                        top = if (hasTop) ta.getDimension(R.styleable.BadgeView_badgePaddingTop, 0f) else defaults.paddingTop,
                        right = if (hasRight) ta.getDimension(R.styleable.BadgeView_badgePaddingRight, 0f) else defaults.paddingRight,
                        bottom = if (hasBottom) ta.getDimension(R.styleable.BadgeView_badgePaddingBottom, 0f) else defaults.paddingBottom,
                        center = if (hasCenter) ta.getDimension(R.styleable.BadgeView_badgePaddingCenter, 0f) else defaults.paddingCenter
                    )
                }

                if (ta.hasValue(R.styleable.BadgeView_badgeStrokeWidth)) {
                    builder.strokeWidth(ta.getDimensionPixelSize(R.styleable.BadgeView_badgeStrokeWidth, 0))
                }
            } finally {
                ta.recycle()
            }
        }

        badgeDrawable = builder.build()
    }

    // Cached bounds to avoid re-measuring in onDraw
    private var lastBoundsLeft = -1
    private var lastBoundsTop = -1
    private var lastBoundsRight = -1
    private var lastBoundsBottom = -1

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val intrinsicW = badgeDrawable.intrinsicWidth + paddingLeft + paddingRight
        val intrinsicH = badgeDrawable.intrinsicHeight + paddingTop + paddingBottom

        val w = resolveSize(intrinsicW, widthMeasureSpec)
        val h = resolveSize(intrinsicH, heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateDrawableBounds()
    }

    private fun updateDrawableBounds() {
        val l = paddingLeft
        val t = paddingTop
        val r = width - paddingRight
        val b = height - paddingBottom
        if (l != lastBoundsLeft || t != lastBoundsTop || r != lastBoundsRight || b != lastBoundsBottom) {
            lastBoundsLeft = l
            lastBoundsTop = t
            lastBoundsRight = r
            lastBoundsBottom = b
            badgeDrawable.setBounds(l, t, r, b)
        }
    }

    override fun onDraw(canvas: Canvas) {
        badgeDrawable.draw(canvas)
    }

    /** Replaces the internal BadgeDrawable and refreshes the view. */
    fun setBadgeDrawable(drawable: BadgeDrawable) {
        badgeDrawable = drawable
        lastBoundsLeft = -1 // force bounds recalculation
        requestLayout()
        invalidate()
    }

    /** Returns the underlying BadgeDrawable. */
    fun getBadgeDrawable(): BadgeDrawable = badgeDrawable

    /** Sets the badge type and re-renders. */
    fun setBadgeType(@BadgeDrawable.BadgeType type: Int) {
        badgeDrawable.setBadgeType(type)
        requestLayout()
        invalidate()
    }

    /** Sets the number (for TYPE_NUMBER). */
    fun setBadgeNumber(number: Int) {
        badgeDrawable.setNumber(number)
        invalidate()
    }

    /** Sets the first text field and re-renders. */
    fun setBadgeText1(text: String) {
        badgeDrawable.setText1(text)
        requestLayout()
        invalidate()
    }

    /** Sets the second text field and re-renders. */
    fun setBadgeText2(text: String) {
        badgeDrawable.setText2(text)
        requestLayout()
        invalidate()
    }

    /** Sets the text size in pixels and re-renders. */
    fun setBadgeTextSize(size: Float) {
        badgeDrawable.setTextSize(size)
        requestLayout()
        invalidate()
    }

    /** Sets the badge background color. */
    fun setBadgeColor(@ColorInt color: Int) {
        badgeDrawable.setBadgeColor(color)
        invalidate()
    }

    /** Sets the text color. */
    fun setBadgeTextColor(@ColorInt color: Int) {
        badgeDrawable.setTextColor(color)
        invalidate()
    }

    /** Sets the corner radius in pixels and re-renders. */
    fun setBadgeCornerRadius(radius: Float) {
        badgeDrawable.setCornerRadius(radius)
        requestLayout()
        invalidate()
    }

    /** Sets the stroke width in pixels. */
    fun setBadgeStrokeWidth(width: Int) {
        badgeDrawable.setStrokeWidth(width)
        invalidate()
    }
}
