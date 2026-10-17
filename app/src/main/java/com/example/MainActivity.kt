package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryRepository
import com.example.ui.components.AboutDeveloperDialog
import com.example.ui.screens.*
import com.example.ui.theme.CalcAlgebraTheme

enum class AppNavScreen(val label: String, val icon: ImageVector) {
  ALGEBRA("Álgebra", Icons.Default.Calculate),
  TUTOR_AI("Tutor IA", Icons.Default.AutoAwesome),
  SYMBOLIC("Simbólico", Icons.Default.AutoFixHigh),
  EQUATIONS("Ecuaciones", Icons.Default.Functions),
  CALCULUS("Cálculo", Icons.Default.Timeline),
  MATRICES("Matrices", Icons.Default.GridOn),
  GRAPH("Gráfica", Icons.AutoMirrored.Filled.ShowChart),
  HISTORY("Historial", Icons.Default.History)
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CalcAlgebraTheme {
        KingCalculatorApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KingCalculatorApp() {
  var currentScreen by remember { mutableStateOf(AppNavScreen.ALGEBRA) }
  var showAboutDialog by remember { mutableStateOf(false) }
  var showMenuDropdown by remember { mutableStateOf(false) }

  // Handle back press to always return to primary Algebra screen
  BackHandler(enabled = currentScreen != AppNavScreen.ALGEBRA) {
    currentScreen = AppNavScreen.ALGEBRA
  }

  if (showAboutDialog) {
    AboutDeveloperDialog(onDismiss = { showAboutDialog = false })
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "King Calculator",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              )
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
              Text(
                text = "v1.2",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        },
        actions = {
          // Camera Scanner / Tutor AI shortcut
          IconButton(
            onClick = {
              currentScreen = if (currentScreen == AppNavScreen.TUTOR_AI) AppNavScreen.ALGEBRA else AppNavScreen.TUTOR_AI
            },
            modifier = Modifier.testTag("action_tutor_toggle")
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "Tutor Gemini IA",
              tint = if (currentScreen == AppNavScreen.TUTOR_AI) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // History shortcut
          IconButton(
            onClick = {
              currentScreen = if (currentScreen == AppNavScreen.HISTORY) AppNavScreen.ALGEBRA else AppNavScreen.HISTORY
            },
            modifier = Modifier.testTag("action_history_toggle")
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = "Historial",
              tint = if (currentScreen == AppNavScreen.HISTORY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Discrete About & Credits icon
          IconButton(
            onClick = { showAboutDialog = true },
            modifier = Modifier.testTag("action_about_info")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Acerca de y Créditos",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Overflow menu for discrete navigation & options
          Box {
            IconButton(onClick = { showMenuDropdown = true }) {
              Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
            }
            DropdownMenu(
              expanded = showMenuDropdown,
              onDismissRequest = { showMenuDropdown = false }
            ) {
              DropdownMenuItem(
                text = { Text("Tutor IA (Cámara & Scanner)") },
                onClick = {
                  currentScreen = AppNavScreen.TUTOR_AI
                  showMenuDropdown = false
                },
                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
              )
              DropdownMenuItem(
                text = { Text("Matrices y Vectores") },
                onClick = {
                  currentScreen = AppNavScreen.MATRICES
                  showMenuDropdown = false
                },
                leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = null) }
              )
              DropdownMenuItem(
                text = { Text("Graficador 2D") },
                onClick = {
                  currentScreen = AppNavScreen.GRAPH
                  showMenuDropdown = false
                },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null) }
              )
              DropdownMenuItem(
                text = { Text("Historial de Cálculos") },
                onClick = {
                  currentScreen = AppNavScreen.HISTORY
                  showMenuDropdown = false
                },
                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
              )
              HorizontalDivider()
              DropdownMenuItem(
                text = { Text("Acerca de y Créditos (v1.2)") },
                onClick = {
                  showAboutDialog = true
                  showMenuDropdown = false
                },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.testTag("main_navigation_bar")
      ) {
        val navItems = listOf(
          AppNavScreen.ALGEBRA,
          AppNavScreen.TUTOR_AI,
          AppNavScreen.SYMBOLIC,
          AppNavScreen.EQUATIONS,
          AppNavScreen.CALCULUS
        )

        navItems.forEach { screen ->
          NavigationBarItem(
            selected = currentScreen == screen,
            onClick = { currentScreen = screen },
            icon = {
              Icon(
                imageVector = screen.icon,
                contentDescription = screen.label
              )
            },
            label = {
              Text(
                text = screen.label,
                fontSize = 10.5.sp,
                fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal
              )
            },
            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
          AppNavScreen.ALGEBRA -> AlgebraicCalcScreen(
            onOpenCredits = { showAboutDialog = true }
          )
          AppNavScreen.TUTOR_AI -> GeminiTutorScreen(
            onSendToCalculator = { extractedText ->
              HistoryRepository.requestInsert(extractedText)
              currentScreen = AppNavScreen.ALGEBRA
            }
          )
          AppNavScreen.SYMBOLIC -> SymbolicAlgebraScreen(
            onSendToCalculator = { expr ->
              HistoryRepository.requestInsert(expr)
              currentScreen = AppNavScreen.ALGEBRA
            }
          )
          AppNavScreen.EQUATIONS -> EquationSolverScreen()
          AppNavScreen.CALCULUS -> CalculusScreen()
          AppNavScreen.MATRICES -> MatrixVectorScreen()
          AppNavScreen.GRAPH -> GraphScreen()
          AppNavScreen.HISTORY -> HistoryScreen(
            onSelectExpression = { expr ->
              HistoryRepository.requestInsert(expr)
              currentScreen = AppNavScreen.ALGEBRA
            },
            onInsertResult = { _ ->
              currentScreen = AppNavScreen.ALGEBRA
            }
          )
        }
      }
    }
  }
}
