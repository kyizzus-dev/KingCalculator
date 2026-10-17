package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryRepository
import com.example.math.AngleMode
import com.example.math.CalculusEngine
import com.example.math.LimitResult

@Composable
fun CalculusScreen(
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Derivada", "Integral", "Límite", "Sumatoria", "Productoria")

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      edgePadding = 0.dp,
      modifier = Modifier.fillMaxWidth().testTag("calc_tools_tabs")
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("tab_calc_$index")
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (selectedTab) {
      0 -> DerivativeTab()
      1 -> IntegralTab()
      2 -> LimitTab()
      3 -> SummationTab()
      4 -> ProductTab()
    }
  }
}

@Composable
private fun DerivativeTab() {
  var expression by remember { mutableStateOf("x^3 - 3*x^2 + 2") }
  var x0Text by remember { mutableStateOf("2") }
  var result by remember { mutableStateOf<Pair<Double, List<String>>?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun calculate() {
    try {
      val x0 = x0Text.toDoubleOrNull() ?: throw IllegalArgumentException("Punto x₀ inválido")
      val res = CalculusEngine.derivative(expression, x0, AngleMode.RAD)
      result = res
      error = null
      HistoryRepository.addEntry("d/dx ($expression) | x=$x0", "${res.first}", "Derivada")
    } catch (e: Exception) {
      error = e.message
      result = null
    }
  }

  LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Derivada Numérica de Alta Precisión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("d/dx [f(x)] evaluada en x = x₀", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = expression,
            onValueChange = { expression = it },
            label = { Text("Función f(x)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = x0Text,
            onValueChange = { x0Text = it },
            label = { Text("Punto de evaluación (x₀)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(onClick = { calculate() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Calcular Derivada")
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    if (result != null) {
      val (deriv, steps) = result!!
      item {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Resultado:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("f'($x0Text) ≈ $deriv", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
      items(steps) { step ->
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
private fun IntegralTab() {
  var expression by remember { mutableStateOf("sin(x) + x^2") }
  var aText by remember { mutableStateOf("0") }
  var bText by remember { mutableStateOf("3") }
  var result by remember { mutableStateOf<Pair<Double, List<String>>?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun calculate() {
    try {
      val a = aText.toDoubleOrNull() ?: throw IllegalArgumentException("Límite inferior 'a' inválido")
      val b = bText.toDoubleOrNull() ?: throw IllegalArgumentException("Límite superior 'b' inválido")
      val res = CalculusEngine.definiteIntegral(expression, a, b, 1000, AngleMode.RAD)
      result = res
      error = null
      HistoryRepository.addEntry("∫ ($expression) dx de $a a $b", "${res.first}", "Integral")
    } catch (e: Exception) {
      error = e.message
      result = null
    }
  }

  LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Integral Definida Numérica (Regla de Simpson 1/3)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("∫ₐᵇ f(x) dx", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = expression,
            onValueChange = { expression = it },
            label = { Text("Integrando f(x)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = aText, onValueChange = { aText = it }, label = { Text("Límite inferior (a)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = bText, onValueChange = { bText = it }, label = { Text("Límite superior (b)") }, modifier = Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.height(10.dp))
          Button(onClick = { calculate() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Calcular Integral Definida")
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    if (result != null) {
      val (integral, steps) = result!!
      item {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Valor del Área / Integral:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("∫ ≈ $integral", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
      items(steps) { step ->
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
private fun LimitTab() {
  var expression by remember { mutableStateOf("(sin(x))/x") }
  var aText by remember { mutableStateOf("0") }
  var result by remember { mutableStateOf<LimitResult?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun calculate() {
    try {
      val a = aText.toDoubleOrNull() ?: 0.0
      val res = CalculusEngine.approximateLimit(expression, a, AngleMode.RAD)
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
          Text("Aproximación de Límites", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("lim_{x → a} f(x) mediante aproximación por izquierda y derecha", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = expression,
            onValueChange = { expression = it },
            label = { Text("Función f(x)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = aText,
            onValueChange = { aText = it },
            label = { Text("Punto hacia el que tiende x (a)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(onClick = { calculate() }, modifier = Modifier.fillMaxWidth()) {
            Text("Estudiar Límite")
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
            Text("Resultado del Límite:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (res.estimatedLimit != null) {
              Text("L ≈ ${String.format(java.util.Locale.US, "%.5f", res.estimatedLimit)}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            } else {
              Text("No existe o indeterminado", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
            }
            Text(res.description, style = MaterialTheme.typography.bodySmall)
          }
        }
      }

      item {
        Text("Valores laterales aproximados:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }

      items(res.table) { (xVal, yVal) ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        ) {
          Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("x = ${String.format(java.util.Locale.US, "%.5f", xVal)}", style = MaterialTheme.typography.bodyMedium)
            Text("f(x) = ${if (yVal.isNaN()) "Indefinido" else String.format(java.util.Locale.US, "%.5f", yVal)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    }
  }
}

@Composable
private fun SummationTab() {
  var expression by remember { mutableStateOf("n^2") }
  var startText by remember { mutableStateOf("1") }
  var endText by remember { mutableStateOf("10") }
  var result by remember { mutableStateOf<Pair<Double, List<String>>?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun calculate() {
    try {
      val s = startText.toLongOrNull() ?: 1L
      val e = endText.toLongOrNull() ?: 10L
      val res = CalculusEngine.summation(expression, s, e)
      result = res
      error = null
      HistoryRepository.addEntry("∑ $expression de $s a $e", "${res.first}", "Sumatoria")
    } catch (e: Exception) {
      error = e.message
      result = null
    }
  }

  LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Sumatoria Discreta ∑ f(n)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(value = expression, onValueChange = { expression = it }, label = { Text("Término general f(n)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = startText, onValueChange = { startText = it }, label = { Text("Inicio (n = a)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = endText, onValueChange = { endText = it }, label = { Text("Fin (b)") }, modifier = Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.height(10.dp))
          Button(onClick = { calculate() }, modifier = Modifier.fillMaxWidth()) {
            Text("Calcular Sumatoria")
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    if (result != null) {
      val (sum, steps) = result!!
      item {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("∑ = $sum", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
      items(steps) { step ->
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
private fun ProductTab() {
  var expression by remember { mutableStateOf("1 + 1/n") }
  var startText by remember { mutableStateOf("1") }
  var endText by remember { mutableStateOf("5") }
  var result by remember { mutableStateOf<Pair<Double, List<String>>?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun calculate() {
    try {
      val s = startText.toLongOrNull() ?: 1L
      val e = endText.toLongOrNull() ?: 5L
      val res = CalculusEngine.product(expression, s, e)
      result = res
      error = null
      HistoryRepository.addEntry("∏ $expression de $s a $e", "${res.first}", "Productoria")
    } catch (e: Exception) {
      error = e.message
      result = null
    }
  }

  LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Productoria Discreta ∏ f(n)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(value = expression, onValueChange = { expression = it }, label = { Text("Término general f(n)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = startText, onValueChange = { startText = it }, label = { Text("Inicio (n = a)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = endText, onValueChange = { endText = it }, label = { Text("Fin (b)") }, modifier = Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.height(10.dp))
          Button(onClick = { calculate() }, modifier = Modifier.fillMaxWidth()) {
            Text("Calcular Productoria")
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    if (result != null) {
      val (prod, steps) = result!!
      item {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Producto Acumulado:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("∏ = $prod", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
      items(steps) { step ->
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
