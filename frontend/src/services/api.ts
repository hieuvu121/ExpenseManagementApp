// Use relative path to leverage Vite dev server proxy during development.
// For production, set VITE_API_BASE_URL to the full backend URL.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/app/v1";

/**
 * Get authentication token from localStorage
 */
const getAuthToken = (): string | null => {
  return localStorage.getItem("authToken");
};

/**
 * Check if user is authenticated
 */
export const isAuthenticated = (): boolean => {
  return getAuthToken() !== null;
};

/**
 * Make authenticated API request
 */
const apiRequest = async (
  endpoint: string,
  options: RequestInit = {}
): Promise<Response> => {
  const token = getAuthToken();
  
  if (!token) {
    throw new Error("User is not authenticated. Please sign in.");
  }

  const headers = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
    ...options.headers,
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(errorData.message || `API request failed: ${response.statusText}`);
  }

  return response;
};

/**
 * API methods for household/group operations
 */
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

/**
 * Save auth token to localStorage
 */
export const saveAuthToken = (token: string): void => {
  localStorage.setItem("authToken", token);
};

/**
 * Remove auth token from localStorage
 */
export const removeAuthToken = (): void => {
  localStorage.removeItem("authToken");
};

