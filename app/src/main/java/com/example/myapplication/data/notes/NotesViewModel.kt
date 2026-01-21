package com.example.myapplication.data.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.util.YandexManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Note(val title: String, val body: String)

class NotesViewModel : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    fun add(title: String, body: String) {
        _notes.value = _notes.value + Note(title, body)
    }

    // Функция синхронизации заметок с Яндекс.Диском
    fun uploadNotesToYandex() {
        viewModelScope.launch {
            try {
                // Превращаем список заметок в JSON строку
                val jsonNotes = Gson().toJson(_notes.value)
                val bytes = jsonNotes.toByteArray(Charsets.UTF_8)
                
                // Загружаем файл notes.json в облако через наш новый менеджер
                YandexManager.uploadPdf("notes.json", bytes) 
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}