import { useState, useEffect, useCallback, useMemo } from "react";
import PendingExpense from "../../components/displayBox/PendingExpense";
import MonthlyExpenseChart from "../../components/displayBox/MonthlyExpenseChart";
import StatisticsChart from "../../components/displayBox/StatisticsChart";
import RecentExpense, { type RecentExpenseItem } from "../../components/displayBox/RecentExpense";
import PageMeta from "../../components/common/PageMeta";
import JoinGroupModal from "../../components/household/JoinGroupModal";
import CreateGroupModal from "../../components/household/CreateGroupModal";
import { useHousehold } from "../../context/HouseholdContext";
import { householdAPI } from "../../services/householdApi";


export default function Home() {
  const [isJoinModalOpen, setIsJoinModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const { households, activeHousehold } = useHousehold();
  const [expenses, setExpenses] = useState<(RecentExpenseItem & { status?: string | null })[]>([]);
  const [isExpensesLoading, setIsExpensesLoading] = useState(false);
  const [expensesError, setExpensesError] = useState<string | null>(null);

  const parseAmount = useCallback((value: number | string | null | undefined) => {
    if (value === null || value === undefined) return 0;
    const parsed = typeof value === "number" ? value : Number(value);
    return Number.isNaN(parsed) ? 0 : parsed;
  }, []);

  const loadExpenses = useCallback(async () => {
    if (!activeHousehold?.id) {
      setExpenses([]);
      return;
    }

    setIsExpensesLoading(true);
    setExpensesError(null);

    try {
      const data = (await householdAPI.getHouseholdExpenses(activeHousehold.id)) as (RecentExpenseItem & {
        status?: string | null;
      })[];
      setExpenses(data || []);
    } catch (err) {
      setExpensesError(err instanceof Error ? err.message : "Failed to load expenses.");
    } finally {
      setIsExpensesLoading(false);
    }
  }, [activeHousehold?.id]);

  useEffect(() => {
    loadExpenses();
  }, [loadExpenses]);

  const recentExpenses = useMemo(() => {
    const sorted = [...expenses].sort((a, b) => {
      const aDate = a.date ? new Date(a.date).getTime() : 0;
      const bDate = b.date ? new Date(b.date).getTime() : 0;
      return bDate - aDate;
    });
    return sorted.slice(0, 5);
  }, [expenses, parseAmount]);

  const monthlyTotals = useMemo(() => {
    const totals = Array.from({ length: 12 }, () => 0);
    expenses.forEach((expense) => {
      if (!expense.date) return;
      const month = new Date(expense.date).getMonth();
      if (Number.isNaN(month) || month < 0 || month > 11) return;
      totals[month] += parseAmount(expense.amount);
    });
    return totals;
  }, [expenses, parseAmount]);

  const dailyTotals = useMemo(() => {
    const totals = Array.from({ length: 7 }, () => 0);
    const now = new Date();
    const day = now.getDay();
    const diffToMonday = (day + 6) % 7;
    const startOfWeek = new Date(now);
    startOfWeek.setHours(0, 0, 0, 0);
    startOfWeek.setDate(now.getDate() - diffToMonday);
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    endOfWeek.setHours(23, 59, 59, 999);

    expenses.forEach((expense) => {
      if (!expense.date) return;
      const expenseDate = new Date(expense.date);
      if (Number.isNaN(expenseDate.getTime())) return;
      if (expenseDate < startOfWeek || expenseDate > endOfWeek) return;
      const weekIndex = (expenseDate.getDay() + 6) % 7;
      totals[weekIndex] += parseAmount(expense.amount);
    });

    return totals;
  }, [expenses, parseAmount]);

  const dailyApprovedTotals = useMemo(() => {
    const totals = Array.from({ length: 7 }, () => 0);
    const now = new Date();
    const day = now.getDay();
    const diffToMonday = (day + 6) % 7;
    const startOfWeek = new Date(now);
    startOfWeek.setHours(0, 0, 0, 0);
    startOfWeek.setDate(now.getDate() - diffToMonday);
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    endOfWeek.setHours(23, 59, 59, 999);

    expenses.forEach((expense) => {
      if (!expense.date) return;
      if (expense.status !== "APPROVED") return;
      const expenseDate = new Date(expense.date);
      if (Number.isNaN(expenseDate.getTime())) return;
      if (expenseDate < startOfWeek || expenseDate > endOfWeek) return;
      const weekIndex = (expenseDate.getDay() + 6) % 7;
      totals[weekIndex] += parseAmount(expense.amount);
    });

    return totals;
  }, [expenses, parseAmount]);

  const monthlyApprovedTotals = useMemo(() => {
    const totals = Array.from({ length: 12 }, () => 0);
    expenses.forEach((expense) => {
      if (!expense.date) return;
      if (expense.status !== "APPROVED") return;
      const month = new Date(expense.date).getMonth();
      if (Number.isNaN(month) || month < 0 || month > 11) return;
      totals[month] += parseAmount(expense.amount);
    });
    return totals;
  }, [expenses]);

  const chartsEmpty = expenses.length === 0;

  // If user has no households after loading is complete, redirect to onboarding
 


  // Show loading state while fetching households


  return (
    <>
      <PageMeta
        title="React.js Expense Dashboard | TailAdmin - React.js Admin Dashboard Template"
        description="This is React.js Expense Dashboard page for TailAdmin - React.js Tailwind CSS Admin Dashboard Template"
      />
      
      {/* Header Section with Join Group Button and Create Group Button */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-gray-800 dark:text-white/90">
            Dashboard
          </h1>
          {households.length > 0 && (
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              {households.length} {households.length === 1 ? "group" : "groups"} available
            </p>
          )}
        </div>
        <div className="flex flex-col gap-2 sm:flex-row sm:gap-3">
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Create Group
          </button>
          <button
            onClick={() => setIsJoinModalOpen(true)}
            className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Join Group
          </button>
        </div>
      </div>

      <div className="grid grid-cols-12 gap-4 md:gap-6">
        <div className="col-span-12 space-y-6 xl:col-span-7">
          <PendingExpense />

          <MonthlyExpenseChart
            data={dailyTotals}
            isLoading={isExpensesLoading}
            error={expensesError}
            isEmpty={chartsEmpty}
          />
        </div>

        <div className="col-span-12 xl:col-span-5 flex">
          <RecentExpense
            expenses={recentExpenses}
            isLoading={isExpensesLoading}
            error={expensesError}
          />
        </div>

        <div className="col-span-12">
          <StatisticsChart
            series={[
              { name: "Total", data: dailyTotals },
              { name: "Approved", data: dailyApprovedTotals },
            ]}
            isLoading={isExpensesLoading}
            error={expensesError}
            isEmpty={chartsEmpty}
          />
        </div>
      </div>

      {/* Join Group Modal */}
      <JoinGroupModal
        isOpen={isJoinModalOpen}
        onClose={() => setIsJoinModalOpen(false)}
      />

      {/* Create Group Modal */}
      <CreateGroupModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
      />
    </>
  );
}
