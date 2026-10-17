package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryItem
import com.example.data.HistoryRepository

@Composable
fun HistoryScreen(
  onSelectExpression: (String) -> Unit,
  onInsertResult: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val historyList by HistoryRepository.history.collectAsState()
  var sortDescending by remember { mutableStateOf(true) }
  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  var snackbarText by remember { mutableStateOf<String?>(null) }
  var selectedItemForAction by remember { mutableStateOf<HistoryItem?>(null) }

  val displayList = remember(historyList, sortDescending) {
    if (sortDescending) historyList else historyList.reversed()
  }

  // Action Dialog on item selection
  if (selectedItemForAction != null) {
    val item = selectedItemForAction!!
    AlertDialog(
      onDismissRequest = { selectedItemForAction = null },
      title = { Text("Reutilizar Cálculo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = item.expression,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "= ${item.result}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Registrado el: ${item.timestamp} • ${item.category}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSelectExpression(item.expression)
            selectedItemForAction = null
          }
        ) {
          Text("Cargar Expresión")
        }
      },
      dismissButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilledTonalButton(
            onClick = {
              HistoryRepository.requestInsert(item.result)
              onInsertResult(item.result)
              selectedItemForAction = null
            }
          ) {
            Text("Insertar Resultado")
          }
          OutlinedButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(item.result))
              snackbarText = "Resultado copiado"
              selectedItemForAction = null
            }
          ) {
            Text("Copiar")
          }
        }
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // Header Row with Sort and Clear
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("Historial Cronológico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      }

      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
          onClick = { sortDescending = !sortDescending },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = if (sortDescending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
            contentDescription = if (sortDescending) "Recientes primero" else "Antiguos primero",
            tint = MaterialTheme.colorScheme.primary
          )
        }

        if (historyList.isNotEmpty()) {
          TextButton(
            onClick = { HistoryRepository.clearHistory() },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.testTag("btn_clear_history")
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Borrar")
          }
        }
      }
    }

    Text(
      text = if (sortDescending) "Orden cronológico: Más recientes primero" else "Orden cronológico: Más antiguos primero",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (displayList.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
          )
          Text(
            text = "No hay cálculos registrados",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Tus operaciones se guardan aquí cronológicamente para reutilizarlas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(displayList, key = { it.id }) { item ->
          HistoryCardItem(
            item = item,
            onClick = { selectedItemForAction = item },
            onReuseExpr = { onSelectExpression(item.expression) },
            onReuseResult = {
              HistoryRepository.requestInsert(item.result)
              onInsertResult(item.result)
            },
            onDelete = { HistoryRepository.removeEntry(item.id) }
          )
        }
      }
    }

    if (snackbarText != null) {
      Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
          TextButton(onClick = { snackbarText = null }) {
            Text("OK")
          }
        }
      ) {
        Text(snackbarText!!)
      }
    }
  }
}

@Composable
private fun HistoryCardItem(
  item: HistoryItem,
  onClick: () -> Unit,
  onReuseExpr: () -> Unit,
  onReuseResult: () -> Unit,
  onDelete: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        SuggestionChip(
          onClick = {},
          label = { Text(item.category, fontSize = 10.sp) },
          modifier = Modifier.height(24.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = item.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Eliminar",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = item.expression,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "= ${item.result}",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace
          )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          FilledTonalButton(
            onClick = onReuseExpr,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text("Reusar", fontSize = 11.sp)
          }
          Button(
            onClick = onReuseResult,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text("Insertar", fontSize = 11.sp)
          }
        }
      }
    }
  }
}
