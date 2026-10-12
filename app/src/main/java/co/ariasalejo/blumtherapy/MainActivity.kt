package co.ariasalejo.blumtherapy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BlumTherapyApp() } }
    }
}

private data class ToolCard(val title: String, val description: String)

@Composable
private fun BlumTherapyApp() {
    val tools = listOf(
        ToolCard("Device overview", "Read-only Android version and device capability summary — planned."),
        ToolCard("Troubleshooting guides", "Guided checks with clear limitations and safe next steps — planned."),
        ToolCard("ADB learning", "Documentation for devices you own or are authorized to manage — planned."),
        ToolCard("Privacy checklist", "Practical guidance for permissions and safe diagnostic sharing — planned.")
    )
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("BlumTherapy", style = MaterialTheme.typography.headlineLarge)
                    Text("Open-source Android toolkit", style = MaterialTheme.typography.titleMedium)
                    Text("Privacy-first foundation. Planned tools are labeled clearly; no diagnostic or repair feature is claimed to be implemented yet.")
                }
            }
            items(tools) { tool ->
                Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(tool.title, style = MaterialTheme.typography.titleMedium)
                        Text(tool.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
