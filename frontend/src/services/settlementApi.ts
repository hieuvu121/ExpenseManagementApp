import { apiRequest } from "./coreApi";

export interface Settlement {
  id: number;
  fromMemberId: number;
  toMemberId: number;
  toMemberName?: string | null;
  expense_split_details_id: number;
  expenseCategory?: string | null;
  amount: number | string;
  date: string | null;
  currency: string | null;
  status: "PENDING" | "COMPLETED" | string;
}

export interface SettlementStats {
  pendingSettlements: Settlement[];
  totalPendingAmount: number | string;
}

export const settlementAPI = {
  getSettlements: async (memberId: number, householdId: number) => {
    const response = await apiRequest(`/settlements/${memberId}/${householdId}`);
    const data = (await response.json()) as {
      settlements?: Settlement[];
    };
    return data.settlements ?? [];
  },

  getCurrentMonthStats: async (memberId: number, householdId: number) => {
    const response = await apiRequest(
      `/settlements/pending/${memberId}/${householdId}/current-month`
    );
    const data = (await response.json()) as {
      data?: SettlementStats;
    };
    return (
      data.data ?? {
        pendingSettlements: [],
        totalPendingAmount: 0,
      }
    );
  },

  toggleSettlementStatus: async (settlementId: number, memberId: number) => {
    const response = await apiRequest(
      `/settlements/${settlementId}/toggle/${memberId}`,
      {
        method: "PUT",
      }
    );
    const data = (await response.json()) as {
      settlement?: Settlement;
    };
    if (!data.settlement) {
      throw new Error("Settlement not returned from server.");
    }
    return data.settlement;
  },
};
