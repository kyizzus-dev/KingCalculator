package com.example.math

import java.util.ArrayDeque
import java.util.Locale
import kotlin.math.*

enum class AngleMode {
  RAD, DEG, GRAD
}

/**
 * Robust mathematical tokenizer & evaluator implementing Dijkstra's Shunting-Yard
 * algorithm supporting variables, complex arithmetic, implicit multiplication,
 * scientific functions, factorials, and combinatorics.
 */
class ExpressionParser(var angleMode: AngleMode = AngleMode.DEG) {

  sealed class Token {
    data class Number(val value: Complex) : Token()
    data class Variable(val name: String) : Token()
    data class Operator(val symbol: Char, val precedence: Int, val rightAssociative: Boolean = false, val isUnary: Boolean = false) : Token()
    data class Function(val name: String, val argCount: Int = 1) : Token()
    data object LeftParen : Token()
    data object RightParen : Token()
    data object Comma : Token()
    data object Factorial : Token()
  }

  fun evaluate(expression: String, variables: Map<String, Complex> = emptyMap()): Complex {
    val cleanExpr = sanitize(expression)
    if (cleanExpr.isEmpty()) return Complex.ZERO
    val tokens = tokenize(cleanExpr)
    val rpn = toRpn(tokens)
    return evaluateRpn(rpn, variables)
  }

  fun evaluateReal(expression: String, x: Double = 0.0, vars: Map<String, Double> = emptyMap()): Double {
    val complexVars = vars.mapValues { Complex(it.value) }.toMutableMap()
    complexVars["x"] = Complex(x)
    val res = evaluate(expression, complexVars)
    return res.re
  }

  private fun sanitize(expr: String): String {
    return expr
      .replace("×", "*")
      .replace("÷", "/")
      .replace("−", "-")
      .replace("π", "pi")
      .replace("√", "sqrt")
      .replace(" ", "")
  }

  private fun tokenize(input: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    var prevToken: Token? = null

    while (i < input.length) {
      val c = input[i]

      when {
        c.isDigit() || c == '.' -> {
          // Check for implicit multiplication e.g. )2 or x2
          if (prevToken is Token.RightParen || prevToken is Token.Variable || prevToken is Token.Factorial) {
            tokens.add(Token.Operator('*', 2))
          }
          val sb = StringBuilder()
          var hasDot = false
          while (i < input.length && (input[i].isDigit() || (!hasDot && input[i] == '.'))) {
            if (input[i] == '.') hasDot = true
            sb.append(input[i])
            i++
          }
          // Support scientific notation (e.g., 1e-3, 2.5e4)
          if (i < input.length && (input[i] == 'e' || input[i] == 'E') &&
            i + 1 < input.length && (input[i + 1].isDigit() || input[i + 1] == '+' || input[i + 1] == '-')
          ) {
            val expChar = input[i]
            val nextChar = input[i + 1]
            if (nextChar.isDigit() || (i + 2 < input.length && input[i + 2].isDigit())) {
              sb.append(expChar)
              i++
              if (input[i] == '+' || input[i] == '-') {
                sb.append(input[i])
                i++
              }
              while (i < input.length && input[i].isDigit()) {
                sb.append(input[i])
                i++
              }
            }
          }
          val numVal = sb.toString().toDoubleOrNull() ?: 0.0
          val tok = Token.Number(Complex(numVal))
          tokens.add(tok)
          prevToken = tok
          continue
        }

        c.isLetter() -> {
          val sb = StringBuilder()
          while (i < input.length && input[i].isLetter()) {
            sb.append(input[i])
            i++
          }
          val word = sb.toString().lowercase(Locale.US)

          // Check implicit multiplication before this word if preceded by number, right paren, or variable
          if (prevToken is Token.Number || prevToken is Token.RightParen || prevToken is Token.Variable || prevToken is Token.Factorial) {
            tokens.add(Token.Operator('*', 2))
          }

          val tok = when (word) {
            "i" -> Token.Number(Complex.I)
            "pi" -> Token.Number(Complex.PI)
            "e" -> Token.Number(Complex.E)
            "phi" -> Token.Number(Complex(1.618033988749895))
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
            "ln", "log", "log10", "log2", "exp", "sqrt", "cbrt",
            "abs", "sign", "floor", "ceil", "round" -> Token.Function(word, 1)
            "npr", "ncr", "root", "pow" -> Token.Function(word, 2)
            else -> Token.Variable(word)
          }
          tokens.add(tok)
          prevToken = tok
          continue
        }

        c == '(' -> {
          if (prevToken is Token.Number || prevToken is Token.RightParen || prevToken is Token.Variable || prevToken is Token.Factorial) {
            tokens.add(Token.Operator('*', 2))
          }
          tokens.add(Token.LeftParen)
          prevToken = Token.LeftParen
          i++
        }

        c == ')' -> {
          tokens.add(Token.RightParen)
          prevToken = Token.RightParen
          i++
        }

        c == '!' -> {
          tokens.add(Token.Factorial)
          prevToken = Token.Factorial
          i++
        }

        c == ',' -> {
          tokens.add(Token.Comma)
          prevToken = Token.Comma
          i++
        }

        c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%' -> {
          val isUnary = (c == '+' || c == '-') && (prevToken == null || prevToken is Token.Operator || prevToken is Token.LeftParen || prevToken is Token.Comma)
          val precedence = when {
            isUnary -> 4
            c == '^' -> 3
            c == '*' || c == '/' || c == '%' -> 2
            else -> 1
          }
          val rightAssoc = c == '^' || isUnary
          val opSymbol = if (isUnary && c == '-') '~' else if (isUnary && c == '+') '#' else c
          val tok = Token.Operator(opSymbol, precedence, rightAssoc, isUnary)
          tokens.add(tok)
          prevToken = tok
          i++
        }

        else -> {
          i++ // skip unknown
        }
      }
    }
    return tokens
  }

  private fun toRpn(tokens: List<Token>): List<Token> {
    val output = mutableListOf<Token>()
    val stack = ArrayDeque<Token>()

    for (token in tokens) {
      when (token) {
        is Token.Number, is Token.Variable -> output.add(token)
        is Token.Factorial -> output.add(token)
        is Token.Function -> stack.push(token)
        is Token.Comma -> {
          while (stack.isNotEmpty() && stack.peek() !is Token.LeftParen) {
            output.add(stack.pop())
          }
        }
        is Token.Operator -> {
          while (stack.isNotEmpty() && stack.peek() is Token.Operator) {
            val top = stack.peek() as Token.Operator
            if ((!token.rightAssociative && token.precedence <= top.precedence) ||
              (token.rightAssociative && token.precedence < top.precedence)
            ) {
              output.add(stack.pop())
            } else {
              break
            }
          }
          stack.push(token)
        }
        is Token.LeftParen -> stack.push(token)
        is Token.RightParen -> {
          while (stack.isNotEmpty() && stack.peek() !is Token.LeftParen) {
            output.add(stack.pop())
          }
          if (stack.isNotEmpty() && stack.peek() is Token.LeftParen) {
            stack.pop() // discard left paren
          }
          if (stack.isNotEmpty() && stack.peek() is Token.Function) {
            output.add(stack.pop())
          }
        }
      }
    }

    while (stack.isNotEmpty()) {
      val top = stack.pop()
      if (top !is Token.LeftParen && top !is Token.RightParen) {
        output.add(top)
      }
    }
    return output
  }

  private fun evaluateRpn(rpn: List<Token>, variables: Map<String, Complex>): Complex {
    val stack = ArrayDeque<Complex>()

    for (token in rpn) {
      when (token) {
        is Token.Number -> stack.push(token.value)
        is Token.Variable -> {
          val v = variables[token.name] ?: when (token.name) {
            "ans" -> variables["ans"] ?: Complex.ZERO
            else -> throw IllegalArgumentException("Variable no definida: ${token.name}")
          }
          stack.push(v)
        }
        is Token.Factorial -> {
          val n = stack.pop()
          stack.push(factorial(n))
        }
        is Token.Operator -> {
          if (token.isUnary) {
            val a = stack.pop()
            when (token.symbol) {
              '~' -> stack.push(-a)
              '#' -> stack.push(a)
            }
          } else {
            val b = stack.pop()
            val a = stack.pop()
            val res = when (token.symbol) {
              '+' -> a + b
              '-' -> a - b
              '*' -> a * b
              '/' -> a / b
              '^' -> a.pow(b)
              '%' -> Complex(a.re % b.re, 0.0)
              else -> throw UnsupportedOperationException("Operador desconocido: ${token.symbol}")
            }
            stack.push(res)
          }
        }
        is Token.Function -> {
          if (token.argCount == 2) {
            val b = stack.pop()
            val a = stack.pop()
            stack.push(evalBinaryFunc(token.name, a, b))
          } else {
            val a = stack.pop()
            stack.push(evalUnaryFunc(token.name, a))
          }
        }
        else -> {}
      }
    }

    return if (stack.isNotEmpty()) stack.pop() else Complex.ZERO
  }

  private fun toRadians(angle: Double): Double = when (angleMode) {
    AngleMode.RAD -> angle
    AngleMode.DEG -> Math.toRadians(angle)
    AngleMode.GRAD -> angle * (Math.PI / 200.0)
  }

  private fun fromRadians(rad: Double): Double = when (angleMode) {
    AngleMode.RAD -> rad
    AngleMode.DEG -> Math.toDegrees(rad)
    AngleMode.GRAD -> rad * (200.0 / Math.PI)
  }

  private fun evalUnaryFunc(name: String, arg: Complex): Complex {
    if (!arg.isReal) {
      // Complex function handling
      return when (name) {
        "sin" -> arg.sin()
        "cos" -> arg.cos()
        "tan" -> arg.tan()
        "ln" -> arg.ln()
        "log", "log10" -> arg.ln() / ln(10.0)
        "log2" -> arg.ln() / ln(2.0)
        "exp" -> arg.exp()
        "sqrt" -> arg.sqrt()
        "abs" -> Complex(arg.abs(), 0.0)
        else -> throw UnsupportedOperationException("Función compleja no implementada para $name")
      }
    }

    val x = arg.re
    return when (name) {
      "sin" -> Complex(sin(toRadians(x)))
      "cos" -> Complex(cos(toRadians(x)))
      "tan" -> {
        val cosVal = cos(toRadians(x))
        if (abs(cosVal) < 1e-15) throw ArithmeticException("Tan indefinida en este ángulo")
        Complex(tan(toRadians(x)))
      }
      "asin" -> {
        if (x < -1.0 || x > 1.0) Complex(0.0).let { arg.sqrt() } // complex branch
        Complex(fromRadians(asin(x)))
      }
      "acos" -> {
        Complex(fromRadians(acos(x)))
      }
      "atan" -> Complex(fromRadians(atan(x)))
      "sinh" -> Complex(sinh(x))
      "cosh" -> Complex(cosh(x))
      "tanh" -> Complex(tanh(x))
      "asinh" -> Complex(ln(x + sqrt(x * x + 1.0)))
      "acosh" -> {
        if (x < 1.0) throw ArithmeticException("acosh indefinido para x < 1")
        Complex(ln(x + sqrt(x * x - 1.0)))
      }
      "atanh" -> {
        if (abs(x) >= 1.0) throw ArithmeticException("atanh indefinido para |x| >= 1")
        Complex(0.5 * ln((1.0 + x) / (1.0 - x)))
      }
      "ln" -> {
        if (x <= 0) Complex(ln(abs(x)), Math.PI) // ln of negative real is ln|x| + pi*i
        else Complex(ln(x))
      }
      "log", "log10" -> {
        if (x <= 0) (Complex(ln(abs(x)), Math.PI) / ln(10.0))
        else Complex(log10(x))
      }
      "log2" -> {
        if (x <= 0) (Complex(ln(abs(x)), Math.PI) / ln(2.0))
        else Complex(ln(x) / ln(2.0))
      }
      "exp" -> Complex(exp(x))
      "sqrt" -> {
        if (x < 0) Complex(0.0, sqrt(-x))
        else Complex(sqrt(x))
      }
      "cbrt" -> Complex(cbrt(x))
      "abs" -> Complex(abs(x))
      "sign" -> Complex(sign(x))
      "floor" -> Complex(floor(x))
      "ceil" -> Complex(ceil(x))
      "round" -> Complex(round(x))
      else -> throw UnsupportedOperationException("Función $name desconocida")
    }
  }

  private fun evalBinaryFunc(name: String, a: Complex, b: Complex): Complex {
    return when (name) {
      "pow" -> a.pow(b)
      "root" -> a.pow(Complex.ONE / b)
      "npr" -> {
        val n = a.re.toLong()
        val r = b.re.toLong()
        if (r < 0 || r > n) Complex.ZERO
        else Complex(permutations(n, r).toDouble())
      }
      "ncr" -> {
        val n = a.re.toLong()
        val r = b.re.toLong()
        if (r < 0 || r > n) Complex.ZERO
        else Complex(combinations(n, r).toDouble())
      }
      else -> throw UnsupportedOperationException("Función binaria $name desconocida")
    }
  }

  private fun factorial(c: Complex): Complex {
    if (!c.isReal) throw ArithmeticException("Factorial solo para números reales")
    val n = c.re
    if (n < 0 || n != floor(n)) {
      // Gamma function approximation for non-integers could be used, or error
      throw ArithmeticException("Factorial requiere entero no negativo")
    }
    if (n > 170) throw ArithmeticException("Desbordamiento en factorial (>170)")
    var res = 1.0
    for (i in 2..n.toInt()) {
      res *= i
    }
    return Complex(res)
  }

  private fun permutations(n: Long, r: Long): Double {
    var res = 1.0
    for (i in 0 until r) {
      res *= (n - i)
    }
    return res
  }

  private fun combinations(n: Long, r: Long): Double {
    val k = if (r > n - r) n - r else r
    var res = 1.0
    for (i in 1..k) {
      res = res * (n - i + 1) / i
    }
    return res
  }

  companion object {
    /**
     * Converts a decimal double into a simplified fraction string like "3/4" or "-5/2" if close.
     */
    fun toFraction(value: Double, tolerance: Double = 1e-6): String? {
      if (value.isNaN() || value.isInfinite()) return null
      val sign = if (value < 0) "-" else ""
      val absVal = abs(value)
      if (absVal == floor(absVal)) return null // integer already

      var h1 = 1L
      var h2 = 0L
      var k1 = 0L
      var k2 = 1L
      var b = absVal

      do {
        val a = b.toLong()
        var aux = h1
        h1 = a * h1 + h2
        h2 = aux
        aux = k1
        k1 = a * k1 + k2
        k2 = aux
        b = 1.0 / (b - a)
      } while (abs(absVal - h1.toDouble() / k1.toDouble()) > absVal * tolerance && k1 < 10000)

      return if (k1 in 2..9999) "$sign$h1/$k1" else null
    }
  }
}
