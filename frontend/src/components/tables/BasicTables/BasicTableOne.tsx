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

interface Expense {
  id: number;
  title?: string;
  category?: string;
  status?: string;
  amount?: number | string;
  date?: string;
  currency?: string;
  createdBy?: string;
}
interface MemberDTO {
  memberId: number;
  fullName: string;
}

export default function BasicTableOne() {
  const { activeHousehold } = useHousehold();
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [members, setMembers] = useState<MemberDTO[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      if (!activeHousehold?.id) {
        setExpenses([]);
        setMembers([]);
        return;
      }

      setLoading(true);
      try {
        const [expData, memberData] = await Promise.all([
          householdAPI.getHouseholdExpenses(activeHousehold.id),
          householdAPI.getHouseholdMembers(activeHousehold.id),
        ]);

        setExpenses(expData || []);
        setMembers(memberData || []);
      } catch (err) {
        console.error("Failed to load household expenses or members:", err);
        setExpenses([]);
        setMembers([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [activeHousehold?.id]);

  // Helper to get payer name from response
  const getPayerName = (exp: Expense) => exp.createdBy || "-";

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
                  Amount
                </TableCell>
              </TableRow>
            </TableHeader>

              <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                {loading ? (
                <TableRow>
                  <TableCell className="px-5 py-4" colSpan={6}>
                    Loading...
                  </TableCell>
                </TableRow>
              ) : expenses.length === 0 ? (
                <TableRow>
                  <TableCell className="px-5 py-4" colSpan={6}>
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
                      {exp.category || "-"}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {exp.currency || "-"}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {exp.date ? new Date(exp.date).toLocaleDateString() : "-"}
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
                      {typeof exp.amount === "number" ? exp.amount.toFixed(2) : exp.amount ?? "-"}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>
    </div>
  );
}
