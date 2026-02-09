package com.panda.karsubadge

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.util.TypedValue
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.panda.karsu_badge.BadgeDrawable
import com.panda.karsu_badge.badgeDrawable
import com.panda.karsubadge.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNumberBadges()
        setupSingleTextBadges()
        setupTwoTextBadges()
        setupComplementaryBadges()
        setupCustomStylingBadges()
        setupBuildUponBadges()
        setupDynamicSetterBadges()
        setupImageViewBadge()
        setupPracticalUsage()
        setupConfiguratorButton()
    }

    // -- Number badge examples --

    private fun setupNumberBadges() {
        binding.tvNumberBadges.showBadges(
            badgeDrawable { type(BadgeDrawable.TYPE_NUMBER); number(3) },
            badgeDrawable { type(BadgeDrawable.TYPE_NUMBER); number(42); badgeColor(0xff336699.toInt()) },
            badgeDrawable { type(BadgeDrawable.TYPE_NUMBER); number(999); badgeColor(0xff009688.toInt()) },
            badgeDrawable { type(BadgeDrawable.TYPE_NUMBER); number(7); badgeColor(0xff222222.toInt()); textColor(0xffFFD700.toInt()) }
        )
    }

    // -- Single text badge examples --

    private fun setupSingleTextBadges() {
        binding.tvOneTextBadges.showBadges(
            badgeDrawable { type(BadgeDrawable.TYPE_ONLY_ONE_TEXT); text1("VIP"); badgeColor(0xff336699.toInt()) },
            badgeDrawable { type(BadgeDrawable.TYPE_ONLY_ONE_TEXT); text1(getString(R.string.badge_new)); badgeColor(0xffE91E63.toInt()) },
            badgeDrawable { type(BadgeDrawable.TYPE_ONLY_ONE_TEXT); text1("PREMIUM"); badgeColor(0xffFF9800.toInt()); textColor(0xff000000.toInt()) },
            BadgeDrawable.Builder()
                .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                .text1("BETA")
                .badgeColor(0xff9C27B0.toInt())
                .build()
        )
    }

    // -- Two text (standard) badge examples --

    private fun setupTwoTextBadges() {
        binding.tvTwoTextBadges.showBadges(
            badgeDrawable { type(BadgeDrawable.TYPE_WITH_TWO_TEXT); text1("TEST"); text2("Pass"); badgeColor(0xff4CAF50.toInt()) },
            badgeDrawable { type(BadgeDrawable.TYPE_WITH_TWO_TEXT); text1("v2.1"); text2("Stable"); badgeColor(0xff2196F3.toInt()) },
            badgeDrawable { type(BadgeDrawable.TYPE_WITH_TWO_TEXT); text1("BUILD"); text2("Fail"); badgeColor(0xffF44336.toInt()) }
        )
    }

    // -- Two text (complementary) badge examples --

    private fun setupComplementaryBadges() {
        binding.tvComplementaryBadges.showBadges(
            badgeDrawable { type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY); text1("LEVEL"); text2("10"); badgeColor(0xffCC9933.toInt()) },
            badgeDrawable { type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY); text1("SCORE"); text2("98"); badgeColor(0xff673AB7.toInt()) },
            badgeDrawable { type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY); text1("STATUS"); text2("Online"); badgeColor(0xff00796B.toInt()); text2Color(0xffB2DFDB.toInt()) }
        )
    }

    // -- Custom styling examples (textSize, padding, cornerRadius, typeface, strokeWidth) --

    private fun setupCustomStylingBadges() {
        binding.tvCustomBadges.showBadges(
            badgeDrawable { type(BadgeDrawable.TYPE_ONLY_ONE_TEXT); text1(getString(R.string.badge_large)); textSize(spToPx(16f)); badgeColor(0xffE91E63.toInt()) },
            badgeDrawable { type(BadgeDrawable.TYPE_ONLY_ONE_TEXT); text1(getString(R.string.badge_wide)); badgeColor(0xff3F51B5.toInt()); padding(left = dpToPx(12f), top = dpToPx(4f), right = dpToPx(12f), bottom = dpToPx(4f)) },
            badgeDrawable { type(BadgeDrawable.TYPE_ONLY_ONE_TEXT); text1(getString(R.string.badge_rounded)); badgeColor(0xffFF5722.toInt()); cornerRadius(dpToPx(20f)) },
            badgeDrawable { type(BadgeDrawable.TYPE_WITH_TWO_TEXT); text1("MONO"); text2("Font"); badgeColor(0xff607D8B.toInt()); typeface(Typeface.MONOSPACE) },
            badgeDrawable { type(BadgeDrawable.TYPE_WITH_TWO_TEXT); text1(getString(R.string.badge_thick)); text2("Border"); badgeColor(0xff795548.toInt()); strokeWidth(dpToPx(3f).toInt()) }
        )
    }

    // -- buildUpon() cloning example --

    private fun setupBuildUponBadges() {
        val original = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
            text1("VER"); text2("1.0"); badgeColor(0xff2196F3.toInt())
        }
        val v2 = original.buildUpon().text2("2.0").build()
        val v3 = original.buildUpon().text2("3.0").badgeColor(0xffF44336.toInt()).build()

        binding.tvBuildUponBadges.showLabeledBadges(
            getString(R.string.label_original) to original,
            getString(R.string.label_v2) to v2,
            getString(R.string.label_v3) to v3
        )
    }

    // -- Dynamic setter mutation example --

    private fun setupDynamicSetterBadges() {
        val numberBadge = BadgeDrawable.Builder()
            .type(BadgeDrawable.TYPE_NUMBER).number(1).badgeColor(0xffCC3333.toInt()).build()
        numberBadge.setNumber(55)
        numberBadge.setBadgeColor(0xff009688.toInt())

        val textBadge = BadgeDrawable.Builder()
            .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT).text1(getString(R.string.badge_old)).badgeColor(0xff9E9E9E.toInt()).build()
        textBadge.setText1(getString(R.string.badge_updated))
        textBadge.setBadgeColor(0xff4CAF50.toInt())

        binding.tvDynamicBadges.showLabeledBadges(
            getString(R.string.label_number_result) to numberBadge,
            getString(R.string.label_text_result) to textBadge
        )
    }

    // -- ImageView badge example --

    private fun setupImageViewBadge() {
        val badge = BadgeDrawable.Builder()
            .type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
            .badgeColor(0xff006A6A.toInt())
            .textSize(spToPx(14f))
            .text1("Author")
            .text2("KarsuBadge")
            .cornerRadius(dpToPx(4f))
            .padding(dpToPx(6f), dpToPx(4f), dpToPx(6f), dpToPx(4f), dpToPx(6f))
            .build()
        binding.ivBadge.setImageDrawable(badge)
    }

    // -- Practical usage: inline badges with text, like real-world labels --

    private fun setupPracticalUsage() {
        val priceBadge = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
            text1("PRICE"); text2("$29.99"); badgeColor(0xff006A6A.toInt())
            textSize(spToPx(14f)); cornerRadius(dpToPx(4f))
            padding(dpToPx(6f), dpToPx(4f), dpToPx(6f), dpToPx(4f), dpToPx(6f))
        }
        val statusBadge = badgeDrawable {
            type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
            text1("Online"); badgeColor(0xff4CAF50.toInt())
            textSize(spToPx(12f)); cornerRadius(dpToPx(4f))
            padding(dpToPx(6f), dpToPx(3f), dpToPx(6f), dpToPx(3f))
        }
        val versionBadge = badgeDrawable {
            type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
            text1("v2.1"); text2("Stable"); badgeColor(0xff2196F3.toInt())
            textSize(spToPx(12f)); cornerRadius(dpToPx(4f))
            padding(dpToPx(6f), dpToPx(3f), dpToPx(6f), dpToPx(3f), dpToPx(4f))
        }

        val parts = mutableListOf<CharSequence>(
            "Price: ", priceBadge.toSpannable(),
            "  Status: ", statusBadge.toSpannable(),
            "  Version: ", versionBadge.toSpannable()
        )
        binding.tvPracticalInline.text = SpannableString(TextUtils.concat(*parts.toTypedArray()))
    }

    // -- Configurator launcher --

    private fun setupConfiguratorButton() {
        binding.btnLaunchConfigurator.setOnClickListener {
            startActivity(Intent(this, ConfiguratorActivity::class.java))
        }
    }

    // -- Helper: display multiple badges in a TextView --

    private fun TextView.showBadges(vararg badges: BadgeDrawable) {
        val parts = mutableListOf<CharSequence>()
        badges.forEachIndexed { i, badge ->
            if (i > 0) parts.add("  ")
            parts.add(badge.toSpannable())
        }
        text = SpannableString(TextUtils.concat(*parts.toTypedArray()))
    }

    // -- Helper: display labeled badges (e.g. "Original: [badge]  v2: [badge]") --

    private fun TextView.showLabeledBadges(vararg pairs: Pair<String, BadgeDrawable>) {
        val parts = mutableListOf<CharSequence>()
        pairs.forEach { (label, badge) ->
            parts.add(label)
            parts.add(badge.toSpannable())
        }
        text = SpannableString(TextUtils.concat(*parts.toTypedArray()))
    }

    // -- Unit conversion utilities --

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
