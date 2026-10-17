package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryRepository
import com.example.math.ExpressionParser
import com.example.math.SymbolicAlgebra

@Composable
fun SymbolicAlgebraScreen(
  onSendToCalculator: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Simplificación Simbólica", "Administrador de Variables")

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      modifier = Modifier.fillMaxWidth().testTag("symbolic_tabs")
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("tab_sym_$index")
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (selectedTab) {
      0 -> SymbolicSimplificationTab(onSendToCalculator)
      1 -> VariableManagerTab(onSendToCalculator)
    }
  }
}

@Composable
private fun SymbolicSimplificationTab(
  onSendToCalculator: (String) -> Unit
) {
  var expression by remember { mutableStateOf("(x + 2)*(x - 3) + 2*x^2") }
  var variableName by remember { mutableStateOf("x") }
  var resultText by remember { mutableStateOf<String?>(null) }
  var resultSteps by remember { mutableStateOf<List<String>>(emptyList()) }
  var error by remember { mutableStateOf<String?>(null) }

  val presets = listOf(
    "(x + 2)*(x - 3)",
    "(2*x + 1)^2",
    "x^2 - 5*x + 6",
    "3*x^2 + 4*x - x^2 + 5",
    "x*(x^2 - 4) + 3*x^2"
  )

  fun handleSimplify() {
    try {
      val (sim, steps) = SymbolicAlgebra.simplify(expression, variableName)
      resultText = sim
      resultSteps = steps
      error = null
      HistoryRepository.addEntry("Simplificar: $expression", sim, "Álgebra Simbólica")
    } catch (e: Exception) {
      error = e.message
      resultText = null
    }
  }

  fun handleFactor() {
    try {
      val fac = SymbolicAlgebra.factor(expression, variableName)
      resultText = fac
      resultSteps = listOf(
        "Expresión: $expression",
        "Factorización de términos cuadráticos y monomios comunes.",
        "Forma factorizada: $fac"
      )
      error = null
      HistoryRepository.addEntry("Factorizar: $expression", fac, "Álgebra Simbólica")
    } catch (e: Exception) {
      error = e.message
      resultText = null
    }
  }

  fun handleDifferentiate() {
    try {
      val (deriv, steps) = SymbolicAlgebra.differentiateSymbolic(expression, variableName)
      resultText = deriv
      resultSteps = steps
      error = null
      HistoryRepository.addEntry("d/d$variableName ($expression)", deriv, "Derivada Simbólica")
    } catch (e: Exception) {
      error = e.message
      resultText = null
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Operaciones Algebraicas Simbólicas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Expande productos, simplifica términos semejantes, factoriza o deriva respecto a una variable.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = expression,
            onValueChange = {
              expression = it
              error = null
            },
            label = { Text("Expresión Algebraica") },
            modifier = Modifier.fillMaxWidth().testTag("input_symbolic_expr"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = variableName,
              onValueChange = { if (it.length <= 2) variableName = it },
              label = { Text("Variable") },
              modifier = Modifier.width(90.dp),
              singleLine = true
            )
            Text(
              text = "Ejemplos rápidos:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(presets) { p ->
              SuggestionChip(
                onClick = { expression = p },
                label = { Text(p, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Button(
              onClick = { handleSimplify() },
              modifier = Modifier.weight(1f).testTag("btn_sym_simplify")
            ) {
              Text("Simplificar")
            }
            FilledTonalButton(
              onClick = { handleFactor() },
              modifier = Modifier.weight(1f).testTag("btn_sym_factor")
            ) {
              Text("Factorizar")
            }
            FilledTonalButton(
              onClick = { handleDifferentiate() },
              modifier = Modifier.weight(1f).testTag("btn_sym_deriv")
            ) {
              Text("Derivar")
            }
          }
        }
      }
    }

    if (error != null) {
      item {
        Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
      }
    }

    if (resultText != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Resultado Simbólico:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              TextButton(
                onClick = { onSendToCalculator(resultText!!) }
              ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Usar en Calc")
              }
            }

            Text(
              text = resultText!!,
              style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
          }
        }
      }

      item {
        Text("Desarrollo paso a paso:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }

      items(resultSteps) { step ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        ) {
          Text(text = step, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodyMedium)
        }
      }
    }
  }
}

@Composable
private fun VariableManagerTab(
  onSendToCalculator: (String) -> Unit
) {
  val variables by HistoryRepository.variables.collectAsState()
  var newVarName by remember { mutableStateOf("") }
  var newVarVal by remember { mutableStateOf("") }
  var evalExpr by remember { mutableStateOf("3*x + 2*y - a") }
  var evalResult by remember { mutableStateOf<String?>(null) }
  var evalError by remember { mutableStateOf<String?>(null) }

  fun evaluateWithActiveVariables() {
    try {
      val parser = ExpressionParser()
      val res = parser.evaluate(evalExpr, variables.mapValues { com.example.math.Complex(it.value) })
      evalResult = res.format(6)
      evalError = null
      HistoryRepository.addEntry("Eval($evalExpr)", evalResult!!, "Variables")
    } catch (e: Exception) {
      evalError = e.message
      evalResult = null
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Variable Definition Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Definir o Actualizar Variables", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("Asigna valores a variables algebraicas (ej. x = 4, y = 2.5, a = -1)", style = MaterialTheme.typography.bodySmall)

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = newVarName,
              onValueChange = { newVarName = it },
              label = { Text("Variable") },
              placeholder = { Text("x") },
              modifier = Modifier.weight(1f).testTag("input_new_var_name"),
              singleLine = true
            )
            OutlinedTextField(
              value = newVarVal,
              onValueChange = { newVarVal = it },
              label = { Text("Valor") },
              placeholder = { Text("5.0") },
              modifier = Modifier.weight(1.5f).testTag("input_new_var_val"),
              singleLine = true
            )
            Button(
              onClick = {
                val v = newVarVal.toDoubleOrNull()
                if (newVarName.isNotBlank() && v != null) {
                  HistoryRepository.setVariable(newVarName, v)
                  newVarName = ""
                  newVarVal = ""
                }
              },
              modifier = Modifier.testTag("btn_save_var")
            ) {
              Icon(Icons.Default.Add, contentDescription = "Guardar")
            }
          }
        }
      }
    }

    // Active Variables List
    item {
      Text("Variables Activas:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }

    items(variables.toList()) { (vName, vVal) ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestionChip(
              onClick = { onSendToCalculator(vName) },
              label = { Text(vName, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
            )
            Text("=", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text("$vVal", style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
          }

          Row {
            IconButton(
              onClick = { onSendToCalculator(vName) }
            ) {
              Icon(Icons.Default.Send, contentDescription = "Insertar en Calc", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(
              onClick = { HistoryRepository.removeVariable(vName) }
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
          }
        }
      }
    }

    // Expression Evaluator with Variables
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Evaluar Expresión con Variables", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = evalExpr,
            onValueChange = { evalExpr = it },
            label = { Text("Expresión con variables (ej: 3*x + 2*y)") },
            modifier = Modifier.fillMaxWidth().testTag("input_eval_vars_expr"),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = { evaluateWithActiveVariables() },
            modifier = Modifier.fillMaxWidth().testTag("btn_eval_with_vars")
          ) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Evaluar con Valores Actuales")
          }

          if (evalError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(evalError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }

          if (evalResult != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("Resultado numérico:", style = MaterialTheme.typography.labelSmall)
                  Text(
                    evalResult!!,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                TextButton(onClick = { onSendToCalculator(evalResult!!) }) {
                  Text("Usar en Calc")
                }
              }
            }
          }
        }
      }
    }
  }
}
