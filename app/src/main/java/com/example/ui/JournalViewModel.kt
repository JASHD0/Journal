package com.example.ui

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.JournalEntry
import com.example.data.JournalRepository
import com.example.data.ProtocolDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(private val repository: JournalRepository) : ViewModel() {

    // Live stream of all database entries
    private val _dbEntries = repository.allEntries
    
    // Search filter state
    val searchQuery = MutableStateFlow("")

    // Filtered list of entries based on keyword search
    val entriesList: StateFlow<List<JournalEntry>> = combine(_dbEntries, searchQuery) { entries, query ->
        if (query.isBlank()) {
            entries
        } else {
            entries.filter { entry ->
                entry.protocolTitle.contains(query, ignoreCase = true) ||
                entry.formattedText.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Form/Draft State Management
    val isWriting = MutableStateFlow(false)
    val activeProtocol = MutableStateFlow<ProtocolDefinition>(ProtocolDefinition.GRATITUDE)
    val draftResponses = mutableStateMapOf<String, String>()
    
    // If editing an existing entry, this holds the ID
    val editingEntryId = MutableStateFlow<Int?>(null)

    fun startNewEntry(protocol: ProtocolDefinition) {
        editingEntryId.value = null
        activeProtocol.value = protocol
        draftResponses.clear()
        // Pre-fill keys in map
        for (field in protocol.fields) {
            draftResponses[field.key] = ""
        }
        isWriting.value = true
    }

    fun startEditing(entry: JournalEntry) {
        editingEntryId.value = entry.id
        val protocol = ProtocolDefinition.getByKey(entry.protocolKey)
        activeProtocol.value = protocol
        
        draftResponses.clear()
        val parsed = ProtocolDefinition.parseResponses(entry.promptResponseMapJson)
        for (field in protocol.fields) {
            draftResponses[field.key] = parsed[field.key] ?: ""
        }
        isWriting.value = true
    }

    fun updateDraftResponse(fieldKey: String, value: String) {
        draftResponses[fieldKey] = value
    }

    fun saveDraft() {
        val protocol = activeProtocol.value
        val responses = draftResponses.toMap()
        val timestamp = System.currentTimeMillis()
        
        // Compile Markdown formatting
        val markdown = protocol.compileMarkdown(responses, timestamp)
        val jsonStr = ProtocolDefinition.serializeResponses(responses)
        
        val entryId = editingEntryId.value

        viewModelScope.launch {
            if (entryId != null) {
                // Update existing
                // Fetch existing timestamps so we keep the original entry's creation date!
                val existing = repository.getEntryById(entryId)
                val originalTime = existing?.timestamp ?: timestamp
                
                val updatedEntry = JournalEntry(
                    id = entryId,
                    timestamp = originalTime,
                    protocolKey = protocol.key,
                    protocolTitle = protocol.title,
                    promptResponseMapJson = jsonStr,
                    formattedText = protocol.compileMarkdown(responses, originalTime)
                )
                repository.update(updatedEntry)
            } else {
                // Insert new
                val newEntry = JournalEntry(
                    protocolKey = protocol.key,
                    protocolTitle = protocol.title,
                    promptResponseMapJson = jsonStr,
                    formattedText = markdown,
                    timestamp = timestamp
                )
                repository.insert(newEntry)
            }
            
            // Clean up states
            cancelDraft()
        }
    }

    fun cancelDraft() {
        isWriting.value = false
        editingEntryId.value = null
        draftResponses.clear()
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    /**
     * Compiles ALL entries into a clean Markdown journal export block.
     */
    fun getFullBackupMarkdown(entries: List<JournalEntry>): String {
        if (entries.isEmpty()) return "Protocol Journal: Empty"
        val sb = StringBuilder()
        sb.append("# Protocol Journal Export\n")
        sb.append("This file contains all compiled journal protocol reflective entries.\n\n")
        sb.append("=========================================\n\n")
        
        for (entry in entries) {
            sb.append(entry.formattedText)
            sb.append("\n\n")
            sb.append("=========================================\n\n")
        }
        return sb.toString()
    }
}

class JournalViewModelFactory(private val repository: JournalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
