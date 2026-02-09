package com.panda.karsubadge

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.Slider
import com.panda.karsu_badge.BadgeDrawable
import com.panda.karsubadge.databinding.ActivityConfiguratorBinding

class ConfiguratorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguratorBinding

    // Current badge state
    private var currentBadgeType = BadgeDrawable.TYPE_ONLY_ONE_TEXT
    private var currentNumber = 0
    private var currentText1 = "Preview"
    private var currentText2 = "Text"
    private var currentTextSizeSp = 14f
    private var currentBadgeColor = 0xff006A6A.toInt()
    private var currentTextColor = 0xffFFFFFF.toInt()
    private var currentCornerRadiusDp = 4f
    private var currentPaddingDp = 4f
    private var currentPaddingCenterDp = 6f
    private var currentStrokeWidthDp = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConfiguratorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.configuratorRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBadgeTypeDropdown()
        setupTextInputs()
        setupColorButtons()
        setupSliders()
        updatePreview()
    }

    // -- Badge type dropdown --

    private fun setupBadgeTypeDropdown() {
        val types = listOf(
            getString(R.string.type_one_text),
            getString(R.string.type_number),
            getString(R.string.type_two_text),
            getString(R.string.type_two_text_complementary)
        )
        val typeValues = listOf(
            BadgeDrawable.TYPE_ONLY_ONE_TEXT,
            BadgeDrawable.TYPE_NUMBER,
            BadgeDrawable.TYPE_WITH_TWO_TEXT,
            BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        binding.spinnerBadgeType.setAdapter(adapter)
        binding.spinnerBadgeType.setText(types[0], false)

        binding.spinnerBadgeType.setOnItemClickListener { _, _, position, _ ->
            currentBadgeType = typeValues[position]
            updateControlVisibility()
            updatePreview()
        }
    }

    // -- Text inputs --

    private fun setupTextInputs() {
        binding.etNumber.addTextChangedListener(simpleWatcher {
            currentNumber = it.toIntOrNull() ?: 0
            updatePreview()
        })
        binding.etText1.addTextChangedListener(simpleWatcher {
            currentText1 = it
            updatePreview()
        })
        binding.etText2.addTextChangedListener(simpleWatcher {
            currentText2 = it
            updatePreview()
        })
    }

    // -- Color preset buttons --

    private fun setupColorButtons() {
        // Badge color presets
        binding.btnBadgeColorTeal.setOnClickListener { setBadgeColor(0xff006A6A.toInt()) }
        binding.btnBadgeColorRed.setOnClickListener { setBadgeColor(0xffCC3333.toInt()) }
        binding.btnBadgeColorBlue.setOnClickListener { setBadgeColor(0xff2196F3.toInt()) }
        binding.btnBadgeColorGreen.setOnClickListener { setBadgeColor(0xff4CAF50.toInt()) }
        binding.btnBadgeColorOrange.setOnClickListener { setBadgeColor(0xffFF9800.toInt()) }
        binding.btnBadgeColorPurple.setOnClickListener { setBadgeColor(0xff9C27B0.toInt()) }
        binding.btnBadgeColorPink.setOnClickListener { setBadgeColor(0xffE91E63.toInt()) }
        binding.btnBadgeColorBlack.setOnClickListener { setBadgeColor(0xff222222.toInt()) }

        // Text color presets
        binding.btnTextColorWhite.setOnClickListener { setTextColor(0xffFFFFFF.toInt()) }
        binding.btnTextColorBlack.setOnClickListener { setTextColor(0xff000000.toInt()) }
        binding.btnTextColorYellow.setOnClickListener { setTextColor(0xffFFD700.toInt()) }
        binding.btnTextColorCyan.setOnClickListener { setTextColor(0xff00BCD4.toInt()) }
    }

    private fun setBadgeColor(color: Int) {
        currentBadgeColor = color
        updatePreview()
    }

    private fun setTextColor(color: Int) {
        currentTextColor = color
        updatePreview()
    }

    // -- Sliders --

    private fun setupSliders() {
        binding.sliderTextSize.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            currentTextSizeSp = value
            binding.tvTextSizeValue.text = "${value.toInt()}sp"
            updatePreview()
        })

        binding.sliderCornerRadius.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            currentCornerRadiusDp = value
            binding.tvCornerRadiusValue.text = "${value.toInt()}dp"
            updatePreview()
        })

        binding.sliderPadding.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            currentPaddingDp = value
            binding.tvPaddingValue.text = "${value.toInt()}dp"
            updatePreview()
        })

        binding.sliderPaddingCenter.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            currentPaddingCenterDp = value
            binding.tvPaddingCenterValue.text = "${value.toInt()}dp"
            updatePreview()
        })

        binding.sliderStrokeWidth.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            currentStrokeWidthDp = value
            binding.tvStrokeWidthValue.text = "${value.toInt()}dp"
            updatePreview()
        })
    }

    // -- Live preview update --

    private fun updatePreview() {
        val paddingPx = dpToPx(currentPaddingDp)
        val badge = BadgeDrawable.Builder()
            .type(currentBadgeType)
            .number(currentNumber)
            .text1(currentText1)
            .text2(currentText2)
            .textSize(spToPx(currentTextSizeSp))
            .badgeColor(currentBadgeColor)
            .textColor(currentTextColor)
            .cornerRadius(dpToPx(currentCornerRadiusDp))
            .padding(paddingPx, paddingPx, paddingPx, paddingPx, dpToPx(currentPaddingCenterDp))
            .strokeWidth(dpToPx(currentStrokeWidthDp).toInt())
            .build()

        binding.badgePreview.setBadgeDrawable(badge)
        binding.tvSpannablePreview.text = badge.toSpannable()
    }

    // -- Show/hide controls based on badge type --

    private fun updateControlVisibility() {
        val isNumber = currentBadgeType == BadgeDrawable.TYPE_NUMBER
        val isDualText = currentBadgeType == BadgeDrawable.TYPE_WITH_TWO_TEXT ||
                currentBadgeType == BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY

        binding.tilNumber.visibility = if (isNumber) View.VISIBLE else View.GONE
        binding.tilText1.visibility = if (!isNumber) View.VISIBLE else View.GONE
        binding.tilText2.visibility = if (isDualText) View.VISIBLE else View.GONE
        binding.layoutPaddingCenter.visibility = if (isDualText) View.VISIBLE else View.GONE
    }

    // -- Utility --

    private fun simpleWatcher(onChanged: (String) -> Unit): TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            onChanged(s?.toString() ?: "")
        }
    }

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
