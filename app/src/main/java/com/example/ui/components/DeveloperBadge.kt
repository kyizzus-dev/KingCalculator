package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeveloperBadge(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .testTag("dev_badge_button"),
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    tonalElevation = 2.dp,
    shape = RoundedCornerShape(20.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Code,
        contentDescription = "Desarrollador",
        modifier = Modifier.size(15.dp),
        tint = MaterialTheme.colorScheme.primary
      )
      Text(
        text = "kyizzus-dev",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 0.2.sp
        )
      )
    }
  }
}

@Composable
fun AboutDeveloperDialog(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val githubUrl = "https://github.com/kyizzus-dev"

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = onDismiss,
        modifier = Modifier.testTag("about_dialog_close")
      ) {
        Text("Cerrar")
      }
    },
    icon = {
      Icon(
        imageVector = Icons.Default.Info,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp)
      )
    },
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "King Calculator",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Versión 1.2 • Calculadora Algebraica & Tutor IA",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
              )
              Column {
                Text(
                  text = "Desarrollador",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Text(
                  text = "kyizzus-dev",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                )
              }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))

            // Clickable GitHub Link
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                  context.startActivity(intent)
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "GitHub: $githubUrl",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary
                )
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                  contentDescription = "Abrir GitHub",
                  modifier = Modifier.size(16.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }

        Text(
          text = "Repositorio oficial donde se publican las versiones actuales y futuras actualizaciones del proyecto.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
          text = "Novedades en Versión 1.2:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
          FeatureItem("• ✨ Tutor IA limpio: paso a paso sin hashtags (#) ni símbolos extraños")
          FeatureItem("• 📱 Soluciones estructuradas por pasos y resultado final destacado")
          FeatureItem("• 🎨 Interfaz optimizada con créditos discretos y accesibles")
          FeatureItem("• 📸 Escáner de problemas matemáticos mediante cámara y galería")
          FeatureItem("• 🔢 Álgebra simbólica, matrices, cálculo y graficador 2D")
        }
      }
    }
  )
}

@Composable
private fun FeatureItem(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
  )
}
