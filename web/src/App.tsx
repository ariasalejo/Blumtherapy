import { useEffect, useMemo, useState, useCallback } from "react";

type Screen = "home" | "plan" | "library" | "support" | "progress";
type Energy = "low" | "steady" | "high";
type Focus = "care" | "connection" | "learning" | "structure";
type Habit = { id: string; title: string; note: string; checkins: string[] };
type Resource = { id: string; title: string; summary: string; category: string; country: string; source_name: string; url: string; reviewed_on?: string };
type Plan = { title: string; minutes: number; steps: Array<{title:string;detail:string}>; rationale?:string; safety_note?:string };

const STORAGE = "blumtherapy.pwa.habits.v1";
const initialHabits: Habit[] = [
  { id:"body", title:"Care for your body", note:"Drink water, eat something nourishing, or rest", checkins:[] },
  { id:"move", title:"Move a little", note:"Stretch or walk gently for a few minutes", checkins:[] },
  { id:"connect", title:"Connect safely", note:"Send a message to someone you trust", checkins:[] },
  { id:"learn", title:"Feed your curiosity", note:"Read or learn one small thing", checkins:[] }
];
const localResources: Resource[] = [
  {id:"find-treatment",title:"FindTreatment.gov",summary:"Official U.S. treatment locator. Confirm services, cost, eligibility and current availability directly.",category:"Support",country:"US",source_name:"FindTreatment",url:"https://findtreatment.gov"},
  {id:"nhs",title:"NHS: getting help for drug addiction",summary:"Official information and starting points for support in the UK. Local pathways differ by nation.",category:"Support",country:"UK",source_name:"NHS",url:"https://www.nhs.uk"},
  {id:"health-canada",title:"Health Canada: treatment",summary:"Explore official treatment information and check province or territory-specific services.",category:"Support",country:"CA",source_name:"Health Canada",url:"https://www.canada.ca"},
  {id:"who",title:"WHO: treatment standards",summary:"International guidance for understanding evidence-based care. This is information, not a local service.",category:"Learning",country:"GLOBAL",source_name:"WHO",url:"https://www.who.int"},
];
const screens: Screen[] = ["home","plan","library","support","progress"];
const screenLabels = ["Home","My plan","Library","Support","My progress"];
const screenIcons = ["✳","◷","✧","♡","⌁"];
const todayKey = () => { const d=new Date(); return d.getFullYear()+"-"+String(d.getMonth()+1).padStart(2,"0")+"-"+String(d.getDate()).padStart(2,"0"); };
function readHabits(): Habit[] { try { const v=localStorage.getItem(STORAGE); return v ? JSON.parse(v) as Habit[] : initialHabits; } catch { return initialHabits; } }
function localPlan(energy:Energy,minutes:number,focus:Focus):Plan {
  const text: Record<Focus,{title:string;low:string;steady:string;high:string;close:string}> = {
    care:{title:"Return to your body",low:"Breathe comfortably and have some water.",steady:"Take a short pause and stretch gently.",high:"Take a walk or prepare something nourishing, if you can.",close:"Finish quietly with a few breaths."},
    connection:{title:"Connect without pressure",low:"Think of someone you feel safe with.",steady:"Send a short message to someone you trust.",high:"Suggest a call or a simple get-together.",close:"Check in briefly afterwards."},
    learning:{title:"Open a new window",low:"Read one paragraph or listen to something calm.",steady:"Spend a few minutes on a topic that interests you.",high:"Explore training or practise a skill.",close:"Summarize one idea you learned."},
    structure:{title:"Make room for what matters",low:"Choose one two-minute task.",steady:"Tidy one small space or prepare for tomorrow.",high:"Break one pending task into three clear steps.",close:"Plan one next step."}
  };
  const item=text[focus], first=energy==="low"?item.low:energy==="steady"?item.steady:item.high;
  const steps=[{title:"One possible step",detail:first}];
  if(energy!=="low"&&minutes>=8) steps.push({title:"A gentle close",detail:item.close});
  return {title:item.title,minutes:energy==="low"?Math.min(5,minutes):minutes,steps,rationale:"This suggestion uses only the choices made now. It does not diagnose or predict outcomes.",safety_note:"If you are in crisis, contact local emergency services or a crisis line."};
}

export default function App() {
  const [screen,setScreen]=useState<Screen>("home");
  const [habits,setHabits]=useState<Habit[]>(readHabits);
  const [newHabit,setNewHabit]=useState("");
  const [energy,setEnergy]=useState<Energy>("steady");
  const [minutes,setMinutes]=useState(10);
  const [focus,setFocus]=useState<Focus>("care");
  const [plan,setPlan]=useState<Plan|null>(null);
  const [planSource,setPlanSource]=useState<"local"|"api"|null>(null);
  const [busy,setBusy]=useState(false);
  const [country,setCountry]=useState("US");
  const [resources,setResources]=useState<Resource[]>(localResources);
  const [query,setQuery]=useState("");
  const [apiConnected,setApiConnected]=useState(false);
  const [online,setOnline]=useState(navigator.onLine);
  const [notice,setNotice]=useState("");

  // Memoize today's key once per render to avoid repeated date construction
  const today = useMemo(() => todayKey(), []);

  // Debounced localStorage write to avoid blocking renders on frequent changes
  useEffect(()=>{
    const id = setTimeout(()=>{
      try{ localStorage.setItem(STORAGE,JSON.stringify(habits)); }catch{/* keep in memory if storage is unavailable */}
    }, 300);
    return ()=>clearTimeout(id);
  },[habits]);

  // Online/offline listeners
  useEffect(()=>{const on=()=>setOnline(navigator.onLine);window.addEventListener("online",on);window.addEventListener("offline",on);return()=>{window.removeEventListener("online",on);window.removeEventListener("offline",on);};},[]);

  // Fetch resources with AbortController and safe cleanup
  useEffect(()=>{
    const controller = new AbortController();
    let active = true;
    const base=(import.meta.env.VITE_API_BASE_URL||"").replace(/\/$/,"");
    (async ()=>{
      if(!base) { if(active){ setResources(localResources.filter(r=>r.country===country||r.country==="GLOBAL")); setApiConnected(false); } return; }
      try{
        const res = await fetch(base+"/api/v1/resources?country="+encodeURIComponent(country),{headers:{Accept:"application/json"}, signal: controller.signal});
        if(!res.ok) throw new Error("API offline");
        const data = await res.json();
        if(active && Array.isArray(data)){ setResources(data); setApiConnected(true); }
      }catch(err){ if(active){ setResources(localResources.filter(r=>r.country===country||r.country==="GLOBAL")); setApiConnected(false); } }
    })();
    return ()=>{ active=false; controller.abort(); };
  },[country]);

  const checkedToday=useMemo(()=>habits.filter(h=>h.checkins.includes(today)).length,[habits,today]);

  // Optimize search: compute lowercase query once and check fields individually (avoids concatenating strings)
  const shownResources=useMemo(()=>{
    const q = query.trim().toLowerCase();
    if(!q) return resources;
    return resources.filter(r => {
      return r.title.toLowerCase().includes(q)
        || r.summary.toLowerCase().includes(q)
        || r.category.toLowerCase().includes(q)
        || r.country.toLowerCase().includes(q);
    });
  },[resources,query]);

  const checkHabit = useCallback((id:string)=>{
    setHabits(prev=>prev.map(h=>h.id!==id?h:{...h,checkins:h.checkins.includes(today)?h.checkins.filter(d=>d!==today):[...h.checkins,today]}));
  },[today]);

  const createPlan=useCallback(async()=>{
    const input={energy,time_minutes:minutes,focus,language:"en"};
    setPlan(localPlan(energy,minutes,focus));setPlanSource("local");setBusy(true);
    const base=(import.meta.env.VITE_API_BASE_URL||"").replace(/\/$/,"");
    if(!base){ setBusy(false); return; }

    const controller = new AbortController();
    const timeout = setTimeout(()=>controller.abort(), 8000);
    try{
      const res = await fetch(base+"/api/v1/plan/suggest",{method:"POST",headers:{"Content-Type":"application/json",Accept:"application/json"},body:JSON.stringify(input), signal: controller.signal});
      if(res.ok){const d=await res.json();if(typeof d.title==="string"&&Array.isArray(d.steps)){setPlan(d);setPlanSource("api");setApiConnected(true);}}
    }catch(e){ /* local fallback is already visible; habit history is never sent */ }
    finally{ clearTimeout(timeout); setBusy(false); }
  },[energy,minutes,focus]);

  const addHabit = useCallback(()=>{
    const title = newHabit.trim().slice(0,70);
    if(!title) return;
    setHabits(p=>[...p,{id:crypto.randomUUID(),title,note:"A small step you chose for yourself",checkins:[]}]);
    setNewHabit("");
    setNotice("Habit added. You can change it whenever you need.");
    const id = setTimeout(()=>setNotice(""), 3000);
    return ()=>clearTimeout(id);
  },[newHabit]);

  const title=screen==="home"?"Your space for today":screen==="plan"?"Build a gentler day":screen==="library"?"Ideas for your next chapter":screen==="support"?"Trusted help, without empty promises":"Your journey is not a race";

  return <div className="shell">
    <aside className="sidebar">
      <a className="brand" href="#" onClick={e=>{e.preventDefault();setScreen("home");}}><span className="brand-mark">b<span>✳</span></span><span><strong>BLUMTHERAPY</strong><small>GENTLE FORWARD</small></span></a>
      <p className="nav-caption">YOUR SPACE</p>
      <nav className="desktop-nav" aria-label="Main navigation">{screens.map((s,i)=><button key={s} className={screen===s?"nav-button active":"nav-button"} onClick={()=>setScreen(s)}><span>{screenIcons[i]}</span><small>{screenLabels[i]}</small></button>)}</nav>
      <div className="sidebar-note"><span>✧</span><p>You can start where you are. No streaks, no guilt, and no pressure to explain your story.</p><small>PRIVATE BY DEFAULT · HABITS STAY ON THIS DEVICE</small></div>
    </aside>
    <main id="main-content" className="main">
      <header className="topbar"><div className="crumb"><span>BLUMTHERAPY</span><b>/</b>{title}</div><div className="top-actions"><span className={online?"net-pill":"net-pill offline"}><i/>{online?"online":"offline"}</span><button className="icon" onClick={()=>setNotice("Help is private and local by default.")}>?</button></div></header>
      <div className="content">
        <section className="page-head"><div><p className="eyebrow">WELLBEING · HABITS · CONNECTION</p><h1>{title}</h1><p>{screen==="home"?"You do not need a perfect plan. Just one step that feels manageable.":""}</p></div></section>

        {screen==="home"&&<><section className="hero"><div className="hero-copy"><span className="live-kicker"><i/> A NEW DAY, WITHOUT PRESSURE</span><h2>You do not have to solve everything today.</h2></div></section>
          <div className="section-head"><div><h2>A little progress is still progress</h2><p>Check in with a habit if it helps. You can begin again any day.</p></div><button className="quiet-button">Manage habits</button></div>
          <section className="habit-list">{habits.slice(0,3).map(h=><button className={h.checkins.includes(today)?"habit-row checked":"habit-row"} key={h.id} onClick={()=>checkHabit(h.id)}><span>{h.title}</span></button>)}</section>
          <section className="summary-grid"><article className="panel soft-panel"><p className="panel-kicker">TODAY, YOUR WAY</p><div className="big-number">{checkedToday}<small> / {habits.length}</small></div></article></section>
        </>}

        {screen==="plan"&&<><section className="plan-grid"><article className="panel planner"><p className="panel-kicker">EXPLAINABLE DAILY-STEP ENGINE</p><h2>What feels possible today?</h2>
          <label>ENERGY YOU HAVE<select value={energy} onChange={e=>setEnergy(e.target.value as Energy)}><option value="low">Low — keep it tiny</option><option value="steady">Steady — a small steady step</option><option value="high">High — you can do more</option></select></label>
          <label>TIME AVAILABLE<select value={minutes} onChange={e=>setMinutes(Number(e.target.value))}><option value={5}>5 minutes</option><option value={10}>10 minutes</option><option value={20}>20 minutes</option></select></label>
          <label>WHAT WOULD YOU LIKE TO NURTURE<select value={focus} onChange={e=>setFocus(e.target.value as Focus)}><option value="care">Self-care</option><option value="connection">Connection</option><option value="learning">Learning</option><option value="structure">Structure</option></select></label>
          <button className="primary full" onClick={createPlan} disabled={busy}>{busy?"Shaping your plan…":"Create a gentle plan"} <span>↗</span></button><small className="privacy-note">Only the choices you make are used to suggest this plan.</small>
        </article><article className="panel generated"><div className="generated-icon">✳</div><p className="panel-kicker">YOUR NEXT POSSIBLE STEP</p>{plan?<><h2>{plan.title}</h2><span className="minutes">{plan.minutes} minutes</span></>:<p>No plan created yet.</p>}</article></section>
        </>}

        {(screen==="support"||screen==="library")&&<section><div className="resource-tools"><label className="search"><span>⌕</span><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search resources" /></label>
          <div className="resource-grid">{shownResources.filter(r=>screen==="support"?/support/i.test(r.category)||r.country!=="GLOBAL":/learning/i.test(r.category)||r.country==="GLOBAL").map((r)=> <article key={r.id} className="resource-card"><h3>{r.title}</h3><p>{r.summary}</p><small>{r.country} · {r.source_name}</small></article>)}</div>
          {shownResources.length===0&&<p className="empty-state">No matches yet. Try another search term.</p>}
          <p className="disclaimer">Resources are starting points, not a guarantee of eligibility, appointments, cost or immediate care. BlumTherapy is not a treatment provider or emergency service.</p>
        </section>}

        {screen==="progress"&&<section className="progress-grid"><article className="panel journey"><div className="journey-orb"><div><b>{checkedToday}</b><small>today</small></div></div><div><p>Your progress is private and resets when you choose.</p></div></article></section>}

        {notice&&<p role="status" className="notice-status">{notice}</p>}
        <footer className="footer"><span>You deserve support without judgment. One step at a time.</span><span>EARLY ACCESS · 0.2</span></footer>
      </div>
    </main>
    <nav className="mobile-nav" aria-label="Main navigation">{screens.map((s,i)=><button key={s} className={screen===s?"mobile-nav-item active":"mobile-nav-item"} onClick={()=>setScreen(s)}><span>{screenIcons[i]}</span></button>)}</nav>
  </div>;
}
