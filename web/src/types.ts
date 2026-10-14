export type Screen = "home" | "plan" | "library" | "support" | "progress";
export type Energy = "low" | "steady" | "high";
export type Focus = "care" | "connection" | "learning" | "structure";

export type Habit = {
  id: string;
  title: string;
  note: string;
  checkins: string[];
};

export type Resource = {
  id: string;
  title: string;
  summary: string;
  category: string;
  country: string;
  source_name: string;
  url: string;
  reviewed_on?: string;
};

export type Plan = {
  title: string;
  minutes: number;
  steps: Array<{ title: string; detail: string }>;
  rationale?: string;
  safety_note?: string;
};
