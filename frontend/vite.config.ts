import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import svgr from "vite-plugin-svgr";

// https://vite.dev/config/
export default defineConfig({
    base:"/TailAdmin/",
  plugins: [
    react(),
    svgr({
      svgrOptions: {
        icon: true,
        // This will transform your SVG to a React component
        exportType: "named",
        namedExport: "ReactComponent",
      },
    }),
  ],
  // Development server proxy to avoid CORS during local development ⚠️
  server: {
    proxy: {
      // Proxy any request starting with /app to the backend at localhost:8080
      // This ensures the browser talks to the Vite dev server (same origin)
      // and the dev server forwards the request to the backend, avoiding CORS.
      '/app': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
