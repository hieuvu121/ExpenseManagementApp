import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import "swiper/swiper-bundle.css";
import "simplebar-react/dist/simplebar.min.css";
import App from "./App.tsx";
import { AppWrapper } from "./components/common/PageMeta.tsx";
import { ThemeProvider } from "./context/ThemeContext.tsx";
import { HouseholdProvider } from "./context/HouseholdContext.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider>
      <HouseholdProvider>
        <AppWrapper>
          <App />
        </AppWrapper>
      </HouseholdProvider>
    </ThemeProvider>
  </StrictMode>
);
