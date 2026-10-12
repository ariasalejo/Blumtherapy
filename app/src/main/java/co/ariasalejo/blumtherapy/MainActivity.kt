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
        setContent { MaterialTheme { RecoveryHome() } }
    }
}

private data class SupportArea(val title: String, val description: String)

@Composable
private fun RecoveryHome() {
    val areas = listOf(
        SupportArea("Find support", "Explore professional treatment, peer support, and community options. Directory integration is planned."),
        SupportArea("Learn and grow", "Discover education, skills, and training resources. Availability depends on your location."),
        SupportArea("Work and purpose", "Explore job-readiness, volunteering, and meaningful activities. No jobs are promised by this app."),
        SupportArea("Your next step", "Choose a small, self-directed goal. Progress is not a moral score, and a setback never removes your dignity."),
        SupportArea("Evidence and hope", "Health information must cite trustworthy sources and explain limitations. Faith-based support is always optional.")
    )
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("BlumTherapy", style = MaterialTheme.typography.headlineLarge)
                    Text("A second chance, grounded in evidence", style = MaterialTheme.typography.titleMedium)
                    Text("An early prototype for recovery support, learning, work, and connection. It is not a treatment provider or emergency service.")
                }
            }
            items(areas) { area ->
                Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(area.title, style = MaterialTheme.typography.titleMedium)
                        Text(area.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
