package com.example.math

import java.util.Locale
import kotlin.math.abs

/**
 * Symbolic algebra engine for polynomial expansion, like-term collection,
 * simplification, factoring, and symbolic differentiation with variable support.
 */
object SymbolicAlgebra {

  /**
   * Represents a single variable polynomial in x:
   * terms[power] = coefficient
   */
  data class Polynomial(val terms: Map<Int, Double>) {

    operator fun plus(other: Polynomial): Polynomial {
      val result = terms.toMutableMap()
      for ((pow, coeff) in other.terms) {
        result[pow] = (result[pow] ?: 0.0) + coeff
      }
      return Polynomial(result.filterValues { abs(it) > 1e-10 })
    }

    operator fun minus(other: Polynomial): Polynomial {
      val result = terms.toMutableMap()
      for ((pow, coeff) in other.terms) {
        result[pow] = (result[pow] ?: 0.0) - coeff
      }
      return Polynomial(result.filterValues { abs(it) > 1e-10 })
    }

    operator fun times(other: Polynomial): Polynomial {
      val result = mutableMapOf<Int, Double>()
      for ((p1, c1) in terms) {
        for ((p2, c2) in other.terms) {
          val p = p1 + p2
          result[p] = (result[p] ?: 0.0) + c1 * c2
        }
      }
      return Polynomial(result.filterValues { abs(it) > 1e-10 })
    }

    operator fun times(scalar: Double): Polynomial {
      if (abs(scalar) < 1e-10) return ZERO
      return Polynomial(terms.mapValues { it.value * scalar })
    }

    fun pow(n: Int): Polynomial {
      if (n == 0) return ONE
      var res = this
      for (i in 1 until n) {
        res *= this
      }
      return res
    }

    fun derivative(variable: String = "x"): Polynomial {
      val result = mutableMapOf<Int, Double>()
      for ((pow, coeff) in terms) {
        if (pow > 0) {
          result[pow - 1] = coeff * pow
        }
      }
      return Polynomial(result)
    }

    fun toPrettyString(variable: String = "x"): String {
      if (terms.isEmpty()) return "0"
      val sorted = terms.entries.sortedByDescending { it.key }
      val sb = StringBuilder()

      for ((index, entry) in sorted.withIndex()) {
        val pow = entry.key
        val coeff = entry.value

        val absCoeff = abs(coeff)
        val coeffStr = if (abs(absCoeff - 1.0) < 1e-10 && pow > 0) "" else formatNum(absCoeff)

        val termStr = when (pow) {
          0 -> formatNum(absCoeff)
          1 -> "${coeffStr}$variable"
          else -> "${coeffStr}$variable^$pow"
        }

        if (index == 0) {
          if (coeff < 0) sb.append("-")
          sb.append(termStr)
        } else {
          if (coeff >= 0) {
            sb.append(" + ")
          } else {
            sb.append(" - ")
          }
          sb.append(termStr)
        }
      }

      return sb.toString()
    }

    fun factor(variable: String = "x"): String {
      if (terms.isEmpty()) return "0"
      val degree = terms.keys.maxOrNull() ?: 0

      // Try factoring out common monomial: c * x^minPower
      val minPower = terms.keys.minOrNull() ?: 0

      // Quadratic factoring ax^2 + bx + c
      if (degree == 2 && minPower == 0) {
        val a = terms[2] ?: 0.0
        val b = terms[1] ?: 0.0
        val c = terms[0] ?: 0.0

        val delta = b * b - 4 * a * c
        if (delta >= 0) {
          val sqrtD = kotlin.math.sqrt(delta)
          if (abs(sqrtD - sqrtD.toInt()) < 1e-8) {
            val r1 = (-b + sqrtD) / (2 * a)
            val r2 = (-b - sqrtD) / (2 * a)

            val factorA = if (abs(a - 1.0) > 1e-10) formatNum(a) else ""
            val f1 = if (r1 >= 0) "($variable - ${formatNum(r1)})" else "($variable + ${formatNum(-r1)})"
            val f2 = if (r2 >= 0) "($variable - ${formatNum(r2)})" else "($variable + ${formatNum(-r2)})"

            return if (abs(r1 - r2) < 1e-10) {
              "$factorA$f1²"
            } else {
              "$factorA$f1$f2"
            }
          }
        }
      }

      // If monomial can be factored out
      if (minPower > 0) {
        val reduced = Polynomial(terms.mapKeys { it.key - minPower })
        val common = if (minPower == 1) variable else "$variable^$minPower"
        return "$common(${reduced.toPrettyString(variable)})"
      }

      return toPrettyString(variable)
    }

    companion object {
      val ZERO = Polynomial(emptyMap())
      val ONE = Polynomial(mapOf(0 to 1.0))
      fun x(variable: String = "x") = Polynomial(mapOf(1 to 1.0))
      fun constant(c: Double) = Polynomial(mapOf(0 to c))
    }
  }

  /**
   * Simplifies and expands an algebraic polynomial expression like:
   * "(x + 2)*(x - 3)", "2*x + 3*x - 4", "(2*x + 1)^2", "x*(x^2 - 4) + 3*x^2"
   */
  fun simplify(expression: String, variable: String = "x"): Pair<String, List<String>> {
    val steps = mutableListOf<String>()
    steps.add("Expresión original: $expression")

    try {
      val poly = parseToPolynomial(expression, variable)
      val simplified = poly.toPrettyString(variable)

      steps.add("Expansión de productos y potencias aplicando propiedad distributiva.")
      steps.add("Agrupación de términos semejantes por exponente decreciente.")
      steps.add("Forma simplificada final: $simplified")

      val factored = poly.factor(variable)
      if (factored != simplified && !factored.contains("null")) {
        steps.add("Forma factorizada: $factored")
      }

      return Pair(simplified, steps)
    } catch (e: Exception) {
      return Pair(expression, listOf("Error al simplificar algebraicamente: ${e.message}"))
    }
  }

  fun expand(expression: String, variable: String = "x"): String {
    val poly = parseToPolynomial(expression, variable)
    return poly.toPrettyString(variable)
  }

  fun factor(expression: String, variable: String = "x"): String {
    val poly = parseToPolynomial(expression, variable)
    return poly.factor(variable)
  }

  fun differentiateSymbolic(expression: String, variable: String = "x"): Pair<String, List<String>> {
    val steps = mutableListOf<String>()
    steps.add("Derivando algebraicamente respecto a $variable: d/d$variable ($expression)")

    val poly = parseToPolynomial(expression, variable)
    val derivPoly = poly.derivative(variable)
    val derivStr = derivPoly.toPrettyString(variable)

    steps.add("Regla de potencia: d/d$variable [c · ${variable}^n] = c · n · ${variable}^(n-1)")
    steps.add("Derivada simbólica: $derivStr")

    return Pair(derivStr, steps)
  }

  fun parseToPolynomial(expr: String, variable: String = "x"): Polynomial {
    val clean = expr.replace(" ", "")
      .replace("×", "*")
      .replace("−", "-")
      .lowercase(Locale.US)
    val v = variable.lowercase(Locale.US)
    val parser = PolynomialParser(clean, v)
    return parser.parse()
  }

  private class PolynomialParser(private val clean: String, private val v: String) {
    private var index = 0

    fun parse(): Polynomial {
      return parseAdditive()
    }

    private fun parseAdditive(): Polynomial {
      var sign = 1.0
      if (index < clean.length && (clean[index] == '+' || clean[index] == '-')) {
        if (clean[index] == '-') sign = -1.0
        index++
      }

      var node = parseMultiplicative() * sign

      while (index < clean.length) {
        val op = clean[index]
        if (op == '+' || op == '-') {
          index++
          val rhs = parseMultiplicative()
          node = if (op == '+') node + rhs else node - rhs
        } else {
          break
        }
      }
      return node
    }

    private fun parseMultiplicative(): Polynomial {
      var node = parsePower()

      while (index < clean.length) {
        val op = clean[index]
        if (op == '*' || op == '/') {
          index++
          val rhs = parsePower()
          node = if (op == '*') {
            node * rhs
          } else {
            val constVal = rhs.terms[0] ?: 1.0
            if (rhs.terms.size == 1 && abs(constVal) > 1e-10) {
              node * (1.0 / constVal)
            } else {
              node
            }
          }
        } else if (clean[index] == '(' || (v.isNotEmpty() && clean[index] == v[0])) {
          // Implicit multiplication
          val rhs = parsePower()
          node *= rhs
        } else {
          break
        }
      }
      return node
    }

    private fun parsePower(): Polynomial {
      var base = parsePrimary()

      // Implicit multiplication with variable e.g. "2x"
      if (v.isNotEmpty() && index < clean.length && clean[index] == v[0]) {
        index++
        base *= Polynomial.x(v)
      }

      if (index < clean.length && clean[index] == '^') {
        index++
        var isNegative = false
        if (index < clean.length && clean[index] == '-') {
          isNegative = true
          index++
        }
        val start = index
        while (index < clean.length && clean[index].isDigit()) index++
        val p = clean.substring(start, index).toIntOrNull() ?: 1
        if (!isNegative) {
          base = base.pow(p)
        }
      }
      return base
    }

    private fun parsePrimary(): Polynomial {
      if (index >= clean.length) return Polynomial.ZERO

      if (clean[index] == '(') {
        index++ // skip '('
        val res = parseAdditive()
        if (index < clean.length && clean[index] == ')') {
          index++ // skip ')'
        }
        return res
      }

      if (clean[index].isLetter()) {
        val start = index
        while (index < clean.length && clean[index].isLetter()) index++
        val name = clean.substring(start, index)
        return if (name == v) Polynomial.x(v) else Polynomial.constant(0.0)
      }

      if (clean[index].isDigit() || clean[index] == '.') {
        val start = index
        while (index < clean.length && (clean[index].isDigit() || clean[index] == '.')) index++
        val num = clean.substring(start, index).toDoubleOrNull() ?: 0.0
        return Polynomial.constant(num)
      }

      return Polynomial.ZERO
    }
  }

  private fun formatNum(v: Double): String {
    if (abs(v - v.toLong()) < 1e-9) return v.toLong().toString()
    val str = String.format(Locale.US, "%.4f", v).trimEnd('0').trimEnd('.')
    return if (str.isEmpty()) "0" else str
  }
}
