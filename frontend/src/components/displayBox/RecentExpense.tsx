import { useState } from "react";
import { Dropdown } from "../ui/dropdown/Dropdown";
import { DropdownItem } from "../ui/dropdown/DropdownItem";
import { MoreDotIcon } from "../../icons";

type ExpenseRow = {
  member: string;
  category: string;
  amount: number;
};

export default function RecentExpense() {
  const [isOpen, setIsOpen] = useState(false);

  const data: ExpenseRow[] = [
    { member: "Hieu", category: "Food", amount: 24.5 },
    { member: "Phong", category: "Transport", amount: 12.0 },
    
  ];

  function toggleDropdown() {
    setIsOpen((prev) => !prev);
  }

  function closeDropdown() {
    setIsOpen(false);
  }

  return (
    <div className="grid grid-cols-1 gap-4 md:gap-6 height-full w-full">
      <div className="h-full rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
              Recent Expense
            </h3>
            <p className="mt-1 text-gray-500 text-theme-sm dark:text-gray-400">
              Latest expense records
            </p>
          </div>

          <div className="relative inline-block">
            <button className="dropdown-toggle" onClick={toggleDropdown}>
              <MoreDotIcon className="text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 size-6" />
            </button>

            <Dropdown isOpen={isOpen} onClose={closeDropdown} className="w-40 p-2">
              <DropdownItem
                onItemClick={closeDropdown}
                className="flex w-full font-normal text-left text-gray-500 rounded-lg hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
              >
                View All
              </DropdownItem>
              <DropdownItem
                onItemClick={closeDropdown}
                className="flex w-full font-normal text-left text-gray-500 rounded-lg hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
              >
                Export
              </DropdownItem>
            </Dropdown>
          </div>
        </div>

        {/* Table */}
        <div className="mt-5 overflow-hidden rounded-xl border border-gray-200 dark:border-gray-800">
          {/* Table Head */}
          <div className="grid grid-cols-3 gap-3 bg-gray-50 px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:bg-white/5 dark:text-gray-400">
            <span>Member</span>
            <span>Category</span>
            <span className="text-right">Amount</span>
          </div>

          {/* Table Body: fixed min height (9 rows), but only render real rows */}
          <div
            className="divide-y divide-gray-200 dark:divide-gray-800 bg-white dark:bg-transparent"
          >
            {data.map((row, idx) => (
              <div
                key={idx}
                className="grid grid-cols-3 gap-3 px-4 py-3 text-sm"
              >
                <span className="font-medium text-gray-800 dark:text-white/90">
                  {row.member}
                </span>
                <span className="text-gray-500 dark:text-gray-400">
                  {row.category}
                </span>
                <span className="text-right font-semibold text-gray-800 dark:text-white/90">
                  ${row.amount.toFixed(2)}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
