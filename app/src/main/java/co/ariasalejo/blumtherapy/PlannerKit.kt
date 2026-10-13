package co.ariasalejo.blumtherapy

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class PlannerHabit(val id: String, val title: String, val completedDates: Set<String>)
internal data class PlannerTask(val id: String, val title: String, val dueDate: String, val completed: Boolean)
internal data class PlannerSnapshot(val habits: List<PlannerHabit>, val tasks: List<PlannerTask>)

internal data class PlannerOpportunity(
    val title: String,
    val jurisdiction: String,
    val category: String,
    val description: String,
    val url: String,
    val reviewedOn: String = "2026-10-12"
)

private fun plannerOpportunities(): List<PlannerOpportunity> = listOf(
    PlannerOpportunity("USAGov · Benefits finder", "United States", "Public benefits",
        "Explore federal support routes and check official eligibility rules for your circumstances.",
        "https://www.usa.gov/benefit-finder"),
    PlannerOpportunity("FindTreatment.gov", "United States", "Health support",
        "Official locator for mental-health and substance-use treatment services; confirm cost and local availability directly.",
        "https://findtreatment.gov/"),
    PlannerOpportunity("GOV.UK · Benefits", "United Kingdom", "Public benefits",
        "Browse official information about benefits, eligibility, claims, appeals and support while looking for work.",
        "https://www.gov.uk/browse/benefits"),
    PlannerOpportunity("Canada · Benefits Finder", "Canada · Federal", "Public benefits",
        "Find federal programs and then follow provincial or territorial links where relevant; this tool does not determine eligibility.",
        "https://www.canada.ca/en/services/benefits/finder.html"),
    PlannerOpportunity("Services Australia · Payment Finder", "Australia", "Payments and support",
        "Estimate or compare payments and inspect the conditions from the Australian Government before planning around a payment.",
        "https://www.servicesaustralia.gov.au/online-estimators?context=64107"),
    PlannerOpportunity("NHS · Your Mind Plan (18+)", "United Kingdom · England / NHS", "Wellbeing planning",
        "A general wellbeing action plan for adults; it is not a medical assessment and does not replace professional advice.",
        "https://www.nhs.uk/every-mind-matters/mental-wellbeing-tips/your-mind-plan-quiz/")
)

/** Private, local-only persistence for the first planner phase. */
internal object PlannerStore {
    private const val PREFS_NAME = "blumtherapy_planner_v1"
    private const val HABITS_KEY = "habits"
    private const val TASKS_KEY = "tasks"

    fun today(): String = LocalDate.now().toString()

    fun load(context: Context, starterHabits: List<String>): PlannerSnapshot {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedHabits = prefs.getString(HABITS_KEY, null)
        val habits = if (savedHabits == null) {
            starterHabits.map { PlannerHabit(UUID.randomUUID().toString(), it, emptySet()) }
                .also { saveHabits(context, it) }
        } else decodeHabits(savedHabits)
        return PlannerSnapshot(habits, decodeTasks(prefs.getString(TASKS_KEY, null)))
    }

    fun addHabit(context: Context, title: String, starterHabits: List<String>) {
        val clean = title.trim().take(80)
        if (clean.isBlank()) return
        saveHabits(context, load(context, starterHabits).habits +
            PlannerHabit(UUID.randomUUID().toString(), clean, emptySet()))
    }

    fun setHabitCompleted(context: Context, id: String, date: String, completed: Boolean, starterHabits: List<String>) {
        if (runCatching { LocalDate.parse(date) }.isFailure) return
        val updated = load(context, starterHabits).habits.map { habit ->
            if (habit.id != id) habit else {
                val dates = habit.completedDates.toMutableSet()
                if (completed) dates.add(date) else dates.remove(date)
                habit.copy(completedDates = dates)
            }
        }
        saveHabits(context, updated)
    }

    fun removeHabit(context: Context, id: String, starterHabits: List<String>) {
        saveHabits(context, load(context, starterHabits).habits.filterNot { it.id == id })
    }

    fun addTask(context: Context, title: String, dueDate: String) {
        val clean = title.trim().take(120)
        val validDate = runCatching { LocalDate.parse(dueDate).toString() }.getOrNull()
        if (clean.isBlank() || validDate == null) return
        saveTasks(context, readTasks(context) + PlannerTask(UUID.randomUUID().toString(), clean, validDate, false))
    }

    fun setTaskCompleted(context: Context, id: String, completed: Boolean) {
        saveTasks(context, readTasks(context).map { if (it.id == id) it.copy(completed = completed) else it })
    }

    fun removeTask(context: Context, id: String) {
        saveTasks(context, readTasks(context).filterNot { it.id == id })
    }

    private fun readTasks(context: Context): List<PlannerTask> =
        decodeTasks(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(TASKS_KEY, null))

    private fun saveHabits(context: Context, habits: List<PlannerHabit>) {
        val array = JSONArray()
        habits.forEach { habit ->
            val dates = JSONArray()
            habit.completedDates.sorted().forEach { dates.put(it) }
            array.put(JSONObject().put("id", habit.id).put("title", habit.title).put("completedDates", dates))
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(HABITS_KEY, array.toString()).apply()
    }

    private fun saveTasks(context: Context, tasks: List<PlannerTask>) {
        val array = JSONArray()
        tasks.forEach { task ->
            array.put(JSONObject().put("id", task.id).put("title", task.title)
                .put("dueDate", task.dueDate).put("completed", task.completed))
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(TASKS_KEY, array.toString()).apply()
    }

    private fun decodeHabits(raw: String): List<PlannerHabit> = runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val row = array.optJSONObject(i) ?: continue
                val title = row.optString("title").trim()
                if (title.isNotBlank()) {
                    val dates = mutableSetOf<String>()
                    val storedDates = row.optJSONArray("completedDates")
                    if (storedDates != null) {
                        for (j in 0 until storedDates.length()) {
                            val date = storedDates.optString(j)
                            if (date.isNotBlank()) dates.add(date)
                        }
                    }
                    add(PlannerHabit(row.optString("id").ifBlank { UUID.randomUUID().toString() }, title, dates))
                }
            }
        }
    }.getOrDefault(emptyList())

    private fun decodeTasks(raw: String?): List<PlannerTask> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val row = array.optJSONObject(i) ?: continue
                    val title = row.optString("title").trim()
                    val date = runCatching { LocalDate.parse(row.optString("dueDate")).toString() }.getOrNull()
                    if (title.isNotBlank() && date != null) {
                        add(PlannerTask(row.optString("id").ifBlank { UUID.randomUUID().toString() },
                            title, date, row.optBoolean("completed", false)))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
}

@androidx.compose.runtime.Composable
internal fun PlannerPanel(
    heading: String,
    intro: String,
    accent: androidx.compose.ui.graphics.Color,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    mutedColor: androidx.compose.ui.graphics.Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val starterHabits = rememberStarterHabits()
    var snapshot by androidx.compose.runtime.remember(context) {
        androidx.compose.runtime.mutableStateOf(PlannerStore.load(context, starterHabits))
    }
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var newHabit by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var newTask by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var dueDate by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(PlannerStore.today()) }
    var dateError by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val today = PlannerStore.today()
    val completedCount = snapshot.habits.count { today in it.completedDates }
    val taskList = snapshot.tasks.sortedWith(compareBy<PlannerTask>({ it.completed }, { it.dueDate }))
    val opportunities = plannerOpportunities()
    val ink = androidx.compose.ui.graphics.Color(0xFF0B1020)

    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = cardColor)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(17.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                    androidx.compose.material3.Text(heading, color = textColor, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    androidx.compose.material3.Text(intro, color = mutedColor, fontSize = 12.sp, lineHeight = 18.sp)
                }
                androidx.compose.material3.Button(
                    onClick = { expanded = !expanded },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = accent, contentColor = ink)
                ) { androidx.compose.material3.Text(if (expanded) "Close" else "Open") }
            }
            androidx.compose.material3.Text(
                completedCount.toString() + " / " + snapshot.habits.size + " habits completed today",
                color = accent, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 12.sp
            )
            if (expanded) {
                androidx.compose.material3.Text("Today: " + today, color = mutedColor, fontSize = 12.sp)
                androidx.compose.material3.Text("Habits", color = textColor, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                if (snapshot.habits.isEmpty()) {
                    androidx.compose.material3.Text("No habits yet. Add one that feels realistic for you.", color = mutedColor, fontSize = 12.sp)
                }
                snapshot.habits.forEach { habit ->
                    androidx.compose.foundation.layout.Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = today in habit.completedDates,
                            onCheckedChange = { checked ->
                                PlannerStore.setHabitCompleted(context, habit.id, today, checked, starterHabits)
                                snapshot = PlannerStore.load(context, starterHabits)
                            },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = accent)
                        )
                        androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                            androidx.compose.material3.Text(habit.title, color = textColor, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            androidx.compose.material3.Text(
                                if (today in habit.completedDates) "Checked in for today" else "Progress without pressure",
                                color = mutedColor, fontSize = 11.sp
                            )
                        }
                        androidx.compose.material3.Text(
                            "Remove", color = mutedColor, fontSize = 11.sp,
                            modifier = androidx.compose.ui.Modifier.clickable {
                                PlannerStore.removeHabit(context, habit.id, starterHabits)
                                snapshot = PlannerStore.load(context, starterHabits)
                            }.padding(5.dp)
                        )
                    }
                }
                androidx.compose.material3.OutlinedTextField(
                    value = newHabit, onValueChange = { newHabit = it.take(80) },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(), singleLine = true,
                    label = { androidx.compose.material3.Text("New habit", color = mutedColor) },
                    placeholder = { androidx.compose.material3.Text("A small, achievable action", color = mutedColor) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = textColor)
                )
                androidx.compose.material3.Button(
                    enabled = newHabit.isNotBlank(),
                    onClick = {
                        PlannerStore.addHabit(context, newHabit, starterHabits)
                        snapshot = PlannerStore.load(context, starterHabits)
                        newHabit = ""
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = accent, contentColor = ink)
                ) { androidx.compose.material3.Text("Add habit") }

                androidx.compose.material3.Text("Dated tasks", color = textColor, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                androidx.compose.material3.OutlinedTextField(
                    value = newTask, onValueChange = { newTask = it.take(120) },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(), singleLine = true,
                    label = { androidx.compose.material3.Text("Task or deadline", color = mutedColor) },
                    placeholder = { androidx.compose.material3.Text("e.g. Check course registration", color = mutedColor) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = textColor)
                )
                androidx.compose.material3.OutlinedTextField(
                    value = dueDate, onValueChange = { dueDate = it.take(10); dateError = false },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(), singleLine = true, isError = dateError,
                    label = { androidx.compose.material3.Text("Date (YYYY-MM-DD)", color = mutedColor) },
                    supportingText = { if (dateError) androidx.compose.material3.Text("Enter a valid date as YYYY-MM-DD") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = textColor)
                )
                androidx.compose.material3.Button(
                    enabled = newTask.isNotBlank(),
                    onClick = {
                        val validDate = runCatching { LocalDate.parse(dueDate.trim()).toString() }.getOrNull()
                        if (validDate == null) {
                            dateError = true
                        } else {
                            PlannerStore.addTask(context, newTask, validDate)
                            snapshot = PlannerStore.load(context, starterHabits)
                            newTask = ""
                            dateError = false
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = accent, contentColor = ink)
                ) { androidx.compose.material3.Text("Save dated task") }
                if (taskList.isEmpty()) {
                    androidx.compose.material3.Text("No dated tasks yet. Add a deadline or next step for a goal.", color = mutedColor, fontSize = 12.sp)
                }
                taskList.forEach { task ->
                    androidx.compose.foundation.layout.Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = task.completed,
                            onCheckedChange = { checked ->
                                PlannerStore.setTaskCompleted(context, task.id, checked)
                                snapshot = PlannerStore.load(context, starterHabits)
                            },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = accent)
                        )
                        androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                            androidx.compose.material3.Text(task.title, color = textColor, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            val status = when {
                                task.completed -> "Completed"
                                task.dueDate < today -> "Overdue · reschedule without guilt"
                                task.dueDate == today -> "Due today"
                                else -> "Due " + task.dueDate
                            }
                            androidx.compose.material3.Text(status, color = mutedColor, fontSize = 11.sp)
                        }
                        androidx.compose.material3.Text(
                            "Remove", color = mutedColor, fontSize = 11.sp,
                            modifier = androidx.compose.ui.Modifier.clickable {
                                PlannerStore.removeTask(context, task.id)
                                snapshot = PlannerStore.load(context, starterHabits)
                            }.padding(5.dp)
                        )
                    }
                }
                androidx.compose.material3.Text("Official opportunities & support", color = textColor, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                androidx.compose.material3.Text(
                    "Official starting points only. Rules, opening dates and eligibility can change; confirm details with the institution.",
                    color = mutedColor, fontSize = 12.sp, lineHeight = 17.sp
                )
                opportunities.forEach { opportunity ->
                    androidx.compose.material3.Card(
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = cardColor),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        androidx.compose.foundation.layout.Column(
                            modifier = androidx.compose.ui.Modifier.padding(13.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                        ) {
                            androidx.compose.material3.Text(opportunity.title, color = textColor, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            androidx.compose.material3.Text(opportunity.jurisdiction + " · " + opportunity.category, color = accent, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            androidx.compose.material3.Text(opportunity.description, color = mutedColor, fontSize = 12.sp, lineHeight = 17.sp)
                            androidx.compose.material3.Button(
                                onClick = { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(opportunity.url))) },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = accent, contentColor = ink)
                            ) { androidx.compose.material3.Text("Open official source ↗") }
                            androidx.compose.material3.Text("Catalogue checked: " + opportunity.reviewedOn, color = mutedColor, fontSize = 10.sp)
                        }
                    }
                }
                androidx.compose.material3.Text(
                    "Saved only on this device. This planner does not confirm appointments, eligibility, enrollment or benefits.",
                    color = mutedColor, fontSize = 11.sp, lineHeight = 16.sp
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun rememberStarterHabits(): List<String> = listOf(
    "Take a brief pause",
    "Practice one small skill",
    "Connect with someone supportive"
)
