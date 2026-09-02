package com.cashbooknepal.app.utils.calculator

/**
 * Safe recursive-descent arithmetic evaluator for the Amount field's inline calculator.
 * Supports +, -, * / ×, / / ÷, decimals, and parentheses. No eval/reflection is used.
 */
object ExpressionEvaluator {

    /**
     * Returns the computed result, or null if [input] is blank, malformed,
     * or involves a division by zero.
     */
    fun evaluate(input: String): Double? {
        val normalized = input
            .trim()
            .replace('×', '*')
            .replace('÷', '/')
            .replace(",", "")

        if (normalized.isEmpty()) return null
        if (!normalized.all { it.isDigit() || it in "+-*/(). " }) return null

        return try {
            val parser = Parser(normalized)
            val result = parser.parseExpression()
            if (!parser.isAtEnd()) null else result
        } catch (e: ArithmeticException) {
            null
        } catch (e: IllegalStateException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private class Parser(private val text: String) {
        private var pos = 0

        fun isAtEnd(): Boolean {
            skipSpaces()
            return pos >= text.length
        }

        fun parseExpression(): Double {
            var result = parseTerm()
            while (true) {
                skipSpaces()
                when (peek()) {
                    '+' -> { pos++; result += parseTerm() }
                    '-' -> { pos++; result -= parseTerm() }
                    else -> return result
                }
            }
        }

        private fun parseTerm(): Double {
            var result = parseFactor()
            while (true) {
                skipSpaces()
                when (peek()) {
                    '*' -> { pos++; result *= parseFactor() }
                    '/' -> {
                        pos++
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        result /= divisor
                    }
                    else -> return result
                }
            }
        }

        private fun parseFactor(): Double {
            skipSpaces()
            when (peek()) {
                '+' -> { pos++; return parseFactor() }
                '-' -> { pos++; return -parseFactor() }
                '(' -> {
                    pos++
                    val result = parseExpression()
                    skipSpaces()
                    if (peek() != ')') throw IllegalStateException("Expected ')'")
                    pos++
                    return result
                }
                else -> return parseNumber()
            }
        }

        private fun parseNumber(): Double {
            skipSpaces()
            val start = pos
            var sawDigitOrDot = false
            while (pos < text.length && (text[pos].isDigit() || text[pos] == '.')) {
                sawDigitOrDot = true
                pos++
            }
            if (!sawDigitOrDot || start == pos) throw IllegalStateException("Expected number")
            return text.substring(start, pos).toDoubleOrNull()
                ?: throw IllegalStateException("Invalid number")
        }

        private fun skipSpaces() {
            while (pos < text.length && text[pos] == ' ') pos++
        }

        private fun peek(): Char? = if (pos < text.length) text[pos] else null
    }
}
