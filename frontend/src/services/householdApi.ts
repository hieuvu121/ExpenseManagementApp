import { apiRequest } from "./coreApi";

interface SplitRequestDTO {
  memberId: number;
  amount: number;
}

interface CreateExpenseRequestDTO {
  amount: number;
  date: string;
  category: string;
  currency: string;
  method: string;
  splits: SplitRequestDTO[];
}

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

  /**
   * Create a new expense in a household
   */
  createExpense: async (householdId: number | string, expenseData: CreateExpenseRequestDTO) => {
    const response = await apiRequest(`/households/${householdId}/expenses`, {
      method: "POST",
      body: JSON.stringify(expenseData),
    });
    return response.json();
  },
};