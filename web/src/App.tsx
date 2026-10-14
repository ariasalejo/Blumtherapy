import { useCallback, useMemo, useState } from "react";
import { useHabits } from "./hooks/useHabits";
import { useResources } from "./hooks/useResources";
import { createLocalPlan, localResources } from "./lib/plan";
import { Screen, Focus, Energy, Plan } from "./types";

const screens: Screen[] = ["home", "plan", "library", "support", "progress"];
const screenLabels = ["Home", "My plan", "Library", "Support", "My progress"];
const screenIcons = ["✳", "◷", "✧", "♡", "⌁"];

export default function App() {
  const [screen, setScreen] = useState<Screen>("home");
  const [newHabit, setNewHabit] = useState("");
  const [energy, setEnergy] = useState<Energy>("steady");
  const [minutes, setMinutes] = useState(10);
  const [focus, setFocus] = useState<Focus>("care");
  const [plan, setPlan] = useState<Plan | null>(null);
  const [planSource, setPlanSource] = useState<"local" | "api" | null>(null);
  const [busy, setBusy] = useState(false);
  const [query, setQuery] = useState("");
  const [notice, setNotice] = useState("");

  const { habits, checkedToday, today, toggleHabit, addHabit } = useHabits();
  const { country, setCountry, resources, online, apiConnected } = useResources("US");

  const shownResources = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return resources;
    return resources.filter((resource) => {
      return resource.title.toLowerCase().includes(q)
        || resource.summary.toLowerCase().includes(q)
        || resource.category.toLowerCase().includes(q)
        || resource.country.toLowerCase().includes(q);
    });
  }, [resources, query]);

  const createPlan = useCallback(async () => {
    const input = { energy, time_minutes: minutes, focus, language: "en" };
    setPlan(createLocalPlan(energy, minutes, focus));
    setPlanSource("local");
    setBusy(true);

    const base = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");
    if (!base) {
      setBusy(false);
      return;
    }

    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), 8000);

    try {
      const response = await fetch(base + "/api/v1/plan/suggest", {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify(input),
        signal: controller.signal,
      });

      if (response.ok) {
        const data = await response.json();
        if (typeof data.title === "string" && Array.isArray(data.steps)) {
          setPlan(data);
          setPlanSource("api");
          setCountry(country);
        }
      }
    } catch {
      // Keep the local fallback plan visible if the API is unavailable.
    } finally {
      window.clearTimeout(timeout);
      setBusy(false);
    }
  }, [country, energy, focus, minutes, setCountry]);

  const handleAddHabit = useCallback(() => {
    const title = newHabit.trim().slice(0, 70);
    if (!title) return;

    addHabit(title);
    setNewHabit("");
    setNotice("Habit added. You can change it whenever you need.");
    window.setTimeout(() => setNotice(""), 3000);
  }, [addHabit, newHabit]);

  const title = screen === "home"
    ? "Your space for today"
    : screen === "plan"
      ? "Build a gentler day"
      : screen === "library"
        ? "Ideas for your next chapter"
        : screen === "support"
          ? "Trusted help, without empty promises"
          : "Your journey is not a race";

  return (
    <div className="shell">
      <aside className="sidebar">
        <a className="brand" href="#" onClick={(event) => { event.preventDefault(); setScreen("home"); }}>
          <span className="brand-mark">b<span>✳</span></span>
          <span><strong>BLUMTHERAPY</strong><small>GENTLE FORWARD</small></span>
        </a>
        <p className="nav-caption">YOUR SPACE</p>
        <nav className="desktop-nav" aria-label="Main navigation">
          {screens.map((screenOption, index) => (
            <button
              key={screenOption}
              className={screen === screenOption ? "nav-button active" : "nav-button"}
              onClick={() => setScreen(screenOption)}
            >
              <span>{screenIcons[index]}</span>
              <small>{screenLabels[index]}</small>
            </button>
          ))}
        </nav>
        <div className="sidebar-note">
          <span>✧</span>
          <p>You can start where you are. No streaks, no guilt, and no pressure to explain your story.</p>
          <small>PRIVATE BY DEFAULT · HABITS STAY ON THIS DEVICE</small>
        </div>
      </aside>

      <main id="main-content" className="main">
        <header className="topbar">
          <div className="crumb">
            <span>BLUMTHERAPY</span>
            <b>/</b>
            {title}
          </div>
          <div className="top-actions">
            <span className={online ? "net-pill" : "net-pill offline"}><i />{online ? "online" : "offline"}</span>
            <button className="icon" onClick={() => setNotice("Help is private and local by default.")}>?</button>
          </div>
        </header>

        <div className="content">
          <section className="page-head">
            <div>
              <p className="eyebrow">WELLBEING · HABITS · CONNECTION</p>
              <h1>{title}</h1>
              <p>{screen === "home" ? "You do not need a perfect plan. Just one step that feels manageable." : ""}</p>
            </div>
          </section>

          {screen === "home" && (
            <>
              <section className="hero">
                <div className="hero-copy">
                  <span className="live-kicker"><i /> A NEW DAY, WITHOUT PRESSURE</span>
                  <h2>You do not have to solve everything today.</h2>
                </div>
              </section>

              <div className="section-head">
                <div>
                  <h2>A little progress is still progress</h2>
                  <p>Check in with a habit if it helps. You can begin again any day.</p>
                </div>
                <button className="quiet-button">Manage habits</button>
              </div>

              <section className="habit-list">
                {habits.slice(0, 3).map((habit) => (
                  <button
                    className={habit.checkins.includes(today) ? "habit-row checked" : "habit-row"}
                    key={habit.id}
                    onClick={() => toggleHabit(habit.id)}
                  >
                    <span>{habit.title}</span>
                  </button>
                ))}
              </section>

              <section className="summary-grid">
                <article className="panel soft-panel">
                  <p className="panel-kicker">TODAY, YOUR WAY</p>
                  <div className="big-number">
                    {checkedToday}
                    <small> / {habits.length}</small>
                  </div>
                </article>
              </section>
            </>
          )}

          {screen === "plan" && (
            <>
              <section className="plan-grid">
                <article className="panel planner">
                  <p className="panel-kicker">EXPLAINABLE DAILY-STEP ENGINE</p>
                  <h2>What feels possible today?</h2>
                  <label>
                    ENERGY YOU HAVE
                    <select value={energy} onChange={(event) => setEnergy(event.target.value as Energy)}>
                      <option value="low">Low — keep it tiny</option>
                      <option value="steady">Steady — a small steady step</option>
                      <option value="high">High — you can do more</option>
                    </select>
                  </label>
                  <label>
                    TIME AVAILABLE
                    <select value={minutes} onChange={(event) => setMinutes(Number(event.target.value))}>
                      <option value={5}>5 minutes</option>
                      <option value={10}>10 minutes</option>
                      <option value={20}>20 minutes</option>
                    </select>
                  </label>
                  <label>
                    WHAT WOULD YOU LIKE TO NURTURE
                    <select value={focus} onChange={(event) => setFocus(event.target.value as Focus)}>
                      <option value="care">Self-care</option>
                      <option value="connection">Connection</option>
                      <option value="learning">Learning</option>
                      <option value="structure">Structure</option>
                    </select>
                  </label>
                  <button className="primary full" onClick={createPlan} disabled={busy}>
                    {busy ? "Shaping your plan…" : "Create a gentle plan"} <span>↗</span>
                  </button>
                  <small className="privacy-note">Only the choices you make are used to suggest this plan.</small>
                </article>

                <article className="panel generated">
                  <div className="generated-icon">✳</div>
                  <p className="panel-kicker">YOUR NEXT POSSIBLE STEP</p>
                  {plan ? (
                    <>
                      <h2>{plan.title}</h2>
                      <span className="minutes">{plan.minutes} minutes</span>
                      <ul>
                        {plan.steps.map((step) => (
                          <li key={`${step.title}-${step.detail}`}>
                            <strong>{step.title}</strong>
                            <p>{step.detail}</p>
                          </li>
                        ))}
                      </ul>
                    </>
                  ) : (
                    <p>No plan created yet.</p>
                  )}
                  {planSource && <small>Source: {planSource}</small>}
                </article>
              </section>
            </>
          )}

          {(screen === "support" || screen === "library") && (
            <section>
              <div className="resource-tools">
                <label className="search">
                  <span>⌕</span>
                  <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search resources" />
                </label>
              </div>

              <div className="resource-grid">
                {shownResources
                  .filter((resource) => screen === "support"
                    ? /support/i.test(resource.category) || resource.country !== "GLOBAL"
                    : /learning/i.test(resource.category) || resource.country === "GLOBAL")
                  .map((resource) => (
                    <article key={resource.id} className="resource-card">
                      <h3>{resource.title}</h3>
                      <p>{resource.summary}</p>
                      <small>{resource.country} · {resource.source_name}</small>
                    </article>
                  ))}
              </div>

              {shownResources.length === 0 && (
                <p className="empty-state">No matches yet. Try another search term.</p>
              )}

              <p className="disclaimer">
                Resources are starting points, not a guarantee of eligibility, appointments, cost or immediate care.
                BlumTherapy is not a treatment provider or emergency service.
              </p>
            </section>
          )}

          {screen === "progress" && (
            <section className="progress-grid">
              <article className="panel journey">
                <div className="journey-orb">
                  <div>
                    <b>{checkedToday}</b>
                    <small>today</small>
                  </div>
                </div>
                <div>
                  <p>Your progress is private and resets when you choose.</p>
                </div>
              </article>
            </section>
          )}

          {screen === "home" && (
            <section className="panel habits-manager">
              <div className="section-head compact">
                <div>
                  <p className="panel-kicker">HABITS YOU CHOOSE</p>
                  <h2>Build a rhythm, not a streak</h2>
                </div>
                <span className="chip">{habits.length} habits</span>
              </div>

              <div className="habit-form">
                <input
                  value={newHabit}
                  onChange={(event) => setNewHabit(event.target.value)}
                  placeholder="Add a new small habit"
                  aria-label="Add a habit"
                />
                <button className="primary" onClick={handleAddHabit}>Add</button>
              </div>
            </section>
          )}

          {notice && <p role="status" className="notice-status">{notice}</p>}

          <footer className="footer">
            <span>You deserve support without judgment. One step at a time.</span>
            <span>EARLY ACCESS · 0.2</span>
          </footer>
        </div>
      </main>

      <nav className="mobile-nav" aria-label="Main navigation">
        {screens.map((screenOption, index) => (
          <button
            key={screenOption}
            className={screen === screenOption ? "mobile-nav-item active" : "mobile-nav-item"}
            onClick={() => setScreen(screenOption)}
          >
            <span>{screenIcons[index]}</span>
          </button>
        ))}
      </nav>
    </div>
  );
}





































































































































































































































































































