package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.JournalDatabase
import com.example.data.JournalRepository
import com.example.ui.JournalDashboard
import com.example.ui.JournalViewModel
import com.example.ui.JournalViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Retrieve database and repository singletons
        val database = JournalDatabase.getDatabase(this)
        val repository = JournalRepository(database.journalDao())
        
        setContent {
            MyApplicationTheme {
                // Instantiating local scoped ViewModel with Custom Factory inside Composable scope
                val viewModel: JournalViewModel = viewModel(
                    factory = JournalViewModelFactory(repository)
                )
                
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    JournalDashboard(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
