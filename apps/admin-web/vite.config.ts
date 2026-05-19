import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

const dpsAgentProxyTarget = process.env.VITE_DPS_AGENT_PROXY_TARGET ?? "http://localhost:4030";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/dps-agent": {
        target: dpsAgentProxyTarget,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/dps-agent/, "")
      }
    }
  }
});
