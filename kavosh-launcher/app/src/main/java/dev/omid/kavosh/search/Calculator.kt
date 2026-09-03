package dev.omid.kavosh.search

/**
 * Tiny recursive-descent arithmetic evaluator for the search bar's calculator mode.
 * Supports + - * / ^ (), decimals, unary minus, and Persian digits (۰-۹) so typing
 * "۱۲ * ۳" works the same as "12 * 3".
 *
 * Deliberately dependency-free: no need to pull in a math library for "what's 15% of 480".
 */
object Calculator {

    /** Returns the numeric result, or null if [input] isn't a recognizable expression. */
    fun tryEvaluate(input: String): Double? {
        val normalized = normalize(input)
        if (normalized.isBlank() || !looksLikeExpression(normalized)) return null
        return try {
            Parser(normalized).parseFully()
        } catch (_: Exception) {
            null
        }
    }

    private fun normalize(input: String): String {
        val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
        val sb = StringBuilder()
        for (c in input.trim()) {
            val pIdx = persianDigits.indexOf(c)
            val aIdx = arabicDigits.indexOf(c)
            when {
                pIdx >= 0 -> sb.append(pIdx)
                aIdx >= 0 -> sb.append(aIdx)
                c == '×' -> sb.append('*')
                c == '÷' -> sb.append('/')
                c == '٫' || c == '،' -> sb.append('.')
                else -> sb.append(c)
            }
        }
        return sb.toString().replace(" ", "")
    }

    /** Quick rejection so plain text like "instagram" never enters the parser. */
    private fun looksLikeExpression(s: String): Boolean {
        if (!s.any { it.isDigit() }) return false
        return s.all { it.isDigit() || it in "+-*/^().%" }
    }

    private class Parser(private val src: String) {
        private var pos = 0

        fun parseFully(): Double {
            val result = parseExpression()
            if (pos != src.length) error("Unexpected trailing input")
            return result
        }

        private fun parseExpression(): Double {
            var value = parseTerm()
            while (pos < src.length && (src[pos] == '+' || src[pos] == '-')) {
                val op = src[pos++]
                val rhs = parseTerm()
                value = if (op == '+') value + rhs else value - rhs
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parsePower()
            while (pos < src.length && (src[pos] == '*' || src[pos] == '/' || src[pos] == '%')) {
                val op = src[pos++]
                val rhs = parsePower()
                value = when (op) {
                    '*' -> value * rhs
                    '/' -> value / rhs
                    else -> value % rhs
                }
            }
            return value
        }

        private fun parsePower(): Double {
            val base = parseUnary()
            if (pos < src.length && src[pos] == '^') {
                pos++
                val exp = parsePower() // right-associative
                return Math.pow(base, exp)
            }
            return base
        }

        private fun parseUnary(): Double {
            if (pos < src.length && src[pos] == '-') {
                pos++
                return -parseUnary()
            }
            if (pos < src.length && src[pos] == '+') {
                pos++
                return parseUnary()
            }
            return parseAtom()
        }

        private fun parseAtom(): Double {
            if (pos < src.length && src[pos] == '(') {
                pos++
                val value = parseExpression()
                if (pos >= src.length || src[pos] != ')') error("Missing )")
                pos++
                return value
            }
            val start = pos
            while (pos < src.length && (src[pos].isDigit() || src[pos] == '.')) pos++
            if (start == pos) error("Expected number at $pos")
            return src.substring(start, pos).toDouble()
        }
    }
}
