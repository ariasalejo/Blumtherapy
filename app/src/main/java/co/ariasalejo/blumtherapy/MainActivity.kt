package co.ariasalejo.blumtherapy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BlumTherapyApp() }
    }
}

private data class Resource(val title: String, val group: String, val subtitle: String, val detail: String, val mark: String, val country: String = "General", val officialUrl: String? = null, val status: String = "Unreviewed")

private val resources = listOf(
    Resource("Find professional support", "Support", "Treatment pathways and peer support", "Start with a qualified health professional or an official local treatment directory. Confirm eligibility, cost, opening hours and availability directly with the provider.", "♡"),
    Resource("A steadier moment", "Wellbeing", "Small grounding and reflection exercises", "Pause, place both feet on the floor, breathe comfortably and name five things you can see. This self-care exercise is not a treatment or crisis service.", "✦"),
    Resource("Learn a new skill", "Learning", "Training, education and digital skills", "Explore public education services in your country or region. Dates, entry requirements and fees vary; confirm current details with the official provider.", "↗"),
    Resource("Work with purpose", "Work", "Job readiness and meaningful activity", "Build a simple CV, identify one skill to practice and explore public employment services. This app does not promise jobs, income or acceptance.", "⌁"),
    Resource("My next small step", "Progress", "Choose a realistic goal for today", "Choose one small action you control, such as contacting a trusted person, taking a walk or reviewing a learning resource. A setback never removes your dignity.", "✓"),
    Resource("Support for family", "Community", "Listen without blame or pressure", "Ask what kind of support the person wants, listen without judgment and offer practical help. Respect boundaries and seek professional guidance when needed.", "♡"),
    Resource("Evidence and recovery", "Learning", "Understand evidence and its limits", "Health information should come from reputable, traceable sources and explain uncertainty. This app does not diagnose, prescribe or replace professional care.", "⌘", "Global", "https://www.who.int/publications/i/item/international-standards-for-the-treatment-of-drug-use-disorders", "Official guidance · not a local service"),
    Resource("FindTreatment.gov · United States", "Support", "Official treatment locator", "A government locator for mental-health and substance-use services. Confirm facility services, eligibility, cost, insurance and availability directly.", "↗", "United States", "https://www.findtreatment.gov/", "Official directory · verify each listing"),
    Resource("NHS drug support · United Kingdom", "Support", "Official treatment information", "Start with NHS guidance and confirm the relevant UK nation and local service. Availability and pathways differ by location.", "↗", "United Kingdom", "https://www.nhs.uk/live-well/addiction-support/drug-addiction-getting-help/", "Official information · local details vary"),
    Resource("Health Canada · Canada", "Support", "Treatment and support options", "Explore official information about treatment and support. Provincial and territorial access, cost and availability differ.", "↗", "Canada", "https://www.canada.ca/en/health-canada/services/substance-use/treatment.html", "Official information · local details vary"),
    Resource("Drug Help · Australia", "Support", "Australian Government resource finder", "Use official government information and confirm state or territory, current contact details and local availability before relying on a service.", "↗", "Australia", "https://www.health.gov.au/our-work/drug-help", "Official information · recheck contacts")
)

private val ink = Color(0xFF0B1020)
private val panel = Color(0xFF141D32)
private val mint = Color(0xFF67F5CE)
private val violet = Color(0xFF9B8CFF)
private val muted = Color(0xFFABB7CB)

@Composable
private fun BlumTherapyApp() {
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf<String?>(null) }
    var light by remember { mutableStateOf(false) }
    var country by remember { mutableStateOf("All") }
    val context = LocalContext.current
    val groups = listOf("All", "Support", "Wellbeing", "Learning", "Work", "Progress", "Community")
    val countries = listOf("All", "General", "United States", "United Kingdom", "Canada", "Australia", "Global")
    val shown = resources.filter { (group == "All" || it.group == group) && (country == "All" || it.country == country) && (query.isBlank() || it.title.contains(query, true) || it.subtitle.contains(query, true) || it.detail.contains(query, true)) }
    val bg = if (light) Color(0xFFF4F7FB) else ink
    val fg = if (light) Color(0xFF152039) else Color(0xFFF4F7FF)
    val card = if (light) Color.White else panel
    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = bg) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(46.dp).background(Brush.linearGradient(listOf(mint, violet)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Text("B", color = ink, fontSize = 27.sp, fontWeight = FontWeight.Black)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("BlumTherapy", color = fg, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("RECOVERY • LEARNING • CONNECTION", color = muted, fontSize = 9.sp, letterSpacing = 1.sp)
                        }
                        Surface(color = card, shape = CircleShape, modifier = Modifier.clickable { light = !light }) {
                            Text(if (light) "☾" else "☼", color = fg, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
                item {
                    Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF1A3340), Color(0xFF242344))), RoundedCornerShape(25.dp)).border(1.dp, Color(0xFF3B5366), RoundedCornerShape(25.dp)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("YOUR NEXT CHAPTER", color = mint, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                        Text("A second chance,\ngrounded in hope.", color = Color.White, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold)
                        Text("Small steps. Real people. Evidence-informed support.", color = Color(0xFFD2D9E8), fontSize = 14.sp, lineHeight = 21.sp)
                        Surface(color = mint, shape = RoundedCornerShape(14.dp), modifier = Modifier.clickable { group = "Support"; expanded = null }) {
                            Text("Explore support  →", color = ink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                        }
                    }
                }
                item {
                    Text("Find your next step", color = fg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Surface(color = card, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                            Text("⌕", color = mint, fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            BasicTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, textStyle = TextStyle(color = fg, fontSize = 14.sp), decorationBox = { inner ->
                                Box { if (query.isEmpty()) Text("Search support, learning, work…", color = muted, fontSize = 14.sp); inner() }
                            })
                        }
                    }
                }
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        groups.forEach { g ->
                            val active = group == g
                            Surface(color = if (active) mint else card, shape = RoundedCornerShape(50), modifier = Modifier.clickable { group = g; expanded = null }) {
                                Text(g, color = if (active) ink else fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                            }
                        }
                    }
                }
                item {
                    Text("Choose a country or scope", color = fg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(7.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        countries.forEach { c ->
                            val active = country == c
                            Surface(color = if (active) violet else card, shape = RoundedCornerShape(50), modifier = Modifier.clickable { country = c; expanded = null }) {
                                Text(c, color = if (active) ink else fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
                            }
                        }
                    }
                }
                item { Text("Explore resources", color = fg, fontSize = 19.sp, fontWeight = FontWeight.Bold) }
                items(shown) { r ->
                    Card(Modifier.fillMaxWidth().clickable { expanded = if (expanded == r.title) null else r.title }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = card)) {
                        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(44.dp).background(if (r.group == "Support") Color(0xFF203D3C) else Color(0xFF292746), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                    Text(r.mark, color = mint, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(r.title, color = fg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(r.subtitle, color = muted, fontSize = 12.sp, lineHeight = 17.sp)
                                }
                                Text(if (expanded == r.title) "−" else "+", color = mint, fontSize = 22.sp)
                            }
                            Text(r.group.uppercase(), color = violet, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            if (expanded == r.title) {
                                Text(r.detail, color = fg, fontSize = 13.sp, lineHeight = 20.sp)
                                Text("${r.country} • ${r.status}", color = violet, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("General information only • Verify local availability, eligibility and cost", color = muted, fontSize = 11.sp, lineHeight = 16.sp)
                                if (r.officialUrl != null) {
                                    Surface(color = mint, shape = RoundedCornerShape(12.dp), modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.officialUrl))) }) {
                                        Text("Open official source ↗", color = ink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Column(Modifier.fillMaxWidth().background(card, RoundedCornerShape(18.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("A note on safety", color = fg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("BlumTherapy is an early informational prototype, not a treatment provider or emergency service. Resources, eligibility and availability must be verified in your country. If you may be in immediate danger, contact local emergency services.", color = muted, fontSize = 12.sp, lineHeight = 18.sp)
                        Text("Privacy-first • No account required • Content stored locally", color = mint, fontSize = 10.sp)
                    }
                }
                item { Text("You deserve support without judgment. One step at a time.", color = muted, fontSize = 11.sp, lineHeight = 17.sp) }
            }
        }
    }
}
