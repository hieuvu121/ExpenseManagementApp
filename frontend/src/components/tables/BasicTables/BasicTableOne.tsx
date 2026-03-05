import { useEffect, useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../ui/table";

import Badge from "../../ui/badge/Badge";
import { useHousehold } from "../../../context/HouseholdContext";
import { householdAPI } from "../../../services/householdApi";
import Button from "../../ui/button/Button";
import { Modal } from "../../ui/modal";

interface Expense {
  id: number;
  title?: string;
  category?: string;
  description?: string;
  status?: string;
  method?: string;
  amount?: number | string;
  date?: string;
  currency?: string;
  createdBy?: string;
}

interface EditableExpense {
  amount: string;
  date: string;
  category: string;
  description: string;
  currency: string;
}

export default function BasicTableOne() {
  const { activeHousehold } = useHousehold();
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null);
  const [isDescriptionModalOpen, setIsDescriptionModalOpen] = useState(false);
  const [selectedDescription, setSelectedDescription] = useState<string>("");
  const [editingExpenseId, setEditingExpenseId] = useState<number | null>(null);
  const [editDraft, setEditDraft] = useState<EditableExpense | null>(null);

  const categoryOptions = ["FOOD", "TRANSPORT", "RENT", "BILLS", "SHOPPING", "ENTERTAINMENT", "OTHER"];
  const currencyOptions = ["AUD", "USD", "VND"];

  const isAdmin = localStorage.getItem("memberRole") === "ROLE_ADMIN";

  const refreshExpenses = async () => {
    if (!activeHousehold?.id) {
      setExpenses([]);
      return;
    }

    setLoading(true);
    try {
      const expData = await householdAPI.getHouseholdExpenses(activeHousehold.id);
      setExpenses(expData || []);
    } catch (err) {
      console.error("Failed to load household expenses:", err);
      setExpenses([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshExpenses();
  }, [activeHousehold?.id]);

  const formatDateForInput = (value?: string) => {
    if (!value) return "";
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) return "";
    return parsed.toISOString().slice(0, 10);
  };

  const startEditing = (expense: Expense) => {
    setActionError(null);
    setEditingExpenseId(expense.id);
    setEditDraft({
      amount: expense.amount !== undefined && expense.amount !== null ? String(expense.amount) : "",
      date: formatDateForInput(expense.date),
      category: expense.category || "FOOD",
      description: expense.description || "",
      currency: expense.currency || "AUD",
    });
  };

  const cancelEditing = () => {
    setEditingExpenseId(null);
    setEditDraft(null);
  };

  const saveEditing = async (expense: Expense) => {
    if (!activeHousehold?.id || !expense.id || !editDraft) {
      return;
    }

    const amountValue = Number(editDraft.amount);
    if (!Number.isFinite(amountValue) || amountValue <= 0) {
      setActionError("Amount must be greater than 0.");
      return;
    }

    const memberIdRaw = localStorage.getItem("memberId");
    const memberId = memberIdRaw ? Number(memberIdRaw) : null;
    if (!memberId || Number.isNaN(memberId)) {
      setActionError("Missing member information. Please re-login and try again.");
      return;
    }

    setActionError(null);
    setActionLoadingId(expense.id);
    try {
      await householdAPI.updateExpense(activeHousehold.id, expense.id, {
        amount: amountValue,
        date: editDraft.date,
        category: editDraft.category,
        description: editDraft.description,
        method: expense.method || "AMOUNT",
        currency: editDraft.currency,
        splits: [
          {
            memberId,
            amount: amountValue,
          },
        ],
      });

      cancelEditing();
      await refreshExpenses();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "Failed to update expense.");
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      <div className="max-w-full overflow-x-auto">
        <div className="min-w-[1102px]">
          <Table>
            {/* Table Header */}
            <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
              <TableRow>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Created By
                </TableCell>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Category
                </TableCell>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Currency
                </TableCell>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Date
                </TableCell>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Status
                </TableCell>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Description
                </TableCell>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Amount
                </TableCell>
                {isAdmin && (
                  <TableCell
                    isHeader
                    className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                  >
                    Actions
                  </TableCell>
                )}
              </TableRow>
            </TableHeader>

            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {loading ? (
                <TableRow>
                  <TableCell className="px-5 py-4 text-gray-800 dark:text-white/90">
                    Loading...
                  </TableCell>
                </TableRow>
              ) : expenses.length === 0 ? (
                <TableRow>
                  <TableCell className="px-5 py-4 text-gray-800 dark:text-white/90">
                    No expenses found for this household.
                  </TableCell>
                </TableRow>
              ) : (
                expenses.map((exp) => (
                  <TableRow key={exp.id}>
                    <TableCell className="px-5 py-4 sm:px-6 text-start">
                      <div>
                        <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                          {exp.createdBy || "-"}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {editingExpenseId === exp.id && editDraft ? (
                        <select
                          value={editDraft.category}
                          onChange={(e) => setEditDraft((prev) => (prev ? { ...prev, category: e.target.value } : prev))}
                          className="w-full rounded-md border border-gray-300 bg-white px-2 py-1 text-sm text-gray-700 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-200"
                        >
                          {categoryOptions.map((option) => (
                            <option key={option} value={option}>
                              {option}
                            </option>
                          ))}
                        </select>
                      ) : (
                        exp.category || "-"
                      )}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {editingExpenseId === exp.id && editDraft ? (
                        <select
                          value={editDraft.currency}
                          onChange={(e) => setEditDraft((prev) => (prev ? { ...prev, currency: e.target.value } : prev))}
                          className="w-full rounded-md border border-gray-300 bg-white px-2 py-1 text-sm text-gray-700 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-200"
                        >
                          {currencyOptions.map((option) => (
                            <option key={option} value={option}>
                              {option}
                            </option>
                          ))}
                        </select>
                      ) : (
                        exp.currency || "-"
                      )}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {editingExpenseId === exp.id && editDraft ? (
                        <input
                          type="date"
                          value={editDraft.date}
                          onChange={(e) => setEditDraft((prev) => (prev ? { ...prev, date: e.target.value } : prev))}
                          className="w-full rounded-md border border-gray-300 bg-white px-2 py-1 text-sm text-gray-700 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-200"
                        />
                      ) : (
                        exp.date ? new Date(exp.date).toLocaleDateString() : "-"
                      )}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      <Badge
                        size="sm"
                        color={
                          exp.status === "APPROVED"
                            ? "success"
                            : exp.status === "PENDING"
                              ? "warning"
                              : "error"
                        }
                      >
                        {exp.status || "-"}
                      </Badge>
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-theme-sm dark:text-gray-400">
                      {editingExpenseId === exp.id && editDraft ? (
                        <input
                          type="text"
                          value={editDraft.description}
                          onChange={(e) => setEditDraft((prev) => (prev ? { ...prev, description: e.target.value } : prev))}
                          className="w-full rounded-md border border-gray-300 bg-white px-2 py-1 text-sm text-gray-700 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-200"
                        />
                      ) : (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => {
                            setSelectedDescription(
                              exp.description && exp.description.trim().length > 0
                                ? exp.description
                                : "No description provided for this expense."
                            );
                            setIsDescriptionModalOpen(true);
                          }}
                        >
                          Description
                        </Button>
                      )}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-theme-sm dark:text-gray-400">
                      {editingExpenseId === exp.id && editDraft ? (
                        <input
                          type="number"
                          step="0.01"
                          min="0"
                          value={editDraft.amount}
                          onChange={(e) => setEditDraft((prev) => (prev ? { ...prev, amount: e.target.value } : prev))}
                          className="w-full rounded-md border border-gray-300 bg-white px-2 py-1 text-sm text-gray-700 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-200"
                        />
                      ) : (
                        typeof exp.amount === "number" ? exp.amount.toFixed(2) : exp.amount ?? "-"
                      )}
                    </TableCell>
                    {isAdmin && (
                      <TableCell className="px-4 py-3 text-gray-500 text-theme-sm dark:text-gray-400">
                        <div className="flex items-center gap-2">
                          {editingExpenseId === exp.id ? (
                            <>
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => saveEditing(exp)}
                                disabled={actionLoadingId === exp.id}
                              >
                                Save
                              </Button>
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={cancelEditing}
                                disabled={actionLoadingId === exp.id}
                              >
                                Cancel
                              </Button>
                            </>
                          ) : (
                            <>
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => startEditing(exp)}
                                disabled={actionLoadingId === exp.id}
                              >
                                Edit
                              </Button>
                              {exp.status === "PENDING" && (
                                <>
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={async () => {
                                  if (!activeHousehold?.id || !exp.id) {
                                    return;
                                  }
                                  setActionError(null);
                                  setActionLoadingId(exp.id);
                                  try {
                                    await householdAPI.approveExpense(activeHousehold.id, exp.id);
                                    await refreshExpenses();
                                  } catch (err) {
                                    setActionError(err instanceof Error ? err.message : "Failed to approve expense.");
                                  } finally {
                                    setActionLoadingId(null);
                                  }
                                }}
                                disabled={actionLoadingId === exp.id}
                              >
                                Approve
                              </Button>
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={async () => {
                                  if (!activeHousehold?.id || !exp.id) {
                                    return;
                                  }
                                  setActionError(null);
                                  setActionLoadingId(exp.id);
                                  try {
                                    await householdAPI.rejectExpense(activeHousehold.id, exp.id);
                                    await refreshExpenses();
                                  } catch (err) {
                                    setActionError(err instanceof Error ? err.message : "Failed to reject expense.");
                                  } finally {
                                    setActionLoadingId(null);
                                  }
                                }}
                                disabled={actionLoadingId === exp.id}
                              >
                                Reject
                              </Button>
                                </>
                              )}
                            </>
                          )}
                        </div>
                      </TableCell>
                    )}
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
          {actionError && (
            <div className="px-5 py-3 text-sm text-error-600 dark:text-error-400">
              {actionError}
            </div>
          )}

          <Modal
            isOpen={isDescriptionModalOpen}
            onClose={() => setIsDescriptionModalOpen(false)}
            className="max-w-[600px] m-4 p-6"
          >
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Expense Description</h3>
              <p className="text-sm text-gray-600 dark:text-gray-300 whitespace-pre-wrap break-words">
                {selectedDescription}
              </p>
              <div className="flex justify-end">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setIsDescriptionModalOpen(false)}
                >
                  Close
                </Button>
              </div>
            </div>
          </Modal>
        </div>
      </div>
    </div>
  );
}
