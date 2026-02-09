package com.panda.karsubadge

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.panda.karsu_badge.BadgeDrawable
import com.panda.karsu_badge.badgeDrawable

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNumberBadges()
        setupOneTextBadges()
        setupTwoTextBadges()
        setupComplementaryBadges()
        setupCustomBadges()
        setupBuildUponBadges()
        setupDynamicBadges()
        setupImageViewBadge()
    }

    // ════════════════════════════════════════
    //  1) TYPE_NUMBER - Sayi badge ornekleri
    // ════════════════════════════════════════

    private fun setupNumberBadges() {
        val tv = findViewById<TextView>(R.id.tvNumberBadges)

        // Kucuk sayi - DSL kullanimi
        val badge1 = badgeDrawable {
            type(BadgeDrawable.TYPE_NUMBER)
            number(3)
        }

        // Orta sayi - farkli renk
        val badge2 = badgeDrawable {
            type(BadgeDrawable.TYPE_NUMBER)
            number(42)
            badgeColor(0xff336699.toInt())
        }

        // Buyuk sayi - sigmazsa "..." gosterir
        val badge3 = badgeDrawable {
            type(BadgeDrawable.TYPE_NUMBER)
            number(999)
            badgeColor(0xff009688.toInt())
        }

        // Farkli metin rengi
        val badge4 = badgeDrawable {
            type(BadgeDrawable.TYPE_NUMBER)
            number(7)
            badgeColor(0xff222222.toInt())
            textColor(0xffFFD700.toInt())
        }

        tv.text = SpannableString(
            TextUtils.concat(
                badge1.toSpannable(), "  ",
                badge2.toSpannable(), "  ",
                badge3.toSpannable(), "  ",
                badge4.toSpannable()
            )
        )
    }

    // ════════════════════════════════════════
    //  2) TYPE_ONLY_ONE_TEXT - Tek metin badge
    // ════════════════════════════════════════

    private fun setupOneTextBadges() {
        val tv = findViewById<TextView>(R.id.tvOneTextBadges)

        // Basit tek metin
        val badge1 = badgeDrawable {
            type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
            text1("VIP")
            badgeColor(0xff336699.toInt())
        }

        // Farkli renk
        val badge2 = badgeDrawable {
            type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
            text1("YENi")
            badgeColor(0xffE91E63.toInt())
        }

        // Uzun metin
        val badge3 = badgeDrawable {
            type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
            text1("PREMIUM")
            badgeColor(0xffFF9800.toInt())
            textColor(0xff000000.toInt())
        }

        // Builder pattern ile
        val badge4 = BadgeDrawable.Builder()
            .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
            .text1("BETA")
            .badgeColor(0xff9C27B0.toInt())
            .build()

        tv.text = SpannableString(
            TextUtils.concat(
                badge1.toSpannable(), "  ",
                badge2.toSpannable(), "  ",
                badge3.toSpannable(), "  ",
                badge4.toSpannable()
            )
        )
    }

    // ════════════════════════════════════════
    //  3) TYPE_WITH_TWO_TEXT - Cift metin badge
    // ════════════════════════════════════════

    private fun setupTwoTextBadges() {
        val tv = findViewById<TextView>(R.id.tvTwoTextBadges)

        // Test durumu
        val badge1 = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
            text1("TEST")
            text2("Pass")
            badgeColor(0xff4CAF50.toInt())
        }

        // Versiyon gosterimi
        val badge2 = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
            text1("v2.1")
            text2("Stable")
            badgeColor(0xff2196F3.toInt())
        }

        // Durum gosterimi
        val badge3 = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
            text1("BUILD")
            text2("Fail")
            badgeColor(0xffF44336.toInt())
        }

        tv.text = SpannableString(
            TextUtils.concat(
                badge1.toSpannable(), "  ",
                badge2.toSpannable(), "  ",
                badge3.toSpannable()
            )
        )
    }

    // ════════════════════════════════════════
    //  4) TYPE_WITH_TWO_TEXT_COMPLEMENTARY
    // ════════════════════════════════════════

    private fun setupComplementaryBadges() {
        val tv = findViewById<TextView>(R.id.tvComplementaryBadges)

        // Seviye gosterimi
        val badge1 = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
            text1("LEVEL")
            text2("10")
            badgeColor(0xffCC9933.toInt())
        }

        // Puan gosterimi
        val badge2 = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
            text1("SCORE")
            text2("98")
            badgeColor(0xff673AB7.toInt())
        }

        // Ozel text2 arka plan rengi (text2Color)
        val badge3 = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
            text1("STATUS")
            text2("Online")
            badgeColor(0xff00796B.toInt())
            text2Color(0xffB2DFDB.toInt())
        }

        tv.text = SpannableString(
            TextUtils.concat(
                badge1.toSpannable(), "  ",
                badge2.toSpannable(), "  ",
                badge3.toSpannable()
            )
        )
    }

    // ════════════════════════════════════════
    //  5) Ozel yapilandirma ornekleri
    //     textSize, padding, cornerRadius, typeface, strokeWidth
    // ════════════════════════════════════════

    private fun setupCustomBadges() {
        val tv = findViewById<TextView>(R.id.tvCustomBadges)

        // Buyuk metin boyutu
        val badge1 = badgeDrawable {
            type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
            text1("Buyuk")
            textSize(spToPx(16f))
            badgeColor(0xffE91E63.toInt())
        }

        // Ozel padding degerleri
        val badge2 = badgeDrawable {
            type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
            text1("Genis")
            badgeColor(0xff3F51B5.toInt())
            padding(left = dpToPx(12f), top = dpToPx(4f), right = dpToPx(12f), bottom = dpToPx(4f))
        }

        // Buyuk kose yuvarlama
        val badge3 = badgeDrawable {
            type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
            text1("Yuvarlak")
            badgeColor(0xffFF5722.toInt())
            cornerRadius(dpToPx(20f))
        }

        // Monospace typeface
        val badge4 = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
            text1("MONO")
            text2("Font")
            badgeColor(0xff607D8B.toInt())
            typeface(Typeface.MONOSPACE)
        }

        // Kalin stroke
        val badge5 = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
            text1("KALIN")
            text2("Border")
            badgeColor(0xff795548.toInt())
            strokeWidth(dpToPx(3f).toInt())
        }

        tv.text = SpannableString(
            TextUtils.concat(
                badge1.toSpannable(), "  ",
                badge2.toSpannable(), "  ",
                badge3.toSpannable(), "  ",
                badge4.toSpannable(), "  ",
                badge5.toSpannable()
            )
        )
    }

    // ════════════════════════════════════════
    //  6) buildUpon() - Mevcut badge'den turetme
    // ════════════════════════════════════════

    private fun setupBuildUponBadges() {
        val tv = findViewById<TextView>(R.id.tvBuildUponBadges)

        // Orijinal badge
        val original = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
            text1("VER")
            text2("1.0")
            badgeColor(0xff2196F3.toInt())
        }

        // buildUpon ile sadece text2'yi degistir
        val modified1 = original.buildUpon()
            .text2("2.0")
            .build()

        // buildUpon ile rengi de degistir
        val modified2 = original.buildUpon()
            .text2("3.0")
            .badgeColor(0xffF44336.toInt())
            .build()

        tv.text = SpannableString(
            TextUtils.concat(
                "Orijinal: ", original.toSpannable(),
                "  v2: ", modified1.toSpannable(),
                "  v3: ", modified2.toSpannable()
            )
        )
    }

    // ════════════════════════════════════════
    //  7) Dinamik Setter kullanimi
    // ════════════════════════════════════════

    private fun setupDynamicBadges() {
        val tv = findViewById<TextView>(R.id.tvDynamicBadges)

        // Badge olustur ve sonradan setter ile guncelle
        val badge = BadgeDrawable.Builder()
            .type(BadgeDrawable.TYPE_NUMBER)
            .number(1)
            .badgeColor(0xffCC3333.toInt())
            .build()

        // Setter ile sayiyi degistir
        badge.setNumber(55)
        // Setter ile rengi degistir
        badge.setBadgeColor(0xff009688.toInt())

        // Tip degistirme ornegi
        val badge2 = BadgeDrawable.Builder()
            .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
            .text1("Eski")
            .badgeColor(0xff9E9E9E.toInt())
            .build()

        // Setter ile metin degistir
        badge2.setText1("Guncellendi")
        badge2.setBadgeColor(0xff4CAF50.toInt())
        badge2.setTextColor(0xffFFFFFF.toInt())

        tv.text = SpannableString(
            TextUtils.concat(
                "Sayi(55): ", badge.toSpannable(),
                "  Metin: ", badge2.toSpannable()
            )
        )
    }

    // ════════════════════════════════════════
    //  8) ImageView ile kullanim
    // ════════════════════════════════════════

    private fun setupImageViewBadge() {
        val iv = findViewById<ImageView>(R.id.ivBadge)

        val badge = BadgeDrawable.Builder()
            .type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
            .badgeColor(0xff336633.toInt())
            .textSize(spToPx(14f))
            .text1("Author")
            .text2("KarsuBadge")
            .cornerRadius(dpToPx(4f))
            .padding(
                left = dpToPx(6f),
                top = dpToPx(4f),
                right = dpToPx(6f),
                bottom = dpToPx(4f),
                center = dpToPx(6f)
            )
            .build()

        iv.setImageDrawable(badge)
    }

    // ════════════════════════════════════════
    //  Yardimci birim donusum fonksiyonlari
    // ════════════════════════════════════════

    private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density + 0.5f
    }
}
