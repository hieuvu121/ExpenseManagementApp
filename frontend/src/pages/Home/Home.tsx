import PendingExpense from "../../components/displayBox/PendingExpense";
import MonthlySalesChart from "../../components/displayBox/MonthlyExpenseChart";
import StatisticsChart from "../../components/displayBox/StatisticsChart";
import RecentExpense from "../../components/displayBox/RecentExpense";
import PageMeta from "../../components/common/PageMeta";

export default function Home() {
  return (
    <>
      <PageMeta
        title="React.js Ecommerce Dashboard | TailAdmin - React.js Admin Dashboard Template"
        description="This is React.js Ecommerce Dashboard page for TailAdmin - React.js Tailwind CSS Admin Dashboard Template"
      />
      <div className="grid grid-cols-12 gap-4 md:gap-6">
        <div className="col-span-12 space-y-6 xl:col-span-7">
          <PendingExpense />

          <MonthlySalesChart />
        </div>

        <div className="col-span-12 xl:col-span-5">
          <RecentExpense />
        </div>

        <div className="col-span-12">
          <StatisticsChart />
        </div>

      </div>
    </>
  );
}
