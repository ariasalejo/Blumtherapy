import { Habit } from "../types";

export const STORAGE = "blumtherapy.pwa.habits.v1";

export const initialHabits: Habit[] = [
  { id: "body", title: "Care for your body", note: "Drink water, eat something nourishing, or rest", checkins: [] },
  { id: "move", title: "Move a little", note: "Stretch or walk gently for a few minutes", checkins: [] },
  { id: "connect", title: "Connect safely", note: "Send a message to someone you trust", checkins: [] },
  { id: "learn", title: "Feed your curiosity", note: "Read or learn one small thing", checkins: [] },
];

export function todayKey(): string {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

export function readHabits(): Habit[] {
  try {
    const saved = localStorage.getItem(STORAGE);
    return saved ? (JSON.parse(saved) as Habit[]) : initialHabits;
  } catch {
    return initialHabits;
  }
}

export function saveHabits(habits: Habit[]) {
  try {
    localStorage.setItem(STORAGE, JSON.stringify(habits));
  } catch {
    // keep in memory if storage is unavailable
  }
}
