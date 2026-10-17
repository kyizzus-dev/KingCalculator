package com.example

import com.example.math.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testExpressionParserBasic() {
    val parser = ExpressionParser(AngleMode.DEG)
    val res = parser.evaluate("2 + 3 * 4")
    assertEquals(14.0, res.re, 1e-6)
  }

  @Test
  fun testExpressionParserTrigAndPower() {
    val parser = ExpressionParser(AngleMode.DEG)
    val sin30 = parser.evaluate("sin(30)")
    assertEquals(0.5, sin30.re, 1e-4)

    val powRes = parser.evaluate("2^3 + sqrt(16)")
    assertEquals(12.0, powRes.re, 1e-6)
  }

  @Test
  fun testComplexNumbers() {
    val parser = ExpressionParser()
    val res = parser.evaluate("(2 + 3*i) * (2 - 3*i)")
    assertEquals(13.0, res.re, 1e-6)
    assertEquals(0.0, res.im, 1e-6)
  }

  @Test
  fun testQuadraticSolver() {
    // x^2 - 5x + 6 = 0 -> roots 3 and 2
    val res = EquationSolver.solveQuadratic(1.0, -5.0, 6.0)
    assertEquals(3.0, res.root1.re, 1e-6)
    assertEquals(2.0, res.root2.re, 1e-6)
  }

  @Test
  fun testMatrixDeterminantAndInverse() {
    // [1 2; 3 4] -> det = 1*4 - 2*3 = -2
    val m = Matrix.from2x2(1.0, 2.0, 3.0, 4.0)
    assertEquals(-2.0, m.determinant(), 1e-6)

    val (inv, _) = m.inverse()
    assertNotNull(inv)
    val identity = m * inv!!
    assertEquals(1.0, identity[0, 0], 1e-4)
    assertEquals(0.0, identity[0, 1], 1e-4)
    assertEquals(0.0, identity[1, 0], 1e-4)
    assertEquals(1.0, identity[1, 1], 1e-4)
  }

  @Test
  fun testCalculusDerivative() {
    // f(x) = x^2, f'(3) = 6
    val (deriv, _) = CalculusEngine.derivative("x^2", 3.0)
    assertEquals(6.0, deriv, 1e-3)
  }

  @Test
  fun testSymbolicSimplification() {
    // (x + 2)*(x - 3) = x^2 - x - 6
    val (simplified, _) = SymbolicAlgebra.simplify("(x + 2)*(x - 3)")
    assertEquals("x^2 - x - 6", simplified)
  }

  @Test
  fun testSymbolicDerivative() {
    // d/dx (x^3 - 3*x^2 + 5*x) = 3x^2 - 6x + 5
    val (deriv, _) = SymbolicAlgebra.differentiateSymbolic("x^3 - 3*x^2 + 5*x")
    assertEquals("3x^2 - 6x + 5", deriv)
  }

  @Test
  fun testEvaluationWithVariables() {
    val parser = ExpressionParser()
    val vars = mapOf("x" to Complex(4.0), "y" to Complex(2.0), "a" to Complex(10.0))
    val res = parser.evaluate("2*x + 3*y - a", vars)
    // 2*4 + 3*2 - 10 = 8 + 6 - 10 = 4
    assertEquals(4.0, res.re, 1e-6)
  }

  @Test
  fun testMathTextCleanerRemovesHashtagsAndLatex() {
    val rawLLMOutput = """
      ### Identificación del problema:
      Debemos resolver la ecuación:
      $$2x + \frac{1}{2} = 5$$

      ### Paso 1: Restar la fracción
      Restamos $\frac{1}{2}$ a ambos lados:
      $2x = 5 - \frac{1}{2}$
      $2x = \frac{9}{2}$

      ### Paso 2: Despejar x
      Dividimos entre 2:
      ${'$'}x = \frac{9}{4} = 2.25${'$'}

      ### Resultado Final:
      \boxed{x = 2.25}
    """.trimIndent()

    val cleaned = com.example.ai.MathTextCleaner.clean(rawLLMOutput)
    // Verify absolutely no '#' or '$' or '\frac' or '\boxed' remains
    assertFalse(cleaned.contains("#"))
    assertFalse(cleaned.contains("$"))
    assertFalse(cleaned.contains("\\frac"))
    assertFalse(cleaned.contains("\\boxed"))
    assertTrue(cleaned.contains("1/2"))
    assertTrue(cleaned.contains("x = 2.25"))

    val parsed = com.example.ai.MathTextCleaner.parseSolution(rawLLMOutput)
    assertEquals(2, parsed.steps.size)
    assertEquals("Restar la fracción", parsed.steps[0].title)
    assertEquals("Despejar x", parsed.steps[1].title)
    assertNotNull(parsed.finalResult)
    assertTrue(parsed.finalResult!!.contains("x = 2.25"))
  }
}
