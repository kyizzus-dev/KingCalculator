package com.example.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryItem(
  val id: String = System.currentTimeMillis().toString() + (100..999).random(),
  val expression: String,
  val result: String,
  val timestamp: String,
  val category: String = "Álgebra"
)

object HistoryRepository {

  private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
  val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

  private val _memoryValue = MutableStateFlow(0.0)
  val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

  private val _lastAnswer = MutableStateFlow("0")
  val lastAnswer: StateFlow<String> = _lastAnswer.asStateFlow()

  // User-defined variables map (e.g., x = 5, y = 2.5, a = 10, b = -3)
  private val _variables = MutableStateFlow<Map<String, Double>>(
    linkedMapOf("x" to 2.0, "y" to 3.0, "z" to 1.0, "a" to 5.0, "b" to -2.0)
  )
  val variables: StateFlow<Map<String, Double>> = _variables.asStateFlow()

  // Event stream to inject an expression or result into the active calculator input
  private val _insertRequest = MutableSharedFlow<String>(extraBufferCapacity = 1)
  val insertRequest: SharedFlow<String> = _insertRequest.asSharedFlow()

  fun addEntry(expression: String, result: String, category: String = "Álgebra") {
    val formatter = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())
    val item = HistoryItem(
      expression = expression,
      result = result,
      timestamp = formatter.format(Date()),
      category = category
    )
    _history.value = listOf(item) + _history.value.take(99)
    _lastAnswer.value = result
  }

  fun clearHistory() {
    _history.value = emptyList()
  }

  fun removeEntry(id: String) {
    _history.value = _history.value.filter { it.id != id }
  }

  fun requestInsert(text: String) {
    _insertRequest.tryEmit(text)
  }

  fun memoryAdd(value: Double) {
    _memoryValue.value += value
  }

  fun memorySubtract(value: Double) {
    _memoryValue.value -= value
  }

  fun memoryClear() {
    _memoryValue.value = 0.0
  }

  fun setVariable(name: String, value: Double) {
    val current = _variables.value.toMutableMap()
    current[name.lowercase().trim()] = value
    _variables.value = current
  }

  fun removeVariable(name: String) {
    val current = _variables.value.toMutableMap()
    current.remove(name.lowercase().trim())
    _variables.value = current
  }
}
