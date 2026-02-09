/*
 * Copyright 2026 KarsuBadge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.panda.karsu_badge

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.IntDef

/**
 * A customizable badge drawable class.
 *
 * Supports 4 different badge types:
 * - [TYPE_NUMBER]: Circular number badge (e.g. notification count)
 * - [TYPE_ONLY_ONE_TEXT]: Single text badge (e.g. "VIP")
 * - [TYPE_WITH_TWO_TEXT]: Dual-section badge with white backgrounds (e.g. "TEST | Pass")
 * - [TYPE_WITH_TWO_TEXT_COMPLEMENTARY]: Dual-section complementary badge (e.g. "LEVEL | 10")
 *
 * Usage examples:
 * ```kotlin
 * // Builder pattern
 * val badge = BadgeDrawable.Builder()
 *     .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
 *     .text1("VIP")
 *     .badgeColor(0xff336699.toInt())
 *     .build()
 *
 * // Kotlin DSL
 * val badge = badgeDrawable {
 *     type(BadgeDrawable.TYPE_NUMBER)
 *     number(9)
 * }
 *
 * // Inline usage in a TextView
 * textView.text = badge.toSpannable()
 * ```
 */
class BadgeDrawable private constructor(private val config: Config) : Drawable() {

    companion object {
        /** Circular number badge type. Shows ellipsis when the number does not fit. */
        const val TYPE_NUMBER = 1

        /** Rectangular single-text badge type. */
        const val TYPE_ONLY_ONE_TEXT = 1 shl 1

        /** Dual-text badge type with both sections having white backgrounds. */
        const val TYPE_WITH_TWO_TEXT = 1 shl 2

        /** Dual-text badge type with complementary-colored second section. */
        const val TYPE_WITH_TWO_TEXT_COMPLEMENTARY = 1 shl 3

        /**
         * Converts a dp value to pixels.
         * @param dipValue value in dp
         * @return value in pixels
         */
        private fun dipToPixels(dipValue: Float): Float {
            val scale = Resources.getSystem().displayMetrics.density
            return dipValue * scale + 0.5f
        }

        /**
         * Converts an sp value to pixels.
         * @param spValue value in sp
         * @return value in pixels
         */
        private fun spToPixels(spValue: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, spValue, Resources.getSystem().displayMetrics
            )
        }
    }

    /** Annotation that restricts values to valid badge types. */
    @IntDef(TYPE_NUMBER, TYPE_ONLY_ONE_TEXT, TYPE_WITH_TWO_TEXT, TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class BadgeType

    /**
     * Internal class holding all badge configuration values.
     * Created by the Builder and passed to BadgeDrawable.
     */
    internal class Config(
        @param:BadgeType var badgeType: Int = TYPE_NUMBER,
        var number: Int = 0,
        var text1: String = "",
        var text2: String = "",
        var textSize: Float = spToPixels(12f),
        @param:ColorInt var badgeColor: Int = 0xffCC3333.toInt(),
        @param:ColorInt var textColor: Int = 0xffFFFFFF.toInt(),
        @param:ColorInt var text2Color: Int? = null,
        var typeface: Typeface = Typeface.DEFAULT_BOLD,
        var cornerRadius: Float = dipToPixels(2f),
        var paddingLeft: Float = dipToPixels(2f),
        var paddingTop: Float = dipToPixels(2f),
        var paddingRight: Float = dipToPixels(2f),
        var paddingBottom: Float = dipToPixels(2f),
        var paddingCenter: Float = dipToPixels(3f),
        var strokeWidth: Int = dipToPixels(1f).toInt()
    ) {
        /** Creates an independent copy of this Config (used by buildUpon). */
        fun copy(): Config = Config(
            badgeType, number, text1, text2, textSize,
            badgeColor, textColor, text2Color, typeface, cornerRadius,
            paddingLeft, paddingTop, paddingRight, paddingBottom,
            paddingCenter, strokeWidth
        )
    }

    // -- Background drawables --
    /** Background shape drawable for the entire badge. */
    private val backgroundDrawable: ShapeDrawable
    /** Background for the text1 section in TYPE_WITH_TWO_TEXT. */
    private val backgroundDrawableOfText1: ShapeDrawable
    /** Background for the text2 section in TYPE_WITH_TWO_TEXT / COMPLEMENTARY. */
    private val backgroundDrawableOfText2: ShapeDrawable

    // -- Size variables --
    private var badgeWidth: Int = 0
    private var badgeHeight: Int = 0

    // -- Corner radius arrays (8 floats for RoundRectShape) --
    /** Corner radii for the full badge. */
    private val outerR = FloatArray(8)
    /** Text1 section: only left corners rounded. */
    private val outerROfText1 = FloatArray(8)
    /** Text2 section: only right corners rounded. */
    private val outerROfText2 = FloatArray(8)

    /** Paint object used for drawing text and shapes. */
    private val paint = Paint().apply {
        isAntiAlias = true
        typeface = config.typeface
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        alpha = 255
    }
    /** Font metrics for vertical text alignment. */
    private var fontMetrics: Paint.FontMetrics

    /** Measured text1 width in pixels. */
    private var text1Width: Int = 0
    /** Measured text2 width in pixels. */
    private var text2Width: Int = 0

    init {
        setCornerRadius(config.cornerRadius)

        backgroundDrawable = ShapeDrawable(RoundRectShape(outerR, null, null))
        backgroundDrawableOfText1 = ShapeDrawable(RoundRectShape(outerROfText1, null, null))
        backgroundDrawableOfText2 = ShapeDrawable(RoundRectShape(outerROfText2, null, null))

        setTextSize(config.textSize)
        fontMetrics = paint.fontMetrics
        measureBadge()
    }

    // ════════════════════════════════════════
    //  Builder - Badge construction class
    // ════════════════════════════════════════

    /**
     * Builder class for creating a BadgeDrawable.
     *
     * ```kotlin
     * val badge = BadgeDrawable.Builder()
     *     .type(BadgeDrawable.TYPE_NUMBER)
     *     .number(5)
     *     .badgeColor(0xffCC3333.toInt())
     *     .build()
     * ```
     */
    class Builder {
        private val config: Config

        constructor() {
            config = Config()
        }

        /** Creates a Builder with an existing Config (used by buildUpon). */
        internal constructor(config: Config) {
            this.config = config
        }

        /** Sets the badge type. [TYPE_NUMBER], [TYPE_ONLY_ONE_TEXT], [TYPE_WITH_TWO_TEXT], [TYPE_WITH_TWO_TEXT_COMPLEMENTARY] */
        fun type(@BadgeType type: Int): Builder = apply { config.badgeType = type }

        /** Sets the number to display (for TYPE_NUMBER). */
        fun number(number: Int): Builder = apply { config.number = number }

        /** Sets the first text field. */
        fun text1(text1: String): Builder = apply { config.text1 = text1 }

        /** Sets the second text field (for dual-text types). */
        fun text2(text2: String): Builder = apply { config.text2 = text2 }

        /** Sets the text size in pixels. */
        fun textSize(size: Float): Builder = apply { config.textSize = size }

        /** Sets the badge background color. */
        fun badgeColor(@ColorInt color: Int): Builder = apply { config.badgeColor = color }

        /** Sets the text color. */
        fun textColor(@ColorInt color: Int): Builder = apply { config.textColor = color }

        /** Sets the text2 section background color for COMPLEMENTARY type. Uses textColor if null. */
        fun text2Color(@ColorInt color: Int): Builder = apply { config.text2Color = color }

        /** Sets the font typeface. */
        fun typeface(typeface: Typeface): Builder = apply { config.typeface = typeface }

        /** Sets the corner radius in pixels. */
        fun cornerRadius(radius: Float): Builder = apply { config.cornerRadius = radius }

        /**
         * Sets badge padding values.
         * @param left left padding
         * @param top top padding
         * @param right right padding
         * @param bottom bottom padding
         * @param center spacing between the two sections in dual-text types
         */
        fun padding(
            left: Float = config.paddingLeft,
            top: Float = config.paddingTop,
            right: Float = config.paddingRight,
            bottom: Float = config.paddingBottom,
            center: Float = config.paddingCenter
        ): Builder = apply {
            config.paddingLeft = left
            config.paddingTop = top
            config.paddingRight = right
            config.paddingBottom = bottom
            config.paddingCenter = center
        }

        /** Sets the stroke width in pixels (inner border for dual-text types). */
        fun strokeWidth(width: Int): Builder = apply { config.strokeWidth = width }

        /** Builds and returns a BadgeDrawable based on the current configuration. */
        fun build(): BadgeDrawable = BadgeDrawable(config)
    }

    // ════════════════════════════════════════
    //  Public API - Getters / Setters
    // ════════════════════════════════════════

    /**
     * Creates a new Builder from the current configuration.
     * Useful for cloning and modifying a badge.
     *
     * ```kotlin
     * val newBadge = existingBadge.buildUpon()
     *     .number(99)
     *     .build()
     * ```
     */
    fun buildUpon(): Builder = Builder(config.copy())

    /** Returns the current badge type. */
    @BadgeType
    fun getBadgeType(): Int = config.badgeType

    /** Changes the badge type and re-measures. */
    fun setBadgeType(@BadgeType type: Int) {
        config.badgeType = type
        measureBadge()
    }

    /** Returns the displayed number. */
    fun getNumber(): Int = config.number

    /** Sets the number to display (for TYPE_NUMBER). */
    fun setNumber(number: Int) {
        config.number = number
    }

    /** Returns the first text field. */
    fun getText1(): String = config.text1

    /** Sets the first text and re-measures the badge. */
    fun setText1(text1: String) {
        config.text1 = text1
        measureBadge()
    }

    /** Returns the second text field. */
    fun getText2(): String = config.text2

    /** Sets the second text and re-measures the badge. */
    fun setText2(text2: String) {
        config.text2 = text2
        measureBadge()
    }

    /** Returns the text size in pixels. */
    fun getTextSize(): Float = config.textSize

    /** Sets the text size, updates the paint, and re-measures the badge. */
    fun setTextSize(textSize: Float) {
        config.textSize = textSize
        paint.textSize = textSize
        fontMetrics = paint.fontMetrics
        measureBadge()
    }

    /** Returns the badge background color. */
    @ColorInt
    fun getBadgeColor(): Int = config.badgeColor

    /** Sets the badge background color. */
    fun setBadgeColor(@ColorInt color: Int) {
        config.badgeColor = color
    }

    /** Returns the text color. */
    @ColorInt
    fun getTextColor(): Int = config.textColor

    /** Sets the text color. */
    fun setTextColor(@ColorInt color: Int) {
        config.textColor = color
    }

    /** Returns the font typeface. */
    fun getTypeface(): Typeface = config.typeface

    /** Sets the font typeface and applies it to the paint. */
    fun setTypeface(typeface: Typeface) {
        config.typeface = typeface
        paint.typeface = typeface
    }

    /** Returns the corner radius. */
    fun getCornerRadius(): Float = config.cornerRadius

    /**
     * Sets the corner radius.
     * - outerR: all corners rounded (single text / number badge)
     * - outerROfText1: only left corners rounded (dual-text left section)
     * - outerROfText2: only right corners rounded (dual-text right section)
     */
    fun setCornerRadius(radius: Float) {
        config.cornerRadius = radius
        outerR.fill(radius)

        outerROfText1[0] = radius
        outerROfText1[1] = radius
        outerROfText1[6] = radius
        outerROfText1[7] = radius
        outerROfText1[2] = 0f
        outerROfText1[3] = 0f
        outerROfText1[4] = 0f
        outerROfText1[5] = 0f

        outerROfText2[0] = 0f
        outerROfText2[1] = 0f
        outerROfText2[6] = 0f
        outerROfText2[7] = 0f
        outerROfText2[2] = radius
        outerROfText2[3] = radius
        outerROfText2[4] = radius
        outerROfText2[5] = radius
    }

    /** Returns the stroke width. */
    fun getStrokeWidth(): Int = config.strokeWidth

    /** Sets the stroke width. */
    fun setStrokeWidth(width: Int) {
        config.strokeWidth = width
    }

    /** Returns the text2 section color, or null if using textColor as fallback. */
    @ColorInt
    fun getText2Color(): Int? = config.text2Color

    /** Sets the text2 section color (for COMPLEMENTARY type). Pass null to use textColor. */
    fun setText2Color(@ColorInt color: Int?) {
        config.text2Color = color
    }

    /** Returns the left padding in pixels. */
    fun getPaddingLeftValue(): Float = config.paddingLeft

    /** Returns the top padding in pixels. */
    fun getPaddingTopValue(): Float = config.paddingTop

    /** Returns the right padding in pixels. */
    fun getPaddingRightValue(): Float = config.paddingRight

    /** Returns the bottom padding in pixels. */
    fun getPaddingBottomValue(): Float = config.paddingBottom

    /** Returns the center padding in pixels (spacing between dual-text sections). */
    fun getPaddingCenterValue(): Float = config.paddingCenter

    /** Sets all padding values and re-measures the badge. */
    fun setPaddingValues(left: Float, top: Float, right: Float, bottom: Float, center: Float) {
        config.paddingLeft = left
        config.paddingTop = top
        config.paddingRight = right
        config.paddingBottom = bottom
        config.paddingCenter = center
        measureBadge()
    }

    // ════════════════════════════════════════
    //  Measurement - Calculates badge dimensions
    // ════════════════════════════════════════

    /**
     * Calculates badge width and height based on the badge type.
     *
     * - TYPE_NUMBER: Circular badge, width = textSize + padding
     * - TYPE_ONLY_ONE_TEXT: text1 width + padding
     * - TYPE_WITH_TWO_TEXT / COMPLEMENTARY: text1 + text2 + padding + paddingCenter
     *
     * If bounds are set and the badge is wider than bounds, text widths are clipped.
     */
    private fun measureBadge() {
        badgeHeight = (config.textSize + config.paddingTop + config.paddingBottom).toInt()

        val t1 = config.text1
        val t2 = config.text2

        when (config.badgeType) {
            TYPE_ONLY_ONE_TEXT -> {
                text1Width = paint.measureText(t1).toInt()
                badgeWidth = (text1Width + config.paddingLeft + config.paddingRight).toInt()
                setCornerRadius(config.cornerRadius)
            }

            TYPE_WITH_TWO_TEXT, TYPE_WITH_TWO_TEXT_COMPLEMENTARY -> {
                text1Width = paint.measureText(t1).toInt()
                text2Width = paint.measureText(t2).toInt()
                badgeWidth = (text1Width + text2Width +
                        config.paddingLeft + config.paddingRight + config.paddingCenter).toInt()
                setCornerRadius(config.cornerRadius)
            }

            else -> { // TYPE_NUMBER
                badgeWidth = (config.textSize + config.paddingLeft + config.paddingRight).toInt()
                setCornerRadius(badgeHeight.toFloat())
            }
        }

        // If bounds are set and badge doesn't fit, clip text widths
        val boundsWidth = bounds.width()
        if (boundsWidth > 0) {
            when (config.badgeType) {
                TYPE_ONLY_ONE_TEXT -> {
                    if (boundsWidth < badgeWidth) {
                        text1Width = (boundsWidth - config.paddingLeft - config.paddingRight).toInt()
                            .coerceAtLeast(0)
                        badgeWidth = boundsWidth
                    }
                }

                TYPE_WITH_TWO_TEXT, TYPE_WITH_TWO_TEXT_COMPLEMENTARY -> {
                    if (boundsWidth < badgeWidth) {
                        if (boundsWidth < (text1Width + config.paddingLeft + config.paddingRight).toInt()) {
                            text1Width = (boundsWidth - config.paddingLeft - config.paddingRight).toInt()
                                .coerceAtLeast(0)
                            text2Width = 0
                        } else {
                            text2Width = (boundsWidth - text1Width -
                                    config.paddingLeft - config.paddingRight - config.paddingCenter).toInt()
                                .coerceAtLeast(0)
                        }
                        badgeWidth = boundsWidth
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════
    //  Drawing - Renders badge onto canvas
    // ════════════════════════════════════════

    /**
     * Re-measures the badge when bounds change.
     * Ensures automatic adaptation when placed in different-sized areas.
     */
    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        measureBadge()
    }

    /**
     * Draws the badge onto the canvas.
     *
     * Drawing order varies by badge type:
     * - TYPE_NUMBER: Background + centered number
     * - TYPE_ONLY_ONE_TEXT: Background + centered text1
     * - TYPE_WITH_TWO_TEXT: Background + white text1 section + white text2 section + colored text
     * - TYPE_WITH_TWO_TEXT_COMPLEMENTARY: Background + text1 + colored text2 background + inverted text2
     */
    override fun draw(canvas: Canvas) {
        val bounds: Rect = bounds

        // Calculate margins to center the badge within bounds
        val marginTopAndBottom = ((bounds.height() - badgeHeight) / 2f).toInt()
        val marginLeftAndRight = ((bounds.width() - badgeWidth) / 2f).toInt()

        // Draw main background (common for all badge types)
        backgroundDrawable.setBounds(
            bounds.left + marginLeftAndRight,
            bounds.top + marginTopAndBottom,
            bounds.right - marginLeftAndRight,
            bounds.bottom - marginTopAndBottom
        )
        backgroundDrawable.paint.color = config.badgeColor
        backgroundDrawable.draw(canvas)

        // Vertical text center point (based on font metrics)
        val textCx = bounds.centerX().toFloat()
        val textCy = bounds.centerY() - (fontMetrics.bottom + fontMetrics.top) / 2f

        val t1 = config.text1
        val t2 = config.text2

        when (config.badgeType) {
            // Single text: draw text1 at badge center
            TYPE_ONLY_ONE_TEXT -> {
                paint.color = config.textColor
                canvas.drawText(cutText(t1, text1Width), textCx, textCy, paint)
            }

            // Complementary dual text: text1 on left, text2 on right with inverted colors
            TYPE_WITH_TWO_TEXT_COMPLEMENTARY -> {
                // Draw text1 on the left side
                paint.color = config.textColor
                canvas.drawText(
                    t1,
                    marginLeftAndRight + config.paddingLeft + text1Width / 2f,
                    textCy,
                    paint
                )

                // Draw complementary background for text2 on the right side
                backgroundDrawableOfText2.setBounds(
                    (bounds.left + marginLeftAndRight + config.paddingLeft +
                            text1Width + config.paddingCenter / 2f).toInt(),
                    bounds.top + marginTopAndBottom + config.strokeWidth,
                    bounds.width() - marginLeftAndRight - config.strokeWidth,
                    bounds.bottom - marginTopAndBottom - config.strokeWidth
                )
                backgroundDrawableOfText2.paint.color = config.text2Color ?: config.textColor
                backgroundDrawableOfText2.draw(canvas)

                // Draw text2 with badge color (inverted)
                paint.color = config.badgeColor
                canvas.drawText(
                    cutText(t2, text2Width),
                    bounds.width() - marginLeftAndRight - config.paddingRight - text2Width / 2f,
                    textCy,
                    paint
                )
            }

            // Dual text: both sections with white backgrounds, text in badge color
            TYPE_WITH_TWO_TEXT -> {
                // White background for text1 (left section)
                backgroundDrawableOfText1.setBounds(
                    bounds.left + marginLeftAndRight + config.strokeWidth,
                    bounds.top + marginTopAndBottom + config.strokeWidth,
                    (bounds.left + marginLeftAndRight + config.paddingLeft +
                            text1Width + config.paddingCenter / 2f - config.strokeWidth / 2f).toInt(),
                    bounds.bottom - marginTopAndBottom - config.strokeWidth
                )
                backgroundDrawableOfText1.paint.color = 0xffFFFFFF.toInt()
                backgroundDrawableOfText1.draw(canvas)

                // Draw text1 in badge color
                paint.color = config.badgeColor
                canvas.drawText(
                    t1,
                    text1Width / 2f + marginLeftAndRight + config.paddingLeft,
                    textCy,
                    paint
                )

                // White background for text2 (right section)
                backgroundDrawableOfText2.setBounds(
                    (bounds.left + marginLeftAndRight + config.paddingLeft +
                            text1Width + config.paddingCenter / 2f + config.strokeWidth / 2f).toInt(),
                    bounds.top + marginTopAndBottom + config.strokeWidth,
                    bounds.width() - marginLeftAndRight - config.strokeWidth,
                    bounds.bottom - marginTopAndBottom - config.strokeWidth
                )
                backgroundDrawableOfText2.paint.color = 0xffFFFFFF.toInt()
                backgroundDrawableOfText2.draw(canvas)

                // Draw text2 in badge color
                paint.color = config.badgeColor
                canvas.drawText(
                    cutText(t2, text2Width),
                    bounds.width() - marginLeftAndRight - config.paddingRight - text2Width / 2f,
                    textCy,
                    paint
                )
            }

            // Number badge: draw number at center
            else -> { // TYPE_NUMBER
                paint.color = config.textColor
                canvas.drawText(cutNumber(config.number, badgeWidth), textCx, textCy, paint)
            }
        }
    }

    /** Returns the intrinsic (measured) width of the badge. */
    override fun getIntrinsicWidth(): Int = badgeWidth

    /** Returns the intrinsic (measured) height of the badge. */
    override fun getIntrinsicHeight(): Int = badgeHeight

    /** Sets the drawable alpha transparency (0-255). */
    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    /** Sets the drawable color filter. */
    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    // ════════════════════════════════════════
    //  Helper methods
    // ════════════════════════════════════════

    /**
     * Returns the ellipsis character if the number does not fit in the given width.
     * @param number the number to display
     * @param width available width in pixels
     * @return the number as a string, or an ellipsis character
     */
    private fun cutNumber(number: Int, width: Int): String {
        val text = number.toString()
        return if (paint.measureText(text) < width) text else "\u2026"
    }

    /**
     * Truncates text to fit within the given width, appending "..." as a suffix.
     * Removes characters one by one; if still too wide, also reduces the suffix dots.
     * @param text the text to truncate
     * @param width available width in pixels
     * @return the truncated text or the original if it fits
     */
    private fun cutText(text: String, width: Int): String {
        if (paint.measureText(text) <= width) return text

        var t = text
        var suffix = "..."
        while (paint.measureText(t + suffix) > width) {
            if (t.isNotEmpty()) {
                t = t.substring(0, t.length - 1)
            }
            if (t.isEmpty()) {
                suffix = suffix.substring(0, suffix.length - 1)
                if (suffix.isEmpty()) break
            }
        }
        return t + suffix
    }

    /**
     * Converts the badge to a SpannableString for inline use in a TextView.
     *
     * ```kotlin
     * val text = TextUtils.concat("Message: ", badge.toSpannable(), " more text")
     * textView.text = text
     * ```
     *
     * @return a SpannableString containing this badge
     */
    fun toSpannable(): SpannableString {
        val spanStr = SpannableString(" ")
        spanStr.setSpan(
            ImageSpan(this, ImageSpan.ALIGN_BOTTOM),
            0, 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        return spanStr
    }
}

// ════════════════════════════════════════
//  Kotlin DSL extension function
// ════════════════════════════════════════

/**
 * Kotlin DSL function for creating a BadgeDrawable.
 *
 * ```kotlin
 * val badge = badgeDrawable {
 *     type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
 *     text1("New")
 *     badgeColor(0xff336699.toInt())
 * }
 * ```
 *
 * @param block configuration block on the Builder
 * @return a configured BadgeDrawable
 */
inline fun badgeDrawable(block: BadgeDrawable.Builder.() -> Unit): BadgeDrawable {
    return BadgeDrawable.Builder().apply(block).build()
}
