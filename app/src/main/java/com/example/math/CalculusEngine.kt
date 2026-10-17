package com.example.math

import kotlin.math.abs

data class LimitResult(
  val approachingA: Double,
  val leftLimit: Double,
  val rightLimit: Double,
  val estimatedLimit: Double?,
  val table: List<Pair<Double, Double>>,
  val description: String
)

object CalculusEngine {

  /**
   * Calculates numerical derivative f'(x0) using central difference + Richardson extrapolation
   */
  fun derivative(expression: String, x0: Double, angleMode: AngleMode = AngleMode.RAD): Pair<Double, List<String>> {
    val parser = ExpressionParser(angleMode)
    val h = 1e-4

    val f1_plus = parser.evaluateReal(expression, x0 + h)
    val f1_minus = parser.evaluateReal(expression, x0 - h)
    val d1 = (f1_plus - f1_minus) / (2 * h)

    val h2 = h / 2.0
    val f2_plus = parser.evaluateReal(expression, x0 + h2)
    val f2_minus = parser.evaluateReal(expression, x0 - h2)
    val d2 = (f2_plus - f2_minus) / (2 * h2)

    val result = (4.0 * d2 - d1) / 3.0

    val steps = listOf(
      "Función f(x) = $expression",
      "Punto de evaluación: x₀ = $x0",
      "Método de diferencias centrales con extrapolación de Richardson",
      "f(x₀ + h) = ${formatNum(f1_plus)}, f(x₀ - h) = ${formatNum(f1_minus)} (h = $h)",
      "Aproximación D(h) = ${formatNum(d1)}",
      "Aproximación D(h/2) = ${formatNum(d2)}",
      "d/dx [f(x)] |_{x=$x0} ≈ ${formatNum(result)}"
    )

    return Pair(result, steps)
  }

  /**
   * Calculates definite integral \int_a^b f(x) dx using Composite Simpson's 1/3 rule
   */
  fun definiteIntegral(
    expression: String,
    a: Double,
    b: Double,
    intervals: Int = 1000,
    angleMode: AngleMode = AngleMode.RAD
  ): Pair<Double, List<String>> {
    val parser = ExpressionParser(angleMode)
    val n = if (intervals % 2 == 0) intervals else intervals + 1
    val h = (b - a) / n

    var sum = parser.evaluateReal(expression, a) + parser.evaluateReal(expression, b)

    for (i in 1 until n) {
      val x = a + i * h
      val fx = parser.evaluateReal(expression, x)
      sum += if (i % 2 == 0) 2 * fx else 4 * fx
    }

    val result = (h / 3.0) * sum

    val steps = listOf(
      "Integral definida ∫ f(x) dx en el intervalo [$a, $b]",
      "Función integrando: f(x) = $expression",
      "Regla compuesta de Simpson 1/3 con $n$ subdivisiones",
      "Paso h = (b - a) / n = ${formatNum(h)}",
      "f(a) = ${formatNum(parser.evaluateReal(expression, a))}, f(b) = ${formatNum(parser.evaluateReal(expression, b))}",
      "Resultado de la integral: ∫ ≈ ${formatNum(result)}"
    )

    return Pair(result, steps)
  }

  /**
   * Evaluates summation \sum_{n=start}^{end} f(n)
   */
  fun summation(expression: String, start: Long, end: Long): Pair<Double, List<String>> {
    if (end < start) throw IllegalArgumentException("El límite superior debe ser mayor o igual al inferior")
    if (end - start > 100000) throw IllegalArgumentException("Intervalo de suma demasiado grande (máx 100,000)")

    val parser = ExpressionParser(AngleMode.RAD)
    var total = 0.0
    val sampleTerms = mutableListOf<String>()

    for (n in start..end) {
      val term = parser.evaluateReal(expression, n.toDouble(), mapOf("n" to n.toDouble()))
      total += term
      if (sampleTerms.size < 4 || n == end) {
        sampleTerms.add("n=$n: ${formatNum(term)}")
      }
    }

    val steps = listOf(
      "Sumatoria ∑ f(n) desde n = $start hasta n = $end",
      "Términos muestra: ${sampleTerms.joinToString(", ")}",
      "Total acumulado: ∑ = ${formatNum(total)}"
    )

    return Pair(total, steps)
  }

  /**
   * Evaluates product \prod_{n=start}^{end} f(n)
   */
  fun product(expression: String, start: Long, end: Long): Pair<Double, List<String>> {
    if (end < start) throw IllegalArgumentException("El límite superior debe ser mayor o igual al inferior")
    if (end - start > 10000) throw IllegalArgumentException("Intervalo de producto demasiado grande (máx 10,000)")

    val parser = ExpressionParser(AngleMode.RAD)
    var prod = 1.0
    val sampleTerms = mutableListOf<String>()

    for (n in start..end) {
      val term = parser.evaluateReal(expression, n.toDouble(), mapOf("n" to n.toDouble()))
      prod *= term
      if (sampleTerms.size < 4 || n == end) {
        sampleTerms.add("n=$n: ${formatNum(term)}")
      }
    }

    val steps = listOf(
      "Productoria ∏ f(n) desde n = $start hasta n = $end",
      "Términos muestra: ${sampleTerms.joinToString(", ")}",
      "Producto acumulado: ∏ = ${formatNum(prod)}"
    )

    return Pair(prod, steps)
  }

  /**
   * Approximates limit lim_{x -> a} f(x)
   */
  fun approximateLimit(expression: String, a: Double, angleMode: AngleMode = AngleMode.RAD): LimitResult {
    val parser = ExpressionParser(angleMode)
    val deltas = listOf(0.1, 0.01, 0.001, 0.0001)
    val table = mutableListOf<Pair<Double, Double>>()

    var leftVal = Double.NaN
    var rightVal = Double.NaN

    for (d in deltas) {
      val xLeft = a - d
      val yLeft = try { parser.evaluateReal(expression, xLeft) } catch (e: Exception) { Double.NaN }
      table.add(Pair(xLeft, yLeft))
      leftVal = yLeft

      val xRight = a + d
      val yRight = try { parser.evaluateReal(expression, xRight) } catch (e: Exception) { Double.NaN }
      table.add(Pair(xRight, yRight))
      rightVal = yRight
    }

    val estimatedLimit = if (!leftVal.isNaN() && !rightVal.isNaN() && abs(leftVal - rightVal) < 1e-2) {
      (leftVal + rightVal) / 2.0
    } else {
      null
    }

    val desc = if (estimatedLimit != null) {
      "El límite lateral por izquierda y por derecha convergen aproximadamente a: ${formatNum(estimatedLimit)}"
    } else {
      "Los límites laterales difieren o la función oscila/diverge cerca de x = $a"
    }

    return LimitResult(a, leftVal, rightVal, estimatedLimit, table, desc)
  }

  private fun formatNum(v: Double): String {
    if (v.isNaN()) return "Indefinido"
    if (v.isInfinite()) return if (v > 0) "+∞" else "-∞"
    if (abs(v - v.toLong()) < 1e-9) return v.toLong().toString()
    val str = String.format(java.util.Locale.US, "%.6f", v).trimEnd('0').trimEnd('.')
    return if (str.isEmpty()) "0" else str
  }
}
