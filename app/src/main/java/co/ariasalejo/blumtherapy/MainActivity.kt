package co.ariasalejo.blumtherapy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BlumTherapyApp() }
    }
}

private val Aubergine = Color(0xFF352B3C)
private val Plum = Color(0xFF694A65)
private val Petal = Color(0xFFEDC2B0)
private val Sage = Color(0xFFDBE4C5)
private val Canvas = Color(0xFFFAF5F0)
private val SoftText = Color(0xFF8B808D)

private data class Resource(val title: String, val group: String, val subtitle: String, val detail: String, val mark: String, val country: String = "Global", val officialUrl: String? = null, val status: String = "Official starting point")
private data class TherapyTab(val title: String, val icon: String)

private val therapyResources = listOf(
    Resource("FindTreatment.gov", "Support", "Official U.S. treatment locator", "Confirm facility services, eligibility, insurance, cost and availability directly.", "↗", "United States", "https://findtreatment.gov/", "Government directory · verify each listing"),
    Resource("NHS: getting help for drug addiction", "Support", "Official UK treatment information", "Local services and pathways differ across UK nations. Check the current local route.", "♡", "United Kingdom", "https://www.nhs.uk/live-well/addiction-support/drug-addiction-getting-help/", "Official information · local details vary"),
    Resource("Health Canada: treatment", "Support", "Canadian treatment and support options", "Provincial and territorial access and costs can differ. Confirm local conditions.", "↗", "Canada", "https://www.canada.ca/en/health-canada/services/substance-use/treatment.html", "Government information · local details vary"),
    Resource("Drug Help", "Support", "Australian Government starting point", "Confirm state or territory, contact details and current service availability.", "⌘", "Australia", "https://www.health.gov.au/our-work/drug-help", "Government information · recheck current details"),
    Resource("WHO: treatment standards", "Learning", "Evidence-informed treatment guidance", "International guidance for learning about quality care. This is not a local service.", "✧", "Global", "https://www.who.int/publications/i/item/international-standards-for-the-treatment-of-drug-use-disorders", "International guidance · not a local service"),
    Resource("A steadier moment", "Wellbeing", "A short grounding practice", "Notice where you are, let your shoulders settle if that feels comfortable, and name a few things you can see. This is a self-care exercise, not a treatment or crisis service.", "◌"),
    Resource("Learn a new skill", "Learning", "Training and curiosity, at your pace", "Explore official education and skills services in your country. Check current fees, requirements and dates directly.", "▤"),
    Resource("Support for family", "Community", "Listen without blame or pressure", "Ask what kind of help the person wants, respect boundaries and seek qualified guidance when needed.", "♡")
)

@Composable
private fun BlumTherapyApp() {
    var screen by remember { mutableStateOf("Home") }
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("All") }
    var country by remember { mutableStateOf("All") }
    var opened by remember { mutableStateOf<Resource?>(null) }
    var light by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val tabs = listOf(TherapyTab("Home", "⌂"), TherapyTab("My plan", "◷"), TherapyTab("Library", "✧"), TherapyTab("Support", "♡"), TherapyTab("My progress", "⌁"))
    val groups = listOf("All", "Support", "Wellbeing", "Learning", "Work", "Progress", "Community")
    val countries = listOf("All", "General", "United States", "United Kingdom", "Canada", "Australia", "Global")
    val bg = if (light) Canvas else Color(0xFF211B27)
    val fg = if (light) Aubergine else Color(0xFFFAF4F0)
    val card = if (light) Color.White else Color(0xFF403746)
    val muted = if (light) Color(0xFF625965) else Color(0xFFB6AEBB)
    val accent = if (light) Plum else Petal
    val shown = therapyResources.filter { r ->
        val inScreen = when (screen) {
            "Library" -> r.group in listOf("Learning", "Wellbeing")
            "Support" -> r.group in listOf("Support", "Community")
            else -> false
        }
        val countryMatch = country == "All" || r.country == "Global" && country == "Global" || r.country.equals(country, true)
        inScreen && (group == "All" || r.group == group) && countryMatch &&
            (query.isBlank() || listOf(r.title, r.subtitle, r.detail, r.group, r.country).any { it.contains(query, true) })
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = bg) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().background(card).border(1.dp, fg.copy(alpha = 0.05f)).padding(horizontal = 18.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    Box(Modifier.size(43.dp).clip(RoundedCornerShape(15.dp)).background(Brush.linearGradient(listOf(Petal, Sage))), contentAlignment = Alignment.Center) {
                        Text("b.", color = Aubergine, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("BLUMTHERAPY", color = fg, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                        Text("WELLBEING · GENTLE RECOVERY", color = accent, fontSize = 8.sp, letterSpacing = 1.sp)
                    }
                    Surface(color = bg, shape = RoundedCornerShape(12.dp), modifier = Modifier.clickable { light = !light }) {
                        Text(if (light) "☾" else "☼", color = fg, modifier = Modifier.padding(10.dp), fontSize = 17.sp)
                    }
                }
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 17.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    when (screen) {
                        "Home" -> {
                            item {
                                Box(
                                    Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF332B49), Color(0xFF58436F), Color(0xFF7D536E))))
                                        .border(1.dp, Color(0xFFD6C5FF).copy(alpha = 0.34f), RoundedCornerShape(28.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.blumtherapy_hero_art),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxWidth().height(252.dp).alpha(0.78f)
                                    )
                                    Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Color(0xF0332B49), Color(0xB0332B49), Color(0x46332B49)))))
                                    Column(Modifier.fillMaxWidth().padding(23.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("A GENTLER WAY FORWARD", color = Petal, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                                        Text("You can start where you are.", color = Color.White, fontSize = 31.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
                                        Text("A calm space to rebuild routines, find trusted support and move at a pace that respects your life.", color = Color(0xFFF0EAF5), fontSize = 13.sp, lineHeight = 20.sp)
                                        BoxWithConstraints(Modifier.fillMaxWidth()) {
                                            if (maxWidth < 340.dp) {
                                                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Surface(color = Sage, shape = RoundedCornerShape(13.dp), modifier = Modifier.fillMaxWidth().clickable { screen = "My plan" }) {
                                                        Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                                                            Text("Shape my day ↗", color = Aubergine, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        }
                                                    }
                                                    Surface(color = Color.White.copy(alpha = 0.10f), shape = RoundedCornerShape(13.dp), modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(13.dp)).clickable { screen = "Support" }) {
                                                        Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                                                            Text("Find support", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                        }
                                                    }
                                                }
                                            } else {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Surface(color = Sage, shape = RoundedCornerShape(13.dp), modifier = Modifier.clickable { screen = "My plan" }) {
                                                        Text("Shape my day ↗", color = Aubergine, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp))
                                                    }
                                                    Surface(color = Color.White.copy(alpha = 0.10f), shape = RoundedCornerShape(13.dp), modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(13.dp)).clickable { screen = "Support" }) {
                                                        Text("Find support", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Text("Small steps, built around you", color = fg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("Choose a starting point. There is no required path and no streak to protect.", color = muted, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                    TherapyShortcut("◷", "My plan", "Habits and dates", Modifier.weight(1f)) { screen = "My plan" }
                                    TherapyShortcut("✧", "Library", "Learn at your pace", Modifier.weight(1f)) { screen = "Library" }
                                }
                            }
                            item {
                                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = card)) {
                                    Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text("✧  Keep this close", color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("A difficult day does not erase your effort. You can pause, choose a smaller step or ask for help.", color = fg, fontSize = 12.sp, lineHeight = 18.sp)
                                        Text("Your habits and dated tasks remain on this device.", color = accent, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                        "My plan" -> item {
                            PlannerPanel(
                                heading = "Your plan, at your pace",
                                intro = "Choose small habits, review dates and record the steps you wanted to try. Pause and adjust without losing your history.",
                                accent = accent,
                                cardColor = card,
                                textColor = fg,
                                mutedColor = muted,
                                languageCode = "en"
                            )
                        }
                        "My progress" -> {
                            item {
                                Text("Your journey is not a race", color = fg, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Notice what helped you keep going, not whether you protected a perfect streak.", color = muted, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                            item {
                                PlannerPanel(
                                    heading = "Your personal journey",
                                    intro = "Review check-ins by date. Blank days are not failures and rest does not delete progress.",
                                    accent = accent,
                                    cardColor = card,
                                    textColor = fg,
                                    mutedColor = muted,
                                    languageCode = "en"
                                )
                            }
                        }
                        "Support", "Library" -> {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(if (screen == "Library") "Ideas for your next chapter" else "Trusted support, without empty promises", color = fg, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                                    Text(if (screen == "Library") "Learning and gentle practices, explored on your terms." else "Official starting points. Confirm coverage, eligibility, cost and local availability directly.", color = muted, fontSize = 12.sp, lineHeight = 18.sp)
                                    OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, placeholder = { Text("Search resources or countries…") })
                                }
                            }
                            if (screen == "Support") {
                                item {
                                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        groups.filter { it in listOf("All", "Support", "Community") }.forEach { g ->
                                            Surface(color = if (group == g) accent else card, shape = RoundedCornerShape(50), modifier = Modifier.clickable { group = g }) {
                                                Text(g, color = if (group == g) Color.White else fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    countries.forEach { c ->
                                        Surface(color = if (country == c) Petal else card, shape = RoundedCornerShape(50), modifier = Modifier.clickable { country = c }) {
                                            Text(c, color = if (country == c) Aubergine else fg, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
                                        }
                                    }
                                }
                            }
                            if (opened != null) {
                                item {
                                    val r = opened!!
                                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = card)) {
                                        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(r.mark, fontSize = 28.sp, color = accent)
                                            Text(r.title, color = fg, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                            Text(r.detail, color = fg, fontSize = 13.sp, lineHeight = 19.sp)
                                            Text(r.country + " · " + r.status, color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text("This is a starting point, not confirmation of care, eligibility or cost.", color = muted, fontSize = 11.sp, lineHeight = 16.sp)
                                            r.officialUrl?.let { url -> Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }) { Text("Open official source ↗") } }
                                            OutlinedButton(onClick = { opened = null }) { Text("Back to resources") }
                                        }
                                    }
                                }
                            }
                            items(shown, key = { it.title }) { r ->
                                Card(Modifier.fillMaxWidth().clickable { opened = r }, shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = card), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                                    Row(Modifier.padding(15.dp), horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Petal.copy(alpha = .55f), Sage))), contentAlignment = Alignment.Center) {
                                            Text(r.mark, color = Aubergine, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Text(r.title, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(r.subtitle, color = muted, fontSize = 11.sp, lineHeight = 16.sp)
                                            Text(r.group.uppercase() + " · " + r.country.uppercase(), color = accent, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
                                        }
                                        Text("›", color = accent, fontSize = 24.sp)
                                    }
                                }
                            }
                            if (shown.isEmpty()) item {
                                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = card)) {
                                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("No matches yet", color = fg, fontWeight = FontWeight.Bold)
                                        Text("Try another search or reset the country filter.", color = muted, fontSize = 12.sp, textAlign = TextAlign.Center)
                                        Button(onClick = { query = ""; group = "All"; country = "All"; opened = null }) { Text("Clear search") }
                                    }
                                }
                            }
                        }
                    }
                    item { Text("You deserve support without judgment. One step at a time.", Modifier.fillMaxWidth().padding(vertical = 8.dp), color = muted, fontSize = 9.sp, textAlign = TextAlign.Center) }
                }

                Row(Modifier.fillMaxWidth().background(card).border(1.dp, fg.copy(alpha = .06f)).padding(horizontal = 4.dp, vertical = 7.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    tabs.forEach { tab ->
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(13.dp))
                                .background(if (screen == tab.title) accent.copy(alpha = .15f) else Color.Transparent)
                                .clickable { screen = tab.title; query = ""; group = "All"; country = "All"; opened = null }
                                .padding(horizontal = 2.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(tab.icon, color = if (screen == tab.title) accent else muted, fontSize = 19.sp)
                            Text(tab.title, color = if (screen == tab.title) accent else fg.copy(alpha = .76f), fontSize = 8.sp, fontWeight = if (screen == tab.title) FontWeight.Bold else FontWeight.Medium, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TherapyShortcut(icon: String, title: String, summary: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(icon, color = MaterialTheme.colorScheme.primary, fontSize = 23.sp)
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(summary, color = SoftText, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
