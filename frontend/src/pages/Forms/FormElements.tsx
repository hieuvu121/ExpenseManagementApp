import { useMemo, useState } from "react";
import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import PageMeta from "../../components/common/PageMeta";
import ComponentCard from "../../components/common/ComponentCard";

import Label from "../../components/form/Label";
import Input from "../../components/form/input/InputField";
import Select from "../../components/form/Select";
import MultiSelect from "../../components/form/MultiSelect";
import Radio from "../../components/form/input/Radio";
import TextArea from "../../components/form/input/TextArea";

import DropzoneComponent from "../../components/form/form-elements/DropZone";
import { useHousehold } from "../../context/HouseholdContext";
import { householdAPI } from "../../services/householdApi";

type SplitMethod = "EQUAL" | "AMOUNT";

export default function AddExpense() {
  const { activeHousehold } = useHousehold();
  
  const members = useMemo(
    () => [
      { id: 1, name: "Dung" },
      { id: 2, name: "Hieu" },
      { id: 3, name: "Phong" },
    ],
    []
  );

  const today = new Date().toISOString().slice(0, 10);

  // Options
  const currencyOptions = useMemo(
    () => [
      { value: "AUD", label: "AUD" },
      { value: "VND", label: "VND" },
      { value: "USD", label: "USD" },
    ],
    []
  );

  const categoryOptions = useMemo(
    () => [
      { value: "FOOD", label: "Food" },
      { value: "TRANSPORT", label: "Transport" },
      { value: "RENT", label: "Rent" },
      { value: "BILLS", label: "Bills" },
      { value: "SHOPPING", label: "Shopping" },
      { value: "ENTERTAINMENT", label: "Entertainment" },
      { value: "OTHER", label: "Other" },
    ],
    []
  );


  const memberSelectOptions = useMemo(
    () => members.map((m) => ({ value: String(m.id), label: m.name })),
    [members]
  );

  const memberMultiOptions = useMemo(
    () =>
      members.map((m) => ({
        value: String(m.id),
        text: m.name,
        selected: false,
      })),
    [members]
  );

  // Form state
  const [title, setTitle] = useState("");
  const [amount, setAmount] = useState<string>("");
  const [currency, setCurrency] = useState("AUD");
  const [date, setDate] = useState(today);
  const [category, setCategory] = useState("FOOD");
  const [paidBy, setPaidBy] = useState<string>(String(members[0]?.id ?? 1));
  const [participants, setParticipants] = useState<string[]>(
    members.map((m) => String(m.id))
  );
  const [splitMethod, setSplitMethod] = useState<SplitMethod>("EQUAL");
  const [note, setNote] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Dynamic split values theo participant
  const [splitValues, setSplitValues] = useState<Record<string, number>>({});

  const selectedParticipants = useMemo(
    () => members.filter((m) => participants.includes(String(m.id))),
    [members, participants]
  );

  const setSplitValue = (userId: number | string, value: string) => {
    const num = Number(value);
    const key = String(userId);
    setSplitValues((prev) => ({
      ...prev,
      [key]: Number.isFinite(num) ? num : 0,
    }));
  };

  /**
   * Calculate split amounts based on split method
   */
  const calculateSplits = (): Array<{ memberId: number; amount: number }> => {
    const amountNum = Number(amount);
    const participantIds = selectedParticipants.map((p) => p.id);
    const splitCount = participantIds.length;

    if (splitMethod === "EQUAL") {
      return participantIds.map((memberId) => ({
        memberId,
        amount: Math.round((amountNum / splitCount) * 100) / 100,
      }));
    }

    if (splitMethod === "AMOUNT") {
      return participantIds.map((memberId) => ({
        memberId,
        amount: splitValues[String(memberId)] || 0,
      }));
    }

    return [];
  };

  // Basic validation
  const amountNumber = Number(amount);
  const isAmountValid = Number.isFinite(amountNumber) && amountNumber > 0;
  const isTitleValid = title.trim().length > 0;
  const isParticipantsValid = participants.length > 0;

  const canSubmit =
    isTitleValid &&
    isAmountValid &&
    isParticipantsValid &&
    activeHousehold &&
    !isLoading;

  const handleSubmit = async () => {
    if (!canSubmit) return;

    setErrorMessage(null);
    setSuccessMessage(null);
    setIsLoading(true);

    try {
      if (!activeHousehold) {
        throw new Error("No active household selected");
      }

      const splits = calculateSplits();

      const expenseData = {
        amount: amountNumber,
        date,
        category,
        method: splitMethod,
        currency,
        splits,
      };

      const response = await householdAPI.createExpense(
        activeHousehold.id,
        expenseData
      );

      setSuccessMessage(
        `Expense created successfully! ID: ${response.id}`
      );

      // Reset form
      setTitle("");
      setAmount("");
      setCurrency("AUD");
      setDate(today);
      setCategory("FOOD");
      setPaidBy(String(members[0]?.id ?? 1));
      setParticipants(members.map((m) => String(m.id)));
      setSplitMethod("EQUAL");
      setSplitValues({});
      setNote("");

      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (error) {
      const errorMsg =
        error instanceof Error ? error.message : "Failed to create expense";
      setErrorMessage(errorMsg);
      console.error("Create expense error:", error);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div>
      <PageMeta
        title="Add Expense | Expense Management"
        description="Create a new expense"
      />
      <PageBreadcrumb pageTitle="Add Expense" />

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        {/* LEFT: Core info */}
        <div className="space-y-6">
          <ComponentCard title="Expense Details">
            <div className="space-y-6">
              {/* Title */}
              <div>
                <Label>Description</Label>
                <Input
                  type="text"
                  placeholder="Dinner, Groceries, Electricity bill..."
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                />
                {!isTitleValid && (
                  <p className="mt-2 text-sm text-red-500">
                    Title is required.
                  </p>
                )}
              </div>

              {/* Amount + Currency */}
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <Label>Amount</Label>
                  <Input
                    type="number"
                    placeholder="0"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                  />
                  {!isAmountValid && (
                    <p className="mt-2 text-sm text-red-500">
                      Amount must be greater than 0.
                    </p>
                  )}
                </div>

                <div>
                  <Label>Currency</Label>
                  <Select
                    options={currencyOptions}
                    placeholder="Select currency"
                    onChange={(v) => setCurrency(v)}
                    className="dark:bg-dark-900"
                  />
                </div>
              </div>

              {/* Date + Category */}
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <Label>Date</Label>
                  <Input
                    type="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                  />
                </div>

                <div>
                  <Label>Category</Label>
                  <Select
                    options={categoryOptions}
                    placeholder="Select category"
                    onChange={(v) => setCategory(v)}
                    className="dark:bg-dark-900"
                  />
                </div>

              
              </div>


              {/* Paid by */}
              <div>
                <Label>Paid by</Label>
                <Select
                  options={memberSelectOptions}
                  placeholder="Select payer"
                  onChange={(v) => setPaidBy(v)}
                  className="dark:bg-dark-900"
                />
              </div>

              {/* Participants */}
              <div>
                <MultiSelect
                  label="Participants"
                  options={memberMultiOptions}
                  defaultSelected={participants}
                  onChange={(values) => setParticipants(values)}
                />
                {!isParticipantsValid && (
                  <p className="mt-2 text-sm text-red-500">
                    Select at least one participant.
                  </p>
                )}
              </div>
            </div>
          </ComponentCard>

          <ComponentCard title="Split Method">
            <div className="space-y-5">
              <div className="flex flex-wrap items-center gap-8">
                <Radio
                  id="split-equal"
                  name="split"
                  value="EQUAL"
                  checked={splitMethod === "EQUAL"}
                  onChange={() => setSplitMethod("EQUAL")}
                  label="Split equally"
                />
                <Radio
                  id="split-amount"
                  name="split"
                  value="AMOUNT"
                  checked={splitMethod === "AMOUNT"}
                  onChange={() => setSplitMethod("AMOUNT")}
                  label="By amount"
                />
              </div>

              {/* Dynamic fields */}
              {splitMethod === "AMOUNT" && (
                <div className="space-y-4">
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Enter amount for each participant.
                  </p>

                  <div className="space-y-3">
                    {selectedParticipants.map((p) => (
                      <div
                        key={p.id}
                        className="grid grid-cols-1 gap-3 sm:grid-cols-3 sm:items-center"
                      >
                        <p className="text-sm font-medium text-gray-800 dark:text-white/90">
                          {p.name}
                        </p>

                        <div className="sm:col-span-2">
                          <Input
                            type="number"
                            placeholder="0"
                            value={
                              splitValues[String(p.id)] !== undefined
                                ? String(splitValues[String(p.id)])
                                : ""
                            }
                            onChange={(e) =>
                              setSplitValue(p.id, e.target.value)
                            }
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </ComponentCard>
        </div>

        {/* RIGHT: Optional */}
        <div className="space-y-6">
          <ComponentCard title="Receipt Upload">
            <DropzoneComponent />
          </ComponentCard>

          <ComponentCard title="Notes (Optional)">
            <div>
              <Label>Notes</Label>
              <TextArea value={note} onChange={(v) => setNote(v)} rows={6} />
            </div>
          </ComponentCard>

          <ComponentCard title="Actions">
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={handleSubmit}
                disabled={!canSubmit}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isLoading ? "Creating..." : "Create Expense"}
              </button>

              <button
                type="button"
                onClick={() => {
                  setTitle("");
                  setAmount("");
                  setCurrency("AUD");
                  setDate(today);
                  setCategory("FOOD");
                  setPaidBy(String(members[0]?.id ?? 1));
                     setParticipants(members.map((m) => String(m.id)));
                  setSplitMethod("EQUAL");
                  setSplitValues({});
                  setNote("");
                  setErrorMessage(null);
                  setSuccessMessage(null);
                }}
                className="inline-flex items-center justify-center rounded-lg border border-gray-200 bg-white px-5 py-2.5 text-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-200 dark:hover:bg-white/[0.03]"
              >
                Reset
              </button>
            </div>

            {/* Success Message */}
            {successMessage && (
              <div className="mt-4 rounded-lg bg-green-50 p-4 text-sm text-green-700 dark:bg-green-900/20 dark:text-green-400">
                {successMessage}
              </div>
            )}

            {/* Error Message */}
            {errorMessage && (
              <div className="mt-4 rounded-lg bg-red-50 p-4 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400">
                {errorMessage}
              </div>
            )}
          </ComponentCard>
        </div>
      </div>
    </div>
  );
}
