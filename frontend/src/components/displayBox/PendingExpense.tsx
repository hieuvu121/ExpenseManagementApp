import { BoxIconLine } from "../../icons";
import Badge from "../ui/badge/Badge";

export default function PendingTasksBox() {
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
                Needed admin approval
              </p>
            </div>
          </div>

          <Badge color="warning">3 pending</Badge>
        </div>

        {/* List */}
        <ul className="mt-5 space-y-3">
          <li className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3 dark:bg-gray-800/40">
            <div className="min-w-0">
              <p className="font-medium text-gray-800 truncate dark:text-white/90">
                Electricity bill
              </p>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                120$
              </p>
            </div>
            <button><Badge color="error">Declined</Badge></button>
            
          </li>

          <li className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3 dark:bg-gray-800/40">
            <div className="min-w-0">
              <p className="font-medium text-gray-800 truncate dark:text-white/90">
                Rice 
              </p>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                50$
              </p>
            </div>
            <Badge color="warning">Pending</Badge>
          </li>

          <li className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3 dark:bg-gray-800/40">
            <div className="min-w-0">
              <p className="font-medium text-gray-800 truncate dark:text-white/90">
                Drinks
              </p>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                100$
              </p>
            </div>
            <Badge color="success">Approved</Badge>
          </li>
        </ul>
      </div>
    </div>
  );
}
