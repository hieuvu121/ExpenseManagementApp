import { apiRequest } from "./coreApi";

export const householdAPI = {
  /**
   * Get all households for current user
   */
  getMyHouseholds: async () => {
    const response = await apiRequest("/households/my");
    return response.json();
  },

  /**
   * Join a household using invite code
   */
  joinHousehold: async (code: string) => {
    const response = await apiRequest("/households/join", {
      method: "POST",
      body: JSON.stringify({ code }),
    });
    return response.json();
  },

  /**
   * Create a new household
   */
  createHousehold: async (name: string) => {
    const response = await apiRequest("/households/create", {
      method: "POST",
      body: JSON.stringify({ name }),
    });
    return response.json();
  },
};