import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/dps-agent": {
        target: "http://localhost:4030",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/dps-agent/, "")
      }
    }
  }
});
