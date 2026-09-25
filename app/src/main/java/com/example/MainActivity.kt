package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.MascaraTvDatabase
import com.example.data.repository.MascaraRepository
import com.example.ui.MascaraTvApp
import com.example.ui.theme.MascaraDarkBg
import com.example.ui.theme.MascaraTvTheme
import com.example.ui.viewmodel.MascaraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MascaraTvDatabase.getDatabase(applicationContext)
        val repository = MascaraRepository(
            videoDao = database.videoDao(),
            channelDao = database.channelDao(),
            commentDao = database.commentDao(),
            historyAndSavedDao = database.historyAndSavedDao()
        )
        val viewModelFactory = MascaraViewModel.provideFactory(repository)

        setContent {
            MascaraTvTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MascaraDarkBg
                ) {
                    val viewModel: MascaraViewModel = viewModel(factory = viewModelFactory)
                    MascaraTvApp(viewModel = viewModel)
                }
            }
        }
    }
}
