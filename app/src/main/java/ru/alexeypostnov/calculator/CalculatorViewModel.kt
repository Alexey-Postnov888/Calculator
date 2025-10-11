package ru.alexeypostnov.calculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorViewModel : ViewModel() {
    private val _displayText = MutableLiveData("0")
    val displayText: LiveData<String> = _displayText

    private var currentInput = StringBuilder()
    private var resetForNextInput = false

    data class OperatorInfo(
        val displaySymbol: String,
        val computationSymbol: String
    )

    enum class OperatorType {
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        SEPARATOR,
        OPEN_BRACKET,
        CLOSE_BRACKET,
        POW
    }

    private fun String.unescape(): String {
        return this.replace("\\\\u([0-9a-fA-F]{4})".toRegex()) {
            it.groupValues[1].toInt(16).toChar().toString()
        }
    }

    private val operators = mapOf(
        OperatorType.PLUS to OperatorInfo("\\u002B".unescape(), "+"),
        OperatorType.MINUS to OperatorInfo("\\u2212".unescape(), "-"),
        OperatorType.MULTIPLY to OperatorInfo("\\u00D7".unescape(), "*"),
        OperatorType.DIVIDE to OperatorInfo("\\u00F7".unescape(), "/"),
        OperatorType.SEPARATOR to OperatorInfo(".", "."),
        OperatorType.OPEN_BRACKET to OperatorInfo("\\u0028".unescape(), "("),
        OperatorType.CLOSE_BRACKET to OperatorInfo("\\u0029".unescape(), ")"),
        OperatorType.POW to OperatorInfo("^", "^")
    )

    private val allDisplaySymbols by lazy {
        val openBracket = operators[OperatorType.OPEN_BRACKET]?.computationSymbol
        val closeBracket = operators[OperatorType.CLOSE_BRACKET]?.computationSymbol

        operators.values.map { it.displaySymbol }.toSet().filter { it != openBracket && it != closeBracket }
    }

    private fun findOperatorTypeByDisplaySymbol(displaySymbol: String): OperatorType? {
        return operators.entries.find { it.value.displaySymbol == displaySymbol }?.key
    }

    fun onNumberClick(number: String) {
        if (resetForNextInput) {
            currentInput.clear()
            resetForNextInput = false
        }

        if (currentInput.isEmpty() && number == "0") return

        if (currentInput.toString() == "0") {
            currentInput.clear()
        }

        currentInput.append(number)
        _displayText.value = currentInput.toString()

    }

    fun onOperatorClick(operator: String) {
        val operatorType = findOperatorTypeByDisplaySymbol(operator)

        if (resetForNextInput) resetForNextInput = false

        if (currentInput.isEmpty()) {
            when (operatorType) {
                OperatorType.SEPARATOR -> {
                    _displayText.value = currentInput.append("0").append(operator).toString()
                    return
                }
                OperatorType.MINUS -> {
                    _displayText.value = currentInput.append(operator).toString()
                    return
                }
                else -> return
            }
        }

        val lastChar = currentInput[currentInput.length - 1].toString()

        if (lastChar == operator) return

        if (allDisplaySymbols.contains(lastChar)) {
            currentInput.deleteCharAt(currentInput.length - 1)
        }

        currentInput.append(operator)
        _displayText.value = currentInput.toString()
    }

    fun onClearAllClick() {
        currentInput.clear()
        _displayText.value = "0"
    }

    fun onBackspaceClick() {
        if (currentInput.isNotEmpty()) {
            currentInput.deleteAt(currentInput.length - 1)
            _displayText.value = if (currentInput.isEmpty()) "0" else currentInput.toString()
        }
    }

    private fun String.isOperator(): Boolean {
        val operatorType = findOperatorTypeByDisplaySymbol(this)
        return operatorType != null && operatorType != OperatorType.OPEN_BRACKET && operatorType != OperatorType.CLOSE_BRACKET
    }

    fun onParenthesisOnClick() {
        val openBracket = operators[OperatorType.OPEN_BRACKET]?.computationSymbol
        val closeBracket = operators[OperatorType.CLOSE_BRACKET]?.computationSymbol
        val lastSymbol = if (currentInput.isNotEmpty()) currentInput[currentInput.length - 1].toString() else ""

        val openBracketsCount = currentInput.count { it.toString() == openBracket }
        val closeBracketsCount = currentInput.count { it.toString() == closeBracket }

        if (lastSymbol == closeBracket) {
            if (openBracketsCount > closeBracketsCount) {
                currentInput.append(closeBracket)
            }
        }

        else if (currentInput.isEmpty() || lastSymbol.isOperator() || lastSymbol == openBracket) {
            currentInput.append(openBracket)
        }

        else if (openBracketsCount > closeBracketsCount) {
            currentInput.append(closeBracket)
        }

        _displayText.value = currentInput.toString()
    }

    fun calculate() {
        try {
            if (currentInput.isEmpty() || currentInput.toString() == "0") return

            val lastChar = currentInput[currentInput.length - 1].toString()
            if (allDisplaySymbols.contains(lastChar)) currentInput.deleteAt(currentInput.length - 1)

            var input = currentInput.toString()
            operators.values.forEach { operator ->
                if (operator.displaySymbol != operator.computationSymbol) {
                    input = input.replace(operator.displaySymbol, operator.computationSymbol)
                }
            }
            val result = ExpressionBuilder(input).build().evaluate()

            val formattedResult = if (result%1 == 0.0) {
                result.toLong().toString()
            } else {
                result.toString()
            }

            _displayText.value = formattedResult
            currentInput.clear().append(formattedResult)
            resetForNextInput = true
        } catch (e: Exception) {
            _displayText.value = "Оой..."
            currentInput.clear()
            resetForNextInput = false
        }
    }
}