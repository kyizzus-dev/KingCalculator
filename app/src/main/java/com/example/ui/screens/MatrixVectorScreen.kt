package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryRepository
import com.example.math.Matrix
import com.example.math.Vector3D

@Composable
fun MatrixVectorScreen(
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Matrices", "Vectores 3D")

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      modifier = Modifier.fillMaxWidth().testTag("matrix_vector_tabs")
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("tab_mat_$index")
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (selectedTab) {
      0 -> MatricesTab()
      1 -> VectorsTab()
    }
  }
}

@Composable
private fun MatricesTab() {
  var matrixSize by remember { mutableIntStateOf(2) } // 2 or 3

  val matA2 = remember { mutableStateListOf("1", "2", "3", "4") }
  val matB2 = remember { mutableStateListOf("5", "6", "7", "8") }

  val matA3 = remember { mutableStateListOf("1", "2", "3", "0", "1", "4", "5", "6", "0") }
  val matB3 = remember { mutableStateListOf("2", "0", "-1", "1", "3", "2", "0", "-2", "1") }

  var scalarText by remember { mutableStateOf("2") }

  var calculationTitle by remember { mutableStateOf<String?>(null) }
  var scalarResult by remember { mutableStateOf<Double?>(null) }
  var matrixResult by remember { mutableStateOf<Matrix?>(null) }
  var stepsResult by remember { mutableStateOf<List<String>>(emptyList()) }
  var error by remember { mutableStateOf<String?>(null) }

  fun getMatrixA(): Matrix {
    return if (matrixSize == 2) {
      Matrix.from2x2(matA2[0].toDouble(), matA2[1].toDouble(), matA2[2].toDouble(), matA2[3].toDouble())
    } else {
      Matrix.from3x3(matA3.map { it.toDouble() }.toDoubleArray())
    }
  }

  fun getMatrixB(): Matrix {
    return if (matrixSize == 2) {
      Matrix.from2x2(matB2[0].toDouble(), matB2[1].toDouble(), matB2[2].toDouble(), matB2[3].toDouble())
    } else {
      Matrix.from3x3(matB3.map { it.toDouble() }.toDoubleArray())
    }
  }

  fun computeDet() {
    try {
      val a = getMatrixA()
      val det = a.determinant()
      calculationTitle = "Determinante de A"
      scalarResult = det
      matrixResult = null
      stepsResult = listOf(
        "Matriz A de dimensión ${matrixSize}x$matrixSize",
        "det(A) = ${Matrix.formatNum(det)}"
      )
      error = null
      HistoryRepository.addEntry("det(A)", Matrix.formatNum(det), "Matrices")
    } catch (e: Exception) {
      error = e.message
    }
  }

  fun computeInverse() {
    try {
      val a = getMatrixA()
      val (inv, steps) = a.inverse()
      calculationTitle = "Matriz Inversa A⁻¹"
      matrixResult = inv
      scalarResult = null
      stepsResult = steps
      error = null
    } catch (e: Exception) {
      error = e.message
    }
  }

  fun computeTranspose() {
    try {
      val a = getMatrixA()
      val trans = a.transpose()
      calculationTitle = "Transpuesta Aᵀ"
      matrixResult = trans
      scalarResult = null
      stepsResult = listOf("Intercambio de filas por columnas.")
      error = null
    } catch (e: Exception) {
      error = e.message
    }
  }

  fun computeTrace() {
    try {
      val a = getMatrixA()
      val tr = a.trace()
      calculationTitle = "Traza Tr(A)"
      scalarResult = tr
      matrixResult = null
      stepsResult = listOf("Suma de los elementos de la diagonal principal: Tr(A) = ${Matrix.formatNum(tr)}")
      error = null
    } catch (e: Exception) {
      error = e.message
    }
  }

  fun computeAdd() {
    try {
      val a = getMatrixA()
      val b = getMatrixB()
      val res = a + b
      calculationTitle = "Suma A + B"
      matrixResult = res
      scalarResult = null
      stepsResult = listOf("Suma componente a componente.")
      error = null
    } catch (e: Exception) {
      error = e.message
    }
  }

  fun computeMultiply() {
    try {
      val a = getMatrixA()
      val b = getMatrixB()
      val res = a * b
      calculationTitle = "Producto de Matrices A × B"
      matrixResult = res
      scalarResult = null
      stepsResult = listOf("Multiplicación fila por columna.")
      error = null
    } catch (e: Exception) {
      error = e.message
    }
  }

  fun computeScalarMult() {
    try {
      val a = getMatrixA()
      val k = scalarText.toDoubleOrNull() ?: 1.0
      val res = a * k
      calculationTitle = "Multiplicación por Escalar ($k · A)"
      matrixResult = res
      scalarResult = null
      stepsResult = listOf("Multiplicación de cada elemento de A por $k.")
      error = null
    } catch (e: Exception) {
      error = e.message
    }
  }

  LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    // Dimension Selector
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Dimensión de Matrices:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        SingleChoiceSegmentedButtonRow {
          SegmentedButton(
            selected = matrixSize == 2,
            onClick = { matrixSize = 2 },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
          ) {
            Text("2 × 2")
          }
          SegmentedButton(
            selected = matrixSize == 3,
            onClick = { matrixSize = 3 },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
          ) {
            Text("3 × 3")
          }
        }
      }
    }

    // Matrix A Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Matriz A (${matrixSize}x${matrixSize})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          MatrixInputGrid(
            size = matrixSize,
            values = if (matrixSize == 2) matA2 else matA3,
            onValueChange = { idx, v ->
              if (matrixSize == 2) matA2[idx] = v else matA3[idx] = v
            }
          )
        }
      }
    }

    // Operations on Matrix A
    item {
      Text("Operaciones sobre Matriz A:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(onClick = { computeDet() }, modifier = Modifier.weight(1f)) { Text("det(A)") }
        Button(onClick = { computeInverse() }, modifier = Modifier.weight(1f)) { Text("A⁻¹") }
        Button(onClick = { computeTranspose() }, modifier = Modifier.weight(1f)) { Text("Aᵀ") }
        Button(onClick = { computeTrace() }, modifier = Modifier.weight(1f)) { Text("Tr(A)") }
      }
    }

    // Matrix B & Joint Operations
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Matriz B (${matrixSize}x${matrixSize})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          MatrixInputGrid(
            size = matrixSize,
            values = if (matrixSize == 2) matB2 else matB3,
            onValueChange = { idx, v ->
              if (matrixSize == 2) matB2[idx] = v else matB3[idx] = v
            }
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilledTonalButton(onClick = { computeAdd() }, modifier = Modifier.weight(1f)) { Text("A + B") }
            FilledTonalButton(onClick = { computeMultiply() }, modifier = Modifier.weight(1f)) { Text("A × B") }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = scalarText,
              onValueChange = { scalarText = it },
              label = { Text("Escalar k") },
              modifier = Modifier.weight(1f)
            )
            FilledTonalButton(onClick = { computeScalarMult() }, modifier = Modifier.weight(1f)) {
              Text("k · A")
            }
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    // Result Display
    if (calculationTitle != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(calculationTitle!!, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (scalarResult != null) {
              Text(
                text = "= ${Matrix.formatNum(scalarResult!!)}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
            }

            if (matrixResult != null) {
              MatrixDisplayBox(matrixResult!!)
            }
          }
        }
      }

      items(stepsResult) { step ->
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
private fun MatrixInputGrid(
  size: Int,
  values: List<String>,
  onValueChange: (Int, String) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    for (r in 0 until size) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        for (c in 0 until size) {
          val idx = r * size + c
          OutlinedTextField(
            value = values[idx],
            onValueChange = { onValueChange(idx, it) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontFamily = FontFamily.Monospace)
          )
        }
      }
    }
  }
}

@Composable
private fun MatrixDisplayBox(matrix: Matrix) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surface,
    modifier = Modifier.padding(vertical = 4.dp).border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      for (r in 0 until matrix.rows) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          for (c in 0 until matrix.cols) {
            Text(
              text = Matrix.formatNum(matrix[r, c]),
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.weight(1f),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }
  }
}

@Composable
private fun VectorsTab() {
  var ux by remember { mutableStateOf("1") }
  var uy by remember { mutableStateOf("2") }
  var uz by remember { mutableStateOf("3") }

  var vx by remember { mutableStateOf("4") }
  var vy by remember { mutableStateOf("5") }
  var vz by remember { mutableStateOf("6") }

  var resultDot by remember { mutableStateOf<Double?>(null) }
  var resultCross by remember { mutableStateOf<Vector3D?>(null) }
  var magU by remember { mutableStateOf<Double?>(null) }
  var magV by remember { mutableStateOf<Double?>(null) }
  var angleRadDeg by remember { mutableStateOf<Pair<Double, Double>?>(null) }
  var unitU by remember { mutableStateOf<Vector3D?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  fun calculate() {
    try {
      val u = Vector3D(ux.toDouble(), uy.toDouble(), uz.toDouble())
      val v = Vector3D(vx.toDouble(), vy.toDouble(), vz.toDouble())

      magU = u.magnitude()
      magV = v.magnitude()
      resultDot = u.dot(v)
      resultCross = u.cross(v)
      angleRadDeg = u.angleWith(v)
      unitU = u.unitVector()
      error = null

      HistoryRepository.addEntry("u·v y u×v", "u·v=$resultDot, u×v=$resultCross", "Vectores")
    } catch (e: Exception) {
      error = e.message
    }
  }

  LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Vectores en el Espacio Tridimensional (ℝ³)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))

          Text("Vector u = (ux, uy, uz)", style = MaterialTheme.typography.bodySmall)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = ux, onValueChange = { ux = it }, label = { Text("ux") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = uy, onValueChange = { uy = it }, label = { Text("uy") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = uz, onValueChange = { uz = it }, label = { Text("uz") }, modifier = Modifier.weight(1f))
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text("Vector v = (vx, vy, vz)", style = MaterialTheme.typography.bodySmall)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = vx, onValueChange = { vx = it }, label = { Text("vx") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = vy, onValueChange = { vy = it }, label = { Text("vy") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = vz, onValueChange = { vz = it }, label = { Text("vz") }, modifier = Modifier.weight(1f))
          }

          Spacer(modifier = Modifier.height(12.dp))
          Button(onClick = { calculate() }, modifier = Modifier.fillMaxWidth()) {
            Text("Calcular Propiedades y Operaciones")
          }
        }
      }
    }

    if (error != null) {
      item { Text(error!!, color = MaterialTheme.colorScheme.error) }
    }

    if (resultDot != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resultados Vectoriales:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Text("Producto Punto (Escalar) u · v:", style = MaterialTheme.typography.labelMedium)
            Text("${Matrix.formatNum(resultDot!!)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Divider()

            Text("Producto Cruz (Vectorial) u × v:", style = MaterialTheme.typography.labelMedium)
            Text("${resultCross}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Divider()

            Text("Magnitudes (Normas):", style = MaterialTheme.typography.labelMedium)
            Text("||u|| = ${Matrix.formatNum(magU!!)},  ||v|| = ${Matrix.formatNum(magV!!)}", style = MaterialTheme.typography.bodyLarge)

            Divider()

            Text("Ángulo entre vectores (θ):", style = MaterialTheme.typography.labelMedium)
            Text("${Matrix.formatNum(angleRadDeg!!.second)}° (${Matrix.formatNum(angleRadDeg!!.first)} rad)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

            if (unitU != null) {
              Divider()
              Text("Vector Unitario de u (û):", style = MaterialTheme.typography.labelMedium)
              Text("$unitU", style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      }
    }
  }
}
