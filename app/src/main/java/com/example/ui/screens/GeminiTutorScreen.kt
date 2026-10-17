package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.ChatMessage
import com.example.ai.GeminiService
import com.example.ai.MathTextCleaner
import com.example.data.HistoryRepository
import kotlinx.coroutines.launch

@Composable
fun GeminiTutorScreen(
  onSendToCalculator: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val listState = rememberLazyListState()

  val messages = remember {
    mutableStateListOf(
      ChatMessage(
        role = "model",
        text = "¡Hola! Soy tu Tutor Matemático en King Calculator (v1.2). " +
               "Toma una foto a un problema o escríbelo aquí y te explicaré la solución paso a paso de forma limpia y detallada."
      )
    )
  }

  var inputText by remember { mutableStateOf("") }
  var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  var statusNotification by remember { mutableStateOf<String?>(null) }

  // Camera Capture Launcher (returns direct Bitmap)
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      attachedBitmap = bitmap
    }
  }

  // Camera Permission Launcher
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      cameraLauncher.launch(null)
    } else {
      statusNotification = "Permiso de cámara no concedido. Puedes usar el botón de galería para seleccionar fotos."
    }
  }

  // Photo Picker Launcher
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          val source = ImageDecoder.createSource(context.contentResolver, uri)
          ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
          }
        } else {
          @Suppress("DEPRECATION")
          MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        attachedBitmap = bitmap
      } catch (e: Exception) {
        statusNotification = "Error al abrir la imagen seleccionada."
      }
    }
  }

  fun checkAndLaunchCamera() {
    val hasPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    if (hasPermission) {
      cameraLauncher.launch(null)
    } else {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  fun sendMessage(promptText: String = inputText) {
    if (promptText.isBlank() && attachedBitmap == null) return

    val currentAttachedImage = attachedBitmap
    val userMsg = ChatMessage(
      role = "user",
      text = if (promptText.isNotBlank()) promptText else "Por favor resuelve este problema paso a paso con explicación clara.",
      imageBitmap = currentAttachedImage
    )
    messages.add(userMsg)

    inputText = ""
    attachedBitmap = null
    isLoading = true

    coroutineScope.launch {
      listState.animateScrollToItem(messages.size - 1)

      val result = GeminiService.sendChat(
        history = messages.dropLast(1),
        newPrompt = userMsg.text,
        imageBitmap = currentAttachedImage
      )

      isLoading = false
      result.onSuccess { responseText ->
        val cleaned = MathTextCleaner.clean(responseText)
        messages.add(
          ChatMessage(
            role = "model",
            text = cleaned
          )
        )
        listState.animateScrollToItem(messages.size - 1)
        HistoryRepository.addEntry("Escáner Gemini IA", cleaned.take(50) + "...", "Tutor IA")
      }.onFailure { error ->
        messages.add(
          ChatMessage(
            role = "model",
            text = "⚠️ No se pudo procesar la consulta: ${error.message}"
          )
        )
        listState.animateScrollToItem(messages.size - 1)
      }
    }
  }

  val promptPresets = listOf(
    "📸 Escanear problema",
    "Resolver paso a paso",
    "¿Cómo simplifico esta ecuación?",
    "Explicar método de integración",
    "Hallar determinante e inversa"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Column {
          Text(
            text = "Tutor Matemático Gemini",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Escanea problemas con cámara y resuelve paso a paso",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      if (messages.size > 1) {
        IconButton(
          onClick = {
            messages.clear()
            messages.add(
              ChatMessage(
                role = "model",
                text = "Conversación reiniciada. ¿Qué problema matemático deseas resolver?"
              )
            )
          },
          modifier = Modifier.size(32.dp).testTag("btn_clear_chat")
        ) {
          Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar chat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    // Scrollable Message Thread
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        CleanChatBubble(
          message = msg,
          onCopy = {
            clipboardManager.setText(AnnotatedString(msg.text))
            statusNotification = "Copiado al portapapeles"
          },
          onSendToCalculator = {
            val finalRes = MathTextCleaner.extractFinalResult(msg.text) ?: msg.text
            onSendToCalculator(finalRes)
          }
        )
      }

      if (isLoading) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Gemini está analizando y resolviendo el problema paso a paso...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    // Quick Prompts
    if (messages.size <= 2) {
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(promptPresets) { preset ->
          SuggestionChip(
            onClick = {
              if (preset.contains("📸")) {
                checkAndLaunchCamera()
              } else {
                sendMessage(preset)
              }
            },
            label = { Text(preset, fontSize = 11.sp) }
          )
        }
      }
    }

    // Attached Image Preview Card
    AnimatedVisibility(visible = attachedBitmap != null) {
      if (attachedBitmap != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Image(
                bitmap = attachedBitmap!!.asImageBitmap(),
                contentDescription = "Problema adjunto",
                modifier = Modifier
                  .size(54.dp)
                  .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
              )
              Column {
                Text(
                  text = "Problema listo para escanear",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Pulsa Enviar para resolver paso a paso",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            IconButton(
              onClick = { attachedBitmap = null },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }

    // Input Bar (Camera + Gallery + TextField + Send)
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Camera Button
        IconButton(
          onClick = { checkAndLaunchCamera() },
          modifier = Modifier.size(40.dp).testTag("btn_camera_capture")
        ) {
          Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Tomar foto con cámara",
            tint = MaterialTheme.colorScheme.primary
          )
        }

        // Gallery / Photo Picker Button
        IconButton(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          modifier = Modifier.size(40.dp).testTag("btn_photo_picker")
        ) {
          Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Seleccionar de galería",
            tint = MaterialTheme.colorScheme.primary
          )
        }

        // Text Input
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = { Text("Escribe o pide resolver la foto...", fontSize = 13.sp) },
          modifier = Modifier
            .weight(1f)
            .testTag("input_gemini_prompt"),
          maxLines = 3,
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
          )
        )

        // Send Button
        IconButton(
          onClick = { sendMessage() },
          enabled = !isLoading && (inputText.isNotBlank() || attachedBitmap != null),
          modifier = Modifier
            .size(42.dp)
            .background(
              color = if (!isLoading && (inputText.isNotBlank() || attachedBitmap != null))
                MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
              shape = CircleShape
            )
            .testTag("btn_gemini_send")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Enviar",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    if (statusNotification != null) {
      Snackbar(
        modifier = Modifier.padding(top = 4.dp),
        action = {
          TextButton(onClick = { statusNotification = null }) { Text("OK") }
        }
      ) {
        Text(statusNotification!!)
      }
    }
  }
}

@Composable
private fun CleanChatBubble(
  message: ChatMessage,
  onCopy: () -> Unit,
  onSendToCalculator: () -> Unit
) {
  val isUser = message.role == "user"

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .padding(end = 6.dp, top = 4.dp)
          .size(30.dp)
          .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
      ),
      color = if (isUser) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
      contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary
      else MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.98f)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        if (message.imageBitmap != null) {
          Image(
            bitmap = message.imageBitmap.asImageBitmap(),
            contentDescription = "Problema escaneado",
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 220.dp)
              .clip(RoundedCornerShape(10.dp))
              .padding(bottom = 8.dp),
            contentScale = ContentScale.Crop
          )
        }

        if (isUser) {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
          )
        } else {
          CleanFormattedSolution(message.text)

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            FilledTonalButton(
              onClick = onSendToCalculator,
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              modifier = Modifier.height(30.dp)
            ) {
              Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Pasar a Calculadora", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            IconButton(
              onClick = onCopy,
              modifier = Modifier.size(30.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copiar solución limpia",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CleanFormattedSolution(rawText: String) {
  val parsed = remember(rawText) { MathTextCleaner.parseSolution(rawText) }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    // Problem Detected Card (if available)
    if (!parsed.problemDetected.isNullOrBlank()) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
          )
          Column {
            Text(
              text = "Problema Detectado",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = parsed.problemDetected,
              style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
            )
          }
        }
      }
    }

    // Step-by-Step Cards
    if (parsed.steps.isNotEmpty()) {
      for (step in parsed.steps) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
          tonalElevation = 1.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Step Header (Number badge + Title) - completely free of '#' and '**'
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${step.number}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Text(
                text = step.title,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  fontSize = 13.5.sp
                )
              )
            }

            // Explanations
            for (line in step.explanationLines) {
              if (line.startsWith("•")) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                  Text(
                    text = line.removePrefix("•").trim(),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                  )
                }
              } else {
                Text(
                  text = line,
                  style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                )
              }
            }

            // Formula highlights
            for (formula in step.formulaLines) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = formula,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.3.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  ),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }
    } else {
      // General response paragraphs
      for (line in parsed.summaryParagraphs) {
        if (line.startsWith("•")) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(
              text = line.removePrefix("•").trim(),
              style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
            )
          }
        } else {
          Text(
            text = line,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
          )
        }
      }
    }

    // Prominent Final Result Card
    if (!parsed.finalResult.isNullOrBlank()) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Column {
            Text(
              text = "RESULTADO FINAL",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.primary
              )
            )
            Text(
              text = parsed.finalResult,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 22.sp
              )
            )
          }
        }
      }
    }
  }
}
