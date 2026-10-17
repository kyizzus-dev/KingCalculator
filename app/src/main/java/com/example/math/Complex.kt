package com.example.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt

/**
 * High-precision Complex number representation for algebraic calculations.
 */
data class Complex(val re: Double, val im: Double = 0.0) {

  val isReal: Boolean
    get() = kotlin.math.abs(im) < 1e-12

  val isZero: Boolean
    get() = kotlin.math.abs(re) < 1e-12 && kotlin.math.abs(im) < 1e-12

  operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
  operator fun plus(other: Double) = Complex(re + other, im)

  operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
  operator fun minus(other: Double) = Complex(re - other, im)

  operator fun times(other: Complex) = Complex(
    re * other.re - im * other.im,
    re * other.im + im * other.re
  )
  operator fun times(other: Double) = Complex(re * other, im * other)

  operator fun div(other: Complex): Complex {
    val d = other.re * other.re + other.im * other.im
    if (d == 0.0) throw ArithmeticException("División por cero")
    return Complex(
      (re * other.re + im * other.im) / d,
      (im * other.re - re * other.im) / d
    )
  }
  operator fun div(other: Double): Complex {
    if (other == 0.0) throw ArithmeticException("División por cero")
    return Complex(re / other, im / other)
  }

  operator fun unaryMinus() = Complex(-re, -im)

  fun abs(): Double = hypot(re, im)
  fun arg(): Double = atan2(im, re)

  fun conjugate() = Complex(re, -im)

  fun sqrt(): Complex {
    val r = abs()
    val u = sqrt((r + re) / 2.0)
    val v = (if (im >= 0) 1.0 else -1.0) * sqrt((r - re) / 2.0)
    return Complex(u, v)
  }

  fun ln(): Complex {
    return Complex(ln(abs()), arg())
  }

  fun exp(): Complex {
    val r = exp(re)
    return Complex(r * cos(im), r * sin(im))
  }

  fun pow(p: Complex): Complex {
    if (isZero) {
      return if (p.re > 0) ZERO else throw ArithmeticException("0^0 o 0 negativo es indefinido")
    }
    return (ln() * p).exp()
  }

  fun pow(n: Double): Complex = pow(Complex(n))

  fun sin(): Complex {
    return Complex(sin(re) * cosh(im), cos(re) * sinh(im))
  }

  fun cos(): Complex {
    return Complex(cos(re) * cosh(im), -sin(re) * sinh(im))
  }

  fun tan(): Complex {
    return sin() / cos()
  }

  fun format(decimals: Int = 6): String {
    val cleanRe = if (kotlin.math.abs(re) < 1e-12) 0.0 else re
    val cleanIm = if (kotlin.math.abs(im) < 1e-12) 0.0 else im

    return when {
      cleanIm == 0.0 -> formatReal(cleanRe, decimals)
      cleanRe == 0.0 -> {
        if (cleanIm == 1.0) "i"
        else if (cleanIm == -1.0) "-i"
        else "${formatReal(cleanIm, decimals)}i"
      }
      cleanIm > 0 -> {
        val imStr = if (cleanIm == 1.0) "i" else "${formatReal(cleanIm, decimals)}i"
        "${formatReal(cleanRe, decimals)} + $imStr"
      }
      else -> {
        val imStr = if (cleanIm == -1.0) "i" else "${formatReal(-cleanIm, decimals)}i"
        "${formatReal(cleanRe, decimals)} - $imStr"
      }
    }
  }

  private fun formatReal(value: Double, decimals: Int): String {
    if (value == value.toLong().toDouble() && kotlin.math.abs(value) < 1e12) {
      return value.toLong().toString()
    }
    val rounded = String.format(java.util.Locale.US, "%.${decimals}f", value).trimEnd('0').trimEnd('.')
    return if (rounded.isEmpty()) "0" else rounded
  }

  companion object {
    val ZERO = Complex(0.0, 0.0)
    val ONE = Complex(1.0, 0.0)
    val I = Complex(0.0, 1.0)
    val PI = Complex(Math.PI, 0.0)
    val E = Complex(Math.E, 0.0)
  }
}
