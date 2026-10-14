import { Energy, Focus, Plan, Resource } from "../types";

export const localResources: Resource[] = [
  {
    id: "find-treatment",
    title: "FindTreatment.gov",
    summary: "Official U.S. treatment locator. Confirm services, cost, eligibility and current availability directly.",
    category: "Support",
    country: "US",
    source_name: "FindTreatment",
    url: "https://findtreatment.gov",
  },
  {
    id: "nhs",
    title: "NHS: getting help for drug addiction",
    summary: "Official information and starting points for support in the UK. Local pathways differ by nation.",
    category: "Support",
    country: "UK",
    source_name: "NHS",
    url: "https://www.nhs.uk",
  },
  {
    id: "health-canada",
    title: "Health Canada: treatment",
    summary: "Explore official treatment information and check province or territory-specific services.",
    category: "Support",
    country: "CA",
    source_name: "Health Canada",
    url: "https://www.canada.ca",
  },
  {
    id: "who",
    title: "WHO: treatment standards",
    summary: "International guidance for understanding evidence-based care. This is information, not a local service.",
    category: "Learning",
    country: "GLOBAL",
    source_name: "WHO",
    url: "https://www.who.int",
  },
];

export function createLocalPlan(energy: Energy, minutes: number, focus: Focus): Plan {
  const text: Record<Focus, { title: string; low: string; steady: string; high: string; close: string }> = {
    care: {
      title: "Return to your body",
      low: "Breathe comfortably and have some water.",
      steady: "Take a short pause and stretch gently.",
      high: "Take a walk or prepare something nourishing, if you can.",
      close: "Finish quietly with a few breaths.",
    },
    connection: {
      title: "Connect without pressure",
      low: "Think of someone you feel safe with.",
      steady: "Send a short message to someone you trust.",
      high: "Suggest a call or a simple get-together.",
      close: "Check in briefly afterwards.",
    },
    learning: {
      title: "Open a new window",
      low: "Read one paragraph or listen to something calm.",
      steady: "Spend a few minutes on a topic that interests you.",
      high: "Explore training or practise a skill.",
      close: "Summarize one idea you learned.",
    },
    structure: {
      title: "Make room for what matters",
      low: "Choose one two-minute task.",
      steady: "Tidy one small space or prepare for tomorrow.",
      high: "Break one pending task into three clear steps.",
      close: "Plan one next step.",
    },
  };

  const item = text[focus];
  const first = energy === "low" ? item.low : energy === "steady" ? item.steady : item.high;
  const steps = [{ title: "One possible step", detail: first }];

  if (energy !== "low" && minutes >= 8) {
    steps.push({ title: "A gentle close", detail: item.close });
  }

  return {
    title: item.title,
    minutes: energy === "low" ? Math.min(5, minutes) : minutes,
    steps,
    rationale: "This suggestion uses only the choices made now. It does not diagnose or predict outcomes.",
    safety_note: "If you are in crisis, contact local emergency services or a crisis line.",
  };
}
