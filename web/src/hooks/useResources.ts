import { useEffect, useState } from "react";
import { localResources } from "../lib/plan";
import { Resource } from "../types";

export function useResources(defaultCountry = "US") {
  const [country, setCountry] = useState(defaultCountry);
  const [resources, setResources] = useState<Resource[]>(localResources);
  const [apiConnected, setApiConnected] = useState(false);
  const [online, setOnline] = useState(typeof navigator === "undefined" ? true : navigator.onLine);

  useEffect(() => {
    const onOnline = () => setOnline(navigator.onLine);
    window.addEventListener("online", onOnline);
    window.addEventListener("offline", onOnline);

    return () => {
      window.removeEventListener("online", onOnline);
      window.removeEventListener("offline", onOnline);
    };
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    let active = true;
    const base = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

    const fallback = () => {
      if (!active) return;
      setResources(localResources.filter((resource) => resource.country === country || resource.country === "GLOBAL"));
      setApiConnected(false);
    };

    const load = async () => {
      if (!base) {
        fallback();
        return;
      }

      try {
        const response = await fetch(base + "/api/v1/resources?country=" + encodeURIComponent(country), {
          headers: { Accept: "application/json" },
          signal: controller.signal,
        });

        if (!response.ok) throw new Error("API offline");

        const data = await response.json();
        if (active && Array.isArray(data)) {
          setResources(data);
          setApiConnected(true);
        }
      } catch {
        fallback();
      }
    };

    load();

    return () => {
      active = false;
      controller.abort();
    };
  }, [country]);

  return {
    country,
    setCountry,
    resources,
    apiConnected,
    online,
  };
}
