package com.example.math

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

data class Matrix(val rows: Int, val cols: Int, val data: Array<DoubleArray>) {

  operator fun get(r: Int, c: Int): Double = data[r][c]
  operator fun set(r: Int, c: Int, value: Double) {
    data[r][c] = value
  }

  fun determinant(): Double {
    require(rows == cols) { "El determinante solo aplica para matrices cuadradas" }
    return when (rows) {
      1 -> data[0][0]
      2 -> data[0][0] * data[1][1] - data[0][1] * data[1][0]
      3 -> {
        val a = data[0][0] * (data[1][1] * data[2][2] - data[1][2] * data[2][1])
        val b = data[0][1] * (data[1][0] * data[2][2] - data[1][2] * data[2][0])
        val c = data[0][2] * (data[1][0] * data[2][1] - data[1][1] * data[2][0])
        a - b + c
      }
      else -> throw UnsupportedOperationException("Determinante implementado hasta 3x3")
    }
  }

  fun trace(): Double {
    require(rows == cols) { "La traza solo aplica para matrices cuadradas" }
    var sum = 0.0
    for (i in 0 until rows) {
      sum += data[i][i]
    }
    return sum
  }

  fun transpose(): Matrix {
    val result = Array(cols) { DoubleArray(rows) }
    for (r in 0 until rows) {
      for (c in 0 until cols) {
        result[c][r] = data[r][c]
      }
    }
    return Matrix(cols, rows, result)
  }

  fun inverse(): Pair<Matrix?, List<String>> {
    require(rows == cols) { "La matriz debe ser cuadrada para calcular la inversa" }
    val det = determinant()
    val steps = mutableListOf<String>()
    steps.add("Cálculo de la inversa de la matriz ${rows}x${cols}:")
    steps.add("Determinante: det(A) = ${formatNum(det)}")

    if (abs(det) < 1e-12) {
      steps.add("det(A) = 0: La matriz es singular (no tiene inversa).")
      return Pair(null, steps)
    }

    steps.add("Como det(A) ≠ 0, la inversa existe: A⁻¹ = (1/det(A)) * adj(A)")

    val invData = Array(rows) { DoubleArray(cols) }

    if (rows == 2) {
      // 2x2 inverse formula
      invData[0][0] = data[1][1] / det
      invData[0][1] = -data[0][1] / det
      invData[1][0] = -data[1][0] / det
      invData[1][1] = data[0][0] / det
      steps.add("Para 2x2: intercambiar diagonal principal y cambiar signos a diagonal secundaria.")
    } else if (rows == 3) {
      // 3x3 adjugate matrix
      for (i in 0..2) {
        for (j in 0..2) {
          val sub = subMatrix(i, j)
          val cofactor = (if ((i + j) % 2 == 0) 1.0 else -1.0) * sub.determinant()
          // Adjugate is the transpose of the cofactor matrix
          invData[j][i] = cofactor / det
        }
      }
      steps.add("Cálculo de la matriz de cofactores transpuesta (matriz adjunta) y división entre det(A).")
    }

    return Pair(Matrix(rows, cols, invData), steps)
  }

  private fun subMatrix(rowToExclude: Int, colToExclude: Int): Matrix {
    val sub = Array(rows - 1) { DoubleArray(cols - 1) }
    var rSub = 0
    for (r in 0 until rows) {
      if (r == rowToExclude) continue
      var cSub = 0
      for (c in 0 until cols) {
        if (c == colToExclude) continue
        sub[rSub][cSub] = data[r][c]
        cSub++
      }
      rSub++
    }
    return Matrix(rows - 1, cols - 1, sub)
  }

  operator fun plus(other: Matrix): Matrix {
    require(rows == other.rows && cols == other.cols) { "Dimensiones incompatibles para suma" }
    val res = Array(rows) { DoubleArray(cols) }
    for (r in 0 until rows) {
      for (c in 0 until cols) {
        res[r][c] = data[r][c] + other.data[r][c]
      }
    }
    return Matrix(rows, cols, res)
  }

  operator fun minus(other: Matrix): Matrix {
    require(rows == other.rows && cols == other.cols) { "Dimensiones incompatibles para resta" }
    val res = Array(rows) { DoubleArray(cols) }
    for (r in 0 until rows) {
      for (c in 0 until cols) {
        res[r][c] = data[r][c] - other.data[r][c]
      }
    }
    return Matrix(rows, cols, res)
  }

  operator fun times(scalar: Double): Matrix {
    val res = Array(rows) { DoubleArray(cols) }
    for (r in 0 until rows) {
      for (c in 0 until cols) {
        res[r][c] = data[r][c] * scalar
      }
    }
    return Matrix(rows, cols, res)
  }

  operator fun times(other: Matrix): Matrix {
    require(cols == other.rows) { "El número de columnas de A debe coincidir con filas de B" }
    val res = Array(rows) { DoubleArray(other.cols) }
    for (r in 0 until rows) {
      for (c in 0 until other.cols) {
        var sum = 0.0
        for (k in 0 until cols) {
          sum += data[r][k] * other.data[k][c]
        }
        res[r][c] = sum
      }
    }
    return Matrix(rows, other.cols, res)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Matrix) return false
    if (rows != other.rows || cols != other.cols) return false
    for (r in 0 until rows) {
      if (!data[r].contentEquals(other.data[r])) return false
    }
    return true
  }

  override fun hashCode(): Int {
    var result = rows
    result = 31 * result + cols
    result = 31 * result + data.contentDeepHashCode()
    return result
  }

  companion object {
    fun zeros(rows: Int, cols: Int): Matrix = Matrix(rows, cols, Array(rows) { DoubleArray(cols) })

    fun identity(size: Int): Matrix {
      val m = zeros(size, size)
      for (i in 0 until size) {
        m.data[i][i] = 1.0
      }
      return m
    }

    fun from2x2(a: Double, b: Double, c: Double, d: Double): Matrix {
      return Matrix(2, 2, arrayOf(doubleArrayOf(a, b), doubleArrayOf(c, d)))
    }

    fun from3x3(arr: DoubleArray): Matrix {
      require(arr.size == 9)
      return Matrix(
        3, 3,
        arrayOf(
          doubleArrayOf(arr[0], arr[1], arr[2]),
          doubleArrayOf(arr[3], arr[4], arr[5]),
          doubleArrayOf(arr[6], arr[7], arr[8])
        )
      )
    }

    fun formatNum(v: Double): String {
      if (abs(v - v.toLong()) < 1e-9) return v.toLong().toString()
      val str = String.format(java.util.Locale.US, "%.4f", v).trimEnd('0').trimEnd('.')
      return if (str.isEmpty()) "0" else str
    }
  }
}

data class Vector3D(val x: Double, val y: Double, val z: Double) {

  fun magnitude(): Double = sqrt(x * x + y * y + z * z)

  fun dot(other: Vector3D): Double = x * other.x + y * other.y + z * other.z

  fun cross(other: Vector3D): Vector3D = Vector3D(
    y * other.z - z * other.y,
    z * other.x - x * other.z,
    x * other.y - y * other.x
  )

  fun unitVector(): Vector3D {
    val m = magnitude()
    if (m == 0.0) throw ArithmeticException("Vector nulo no tiene vector unitario")
    return Vector3D(x / m, y / m, z / m)
  }

  fun angleWith(other: Vector3D): Pair<Double, Double> {
    val m1 = magnitude()
    val m2 = other.magnitude()
    if (m1 == 0.0 || m2 == 0.0) throw ArithmeticException("Vector nulo no define ángulo")
    val cosTheta = (dot(other) / (m1 * m2)).coerceIn(-1.0, 1.0)
    val rad = acos(cosTheta)
    val deg = Math.toDegrees(rad)
    return Pair(rad, deg)
  }

  override fun toString(): String {
    return "(${Matrix.formatNum(x)}, ${Matrix.formatNum(y)}, ${Matrix.formatNum(z)})"
  }
}
