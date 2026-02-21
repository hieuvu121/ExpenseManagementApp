import { useEffect, useState } from "react";
import { BoxIconLine } from "../../icons";
import Badge from "../ui/badge/Badge";
import Button from "../ui/button/Button";
import { useHousehold } from "../../context/HouseholdContext";
import { householdAPI } from "../../services/householdApi";

interface ExpenseSummary {
  id: number;
  category?: string;
  amount?: number | string;
  currency?: string;
  status?: string;
}

const getStoredRole = () => localStorage.getItem("memberRole");

export default function PendingTasksBox() {
  const { activeHousehold } = useHousehold();
  const [pendingExpenses, setPendingExpenses] = useState<ExpenseSummary[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isAdmin = getStoredRole() === "ROLE_ADMIN";

  const loadPendingExpenses = async () => {
    if (!activeHousehold?.id) {
      setPendingExpenses([]);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const expenses = await householdAPI.getHouseholdExpenses(activeHousehold.id);
      const pending = (expenses || []).filter(
        (expense: ExpenseSummary) => expense.status === "PENDING"
      );
      setPendingExpenses(pending);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load pending expenses.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadPendingExpenses();
  }, [activeHousehold?.id]);

  const handleApprove = async (expenseId: number) => {
    if (!activeHousehold?.id) {
      return;
    }

    setError(null);
    try {
      await householdAPI.approveExpense(activeHousehold.id, expenseId);
      await loadPendingExpenses();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to approve expense.");
    }
  };

  const handleReject = async (expenseId: number) => {
    if (!activeHousehold?.id) {
      return;
    }

    setError(null);
    try {
      await householdAPI.rejectExpense(activeHousehold.id, expenseId);
      await loadPendingExpenses();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to reject expense.");
    }
  };

  return (
    <div className="grid grid-cols-1 gap-4 md:gap-6">
      <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-12 h-12 bg-gray-100 rounded-xl dark:bg-gray-800">
              <BoxIconLine className="text-gray-800 size-6 dark:text-white/90" />
            </div>

            <div>
              <h4 className="font-bold text-gray-800 text-title-sm dark:text-white/90">
                Pending Expenses
              </h4>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Awaiting admin approval
              </p>
            </div>
          </div>

          <Badge color="warning">
            {pendingExpenses.length} pending
          </Badge>
        </div>

        {error && (
          <p className="mt-4 text-sm text-error-600 dark:text-error-400">
            {error}
          </p>
        )}

        {/* List */}
        <ul className="mt-5 space-y-3">
          {isLoading ? (
            <li className="text-sm text-gray-500 dark:text-gray-400">Loading...</li>
          ) : pendingExpenses.length === 0 ? (
            <li className="text-sm text-gray-500 dark:text-gray-400">
              No pending expenses.
            </li>
          ) : (
            pendingExpenses.map((expense) => (
              <li
                key={expense.id}
                className="flex flex-col gap-3 rounded-xl bg-gray-50 px-4 py-3 dark:bg-gray-800/40 sm:flex-row sm:items-center sm:justify-between"
              >
                <div className="min-w-0">
                  <p className="font-medium text-gray-800 truncate dark:text-white/90">
                    {expense.category || "Untitled"}
                  </p>
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    {expense.currency ? `${expense.currency} ` : ""}
                    {expense.amount ?? "-"}
                  </p>
                </div>
                {isAdmin && (
                  <div className="flex items-center gap-2">
                    <Button size="sm" variant="outline" onClick={() => handleApprove(expense.id)}>
                      Approve
                    </Button>
                    <Button size="sm" variant="outline" onClick={() => handleReject(expense.id)}>
                      Reject
                    </Button>
                  </div>
                )}
              </li>
            ))
          )}
        </ul>
      </div>
    </div>
  );
}
