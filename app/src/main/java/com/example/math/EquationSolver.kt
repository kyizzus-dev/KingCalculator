package com.example.math

import kotlin.math.abs
import kotlin.math.sqrt

sealed class SolverResult {
  data class Linear(
    val x: Double,
    val steps: List<String>
  ) : SolverResult()

  data class Quadratic(
    val a: Double,
    val b: Double,
    val c: Double,
    val discriminant: Double,
    val root1: Complex,
    val root2: Complex,
    val vertexX: Double,
    val vertexY: Double,
    val steps: List<String>
  ) : SolverResult()

  data class System2x2(
    val determinant: Double,
    val x: Double?,
    val y: Double?,
    val isInfinite: Boolean,
    val isNoSolution: Boolean,
    val steps: List<String>
  ) : SolverResult()

  data class System3x3(
    val determinant: Double,
    val x: Double?,
    val y: Double?,
    val z: Double?,
    val isInfinite: Boolean,
    val isNoSolution: Boolean,
    val steps: List<String>
  ) : SolverResult()

  data class NewtonRoot(
    val root: Double,
    val iterations: List<IterationRow>,
    val converged: Boolean,
    val steps: List<String>
  ) : SolverResult()
}

data class IterationRow(
  val step: Int,
  val x: Double,
  val fx: Double,
  val fPrimeX: Double
)

object EquationSolver {

  fun solveLinear(a: Double, b: Double, c: Double): SolverResult.Linear {
    if (abs(a) < 1e-12) {
      throw IllegalArgumentException("El coeficiente 'a' no puede ser 0 para una ecuación lineal")
    }
    val x = (c - b) / a
    val steps = listOf(
      "Ecuación inicial: ${formatCoeff(a)}x + ${formatNum(b)} = ${formatNum(c)}",
      "Restar ${formatNum(b)} en ambos lados: ${formatCoeff(a)}x = ${formatNum(c - b)}",
      "Dividir entre ${formatCoeff(a)}: x = ${formatNum(c - b)} / ${formatCoeff(a)}",
      "Solución final: x = ${formatNum(x)}"
    )
    return SolverResult.Linear(x, steps)
  }

  fun solveQuadratic(a: Double, b: Double, c: Double): SolverResult.Quadratic {
    if (abs(a) < 1e-12) {
      throw IllegalArgumentException("El coeficiente 'a' no puede ser 0 para una ecuación cuadrática")
    }
    val delta = b * b - 4 * a * c
    val h = -b / (2 * a)
    val k = c - (b * b) / (4 * a)

    val steps = mutableListOf<String>()
    steps.add("Ecuación: ${formatCoeff(a)}x² + ${formatCoeff(b)}x + ${formatNum(c)} = 0")
    steps.add("Fórmula general: x = [-b ± √(b² - 4ac)] / (2a)")
    steps.add("Discriminante (Δ): Δ = ($b)² - 4($a)($c) = ${formatNum(delta)}")

    val root1: Complex
    val root2: Complex

    when {
      delta > 1e-12 -> {
        val sqrtD = sqrt(delta)
        val r1 = (-b + sqrtD) / (2 * a)
        val r2 = (-b - sqrtD) / (2 * a)
        root1 = Complex(r1)
        root2 = Complex(r2)
        steps.add("Como Δ > 0, existen dos raíces reales distintas:")
        steps.add("x₁ = (-($b) + ${formatNum(sqrtD)}) / ${formatNum(2 * a)} = ${formatNum(r1)}")
        steps.add("x₂ = (-($b) - ${formatNum(sqrtD)}) / ${formatNum(2 * a)} = ${formatNum(r2)}")
      }
      abs(delta) <= 1e-12 -> {
        val r = -b / (2 * a)
        root1 = Complex(r)
        root2 = Complex(r)
        steps.add("Como Δ = 0, existe una raíz real doble:")
        steps.add("x₁ = x₂ = -($b) / (2 * $a) = ${formatNum(r)}")
      }
      else -> {
        val sqrtAbsD = sqrt(-delta)
        val realPart = -b / (2 * a)
        val imagPart = sqrtAbsD / (2 * abs(a))
        root1 = Complex(realPart, imagPart)
        root2 = Complex(realPart, -imagPart)
        steps.add("Como Δ < 0, existen dos raíces complejas conjugadas:")
        steps.add("x₁ = ${root1.format(4)}")
        steps.add("x₂ = ${root2.format(4)}")
      }
    }

    steps.add("Vértice de la parábola: V(h, k) = (${formatNum(h)}, ${formatNum(k)})")

    return SolverResult.Quadratic(a, b, c, delta, root1, root2, h, k, steps)
  }

  fun solveSystem2x2(
    a1: Double, b1: Double, c1: Double,
    a2: Double, b2: Double, c2: Double
  ): SolverResult.System2x2 {
    val det = a1 * b2 - a2 * b1
    val detX = c1 * b2 - c2 * b1
    val detY = a1 * c2 - a2 * c1

    val steps = mutableListOf<String>()
    steps.add("Sistema:")
    steps.add("1) ${formatCoeff(a1)}x + ${formatCoeff(b1)}y = ${formatNum(c1)}")
    steps.add("2) ${formatCoeff(a2)}x + ${formatCoeff(b2)}y = ${formatNum(c2)}")
    steps.add("Determinante principal Δ = ($a1)($b2) - ($a2)($b1) = ${formatNum(det)}")

    return if (abs(det) > 1e-12) {
      val x = detX / det
      val y = detY / det
      steps.add("Δx = ($c1)($b2) - ($c2)($b1) = ${formatNum(detX)}")
      steps.add("Δy = ($a1)($c2) - ($a2)($c1) = ${formatNum(detY)}")
      steps.add("Por regla de Cramer:")
      steps.add("x = Δx / Δ = ${formatNum(detX)} / ${formatNum(det)} = ${formatNum(x)}")
      steps.add("y = Δy / Δ = ${formatNum(detY)} / ${formatNum(det)} = ${formatNum(y)}")
      SolverResult.System2x2(det, x, y, isInfinite = false, isNoSolution = false, steps)
    } else {
      if (abs(detX) < 1e-12 && abs(detY) < 1e-12) {
        steps.add("Δ = 0 y Δx = Δy = 0: Sistema compatible indeterminado (infinitas soluciones)")
        SolverResult.System2x2(det, null, null, isInfinite = true, isNoSolution = false, steps)
      } else {
        steps.add("Δ = 0 pero Δx ≠ 0 o Δy ≠ 0: Sistema incompatible (sin solución)")
        SolverResult.System2x2(det, null, null, isInfinite = false, isNoSolution = true, steps)
      }
    }
  }

  fun solveSystem3x3(
    a: DoubleArray, b: DoubleArray, c: DoubleArray, d: DoubleArray
  ): SolverResult.System3x3 {
    fun det3(
      m00: Double, m01: Double, m02: Double,
      m10: Double, m11: Double, m12: Double,
      m20: Double, m21: Double, m22: Double
    ): Double {
      return m00 * (m11 * m22 - m12 * m21) -
             m01 * (m10 * m22 - m12 * m20) +
             m02 * (m10 * m21 - m11 * m20)
    }

    val det = det3(a[0], b[0], c[0], a[1], b[1], c[1], a[2], b[2], c[2])
    val detX = det3(d[0], b[0], c[0], d[1], b[1], c[1], d[2], b[2], c[2])
    val detY = det3(a[0], d[0], c[0], a[1], d[1], c[1], a[2], d[2], c[2])
    val detZ = det3(a[0], b[0], d[0], a[1], b[1], d[1], a[2], b[2], d[2])

    val steps = mutableListOf<String>()
    steps.add("Sistema de 3 ecuaciones lineales con 3 incógnitas:")
    for (i in 0..2) {
      steps.add("${formatCoeff(a[i])}x + ${formatCoeff(b[i])}y + ${formatCoeff(c[i])}z = ${formatNum(d[i])}")
    }
    steps.add("Determinante principal del sistema: Δ = ${formatNum(det)}")

    return if (abs(det) > 1e-12) {
      val x = detX / det
      val y = detY / det
      val z = detZ / det
      steps.add("Δx = ${formatNum(detX)}, Δy = ${formatNum(detY)}, Δz = ${formatNum(detZ)}")
      steps.add("x = Δx / Δ = ${formatNum(x)}")
      steps.add("y = Δy / Δ = ${formatNum(y)}")
      steps.add("z = Δz / Δ = ${formatNum(z)}")
      SolverResult.System3x3(det, x, y, z, isInfinite = false, isNoSolution = false, steps)
    } else {
      val allZeros = abs(detX) < 1e-12 && abs(detY) < 1e-12 && abs(detZ) < 1e-12
      if (allZeros) {
        steps.add("Δ = 0 y determinantes nulos: Infinitas soluciones (Sistema Indeterminado)")
        SolverResult.System3x3(det, null, null, null, isInfinite = true, isNoSolution = false, steps)
      } else {
        steps.add("Δ = 0 con determinantes independientes no nulos: Sin solución (Incompatible)")
        SolverResult.System3x3(det, null, null, null, isInfinite = false, isNoSolution = true, steps)
      }
    }
  }

  fun solveNewtonRaphson(
    expression: String,
    initialX: Double,
    maxIter: Int = 20,
    tol: Double = 1e-7
  ): SolverResult.NewtonRoot {
    val parser = ExpressionParser(AngleMode.RAD)
    var currentX = initialX
    val iterations = mutableListOf<IterationRow>()
    val steps = mutableListOf<String>()
    steps.add("Resolviendo f(x) = 0 para f(x) = $expression con x₀ = $initialX")
    steps.add("Método de Newton-Raphson: xₙ₊₁ = xₙ - f(xₙ)/f'(xₙ)")

    var converged = false
    val h = 1e-5

    for (step in 1..maxIter) {
      val fx = parser.evaluateReal(expression, currentX)
      // Numerical derivative
      val fPrime = (parser.evaluateReal(expression, currentX + h) - parser.evaluateReal(expression, currentX - h)) / (2 * h)

      iterations.add(IterationRow(step, currentX, fx, fPrime))

      if (abs(fPrime) < 1e-14) {
        steps.add("Paso $step: Derivada cercana a cero (f'(x) ≈ 0), no se puede continuar.")
        break
      }

      val nextX = currentX - (fx / fPrime)
      if (abs(nextX - currentX) < tol || abs(fx) < tol) {
        currentX = nextX
        converged = true
        steps.add("Convergencia alcanzada en la iteración $step.")
        break
      }
      currentX = nextX
    }

    steps.add("Raíz aproximada: x ≈ ${formatNum(currentX)}")
    return SolverResult.NewtonRoot(currentX, iterations, converged, steps)
  }

  private fun formatNum(v: Double): String {
    if (abs(v - v.toLong()) < 1e-9) return v.toLong().toString()
    val str = String.format(java.util.Locale.US, "%.5f", v).trimEnd('0').trimEnd('.')
    return if (str.isEmpty()) "0" else str
  }

  private fun formatCoeff(v: Double): String {
    return when {
      v == 1.0 -> ""
      v == -1.0 -> "-"
      abs(v - v.toLong()) < 1e-9 -> v.toLong().toString()
      else -> formatNum(v)
    }
  }
}
