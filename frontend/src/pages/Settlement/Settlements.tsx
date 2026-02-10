import { useCallback, useEffect, useMemo, useState } from "react";
import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import PageMeta from "../../components/common/PageMeta";
import Badge from "../../components/ui/badge/Badge";
import Button from "../../components/ui/button/Button";
import { settlementAPI, type Settlement, type SettlementStats } from "../../services/settlementApi";

const getStoredNumber = (key: string): number | null => {
  const value = localStorage.getItem(key);
  if (!value) {
    return null;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
};

const formatAmount = (amount: number | string, currency?: string | null) => {
  const currencyLabel = currency ? `${currency} ` : "";
  return `${currencyLabel}${amount}`;
};

const formatOptional = (value?: string | null) => value || "-";

const statusBadgeColor = (status: string) => {
  if (status === "COMPLETED") {
    return "success";
  }
  if (status === "PENDING") {
    return "warning";
  }
  return "info";
};

export default function Settlements() {
  const [settlements, setSettlements] = useState<Settlement[]>([]);
  const [stats, setStats] = useState<SettlementStats>({
    pendingSettlements: [],
    totalPendingAmount: 0,
  });
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isToggling, setIsToggling] = useState<number | null>(null);

  const memberId = useMemo(() => getStoredNumber("memberId"), []);
  const householdId = useMemo(() => getStoredNumber("activeHouseholdId"), []);

  const loadSettlements = useCallback(async () => {
    if (!memberId || !householdId) {
      setError("Missing member or household information. Please sign in again.");
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const [settlementList, statsData] = await Promise.all([
        settlementAPI.getSettlements(memberId, householdId),
        settlementAPI.getCurrentMonthStats(memberId, householdId),
      ]);
      setSettlements(settlementList);
      setStats(statsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load settlements.");
    } finally {
      setIsLoading(false);
    }
  }, [memberId, householdId]);

  useEffect(() => {
    loadSettlements();
  }, [loadSettlements]);

  const handleToggleStatus = async (settlementId: number) => {
    if (!memberId) {
      setError("Missing member information. Please sign in again.");
      return;
    }

    setIsToggling(settlementId);
    setError(null);

    try {
      await settlementAPI.toggleSettlementStatus(settlementId, memberId);
      await loadSettlements();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update status.");
    } finally {
      setIsToggling(null);
    }
  };

  return (
    <>
      <PageMeta
        title="Settlements | Expense Management"
        description="View pending settlements and monthly statistics."
      />
      <PageBreadcrumb pageTitle="Settlements" />

      {error && (
        <div className="mb-6 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-600 dark:border-error-500/40 dark:bg-error-500/10 dark:text-error-400">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Current Month Pending
          </h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Settlements awaiting completion this month.
          </p>
          <div className="mt-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">Total pending</p>
              <p className="text-2xl font-semibold text-gray-800 dark:text-white/90">
                {formatAmount(stats.totalPendingAmount, stats.pendingSettlements[0]?.currency)}
              </p>
            </div>
            <Badge color="warning">
              {stats.pendingSettlements.length} pending
            </Badge>
          </div>
        </div>

        <div className="xl:col-span-2 rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
                Pending Settlements
              </h3>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Current month pending list
              </p>
            </div>
            {isLoading && (
              <span className="text-sm text-gray-400">Loading...</span>
            )}
          </div>

          <div className="mt-5 space-y-3">
            {stats.pendingSettlements.length === 0 && !isLoading ? (
              <p className="text-sm text-gray-500 dark:text-gray-400">
                No pending settlements found.
              </p>
            ) : (
              stats.pendingSettlements.map((settlement) => (
                <div
                  key={settlement.id}
                  className="flex flex-col gap-3 rounded-xl border border-gray-100 bg-gray-50 p-4 dark:border-gray-800 dark:bg-gray-900/40 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div>
                    <p className="text-sm font-medium text-gray-800 dark:text-white/90">
                      Settlement #{settlement.id}
                    </p>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      {formatAmount(settlement.amount, settlement.currency)}
                    </p>
                    <p className="text-xs text-gray-400 dark:text-gray-500">
                      To: {formatOptional(settlement.toMemberName)} • Category: {formatOptional(settlement.expenseCategory)}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <Badge color={statusBadgeColor(settlement.status)}>
                      {settlement.status}
                    </Badge>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleToggleStatus(settlement.id)}
                      disabled={isToggling === settlement.id}
                    >
                      {settlement.status === "PENDING"
                        ? "Mark completed"
                        : "Mark pending"}
                    </Button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      <div className="mt-6 rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
              All Settlements
            </h3>
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Toggle status between pending and completed.
            </p>
          </div>
          {isLoading && (
            <span className="text-sm text-gray-400">Loading...</span>
          )}
        </div>

        <div className="mt-5 overflow-x-auto">
          <table className="min-w-full border-collapse text-left">
            <thead>
              <tr className="border-b border-gray-200 text-sm text-gray-500 dark:border-gray-800 dark:text-gray-400">
                <th className="px-3 py-2 font-medium">ID</th>
                <th className="px-3 py-2 font-medium">Amount</th>
                <th className="px-3 py-2 font-medium">To</th>
                <th className="px-3 py-2 font-medium">Category</th>
                <th className="px-3 py-2 font-medium">Date</th>
                <th className="px-3 py-2 font-medium">Status</th>
                <th className="px-3 py-2 font-medium">Action</th>
              </tr>
            </thead>
            <tbody>
              {settlements.length === 0 && !isLoading ? (
                <tr>
                  <td
                    colSpan={7}
                    className="px-3 py-4 text-sm text-gray-500 dark:text-gray-400"
                  >
                    No settlements found.
                  </td>
                </tr>
              ) : (
                settlements.map((settlement) => (
                  <tr
                    key={settlement.id}
                    className="border-b border-gray-100 text-sm text-gray-700 dark:border-gray-800 dark:text-gray-300"
                  >
                    <td className="px-3 py-3">#{settlement.id}</td>
                    <td className="px-3 py-3">
                      {formatAmount(settlement.amount, settlement.currency)}
                    </td>
                    <td className="px-3 py-3">
                      {formatOptional(settlement.toMemberName)}
                    </td>
                    <td className="px-3 py-3">
                      {formatOptional(settlement.expenseCategory)}
                    </td>
                    <td className="px-3 py-3">
                      {settlement.date ?? "-"}
                    </td>
                    <td className="px-3 py-3">
                      <Badge color={statusBadgeColor(settlement.status)}>
                        {settlement.status}
                      </Badge>
                    </td>
                    <td className="px-3 py-3">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleToggleStatus(settlement.id)}
                        disabled={isToggling === settlement.id}
                      >
                        {settlement.status === "PENDING"
                          ? "Mark completed"
                          : "Mark pending"}
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}
