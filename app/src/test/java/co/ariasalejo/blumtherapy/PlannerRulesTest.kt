package co.ariasalejo.blumtherapy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlannerRulesTest {
    @Test
    fun acceptsIsoDatesAndRejectsImpossibleDates() {
        assertEquals("2024-02-29", PlannerRules.normalizeDate(" 2024-02-29 "))
        assertNull(PlannerRules.normalizeDate("2026-02-29"))
        assertNull(PlannerRules.normalizeDate("not-a-date"))
    }

    @Test
    fun datePickerRoundTripsUsingUtcWithoutShiftingCalendarDay() {
        val selected = PlannerRules.dateToUtcMillis("2026-10-12")
        assertEquals("2026-10-12", PlannerRules.dateFromUtcMillis(selected))
        assertNull(PlannerRules.dateToUtcMillis("2026-02-29"))
        assertNull(PlannerRules.dateFromUtcMillis(null))
    }

    @Test
    fun habitCompletionIsIdempotentAndPreservesOtherDays() {
        val previous = setOf("2026-10-10")
        val completed = PlannerRules.updateCompletionDates(previous, "2026-10-12", true)
        assertEquals(setOf("2026-10-10", "2026-10-12"), completed)
        assertEquals(completed, PlannerRules.updateCompletionDates(completed, "2026-10-12", true))
        assertEquals(previous, PlannerRules.updateCompletionDates(completed, "2026-10-12", false))
    }

    @Test
    fun taskStatusHandlesPastTodayFutureAndCompletedTasks() {
        val today = "2026-10-12"
        assertEquals(PlannerTaskState.OVERDUE, PlannerRules.status(PlannerTask("1", "Past", "2026-10-11", false), today))
        assertEquals(PlannerTaskState.DUE_TODAY, PlannerRules.status(PlannerTask("2", "Today", today, false), today))
        assertEquals(PlannerTaskState.UPCOMING, PlannerRules.status(PlannerTask("3", "Future", "2026-10-13", false), today))
        assertEquals(PlannerTaskState.COMPLETED, PlannerRules.status(PlannerTask("4", "Done late", "2026-10-11", true), today))
    }
}
