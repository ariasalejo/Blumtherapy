import { useCallback, useEffect, useMemo, useState } from "react";
import { Habit } from "../types";
import { initialHabits, readHabits, saveHabits, todayKey } from "../lib/storage";

export function useHabits() {
  const [habits, setHabits] = useState<Habit[]>(readHabits);
  const today = useMemo(() => todayKey(), []);

  useEffect(() => {
    const timer = window.setTimeout(() => saveHabits(habits), 250);
    return () => window.clearTimeout(timer);
  }, [habits]);

  const checkedToday = useMemo(
    () => habits.filter((habit) => habit.checkins.includes(today)).length,
    [habits, today],
  );

  const toggleHabit = useCallback((id: string) => {
    setHabits((previousHabits) =>
      previousHabits.map((habit) => (habit.id !== id
        ? habit
        : {
            ...habit,
            checkins: habit.checkins.includes(today)
              ? habit.checkins.filter((date) => date !== today)
              : [...habit.checkins, today],
          })),
    );
  }, [today]);

  const addHabit = useCallback((title: string) => {
    const cleanTitle = title.trim().slice(0, 70);
    if (!cleanTitle) return;

    setHabits((previousHabits) => [
      ...previousHabits,
      { id: crypto.randomUUID(), title: cleanTitle, note: "A small step you chose for yourself", checkins: [] },
    ]);
  }, []);

  return {
    habits,
    checkedToday,
    today,
    toggleHabit,
    addHabit,
  };
}
