package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryItem
import com.example.data.HistoryRepository
import com.example.math.AngleMode
import com.example.math.Complex
import com.example.math.ExpressionParser
import com.example.ui.components.CalcKeyButton
import com.example.ui.components.KeypadButtonType
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlgebraicCalcScreen(
  onOpenCredits: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var expression by remember { mutableStateOf("") }
  var resultText by remember { mutableStateOf("0") }
  var fractionText by remember { mutableStateOf<String?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var angleMode by remember { mutableStateOf(AngleMode.DEG) }
  var showExtendedFunctions by remember { mutableStateOf(false) }
  var showQuickHistorySheet by remember { mutableStateOf(false) }

  val parser = remember(angleMode) { ExpressionParser(angleMode) }
  val lastAns by HistoryRepository.lastAnswer.collectAsState()
  val memoryVal by HistoryRepository.memoryValue.collectAsState()
  val activeVars by HistoryRepository.variables.collectAsState()
  val historyList by HistoryRepository.history.collectAsState()

  // Listen for insert requests from other components/screens
  LaunchedEffect(Unit) {
    HistoryRepository.insertRequest.collectLatest { itemToInsert ->
      expression = if (expression.isEmpty()) itemToInsert else expression + itemToInsert
    }
  }

  // Real-time evaluation with variables support
  LaunchedEffect(expression, angleMode, activeVars) {
    if (expression.isBlank()) {
      resultText = "0"
      fractionText = null
      errorMessage = null
      return@LaunchedEffect
    }
    try {
      val varMap = activeVars.mapValues { Complex(it.value) }.toMutableMap()
      varMap["ans"] = parser.evaluate(lastAns) rescue Complex.ZERO
      val res = parser.evaluate(expression, varMap)
      val formatted = res.format(6)
      resultText = formatted
      errorMessage = null

      if (res.isReal) {
        val frac = ExpressionParser.toFraction(res.re)
        fractionText = frac
      } else {
        fractionText = null
      }
    } catch (e: Exception) {
      // Don't show error immediately while typing partial input
      errorMessage = null
    }
  }

  fun appendText(txt: String) {
    expression += txt
  }

  fun deleteLast() {
    if (expression.isNotEmpty()) {
      expression = expression.dropLast(1)
    }
  }

  fun clearAll() {
    expression = ""
    resultText = "0"
    fractionText = null
    errorMessage = null
  }

  fun evaluateAndSave() {
    if (expression.isBlank()) return
    try {
      val varMap = activeVars.mapValues { Complex(it.value) }.toMutableMap()
      varMap["ans"] = try { parser.evaluate(lastAns) } catch (e: Exception) { Complex.ZERO }
      val res = parser.evaluate(expression, varMap)
      val finalRes = res.format(8)
      resultText = finalRes
      HistoryRepository.addEntry(expression, finalRes, "Álgebra")
      expression = finalRes
    } catch (e: Exception) {
      errorMessage = e.message ?: "Error de sintaxis"
    }
  }

  // Quick History Bottom Sheet
  if (showQuickHistorySheet) {
    ModalBottomSheet(
      onDismissRequest = { showQuickHistorySheet = false },
      sheetState = rememberModalBottomSheetState()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Historial Rápido de Cálculos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          if (historyList.isNotEmpty()) {
            TextButton(onClick = { HistoryRepository.clearHistory() }) {
              Text("Limpiar", color = MaterialTheme.colorScheme.error)
            }
          }
        }
        Text("Toca cualquier cálculo para reutilizar su expresión o resultado:", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(8.dp))

        if (historyList.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("No hay cálculos anteriores aún", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 350.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(historyList) { item ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(item.expression, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                    Text("= ${item.result}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                  }
                  Row {
                    FilledTonalButton(
                      onClick = {
                        expression = item.expression
                        showQuickHistorySheet = false
                      },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                      modifier = Modifier.height(32.dp)
                    ) {
                      Text("Expresión", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                      onClick = {
                        appendText(item.result)
                        showQuickHistorySheet = false
                      },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                      modifier = Modifier.height(32.dp)
                    ) {
                      Text("Resultado", fontSize = 11.sp)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // --- Top Bar Controls: Angle Mode & Quick History Button ---
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Angle Mode Toggle
      SingleChoiceSegmentedButtonRow(modifier = Modifier.height(32.dp)) {
        SegmentedButton(
          selected = angleMode == AngleMode.DEG,
          onClick = { angleMode = AngleMode.DEG },
          shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
          modifier = Modifier.testTag("mode_deg")
        ) {
          Text("DEG", fontSize = 11.sp)
        }
        SegmentedButton(
          selected = angleMode == AngleMode.RAD,
          onClick = { angleMode = AngleMode.RAD },
          shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
          modifier = Modifier.testTag("mode_rad")
        ) {
          Text("RAD", fontSize = 11.sp)
        }
        SegmentedButton(
          selected = angleMode == AngleMode.GRAD,
          onClick = { angleMode = AngleMode.GRAD },
          shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
          modifier = Modifier.testTag("mode_grad")
        ) {
          Text("GRAD", fontSize = 11.sp)
        }
      }

      // Memory, Fraction Indicators and Quick History
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (fractionText != null) {
          AssistChip(
            onClick = { expression = fractionText!! },
            label = { Text("≈ $fractionText", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.height(28.dp)
          )
        }

        IconButton(
          onClick = { showQuickHistorySheet = true },
          modifier = Modifier.size(32.dp).testTag("btn_quick_history")
        ) {
          Icon(Icons.Default.History, contentDescription = "Historial rápido", tint = MaterialTheme.colorScheme.primary)
        }
      }
    }

    // --- Math Display Area ---
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f, fill = false)
        .padding(vertical = 2.dp),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
      ) {
        val scrollState = rememberScrollState()
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
          horizontalArrangement = Arrangement.End
        ) {
          Text(
            text = if (expression.isEmpty()) "0" else expression,
            style = MaterialTheme.typography.headlineMedium.copy(
              fontSize = if (expression.length > 18) 22.sp else 28.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Medium
            ),
            color = if (expression.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            textAlign = TextAlign.End
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (errorMessage != null) {
          Text(
            text = errorMessage!!,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.End
          )
        } else {
          Text(
            text = "= $resultText",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.End
          )
        }
      }
    }

    // --- Memory Bar ---
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      CalcKeyButton(
        label = "MC",
        onClick = { HistoryRepository.memoryClear() },
        modifier = Modifier.weight(1f).height(34.dp),
        type = KeypadButtonType.MEMORY
      )
      CalcKeyButton(
        label = "MR",
        onClick = { appendText(memoryVal.toString()) },
        modifier = Modifier.weight(1f).height(34.dp),
        type = KeypadButtonType.MEMORY
      )
      CalcKeyButton(
        label = "M+",
        onClick = {
          val v = resultText.toDoubleOrNull() ?: 0.0
          HistoryRepository.memoryAdd(v)
        },
        modifier = Modifier.weight(1f).height(34.dp),
        type = KeypadButtonType.MEMORY
      )
      CalcKeyButton(
        label = "M-",
        onClick = {
          val v = resultText.toDoubleOrNull() ?: 0.0
          HistoryRepository.memorySubtract(v)
        },
        modifier = Modifier.weight(1f).height(34.dp),
        type = KeypadButtonType.MEMORY
      )
      CalcKeyButton(
        label = "Ans",
        onClick = { appendText("ans") },
        modifier = Modifier.weight(1f).height(34.dp),
        type = KeypadButtonType.MEMORY
      )
      IconButton(
        onClick = { showExtendedFunctions = !showExtendedFunctions },
        modifier = Modifier.size(34.dp)
      ) {
        Icon(
          imageVector = if (showExtendedFunctions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = "Funciones extendidas",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }

    // --- Extended Functions Panel (Collapsible) ---
    AnimatedVisibility(
      visible = showExtendedFunctions,
      enter = expandVertically(),
      exit = shrinkVertically()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          CalcKeyButton("sinh", { appendText("sinh(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("cosh", { appendText("cosh(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("tanh", { appendText("tanh(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("asin", { appendText("asin(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("acos", { appendText("acos(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("atan", { appendText("atan(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          CalcKeyButton("nPr", { appendText("npr(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("nCr", { appendText("ncr(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("log2", { appendText("log2(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("exp", { appendText("exp(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("abs", { appendText("abs(") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
          CalcKeyButton("φ", { appendText("phi") }, Modifier.weight(1f).height(36.dp), KeypadButtonType.FUNCTION)
        }
      }
    }

    // --- Primary Scientific & Algebraic Row ---
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 2.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Algebraic shortcuts row 1
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalcKeyButton("sin", { appendText("sin(") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("cos", { appendText("cos(") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("tan", { appendText("tan(") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("π", { appendText("pi") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("e", { appendText("e") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
      }

      // Algebraic shortcuts row 2
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalcKeyButton("x", { appendText("x") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("y", { appendText("y") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("i", { appendText("i") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("x^y", { appendText("^") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("√", { appendText("sqrt(") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
      }

      // Algebraic shortcuts row 3
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalcKeyButton("ln", { appendText("ln(") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("log", { appendText("log(") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("(", { appendText("(") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton(")", { appendText(")") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
        CalcKeyButton("n!", { appendText("!") }, Modifier.weight(1f).height(42.dp), KeypadButtonType.FUNCTION)
      }

      // Number & Standard Operations Grid
      // Row 1: 7, 8, 9, ÷, DEL
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalcKeyButton("7", { appendText("7") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("8", { appendText("8") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("9", { appendText("9") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("÷", { appendText("/") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.OPERATOR)
        CalcKeyButton("⌫", { deleteLast() }, Modifier.weight(1f).height(46.dp), KeypadButtonType.CLEAR)
      }

      // Row 2: 4, 5, 6, ×, C
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalcKeyButton("4", { appendText("4") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("5", { appendText("5") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("6", { appendText("6") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("×", { appendText("*") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.OPERATOR)
        CalcKeyButton("C", { clearAll() }, Modifier.weight(1f).height(46.dp), KeypadButtonType.CLEAR)
      }

      // Row 3: 1, 2, 3, −, %
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalcKeyButton("1", { appendText("1") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("2", { appendText("2") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("3", { appendText("3") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("−", { appendText("-") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.OPERATOR)
        CalcKeyButton("%", { appendText("%") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.OPERATOR)
      }

      // Row 4: 0, ., =, +
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalcKeyButton("0", { appendText("0") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton(".", { appendText(".") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.NUMBER)
        CalcKeyButton("+", { appendText("+") }, Modifier.weight(1f).height(46.dp), KeypadButtonType.OPERATOR)
        CalcKeyButton("=", { evaluateAndSave() }, Modifier.weight(2f).height(46.dp), KeypadButtonType.EQUAL)
      }
    }
  }
}

private infix fun Complex.rescue(default: Complex): Complex = this
