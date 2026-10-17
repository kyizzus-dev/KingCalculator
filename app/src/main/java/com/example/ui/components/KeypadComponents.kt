package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class KeypadButtonType {
  NUMBER,
  OPERATOR,
  FUNCTION,
  EQUAL,
  CLEAR,
  MEMORY
}

@Composable
fun CalcKeyButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  type: KeypadButtonType = KeypadButtonType.NUMBER,
  subLabel: String? = null
) {
  val isDark = MaterialTheme.colorScheme.background == CalcBackgroundDark

  val containerColor = when (type) {
    KeypadButtonType.NUMBER -> if (isDark) CalcKeyNumberDark else CalcKeyNumberLight
    KeypadButtonType.OPERATOR -> if (isDark) CalcKeyOperatorDark else CalcKeyOperatorLight
    KeypadButtonType.FUNCTION -> if (isDark) CalcKeyFunctionDark else CalcKeyFunctionLight
    KeypadButtonType.EQUAL -> CalcKeyEquals
    KeypadButtonType.CLEAR -> CalcKeyClear
    KeypadButtonType.MEMORY -> MaterialTheme.colorScheme.surfaceVariant
  }

  val contentColor = when (type) {
    KeypadButtonType.EQUAL -> Color.White
    KeypadButtonType.CLEAR -> Color.White
    KeypadButtonType.OPERATOR -> MaterialTheme.colorScheme.primary
    KeypadButtonType.FUNCTION -> MaterialTheme.colorScheme.primary
    KeypadButtonType.MEMORY -> MaterialTheme.colorScheme.onSurfaceVariant
    KeypadButtonType.NUMBER -> MaterialTheme.colorScheme.onSurface
  }

  val tag = "key_" + label.lowercase()
    .replace(" ", "_")
    .replace("×", "mul")
    .replace("÷", "div")
    .replace("+", "add")
    .replace("-", "sub")
    .replace("=", "eq")
    .replace("(", "lparen")
    .replace(")", "rparen")

  Surface(
    modifier = modifier
      .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag(tag),
    color = containerColor,
    contentColor = contentColor,
    shape = RoundedCornerShape(14.dp),
    tonalElevation = if (type == KeypadButtonType.NUMBER) 1.dp else 2.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(4.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        if (subLabel != null) {
          Text(
            text = subLabel,
            fontSize = 9.sp,
            color = contentColor.copy(alpha = 0.65f),
            fontWeight = FontWeight.Normal
          )
        }
        Text(
          text = label,
          fontSize = when {
            label.length > 4 -> 13.sp
            label.length > 2 -> 16.sp
            type == KeypadButtonType.NUMBER -> 22.sp
            type == KeypadButtonType.OPERATOR -> 22.sp
            else -> 18.sp
          },
          fontWeight = if (type == KeypadButtonType.NUMBER || type == KeypadButtonType.EQUAL) FontWeight.Bold else FontWeight.Medium,
          fontFamily = if (type == KeypadButtonType.NUMBER) FontFamily.SansSerif else FontFamily.Default
        )
      }
    }
  }
}
