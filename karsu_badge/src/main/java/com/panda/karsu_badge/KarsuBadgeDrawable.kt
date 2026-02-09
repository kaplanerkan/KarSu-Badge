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
 * Ozellestirilerbilir badge (rozet) drawable sinifi.
 *
 * 4 farkli badge tipini destekler:
 * - [TYPE_NUMBER]: Yuvarlak sayi badge'i (orn. bildirim sayisi)
 * - [TYPE_ONLY_ONE_TEXT]: Tek metin iceren badge (orn. "VIP")
 * - [TYPE_WITH_TWO_TEXT]: Cift bolumlu metin badge'i, beyaz arka planli (orn. "TEST | Pass")
 * - [TYPE_WITH_TWO_TEXT_COMPLEMENTARY]: Cift bolumlu tamamlayici badge (orn. "LEVEL | 10")
 *
 * Kullanim ornekleri:
 * ```kotlin
 * // Builder pattern ile
 * val badge = BadgeDrawable.Builder()
 *     .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
 *     .text1("VIP")
 *     .badgeColor(0xff336699.toInt())
 *     .build()
 *
 * // Kotlin DSL ile
 * val badge = badgeDrawable {
 *     type(BadgeDrawable.TYPE_NUMBER)
 *     number(9)
 * }
 *
 * // TextView icinde kullanim
 * textView.text = badge.toSpannable()
 * ```
 */
class BadgeDrawable private constructor(private val config: Config) : Drawable() {

    companion object {
        /** Yuvarlak sayi badge tipi. Sayi sigmazsa "..." gosterir. */
        const val TYPE_NUMBER = 1

        /** Tek metin iceren dikdortgen badge tipi. */
        const val TYPE_ONLY_ONE_TEXT = 1 shl 1

        /** Cift metin, iki bolum de beyaz arka planli badge tipi. */
        const val TYPE_WITH_TWO_TEXT = 1 shl 2

        /** Cift metin, ikinci bolum tamamlayici renkli badge tipi. */
        const val TYPE_WITH_TWO_TEXT_COMPLEMENTARY = 1 shl 3

        /**
         * dp degerini piksel cinsine donusturur.
         * @param dipValue dp cinsinden deger
         * @return piksel cinsinden deger
         */
        private fun dipToPixels(dipValue: Float): Float {
            val scale = Resources.getSystem().displayMetrics.density
            return dipValue * scale + 0.5f
        }

        /**
         * sp degerini piksel cinsine donusturur.
         * @param spValue sp cinsinden deger
         * @return piksel cinsinden deger
         */
        private fun spToPixels(spValue: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, spValue, Resources.getSystem().displayMetrics
            )
        }
    }

    /** Badge tipini sinirlandiran annotation. */
    @IntDef(TYPE_NUMBER, TYPE_ONLY_ONE_TEXT, TYPE_WITH_TWO_TEXT, TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class BadgeType

    /**
     * Badge'in tum yapilandirma degerlerini tutan dahili sinif.
     * Builder tarafindan olusturulur ve BadgeDrawable'a aktarilir.
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
        /** Config'in bagimsiz bir kopyasini olusturur (buildUpon icin). */
        fun copy(): Config = Config(
            badgeType, number, text1, text2, textSize,
            badgeColor, textColor, text2Color, typeface, cornerRadius,
            paddingLeft, paddingTop, paddingRight, paddingBottom,
            paddingCenter, strokeWidth
        )
    }

    // -- Arka plan drawable'lari --
    /** Tum badge icin arka plan shape drawable. */
    private val backgroundDrawable: ShapeDrawable
    /** TYPE_WITH_TWO_TEXT tipinde text1 bolumu icin arka plan. */
    private val backgroundDrawableOfText1: ShapeDrawable
    /** TYPE_WITH_TWO_TEXT / COMPLEMENTARY tipinde text2 bolumu icin arka plan. */
    private val backgroundDrawableOfText2: ShapeDrawable

    // -- Boyut degiskenleri --
    private var badgeWidth: Int = 0
    private var badgeHeight: Int = 0

    // -- Kose yuvarlama dizileri (RoundRectShape icin 8 float) --
    /** Tum badge icin kose yuvarlakliklari. */
    private val outerR = FloatArray(8)
    /** Text1 bolumu: sadece sol kose yuvarlak. */
    private val outerROfText1 = FloatArray(8)
    /** Text2 bolumu: sadece sag kose yuvarlak. */
    private val outerROfText2 = FloatArray(8)

    /** Metin ve sekil cizimi icin kullanilan Paint nesnesi. */
    private val paint = Paint().apply {
        isAntiAlias = true
        typeface = config.typeface
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        alpha = 255
    }
    /** Metin dikey hizalamasi icin font metrikleri. */
    private var fontMetrics: Paint.FontMetrics

    /** Olculen text1 genisligi (piksel). */
    private var text1Width: Int = 0
    /** Olculen text2 genisligi (piksel). */
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
    //  Builder - Badge olusturma sinifi
    // ════════════════════════════════════════

    /**
     * BadgeDrawable olusturmak icin Builder sinifi.
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

        /** Mevcut Config ile Builder olusturur (buildUpon icin). */
        internal constructor(config: Config) {
            this.config = config
        }

        /** Badge tipini ayarlar. [TYPE_NUMBER], [TYPE_ONLY_ONE_TEXT], [TYPE_WITH_TWO_TEXT], [TYPE_WITH_TWO_TEXT_COMPLEMENTARY] */
        fun type(@BadgeType type: Int): Builder = apply { config.badgeType = type }

        /** TYPE_NUMBER tipinde gosterilecek sayiyi ayarlar. */
        fun number(number: Int): Builder = apply { config.number = number }

        /** Birinci metin alanini ayarlar. */
        fun text1(text1: String): Builder = apply { config.text1 = text1 }

        /** Ikinci metin alanini ayarlar (cift metinli tipler icin). */
        fun text2(text2: String): Builder = apply { config.text2 = text2 }

        /** Metin boyutunu piksel cinsinden ayarlar. */
        fun textSize(size: Float): Builder = apply { config.textSize = size }

        /** Badge arka plan rengini ayarlar. */
        fun badgeColor(@ColorInt color: Int): Builder = apply { config.badgeColor = color }

        /** Metin rengini ayarlar. */
        fun textColor(@ColorInt color: Int): Builder = apply { config.textColor = color }

        /** COMPLEMENTARY tipinde text2 arka plan rengini ayarlar. null ise textColor kullanilir. */
        fun text2Color(@ColorInt color: Int): Builder = apply { config.text2Color = color }

        /** Yazi tipini (font) ayarlar. */
        fun typeface(typeface: Typeface): Builder = apply { config.typeface = typeface }

        /** Kose yuvarlama yaricapini piksel cinsinden ayarlar. */
        fun cornerRadius(radius: Float): Builder = apply { config.cornerRadius = radius }

        /**
         * Badge ic bosluk (padding) degerlerini ayarlar.
         * @param left sol bosluk
         * @param top ust bosluk
         * @param right sag bosluk
         * @param bottom alt bosluk
         * @param center cift metinli tiplerde iki bolum arasi bosluk
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

        /** Cizgi kalinligini piksel cinsinden ayarlar (cift metinli tiplerde ic border). */
        fun strokeWidth(width: Int): Builder = apply { config.strokeWidth = width }

        /** Yapilandirmaya gore BadgeDrawable olusturur ve dondurur. */
        fun build(): BadgeDrawable = BadgeDrawable(config)
    }

    // ════════════════════════════════════════
    //  Public API - Getter/Setter metodlari
    // ════════════════════════════════════════

    /**
     * Mevcut ayarlardan yeni bir Builder olusturur.
     * Badge'i kopyalayip degistirmek icin kullanilir.
     *
     * ```kotlin
     * val newBadge = existingBadge.buildUpon()
     *     .number(99)
     *     .build()
     * ```
     */
    fun buildUpon(): Builder = Builder(config.copy())

    /** Mevcut badge tipini dondurur. */
    @BadgeType
    fun getBadgeType(): Int = config.badgeType

    /** Badge tipini degistirir ve yeniden olcer. */
    fun setBadgeType(@BadgeType type: Int) {
        config.badgeType = type
        measureBadge()
    }

    /** Gosterilen sayiyi dondurur. */
    fun getNumber(): Int = config.number

    /** Gosterilecek sayiyi ayarlar (TYPE_NUMBER icin). */
    fun setNumber(number: Int) {
        config.number = number
    }

    /** Birinci metin alanini dondurur. */
    fun getText1(): String = config.text1

    /** Birinci metni ayarlar ve badge'i yeniden olcer. */
    fun setText1(text1: String) {
        config.text1 = text1
        measureBadge()
    }

    /** Ikinci metin alanini dondurur. */
    fun getText2(): String = config.text2

    /** Ikinci metni ayarlar ve badge'i yeniden olcer. */
    fun setText2(text2: String) {
        config.text2 = text2
        measureBadge()
    }

    /** Metin boyutunu dondurur (piksel). */
    fun getTextSize(): Float = config.textSize

    /** Metin boyutunu ayarlar, paint'i gunceller ve badge'i yeniden olcer. */
    fun setTextSize(textSize: Float) {
        config.textSize = textSize
        paint.textSize = textSize
        fontMetrics = paint.fontMetrics
        measureBadge()
    }

    /** Badge arka plan rengini dondurur. */
    @ColorInt
    fun getBadgeColor(): Int = config.badgeColor

    /** Badge arka plan rengini ayarlar. */
    fun setBadgeColor(@ColorInt color: Int) {
        config.badgeColor = color
    }

    /** Metin rengini dondurur. */
    @ColorInt
    fun getTextColor(): Int = config.textColor

    /** Metin rengini ayarlar. */
    fun setTextColor(@ColorInt color: Int) {
        config.textColor = color
    }

    /** Yazi tipini dondurur. */
    fun getTypeface(): Typeface = config.typeface

    /** Yazi tipini ayarlar ve paint'e uygular. */
    fun setTypeface(typeface: Typeface) {
        config.typeface = typeface
        paint.typeface = typeface
    }

    /** Kose yuvarlama yaricapini dondurur. */
    fun getCornerRadius(): Float = config.cornerRadius

    /**
     * Kose yuvarlama yaricapini ayarlar.
     * - outerR: tum koseler yuvarlak (tek metin / sayi badge)
     * - outerROfText1: sadece sol koseler yuvarlak (cift metin sol bolum)
     * - outerROfText2: sadece sag koseler yuvarlak (cift metin sag bolum)
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

    /** Cizgi kalinligini dondurur. */
    fun getStrokeWidth(): Int = config.strokeWidth

    /** Cizgi kalinligini ayarlar. */
    fun setStrokeWidth(width: Int) {
        config.strokeWidth = width
    }

    // ════════════════════════════════════════
    //  Olcum - Badge boyutlarini hesaplar
    // ════════════════════════════════════════

    /**
     * Badge genislik ve yuksekligini badge tipine gore hesaplar.
     *
     * - TYPE_NUMBER: Yuvarlak badge, genislik = textSize + padding
     * - TYPE_ONLY_ONE_TEXT: text1 genisligi + padding
     * - TYPE_WITH_TWO_TEXT / COMPLEMENTARY: text1 + text2 + padding + paddingCenter
     *
     * Eger bounds ayarliysa ve badge bounds'tan genisse, metinler kirpilir.
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

        // Bounds ayarliysa ve badge sigmiiyorsa metin genisliklerini kirp
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
    //  Cizim - Canvas uzerine badge cizer
    // ════════════════════════════════════════

    /**
     * Bounds degistiginde badge'i yeniden olcer.
     * Boylece farkli boyutlu alanlara yerlestirildiginde otomatik uyum saglar.
     */
    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        measureBadge()
    }

    /**
     * Badge'i canvas uzerine cizer.
     *
     * Cizim sirasi badge tipine gore degisir:
     * - TYPE_NUMBER: Arka plan + ortaya sayi
     * - TYPE_ONLY_ONE_TEXT: Arka plan + ortaya text1
     * - TYPE_WITH_TWO_TEXT: Arka plan + beyaz text1 bolumu + beyaz text2 bolumu + renkli metinler
     * - TYPE_WITH_TWO_TEXT_COMPLEMENTARY: Arka plan + text1 + renkli text2 arka plani + ters renkli text2
     */
    override fun draw(canvas: Canvas) {
        val bounds: Rect = bounds

        // Badge'i bounds icinde ortalamak icin margin hesapla
        val marginTopAndBottom = ((bounds.height() - badgeHeight) / 2f).toInt()
        val marginLeftAndRight = ((bounds.width() - badgeWidth) / 2f).toInt()

        // Ana arka plan cizimi (tum badge tipleri icin ortak)
        backgroundDrawable.setBounds(
            bounds.left + marginLeftAndRight,
            bounds.top + marginTopAndBottom,
            bounds.right - marginLeftAndRight,
            bounds.bottom - marginTopAndBottom
        )
        backgroundDrawable.paint.color = config.badgeColor
        backgroundDrawable.draw(canvas)

        // Metin dikey orta noktasi (font metriklerine gore)
        val textCx = bounds.centerX().toFloat()
        val textCy = bounds.centerY() - (fontMetrics.bottom + fontMetrics.top) / 2f

        val t1 = config.text1
        val t2 = config.text2

        when (config.badgeType) {
            // Tek metin: badge ortasina text1 yaz
            TYPE_ONLY_ONE_TEXT -> {
                paint.color = config.textColor
                canvas.drawText(cutText(t1, text1Width), textCx, textCy, paint)
            }

            // Tamamlayici cift metin: text1 solda, text2 sag bolumde ters renkli
            TYPE_WITH_TWO_TEXT_COMPLEMENTARY -> {
                // Text1'i sol tarafa yaz
                paint.color = config.textColor
                canvas.drawText(
                    t1,
                    marginLeftAndRight + config.paddingLeft + text1Width / 2f,
                    textCy,
                    paint
                )

                // Text2 icin sag tarafa tamamlayici arka plan ciz
                backgroundDrawableOfText2.setBounds(
                    (bounds.left + marginLeftAndRight + config.paddingLeft +
                            text1Width + config.paddingCenter / 2f).toInt(),
                    bounds.top + marginTopAndBottom + config.strokeWidth,
                    bounds.width() - marginLeftAndRight - config.strokeWidth,
                    bounds.bottom - marginTopAndBottom - config.strokeWidth
                )
                backgroundDrawableOfText2.paint.color = config.text2Color ?: config.textColor
                backgroundDrawableOfText2.draw(canvas)

                // Text2'yi badge rengiyle (ters renk) yaz
                paint.color = config.badgeColor
                canvas.drawText(
                    cutText(t2, text2Width),
                    bounds.width() - marginLeftAndRight - config.paddingRight - text2Width / 2f,
                    textCy,
                    paint
                )
            }

            // Cift metin: her iki bolum beyaz arka planli, metinler badge renkli
            TYPE_WITH_TWO_TEXT -> {
                // Text1 icin sol beyaz arka plan
                backgroundDrawableOfText1.setBounds(
                    bounds.left + marginLeftAndRight + config.strokeWidth,
                    bounds.top + marginTopAndBottom + config.strokeWidth,
                    (bounds.left + marginLeftAndRight + config.paddingLeft +
                            text1Width + config.paddingCenter / 2f - config.strokeWidth / 2f).toInt(),
                    bounds.bottom - marginTopAndBottom - config.strokeWidth
                )
                backgroundDrawableOfText1.paint.color = 0xffFFFFFF.toInt()
                backgroundDrawableOfText1.draw(canvas)

                // Text1'i badge rengiyle yaz
                paint.color = config.badgeColor
                canvas.drawText(
                    t1,
                    text1Width / 2f + marginLeftAndRight + config.paddingLeft,
                    textCy,
                    paint
                )

                // Text2 icin sag beyaz arka plan
                backgroundDrawableOfText2.setBounds(
                    (bounds.left + marginLeftAndRight + config.paddingLeft +
                            text1Width + config.paddingCenter / 2f + config.strokeWidth / 2f).toInt(),
                    bounds.top + marginTopAndBottom + config.strokeWidth,
                    bounds.width() - marginLeftAndRight - config.strokeWidth,
                    bounds.bottom - marginTopAndBottom - config.strokeWidth
                )
                backgroundDrawableOfText2.paint.color = 0xffFFFFFF.toInt()
                backgroundDrawableOfText2.draw(canvas)

                // Text2'yi badge rengiyle yaz
                paint.color = config.badgeColor
                canvas.drawText(
                    cutText(t2, text2Width),
                    bounds.width() - marginLeftAndRight - config.paddingRight - text2Width / 2f,
                    textCy,
                    paint
                )
            }

            // Sayi badge: ortaya sayi yaz
            else -> { // TYPE_NUMBER
                paint.color = config.textColor
                canvas.drawText(cutNumber(config.number, badgeWidth), textCx, textCy, paint)
            }
        }
    }

    /** Badge'in dogal genisligini dondurur (olculmus genislik). */
    override fun getIntrinsicWidth(): Int = badgeWidth

    /** Badge'in dogal yuksekligini dondurur (olculmus yukseklik). */
    override fun getIntrinsicHeight(): Int = badgeHeight

    /** Drawable seffaflik degerini ayarlar (0-255). */
    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    /** Drawable renk filtresini ayarlar. */
    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    // ════════════════════════════════════════
    //  Yardimci metodlar
    // ════════════════════════════════════════

    /**
     * Sayi mevcut genislige sigmiiyorsa "..." (ellipsis) karakteri dondurur.
     * @param number gosterilecek sayi
     * @param width mevcut piksel genisligi
     * @return sayi metni veya "..." karakteri
     */
    private fun cutNumber(number: Int, width: Int): String {
        val text = number.toString()
        return if (paint.measureText(text) < width) text else "\u2026"
    }

    /**
     * Metni mevcut genislige sigdirmak icin kirpar ve sonuna "..." ekler.
     * Metin sigmiyorsa karakter karakter kisaltir, hala sigmiyorsa "..." noktalarini da azaltir.
     * @param text kirpilacak metin
     * @param width mevcut piksel genisligi
     * @return kirpilmis metin veya orijinal metin
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
     * Badge'i SpannableString'e donusturur.
     * TextView icinde satir ici (inline) badge gostermek icin kullanilir.
     *
     * ```kotlin
     * val text = TextUtils.concat("Mesaj: ", badge.toSpannable(), " diger metin")
     * textView.text = text
     * ```
     *
     * @return Badge'i iceren SpannableString
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
//  Kotlin DSL uzanti fonksiyonu
// ════════════════════════════════════════

/**
 * BadgeDrawable olusturmak icin Kotlin DSL fonksiyonu.
 *
 * ```kotlin
 * val badge = badgeDrawable {
 *     type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
 *     text1("Yeni")
 *     badgeColor(0xff336699.toInt())
 * }
 * ```
 *
 * @param block Builder uzerinde yapilandirma blogu
 * @return yapilandirilmis BadgeDrawable
 */
inline fun badgeDrawable(block: BadgeDrawable.Builder.() -> Unit): BadgeDrawable {
    return BadgeDrawable.Builder().apply(block).build()
}
