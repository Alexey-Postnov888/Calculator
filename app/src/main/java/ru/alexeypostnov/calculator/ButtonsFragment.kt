package ru.alexeypostnov.calculator

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.color.DynamicColors
import dev.androidbroadcast.vbpd.viewBinding
import ru.alexeypostnov.calculator.databinding.FragmentButtonsBinding

class ButtonsFragment : Fragment(R.layout.fragment_buttons) {
    private val binding: FragmentButtonsBinding by viewBinding(FragmentButtonsBinding::bind)
    private val viewModel: CalculatorViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(requireActivity())
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typeface = ResourcesCompat.getFont(requireContext(), R.font.comfortaa)

        for (i in 0 until binding.root.childCount) {
            val child = binding.root.getChildAt(i)
            if (child is Button) {
                child.typeface = typeface
                child.setOnClickListener { handleClick(it as Button) }
            }
        }
    }

    private fun handleClick(button: Button) {
        button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

        when (button.tag) {
            "number" -> viewModel.onNumberClick(button.text.toString())
            "operator" -> {
                when (button.id) {
                    binding.powBtn.id -> viewModel.onOperatorClick("^")
                    else -> viewModel.onOperatorClick(button.text.toString())
                }
            }
            "function" -> handleFunctionClick(button.id)
            "parenthesis" -> viewModel.onParenthesisOnClick()
        }
    }

    private fun handleFunctionClick(buttonId: Int) {
        when (buttonId) {
            R.id.clearAllBtn -> viewModel.onClearAllClick()
            R.id.backspaceBtn -> viewModel.onBackspaceClick()
            R.id.equalsBtn -> viewModel.calculate()
        }
    }
}
