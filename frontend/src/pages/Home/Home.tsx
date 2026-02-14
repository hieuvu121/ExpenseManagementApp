import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router";
import PendingExpense from "../../components/displayBox/PendingExpense";
import MonthlySalesChart from "../../components/displayBox/MonthlyExpenseChart";
import StatisticsChart from "../../components/displayBox/StatisticsChart";
import RecentExpense from "../../components/displayBox/RecentExpense";
import PageMeta from "../../components/common/PageMeta";
import JoinGroupModal from "../../components/household/JoinGroupModal";
import CreateGroupModal from "../../components/household/CreateGroupModal";
import { useHousehold } from "../../context/HouseholdContext";


export default function Home() {
  const [isJoinModalOpen, setIsJoinModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const { households} = useHousehold();
  const navigate = useNavigate();
  const hasRedirected = useRef(false);

  // If user has no households after loading is complete, redirect to onboarding
 


  // Show loading state while fetching households


  return (
    <>
      <PageMeta
        title="React.js Ecommerce Dashboard | TailAdmin - React.js Admin Dashboard Template"
        description="This is React.js Ecommerce Dashboard page for TailAdmin - React.js Tailwind CSS Admin Dashboard Template"
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

          <MonthlySalesChart />
        </div>

        <div className="col-span-12 xl:col-span-5 flex">
          <RecentExpense />
        </div>

        <div className="col-span-12">
          <StatisticsChart />
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
