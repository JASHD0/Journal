package com.example.data

import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalDao: JournalDao) {
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntriesFlow()

    suspend fun getEntryById(id: Int): JournalEntry? {
        return journalDao.getEntryById(id)
    }

    suspend fun insert(entry: JournalEntry) {
        journalDao.insertEntry(entry)
    }

    suspend fun update(entry: JournalEntry) {
        journalDao.updateEntry(entry)
    }

    suspend fun delete(entry: JournalEntry) {
        journalDao.deleteEntry(entry)
    }

    suspend fun deleteById(id: Int) {
        journalDao.deleteEntryById(id)
    }
}
