import { useEffect, useMemo, useState } from "react";

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
  {id:"find-treatment",title:"FindTreatment.gov",summary:"Official U.S. treatment locator. Confirm services, cost, eligibility and current availability directly.",category:"Support",country:"US",source_name:"U.S. Government",url:"https://findtreatment.gov/"},
  {id:"nhs",title:"NHS: getting help for drug addiction",summary:"Official information and starting points for support in the UK. Local pathways differ by nation.",category:"Support",country:"UK",source_name:"NHS",url:"https://www.nhs.uk/live-well/addiction-support/drug-addiction-getting-help/"},
  {id:"health-canada",title:"Health Canada: treatment",summary:"Explore official treatment information and check province or territory-specific services.",category:"Support",country:"CA",source_name:"Government of Canada",url:"https://www.canada.ca/en/health-canada/services/substance-use/treatment.html"},
  {id:"who",title:"WHO: treatment standards",summary:"International guidance for understanding evidence-based care. This is information, not a local service.",category:"Learning",country:"GLOBAL",source_name:"World Health Organization",url:"https://www.who.int/publications/i/item/international-standards-for-the-treatment-of-drug-use-disorders"}
];
const screens: Screen[] = ["home","plan","library","support","progress"];
const screenLabels = ["Home","My plan","Library","Support","My progress"];
const screenIcons = ["✳","◷","✧","♡","⌁"];
const todayKey = () => { const d=new Date(); return d.getFullYear()+"-"+String(d.getMonth()+1).padStart(2,"0")+"-"+String(d.getDate()).padStart(2,"0"); };
function readHabits(): Habit[] { try { const v=localStorage.getItem(STORAGE); return v ? JSON.parse(v) as Habit[] : initialHabits; } catch { return initialHabits; } }
function localPlan(energy:Energy,minutes:number,focus:Focus):Plan {
  const text: Record<Focus,{title:string;low:string;steady:string;high:string;close:string}> = {
    care:{title:"Return to your body",low:"Breathe comfortably and have some water.",steady:"Take a short pause and stretch gently.",high:"Take a walk or prepare something nourishing, if you can.",close:"Choose only what feels safe and possible."},
    connection:{title:"Connect without pressure",low:"Think of someone you feel safe with.",steady:"Send a short message to someone you trust.",high:"Suggest a call or a simple get-together.",close:"You do not need to explain more than you want."},
    learning:{title:"Open a new window",low:"Read one paragraph or listen to something calm.",steady:"Spend a few minutes on a topic that interests you.",high:"Explore training or practise a skill.",close:"Stopping after a few minutes is okay, too."},
    structure:{title:"Make room for what matters",low:"Choose one two-minute task.",steady:"Tidy one small space or prepare for tomorrow.",high:"Break one pending task into three clear steps.",close:"The plan can change whenever you need."}
  };
  const item=text[focus], first=energy==="low"?item.low:energy==="steady"?item.steady:item.high;
  const steps=[{title:"One possible step",detail:first}];
  if(energy!=="low"&&minutes>=8) steps.push({title:"A gentle close",detail:item.close});
  return {title:item.title,minutes:energy==="low"?Math.min(5,minutes):minutes,steps,rationale:"This suggestion uses only the choices made now. It does not diagnose or predict outcomes.",safety_note:"You can change or ignore any step. This is not treatment or emergency support."};
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

  useEffect(()=>{try{localStorage.setItem(STORAGE,JSON.stringify(habits));}catch{/* keep in memory if storage is unavailable */}},[habits]);
  useEffect(()=>{const on=()=>setOnline(navigator.onLine);window.addEventListener("online",on);window.addEventListener("offline",on);return()=>{window.removeEventListener("online",on);window.removeEventListener("offline",on);};},[]);
  useEffect(()=>{
    let active=true;
    const base=(import.meta.env.VITE_API_BASE_URL||"").replace(/\/$/,"");
    fetch(base+"/api/v1/resources?country="+encodeURIComponent(country),{headers:{Accept:"application/json"}})
      .then(r=>{if(!r.ok)throw new Error("API offline");return r.json();})
      .then((data:Resource[])=>{if(active&&Array.isArray(data)){setResources(data);setApiConnected(true);}})
      .catch(()=>{if(active){setResources(localResources.filter(r=>r.country===country||r.country==="GLOBAL"));setApiConnected(false);}});
    return()=>{active=false;};
  },[country]);
  const checkedToday=useMemo(()=>habits.filter(h=>h.checkins.includes(todayKey())).length,[habits]);
  const shownResources=useMemo(()=>resources.filter(r=>(r.title+" "+r.summary+" "+r.category+" "+r.country).toLowerCase().includes(query.toLowerCase())),[resources,query]);
  const checkHabit=(id:string)=>setHabits(prev=>prev.map(h=>h.id!==id?h:{...h,checkins:h.checkins.includes(todayKey())?h.checkins.filter(d=>d!==todayKey()):[...h.checkins,todayKey()]}));
  const createPlan=async()=>{
    const input={energy,time_minutes:minutes,focus,language:"en"};
    setPlan(localPlan(energy,minutes,focus));setPlanSource("local");setBusy(true);
    try{
      const base=(import.meta.env.VITE_API_BASE_URL||"").replace(/\/$/,"");
      const res=await fetch(base+"/api/v1/plan/suggest",{method:"POST",headers:{"Content-Type":"application/json",Accept:"application/json"},body:JSON.stringify(input)});
      if(res.ok){const d=await res.json();if(typeof d.title==="string"&&Array.isArray(d.steps)){setPlan(d);setPlanSource("api");setApiConnected(true);}}
    }catch{/* local fallback is already visible; habit history is never sent */}
    finally{setBusy(false);}
  };
  const addHabit=()=>{const title=newHabit.trim().slice(0,70);if(!title)return;setHabits(p=>[...p,{id:crypto.randomUUID(),title,note:"A small step you chose for yourself",checkins:[]}]);setNewHabit("");setNotice("Habit added. You can change it whenever you need.");};
  const title=screen==="home"?"Your space for today":screen==="plan"?"Build a gentler day":screen==="library"?"Ideas for your next chapter":screen==="support"?"Trusted help, without empty promises":"Your journey is not a race";

  return <div className="shell">
    <aside className="sidebar">
      <a className="brand" href="#" onClick={e=>{e.preventDefault();setScreen("home");}}><span className="brand-mark">b<span>✳</span></span><span><strong>BLUMTHERAPY</strong><small>GENTLE FORWARD MOTION</small></span></a>
      <p className="nav-caption">YOUR SPACE</p>
      <nav className="desktop-nav" aria-label="Main navigation">{screens.map((s,i)=><button key={s} className={screen===s?"nav-button active":"nav-button"} onClick={()=>setScreen(s)}><span>{screenIcons[i]}</span>{screenLabels[i]}{screen===s&&<i/>}</button>)}</nav>
      <div className="sidebar-note"><span>✧</span><p>You can start where you are. No streaks, no guilt, and no pressure to explain your story.</p><small>PRIVATE BY DEFAULT · HABITS STAY ON THIS DEVICE</small></div>
    </aside>
    <main id="main-content" className="main">
      <header className="topbar"><div className="crumb"><span>BLUMTHERAPY</span><b>/</b>{title}</div><div className="top-actions"><span className={online?"net-pill":"net-pill offline"}><i/>{online?"Online":"Offline mode"}</span><span className="profile-mark">✧</span></div></header>
      <div className="content">
        <section className="page-head"><div><p className="eyebrow">WELLBEING · HABITS · CONNECTION</p><h1>{title}</h1><p>{screen==="home"?"You do not need a perfect plan. Just one step that feels possible.":screen==="plan"?"Choose the time and energy you have. A useful plan should adapt to you, not the other way around.":screen==="library"?"Trusted starting points and gentle practices to explore at your own pace.":screen==="support"?"Official resources are starting points. Check local services, cost and eligibility directly.":"Notice what you tried, not what you missed."}</p></div><span className="date-pill">{new Date().toLocaleDateString("en-US",{weekday:"short",month:"short",day:"numeric"})}</span></section>

        {screen==="home"&&<><section className="hero"><div className="hero-copy"><span className="live-kicker"><i/> A NEW DAY, WITHOUT PRESSURE</span><h2>You do not have to solve everything today.</h2><p>A calm space to rebuild routines, explore trusted support and move forward without judgment.</p><button className="primary" onClick={()=>setScreen("plan")}>Shape my day <span>↗</span></button><small>Rest counts. Starting over is allowed.</small></div><div className="hero-art" aria-hidden="true"><div className="halo h1"/><div className="halo h2"/><div className="orbit"/><div className="seed">✳</div><span className="art-caption">GROW AT YOUR PACE</span></div></section>
          <div className="section-head"><div><h2>A little progress is still progress</h2><p>Check in with a habit if it helps. You can begin again any day.</p></div><button className="quiet-button" onClick={()=>setScreen("plan")}>Open my plan ↗</button></div>
          <section className="habit-list">{habits.slice(0,3).map(h=><button className={h.checkins.includes(todayKey())?"habit-row checked":"habit-row"} key={h.id} onClick={()=>checkHabit(h.id)}><span className="habit-mark">{h.checkins.includes(todayKey())?"✓":"✳"}</span><span className="habit-copy"><b>{h.title}</b><small>{h.note}</small></span><span className="habit-action">{h.checkins.includes(todayKey())?"Done":"+"}</span></button>)}</section>
          <section className="summary-grid"><article className="panel soft-panel"><p className="panel-kicker">TODAY, YOUR WAY</p><div className="big-number">{checkedToday}<small> / {habits.length}</small></div><p>habits checked in today</p><div className="meter"><span style={{width:(habits.length?checkedToday/habits.length*100:0)+"%"}}/></div><button className="quiet-button" onClick={()=>setScreen("progress")}>See my journey ↗</button></article><article className="panel quote-panel"><span className="quote-symbol">✧</span><div><b>A note to keep close</b><p>A difficult day does not erase the effort you have already made. You can choose a smaller step, pause, or ask for support.</p><button className="quiet-button" onClick={()=>setScreen("support")}>Explore support ↗</button></div></article></section>
        </>}

        {screen==="plan"&&<><section className="plan-grid"><article className="panel planner"><p className="panel-kicker">EXPLAINABLE DAILY-STEP ENGINE</p><h2>What feels possible today?</h2><p className="muted">These choices adjust the size of a step. They do not assess, diagnose or score you.</p>
          <label>ENERGY YOU HAVE<select value={energy} onChange={e=>setEnergy(e.target.value as Energy)}><option value="low">Low — keep it tiny</option><option value="steady">Steady — a small try</option><option value="high">Energised — more room</option></select></label>
          <label>TIME AVAILABLE<select value={minutes} onChange={e=>setMinutes(Number(e.target.value))}><option value={5}>5 minutes</option><option value={10}>10 minutes</option><option value={20}>20 minutes</option></select></label>
          <label>WHAT WOULD YOU LIKE TO NURTURE<select value={focus} onChange={e=>setFocus(e.target.value as Focus)}><option value="care">Self-care</option><option value="connection">Connection</option><option value="learning">Learning</option><option value="structure">Structure</option></select></label>
          <button className="primary full" onClick={createPlan} disabled={busy}>{busy?"Shaping your plan…":"Create a gentle plan"} <span>↗</span></button><small className="privacy-note">Only energy, time, focus and language are sent to the API. Your habit history never leaves this device.</small>
        </article><article className="panel generated"><div className="generated-icon">✳</div><p className="panel-kicker">YOUR NEXT POSSIBLE STEP</p>{plan?<><h2>{plan.title}</h2><span className="duration">◷ {plan.minutes} minutes, approximately · {planSource==="api"?"API connected":"available locally"}</span><ol>{plan.steps.map((s,i)=><li key={s.title+i}><span>{String(i+1).padStart(2,"0")}</span><div><b>{s.title}</b><p>{s.detail}</p></div></li>)}</ol><p className="generated-note">{plan.rationale} {plan.safety_note}</p></>:<><h2>Your plan begins with your choices.</h2><p className="muted">Choose an energy level, time and focus, then generate a plan. It will work offline too.</p><button className="secondary" onClick={createPlan}>Build my first plan</button></>}</article></section>
          <section className="panel habits-manager"><div className="section-head compact"><div><p className="panel-kicker">HABITS YOU CHOOSE</p><h2>Build a rhythm, not a streak</h2></div><span className="count-pill">{habits.length} habits</span></div><div className="add-row"><input value={newHabit} onChange={e=>setNewHabit(e.target.value)} maxLength={70} placeholder="Add one small habit…" onKeyDown={e=>{if(e.key==="Enter")addHabit();}}/><button className="secondary" onClick={addHabit}>Add +</button></div>{habits.map(h=><div className="manager-row" key={h.id}><button className={h.checkins.includes(todayKey())?"check done":"check"} onClick={()=>checkHabit(h.id)}>{h.checkins.includes(todayKey())?"✓":"○"}</button><div><b>{h.title}</b><small>{h.note}</small></div><button className="delete" aria-label={"Delete "+h.title} onClick={()=>setHabits(prev=>prev.filter(x=>x.id!==h.id))}>×</button></div>)}</section>
        </>}

        {(screen==="support"||screen==="library")&&<section><div className="resource-tools"><label className="search"><span>⌕</span><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search country, topic or resource…"/></label><label className="country-select">REGION<select value={country} onChange={e=>setCountry(e.target.value)}><option value="US">United States</option><option value="UK">United Kingdom</option><option value="CA">Canada</option><option value="GLOBAL">International resources</option></select></label></div><div className="api-status"><span className={apiConnected?"status-dot good":"status-dot"}/>{apiConnected?"Connected to reviewed public resources API":"Showing local fallback resources"} · {online?"Network available":"Offline"}</div>
          <div className="resource-grid">{shownResources.filter(r=>screen==="support"?/support/i.test(r.category)||r.country!=="GLOBAL":/learning/i.test(r.category)||r.country==="GLOBAL").map((r,i)=><article className="resource-card" key={r.id}><div className={"resource-icon tone-"+(i%4)}>{["♡","✧","↗","⌘"][i%4]}</div><div className="resource-meta"><span>{r.category}</span><span>{r.country}</span></div><h3>{r.title}</h3><p>{r.summary}</p><div className="source-line">{r.source_name}{r.reviewed_on?" · reviewed "+r.reviewed_on:""}</div><a href={r.url} target="_blank" rel="noopener noreferrer">Open official source ↗</a></article>)}</div>
          {shownResources.length===0&&<p className="empty-state">No matches yet. Try another search term.</p>}
          <p className="disclaimer">Resources are starting points, not a guarantee of eligibility, appointments, cost or immediate care. BlumTherapy is not a treatment provider or emergency service.</p>
        </section>}

        {screen==="progress"&&<section className="progress-grid"><article className="panel journey"><div className="journey-orb"><div><b>{checkedToday}</b><small>today</small></div></div><div><p className="panel-kicker">WITHOUT COMPARISON</p><h2>Your progress is not erased</h2><p className="muted">No streak scores and no penalties for rest. This summary reflects only the check-ins you chose to save.</p></div></article><article className="panel"><div className="section-head compact"><div><p className="panel-kicker">LAST SEVEN DAYS</p><h2>Small signals, not grades</h2></div></div><div className="week-bars">{Array.from({length:7},(_,i)=>{const d=new Date();d.setDate(d.getDate()-(6-i));const key=d.getFullYear()+"-"+String(d.getMonth()+1).padStart(2,"0")+"-"+String(d.getDate()).padStart(2,"0");const n=habits.filter(h=>h.checkins.includes(key)).length;return <div className="week-day" key={key}><div className="bar-track"><span style={{height:(habits.length?Math.max(8,n/habits.length*100):8)+"%"}}/></div><small>{d.toLocaleDateString("en-US",{weekday:"short"}).slice(0,3)}</small><b>{n}</b></div>;})}</div><p className="muted fine">Blank days are not failures. Rest and support are part of life, too.</p></article><article className="panel"><p className="panel-kicker">YOUR HABITS</p>{habits.map(h=><div className="history-row" key={h.id}><span className={h.checkins.includes(todayKey())?"check-mini done":"check-mini"}>{h.checkins.includes(todayKey())?"✓":"·"}</span><div><b>{h.title}</b><small>{h.checkins.length} saved check-ins</small></div></div>)}</article></section>}

        {notice&&<p role="status" className="notice-status">{notice}</p>}
        <footer className="footer"><span>You deserve support without judgment. One step at a time.</span><span>EARLY ACCESS · 0.2</span></footer>
      </div>
    </main>
    <nav className="mobile-nav" aria-label="Main navigation">{screens.map((s,i)=><button key={s} className={screen===s?"mobile-nav-item active":"mobile-nav-item"} onClick={()=>setScreen(s)}><span>{screenIcons[i]}</span><small>{screenLabels[i]}</small></button>)}</nav>
  </div>;
}