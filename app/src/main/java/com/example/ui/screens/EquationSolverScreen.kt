package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryRepository
import com.example.math.EquationSolver
import com.example.math.SolverResult

@Composable
fun EquationSolverScreen(
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Cuadrática", "Lineal", "Sistema 2x2", "Sistema 3x3", "Newton-Raphson")

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      edgePadding = 0.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("eq_solver_tabs")
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("tab_eq_$index")
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (selectedTab) {
      0 -> QuadraticSolverTab()
      1 -> LinearSolverTab()
      2 -> System2x2SolverTab()
      3 -> System3x3SolverTab()
      4 -> NewtonRaphsonTab()
    }
  }
}

@Composable
private fun QuadraticSolverTab() {
  var aText by remember { mutableStateOf("1") }
  var bText by remember { mutableStateOf("-5") }
  var cText by remember { mutableStateOf("6") }
  var result by remember { mutableStateOf<SolverResult.Quadratic?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun solve() {
    try {
      val a = aText.toDoubleOrNull() ?: throw IllegalArgumentException("Coeficiente 'a' inválido")
      val b = bText.toDoubleOrNull() ?: throw IllegalArgumentException("Coeficiente 'b' inválido")
      val c = cText.toDoubleOrNull() ?: throw IllegalArgumentException("Coeficiente 'c' inválido")
      val res = EquationSolver.solveQuadratic(a, b, c)
      result = res
      error = null
      HistoryRepository.addEntry("${a}x² + ${b}x + $c = 0", "x₁=${res.root1.format(3)}, x₂=${res.root2.format(3)}", "Ecuación Cuadrática")
    } catch (e: Exception) {
      error = e.message ?: "Error al resolver"
      result = null
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
          Text("Ecuación General de Segundo Grado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("ax² + bx + c = 0", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = aText,
              onValueChange = { aText = it },
              label = { Text("a") },
              modifier = Modifier.weight(1f).testTag("input_quad_a"),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true
            )
            OutlinedTextField(
              value = bText,
              onValueChange = { bText = it },
              label = { Text("b") },
              modifier = Modifier.weight(1f).testTag("input_quad_b"),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true
            )
            OutlinedTextField(
              value = cText,
              onValueChange = { cText = it },
              label = { Text("c") },
              modifier = Modifier.weight(1f).testTag("input_quad_c"),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = { solve() },
            modifier = Modifier.fillMaxWidth().testTag("btn_solve_quad")
          ) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calcular Raíces y Pasos")
          }
        }
      }
    }

    if (error != null) {
      item {
        Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
      }
    }

    if (result != null) {
      val res = result!!
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Solución:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("x₁ = ${res.root1.format(5)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text("x₂ = ${res.root2.format(5)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text("Discriminante Δ: ${res.discriminant}", style = MaterialTheme.typography.bodyMedium)
            Text("Vértice de la parábola: V(${res.vertexX}, ${res.vertexY})", style = MaterialTheme.typography.bodyMedium)
          }
        }
      }

      item {
        Text("Desarrollo paso a paso:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }

      items(res.steps) { step ->
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
private fun LinearSolverTab() {
  var aText by remember { mutableStateOf("2") }
  var bText by remember { mutableStateOf("3") }
  var cText by remember { mutableStateOf("11") }
  var result by remember { mutableStateOf<SolverResult.Linear?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun solve() {
    try {
      val a = aText.toDoubleOrNull() ?: throw IllegalArgumentException("Coeficiente 'a' inválido")
      val b = bText.toDoubleOrNull() ?: throw IllegalArgumentException("Coeficiente 'b' inválido")
      val c = cText.toDoubleOrNull() ?: throw IllegalArgumentException("Término 'c' inválido")
      val res = EquationSolver.solveLinear(a, b, c)
      result = res
      error = null
      HistoryRepository.addEntry("${a}x + $b = $c", "x = ${res.x}", "Ecuación Lineal")
    } catch (e: Exception) {
      error = e.message
      result = null
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
          Text("Ecuación Lineal de Primer Grado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("ax + b = c", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(value = aText, onValueChange = { aText = it }, label = { Text("a") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = bText, onValueChange = { bText = it }, label = { Text("b") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = cText, onValueChange = { cText = it }, label = { Text("c") }, modifier = Modifier.weight(1f), singleLine = true)
          }
          Spacer(modifier = Modifier.height(10.dp))
          Button(onClick = { solve() }, modifier = Modifier.fillMaxWidth()) {
            Text("Resolver x")
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    if (result != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Resultado:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("x = ${result!!.x}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
      items(result!!.steps) { step ->
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
private fun System2x2SolverTab() {
  var a1 by remember { mutableStateOf("2") }
  var b1 by remember { mutableStateOf("3") }
  var c1 by remember { mutableStateOf("8") }

  var a2 by remember { mutableStateOf("1") }
  var b2 by remember { mutableStateOf("-1") }
  var c2 by remember { mutableStateOf("-1") }

  var result by remember { mutableStateOf<SolverResult.System2x2?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun solve() {
    try {
      val res = EquationSolver.solveSystem2x2(
        a1.toDouble(), b1.toDouble(), c1.toDouble(),
        a2.toDouble(), b2.toDouble(), c2.toDouble()
      )
      result = res
      error = null
      val ansStr = if (res.x != null && res.y != null) "x=${res.x}, y=${res.y}" else "Sin sol. única"
      HistoryRepository.addEntry("Sistema 2x2", ansStr, "Sistema 2x2")
    } catch (e: Exception) {
      error = e.message
      result = null
    }
  }

  LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Sistema de Ecuaciones 2×2 (Cramer)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Text("Ecuación 1: a₁x + b₁y = c₁", style = MaterialTheme.typography.bodySmall)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = a1, onValueChange = { a1 = it }, label = { Text("a₁") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = b1, onValueChange = { b1 = it }, label = { Text("b₁") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = c1, onValueChange = { c1 = it }, label = { Text("c₁") }, modifier = Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text("Ecuación 2: a₂x + b₂y = c₂", style = MaterialTheme.typography.bodySmall)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = a2, onValueChange = { a2 = it }, label = { Text("a₂") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = b2, onValueChange = { b2 = it }, label = { Text("b₂") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = c2, onValueChange = { c2 = it }, label = { Text("c₂") }, modifier = Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.height(10.dp))
          Button(onClick = { solve() }, modifier = Modifier.fillMaxWidth()) {
            Text("Resolver Sistema (Regla de Cramer)")
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    if (result != null) {
      val res = result!!
      item {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (res.x != null && res.y != null) {
              Text("Solución única:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("x = ${res.x}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
              Text("y = ${res.y}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            } else if (res.isInfinite) {
              Text("Infinitas Soluciones (Sistema compatible indeterminado)", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            } else {
              Text("Sin Solución (Sistema incompatible)", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
      items(res.steps) { step ->
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
private fun System3x3SolverTab() {
  val eq1 = remember { mutableStateListOf("1", "1", "1", "6") }
  val eq2 = remember { mutableStateListOf("0", "2", "5", "-4") }
  val eq3 = remember { mutableStateListOf("2", "5", "-1", "27") }

  var result by remember { mutableStateOf<SolverResult.System3x3?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun solve() {
    try {
      val a = doubleArrayOf(eq1[0].toDouble(), eq2[0].toDouble(), eq3[0].toDouble())
      val b = doubleArrayOf(eq1[1].toDouble(), eq2[1].toDouble(), eq3[1].toDouble())
      val c = doubleArrayOf(eq1[2].toDouble(), eq2[2].toDouble(), eq3[2].toDouble())
      val d = doubleArrayOf(eq1[3].toDouble(), eq2[3].toDouble(), eq3[3].toDouble())

      val res = EquationSolver.solveSystem3x3(a, b, c, d)
      result = res
      error = null
    } catch (e: Exception) {
      error = e.message
      result = null
    }
  }

  LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Sistema de 3 Ecuaciones con 3 Incógnitas (x, y, z)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("a·x + b·y + c·z = d", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.height(8.dp))

          listOf(Pair("Ec 1", eq1), Pair("Ec 2", eq2), Pair("Ec 3", eq3)).forEach { (name, list) ->
            Text(name, style = MaterialTheme.typography.labelSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              OutlinedTextField(value = list[0], onValueChange = { list[0] = it }, label = { Text("x") }, modifier = Modifier.weight(1f))
              OutlinedTextField(value = list[1], onValueChange = { list[1] = it }, label = { Text("y") }, modifier = Modifier.weight(1f))
              OutlinedTextField(value = list[2], onValueChange = { list[2] = it }, label = { Text("z") }, modifier = Modifier.weight(1f))
              OutlinedTextField(value = list[3], onValueChange = { list[3] = it }, label = { Text("d") }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(4.dp))
          }

          Spacer(modifier = Modifier.height(10.dp))
          Button(onClick = { solve() }, modifier = Modifier.fillMaxWidth()) {
            Text("Resolver por Determinantes (Cramer)")
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    if (result != null) {
      val res = result!!
      item {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (res.x != null && res.y != null && res.z != null) {
              Text("Solución del Sistema:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("x = ${res.x}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
              Text("y = ${res.y}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
              Text("z = ${res.z}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            } else {
              Text("Sistema sin solución única (Δ = 0)", color = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
      items(res.steps) { step ->
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
private fun NewtonRaphsonTab() {
  var expr by remember { mutableStateOf("x^3 - 2*x - 5") }
  var seedX by remember { mutableStateOf("2.0") }
  var result by remember { mutableStateOf<SolverResult.NewtonRoot?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun solve() {
    try {
      val s = seedX.toDoubleOrNull() ?: 0.0
      val res = EquationSolver.solveNewtonRaphson(expr, s)
      result = res
      error = null
      HistoryRepository.addEntry("Newton: $expr = 0", "x ≈ ${res.root}", "Newton-Raphson")
    } catch (e: Exception) {
      error = e.message
      result = null
    }
  }

  LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Método de Newton-Raphson f(x) = 0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("Encuentra raíces reales de funciones no lineales iterativamente.", style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = expr,
            onValueChange = { expr = it },
            label = { Text("Función f(x)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = seedX,
            onValueChange = { seedX = it },
            label = { Text("Valor Inicial (x₀)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(onClick = { solve() }, modifier = Modifier.fillMaxWidth()) {
            Text("Calcular Raíz por Newton-Raphson")
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    if (result != null) {
      val res = result!!
      item {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Raíz Encontrada:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("x ≈ ${res.root}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(if (res.converged) "Convergencia exitosa" else "Máximo de iteraciones alcanzado", style = MaterialTheme.typography.bodySmall)
          }
        }
      }

      item {
        Text("Tabla de iteraciones:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }

      items(res.iterations) { row ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        ) {
          Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Paso ${row.step}: x = ${String.format(java.util.Locale.US, "%.5f", row.x)}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            Text("f(x) = ${String.format(java.util.Locale.US, "%.2e", row.fx)}", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
  }
}
