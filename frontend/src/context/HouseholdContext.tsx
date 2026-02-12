import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { householdAPI, isAuthenticated } from "../services/api";

interface Household {
  id: number;
  name: string;
  code: string;
  role: string;
  memberId?: number;
}

interface HouseholdContextType {
  households: Household[];
  activeHousehold: Household | null;
  isLoading: boolean;
  error: string | null;
  refreshHouseholds: () => Promise<void>;
  setActiveHousehold: (household: Household | null) => void;
  addHousehold: (household: Household) => void;
  clearHouseholds: () => void;
}

const HouseholdContext = createContext<HouseholdContextType | undefined>(undefined);

export const HouseholdProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [households, setHouseholds] = useState<Household[]>([]);
  const [activeHousehold, setActiveHousehold] = useState<Household | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);


  useEffect(() => {
  refreshHouseholds().finally(() => setHasLoadedOnce(true));
}, []);


  /**
   * Fetch households from backend
   */
  const refreshHouseholds = async () => {
    setIsLoading(true);
    setError(null);

    if (!isAuthenticated()) {
      setHouseholds([]);
      setActiveHousehold(null);
      setIsLoading(false);
      return;
    }

    try {
      const data = await householdAPI.getMyHouseholds();
      setHouseholds(data);
      
      // Set first household as active if none is selected
      if (data.length > 0 && !activeHousehold) {
        setActiveHousehold(data[0]);
      } else if (data.length === 0) {
        setActiveHousehold(null);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch households");
      console.error("Error fetching households:", err);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Add a new household to the list (after joining)
   */
  const addHousehold = (household: Household) => {
    setHouseholds((prev) => {
      // Check if household already exists
      if (prev.some((h) => h.id === household.id)) {
        return prev;
      }
      return [...prev, household];
    });
    
    // Set the newly joined household as active
    setActiveHousehold(household);
  };

  /**
   * Clear households and active selection (used on login to force onboarding)
   */
  const clearHouseholds = () => {
    setHouseholds([]);
    setActiveHousehold(null);
    localStorage.removeItem("activeHouseholdId");
    localStorage.removeItem("memberId");
  };

  // Load households on mount and when authentication changes
  useEffect(() => {
    console.log("🔍 HouseholdProvider mounting, checking authentication...");
    if (isAuthenticated()) {
      console.log("✅ User is authenticated, fetching households...");
      refreshHouseholds();
    } else {
      console.log("❌ User is not authenticated");
      setIsLoading(false);
    }
  }, []);

  // Save active household to localStorage
  useEffect(() => {
    if (activeHousehold) {
      localStorage.setItem("activeHouseholdId", activeHousehold.id.toString());
      console.log("✅ Saved activeHouseholdId to localStorage:", activeHousehold.id);
      
      if (activeHousehold.memberId) {
        localStorage.setItem("memberId", activeHousehold.memberId.toString());
        console.log("✅ Saved memberId to localStorage:", activeHousehold.memberId);
      } else {
        console.warn("⚠️ No memberId in activeHousehold:", activeHousehold);
      }
    }
  }, [activeHousehold]);

  // Load active household from localStorage on mount
  useEffect(() => {
    const savedHouseholdId = localStorage.getItem("activeHouseholdId");
    if (savedHouseholdId && households.length > 0) {
      const household = households.find((h) => h.id.toString() === savedHouseholdId);
      if (household) {
        setActiveHousehold(household);
      }
    }
  }, [households]);

  return (
    <HouseholdContext.Provider
      value={{
        households,
        activeHousehold,
        isLoading,
        error,
        refreshHouseholds,
        setActiveHousehold,
        addHousehold,
        clearHouseholds,
      }}
    >
      {children}
    </HouseholdContext.Provider>
  );
};

export const useHousehold = () => {
  const context = useContext(HouseholdContext);
  if (context === undefined) {
    throw new Error("useHousehold must be used within a HouseholdProvider");
  }
  return context;
};

