package com.example.ai

/**
 * Data structures for structured, clean step-by-step mathematical solutions.
 */
data class SolutionStep(
  val number: Int,
  val title: String,
  val explanationLines: List<String>,
  val formulaLines: List<String>
)

data class ParsedMathSolution(
  val problemDetected: String?,
  val steps: List<SolutionStep>,
  val finalResult: String?,
  val summaryParagraphs: List<String>
)

/**
 * Robust mathematical text cleaner and structured solution parser.
 * Completely cleans LLM outputs: eliminates hashtags (#, ##, ###), markdown asterisks,
 * LaTeX macros (\frac, \cdot, \times, \boxed, etc.), and prepares human-readable,
 * student-friendly step-by-step solutions.
 */
object MathTextCleaner {

  fun clean(raw: String): String {
    if (raw.isBlank()) return ""

    var text = raw

    // 1. Remove code fences: ```math ... ``` or ```markdown or ```
    text = text.replace(Regex("""```[a-zA-Z0-9_-]*\n?"""), "")
    text = text.replace("```", "")

    // 2. Remove LaTeX math display brackets: \[ ... \] and \( ... \)
    text = text.replace("\\[", "\n")
    text = text.replace("\\]", "\n")
    text = text.replace("\\(", "")
    text = text.replace("\\)", "")

    // 3. Remove dollar signs used for LaTeX math delimiters ($$ and $)
    text = text.replace("$$", "")
    text = text.replace("$", "")

    // 4. Remove LaTeX environments: \begin{aligned}, \end{aligned}, \begin{cases}, etc.
    text = text.replace(Regex("""\\begin\{[^{}]+\}"""), "")
    text = text.replace(Regex("""\\end\{[^{}]+\}"""), "")

    // 5. Remove LaTeX boxed and text wrappers
    text = text.replace(Regex("""\\boxed\{([^{}]+)\}"""), "$1")
    text = text.replace(Regex("""\\textbf\{([^{}]+)\}"""), "$1")
    text = text.replace(Regex("""\\textit\{([^{}]+)\}"""), "$1")
    text = text.replace(Regex("""\\text\{([^{}]+)\}"""), "$1")
    text = text.replace(Regex("""\\mathrm\{([^{}]+)\}"""), "$1")
    text = text.replace(Regex("""\\mathbf\{([^{}]+)\}"""), "$1")

    // 6. Recursively convert LaTeX fractions: \frac{a}{b} -> (a / b) or a / b
    val fracRegex = Regex("""\\frac\{([^{}]+)\}\{([^{}]+)\}""")
    var iterations = 0
    while (fracRegex.containsMatchIn(text) && iterations < 8) {
      text = fracRegex.replace(text) { match ->
        val num = match.groupValues[1].trim()
        val den = match.groupValues[2].trim()
        if (num.length <= 4 && den.length <= 4 && !num.contains(" ") && !den.contains(" ")) {
          "$num/$den"
        } else {
          "($num / $den)"
        }
      }
      iterations++
    }

    // 7. Convert LaTeX roots: \sqrt[n]{a} -> ⁿ√(a) and \sqrt{a} -> √(a)
    text = text.replace(Regex("""\\sqrt\[([^{}]+)\]\{([^{}]+)\}"""), "$1√($2)")
    text = text.replace(Regex("""\\sqrt\{([^{}]+)\}"""), "√($1)")

    // 8. Convert LaTeX superscripts and subscripts to clean Unicode
    text = text
      .replace("^{2}", "²")
      .replace("^2", "²")
      .replace("^{3}", "³")
      .replace("^3", "³")
      .replace("^{0}", "⁰")
      .replace("^0", "⁰")
      .replace("^{1}", "¹")
      .replace("^1", "¹")
      .replace("^{4}", "⁴")
      .replace("^4", "⁴")
      .replace("^{5}", "⁵")
      .replace("^5", "⁵")
      .replace("^{6}", "⁶")
      .replace("^6", "⁶")
      .replace("^{7}", "⁷")
      .replace("^7", "⁷")
      .replace("^{8}", "⁸")
      .replace("^8", "⁸")
      .replace("^{9}", "⁹")
      .replace("^9", "⁹")
      .replace("_{0}", "₀")
      .replace("_0", "₀")
      .replace("_{1}", "₁")
      .replace("_1", "₁")
      .replace("_{2}", "₂")
      .replace("_2", "₂")
      .replace("_{3}", "₃")
      .replace("_3", "₃")

    // 9. Convert LaTeX mathematical operators and Greek symbols to Unicode
    text = text
      .replace("""\\cdot""", "·")
      .replace("""\cdot""", "·")
      .replace("""\\times""", "×")
      .replace("""\times""", "×")
      .replace("""\\div""", "÷")
      .replace("""\div""", "÷")
      .replace("""\\pm""", "±")
      .replace("""\pm""", "±")
      .replace("""\\mp""", "∓")
      .replace("""\mp""", "∓")
      .replace("""\\leq""", "≤")
      .replace("""\leq""", "≤")
      .replace("""\\le""", "≤")
      .replace("""\le""", "≤")
      .replace("""\\geq""", "≥")
      .replace("""\geq""", "≥")
      .replace("""\\ge""", "≥")
      .replace("""\ge""", "≥")
      .replace("""\\neq""", "≠")
      .replace("""\neq""", "≠")
      .replace("""\\approx""", "≈")
      .replace("""\approx""", "≈")
      .replace("""\\equiv""", "≡")
      .replace("""\equiv""", "≡")
      .replace("""\\pi""", "π")
      .replace("""\pi""", "π")
      .replace("""\\infty""", "∞")
      .replace("""\infty""", "∞")
      .replace("""\\alpha""", "α")
      .replace("""\alpha""", "α")
      .replace("""\\beta""", "β")
      .replace("""\beta""", "β")
      .replace("""\\theta""", "θ")
      .replace("""\theta""", "θ")
      .replace("""\\Delta""", "Δ")
      .replace("""\Delta""", "Δ")
      .replace("""\\lambda""", "λ")
      .replace("""\lambda""", "λ")
      .replace("""\\mu""", "μ")
      .replace("""\mu""", "μ")
      .replace("""\\sigma""", "σ")
      .replace("""\sigma""", "σ")
      .replace("""\\sum""", "∑")
      .replace("""\sum""", "∑")
      .replace("""\\int""", "∫")
      .replace("""\int""", "∫")
      .replace("""\\partial""", "∂")
      .replace("""\partial""", "∂")
      .replace("""\\to""", "→")
      .replace("""\to""", "→")
      .replace("""\\rightarrow""", "→")
      .replace("""\rightarrow""", "→")
      .replace("""\\Leftarrow""", "⇐")
      .replace("""\Leftarrow""", "⇐")
      .replace("""\\Rightarrow""", "⇒")
      .replace("""\Rightarrow""", "⇒")
      .replace("""\\Leftrightarrow""", "⇔")
      .replace("""\Leftrightarrow""", "⇔")
      .replace("""\\%""", "%")
      .replace("""\%""", "%")
      .replace("""\\$""", "$")
      .replace("""\$""", "$")
      .replace("""\\&""", "&")
      .replace("""\&""", "&")

    // 10. Remove LaTeX formatting commands like \left, \right, \quad, \displaystyle
    text = text
      .replace("""\left(""", "(")
      .replace("""\right)""", ")")
      .replace("""\left[""", "[")
      .replace("""\right]""", "]")
      .replace("""\left\{""", "{")
      .replace("""\right\}""", "}")
      .replace("""\left|""", "|")
      .replace("""\right|""", "|")
      .replace("""\left.""", "")
      .replace("""\right.""", "")
      .replace("""\displaystyle""", "")
      .replace("""\limits""", "")
      .replace("""\quad""", " ")
      .replace("""\qquad""", "  ")
      .replace("""\,""", " ")
      .replace("""\;""", " ")
      .replace("""\!""", "")

    // 11. Handle LaTeX double backslash as newline and alignment &
    text = text.replace("""\\""", "\n")
    text = text.replace("&=", " = ")
    text = text.replace("&", " ")

    // 12. Strip lingering backslashes before common functions
    text = text
      .replace("""\sin""", "sin")
      .replace("""\cos""", "cos")
      .replace("""\tan""", "tan")
      .replace("""\ln""", "ln")
      .replace("""\log""", "log")
      .replace("""\exp""", "exp")

    // 13. Line by line cleanup: HASHTAGS, DIVIDERS, ASTERISKS, BULLETS
    val lines = text.lines()
    val cleanedLines = mutableListOf<String>()

    for (line in lines) {
      var trimmed = line.trim()

      // Skip horizontal divider lines like "---", "***", "___"
      if (trimmed.matches(Regex("""^[-*_]{3,}$"""))) {
        cleanedLines.add("")
        continue
      }

      // Remove blockquote prefix '>'
      if (trimmed.startsWith(">")) {
        trimmed = trimmed.removePrefix(">").trim()
      }

      // CRITICAL FIX: Eliminate ALL leading hashtags '#', '##', '###', etc.
      // Regardless of how many # or leading spaces there are
      if (trimmed.startsWith("#")) {
        trimmed = trimmed.replace(Regex("""^#+\s*"""), "")
      }

      // Also remove any stray '#' character anywhere in the line
      // unless immediately followed by a digit and preceded by word (e.g. rarely "ejercicio #1")
      // To be completely safe against hashtags showing up:
      trimmed = trimmed.replace(Regex("""#+"""), "").trim()

      // CRITICAL FIX: Remove Markdown bold/italic asterisks '**', '***', '__'
      trimmed = trimmed.replace("**", "")
      trimmed = trimmed.replace("***", "")
      trimmed = trimmed.replace("__", "")

      // Normalize lone bullet markers (* or -) into clean bullet •
      if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("+ ")) {
        trimmed = "• " + trimmed.substring(2).trim()
      }

      cleanedLines.add(trimmed)
    }

    // Collapse multiple blank lines
    val result = StringBuilder()
    var prevBlank = false
    for (l in cleanedLines) {
      if (l.isBlank()) {
        if (!prevBlank) {
          result.append("\n")
          prevBlank = true
        }
      } else {
        if (result.isNotEmpty() && !prevBlank) {
          result.append("\n")
        }
        result.append(l)
        prevBlank = false
      }
    }

    return result.toString().trim()
  }

  /**
   * Parses the cleaned text into a structured step-by-step solution.
   */
  fun parseSolution(raw: String): ParsedMathSolution {
    val cleaned = clean(raw)
    val lines = cleaned.lines().map { it.trim() }.filter { it.isNotEmpty() }

    var problemDetected: String? = null
    var finalResult: String? = null
    val steps = mutableListOf<SolutionStep>()
    val generalParagraphs = mutableListOf<String>()

    var currentStepNumber = 0
    var currentStepTitle = ""
    val currentStepExplanations = mutableListOf<String>()
    val currentStepFormulas = mutableListOf<String>()

    fun commitCurrentStep() {
      if (currentStepNumber > 0 || currentStepTitle.isNotEmpty()) {
        steps.add(
          SolutionStep(
            number = if (currentStepNumber > 0) currentStepNumber else steps.size + 1,
            title = currentStepTitle.ifBlank { "Paso ${steps.size + 1}" },
            explanationLines = currentStepExplanations.toList(),
            formulaLines = currentStepFormulas.toList()
          )
        )
        currentStepExplanations.clear()
        currentStepFormulas.clear()
        currentStepTitle = ""
        currentStepNumber = 0
      }
    }

    var readingFinalResult = false
    val finalResultLines = mutableListOf<String>()

    for (line in lines) {
      val lower = line.lowercase()

      // Check for Final Result header
      if (lower.startsWith("resultado final") || lower.startsWith("solución final") ||
          lower.startsWith("respuesta final") || lower.startsWith("resultado:") ||
          lower.startsWith("respuesta:")
      ) {
        commitCurrentStep()
        val colonIdx = line.indexOf(':')
        val directContent = if (colonIdx != -1) line.substring(colonIdx + 1).trim() else ""
        if (directContent.isNotBlank()) {
          finalResultLines.add(directContent)
        }
        readingFinalResult = true
        continue
      }

      if (readingFinalResult) {
        finalResultLines.add(line)
        continue
      }

      // Check for Problem Statement
      if (lower.startsWith("problema:") || lower.startsWith("enunciado:") ||
          lower.startsWith("planteamiento:") || lower.startsWith("problema detectado:")
      ) {
        val colonIdx = line.indexOf(':')
        val content = if (colonIdx != -1) line.substring(colonIdx + 1).trim() else line
        problemDetected = if (content.isNotBlank()) content else null
        continue
      }

      // Check for Step Header (e.g. "Paso 1: Agrupar", "1. Identificar variables", "Paso 2")
      val stepMatch = Regex("""^(?:Paso\s*(\d+)[:.]?|(\d+)\.)\s*(.*)$""", RegexOption.IGNORE_CASE).find(line)
      if (stepMatch != null) {
        commitCurrentStep()
        val numStr = stepMatch.groupValues[1].ifEmpty { stepMatch.groupValues[2] }
        val titleStr = stepMatch.groupValues[3].trim()
        currentStepNumber = numStr.toIntOrNull() ?: (steps.size + 1)
        currentStepTitle = if (titleStr.isNotBlank()) titleStr else "Paso $currentStepNumber"
        continue
      }

      // Classify line inside an active step or as general paragraph
      if (currentStepNumber > 0 || steps.isNotEmpty()) {
        // If line looks like a prominent formula (contains '=', '>', '<', '·', 'x', '+', etc. and is concise)
        if (isMathExpressionLine(line)) {
          currentStepFormulas.add(line)
        } else {
          currentStepExplanations.add(line)
        }
      } else {
        if (problemDetected == null && (lower.contains("determinar") || lower.contains("calcular") || lower.contains("resolver"))) {
          problemDetected = line
        } else {
          generalParagraphs.add(line)
        }
      }
    }

    commitCurrentStep()

    if (finalResultLines.isNotEmpty()) {
      finalResult = finalResultLines.joinToString("\n").trim()
    }

    // If finalResult was not explicitly marked with "Resultado Final:", look for it at the end
    if (finalResult.isNullOrBlank()) {
      finalResult = extractFinalResult(cleaned)
    }

    return ParsedMathSolution(
      problemDetected = problemDetected,
      steps = steps,
      finalResult = finalResult,
      summaryParagraphs = generalParagraphs
    )
  }

  private fun isMathExpressionLine(line: String): Boolean {
    val trimmed = line.trim()
    if (trimmed.length > 80) return false
    val mathSymbols = listOf("=", "+", "·", "×", "÷", "√", "²", "³", "^", "/", "≤", "≥", "≠")
    val hasSymbol = mathSymbols.any { trimmed.contains(it) }
    val hasDigits = trimmed.any { it.isDigit() }
    val wordCount = trimmed.split(Regex("""\s+""")).size
    return (hasSymbol && hasDigits && wordCount <= 8) || (trimmed.startsWith("x =") || trimmed.startsWith("y ="))
  }

  /**
   * Helper to extract the final result string to pass to the algebraic calculator.
   */
  fun extractFinalResult(text: String): String? {
    val lower = text.lowercase()
    val markers = listOf(
      "resultado final:", "solución final:", "respuesta final:",
      "resultado:", "respuesta:", "solución:"
    )
    for (m in markers) {
      val idx = lower.lastIndexOf(m)
      if (idx != -1) {
        val snippet = text.substring(idx + m.length).trim()
        val firstLine = snippet.lines().firstOrNull { it.isNotBlank() }
        if (!firstLine.isNullOrBlank()) {
          return firstLine.take(120)
        }
      }
    }
    return null
  }
}
