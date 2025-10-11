package ru.alexeypostnov.calculator

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.color.DynamicColors
import ru.alexeypostnov.calculator.databinding.FragmentButtonsBinding

class ButtonsFragment : Fragment() {
    private var _binding: FragmentButtonsBinding? = null
    private val binding: FragmentButtonsBinding
        get() = requireNotNull(_binding)

    private val viewModel: CalculatorViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentButtonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (i in 0 until binding.root.childCount) {
            val child = binding.root.getChildAt(i)
            if (child is Button) {
                child.setOnClickListener { handleClick(it as Button) }
            }
        }
    }

    private fun handleClick(button: Button) {
        button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

        when (button.tag) {
            "number" -> viewModel.onNumberClick(button.text.toString())
            "operator" -> viewModel.onOperatorClick(button.text.toString())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
