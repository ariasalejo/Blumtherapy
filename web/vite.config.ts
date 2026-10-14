import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig({
  base: "/Blumtherapy/",
  plugins: [
    react(),
    VitePWA({
      registerType: "autoUpdate",
      includeAssets: ["icon.svg"],
      manifest: {
        id: "/Blumtherapy/",
        name: "BlumTherapy — Wellbeing and gentle habits",
        short_name: "BlumTherapy",
        description: "Private, non-punitive daily steps and trusted support starting points.",
        lang: "en",
        start_url: "/Blumtherapy/",
        scope: "/Blumtherapy/",
        display: "standalone",
        orientation: "portrait-primary",
        background_color: "#f8f3ef",
        theme_color: "#352b3c",
        icons: [{ src: "/Blumtherapy/icon.svg", sizes: "any", type: "image/svg+xml", purpose: "any maskable" }]
      },
      workbox: {
        navigateFallback: "/Blumtherapy/index.html",
        globPatterns: ["**/*.{js,css,html,svg,woff2}"],
        cleanupOutdatedCaches: true,
        runtimeCaching: [{
          urlPattern: /\/api\/v1\/resources(?:\?.*)?$/,
          handler: "NetworkFirst",
          options: {
            cacheName: "blumtherapy-reviewed-public-resources",
            networkTimeoutSeconds: 3,
            expiration: { maxEntries: 40, maxAgeSeconds: 86400 },
            cacheableResponse: { statuses: [0, 200] }
          }
        }]
      }
    })
  ],
  server: { host: true, port: 5173, proxy: { "/api": { target: "http://127.0.0.1:8000", changeOrigin: true } } }
});