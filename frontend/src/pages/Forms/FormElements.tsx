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

type SplitMethod = "EQUAL" | "PERCENT" | "AMOUNT" | "SHARES";

export default function AddExpense() {
  
  const members = useMemo(
    () => [
      { id: "u1", name: "Dung" },
      { id: "u2", name: "Hieu" },
      { id: "u3", name: "Phong" },
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
    () => members.map((m) => ({ value: m.id, label: m.name })),
    [members]
  );

  const memberMultiOptions = useMemo(
    () =>
      members.map((m) => ({
        value: m.id,
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
  const [paidBy, setPaidBy] = useState(members[0]?.id ?? "");
  const [participants, setParticipants] = useState<string[]>(
    members.map((m) => m.id) // default chọn hết
  );
  const [splitMethod, setSplitMethod] = useState<SplitMethod>("EQUAL");
  const [note, setNote] = useState("");

  // Dynamic split values theo participant
  const [splitValues, setSplitValues] = useState<Record<string, number>>({});

  const selectedParticipants = useMemo(
    () => members.filter((m) => participants.includes(m.id)),
    [members, participants]
  );

  const setSplitValue = (userId: string, value: string) => {
    const num = Number(value);
    setSplitValues((prev) => ({
      ...prev,
      [userId]: Number.isFinite(num) ? num : 0,
    }));
  };

  // Basic validation
  const amountNumber = Number(amount);
  const isAmountValid = Number.isFinite(amountNumber) && amountNumber > 0;
  const isTitleValid = title.trim().length > 0;
  const isParticipantsValid = participants.length > 0;

  const canSubmit = isTitleValid && isAmountValid && isParticipantsValid;

  const handleSubmit = () => {
    if (!canSubmit) return;

    const payload = {
      title: title.trim(),
      amount: amountNumber,
      currency,
      date,
      category,
      paidBy,
      participants,
      splitMethod,
      splitValues: splitMethod === "EQUAL" ? {} : splitValues,
      note: note.trim(),
    };

    console.log("Create expense payload:", payload);
    // TODO: call API createExpense(payload)
  };

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
                  id="split-percent"
                  name="split"
                  value="PERCENT"
                  checked={splitMethod === "PERCENT"}
                  onChange={() => setSplitMethod("PERCENT")}
                  label="By percentage"
                />
                <Radio
                  id="split-amount"
                  name="split"
                  value="AMOUNT"
                  checked={splitMethod === "AMOUNT"}
                  onChange={() => setSplitMethod("AMOUNT")}
                  label="By amount"
                />
                <Radio
                  id="split-shares"
                  name="split"
                  value="SHARES"
                  checked={splitMethod === "SHARES"}
                  onChange={() => setSplitMethod("SHARES")}
                  label="By shares"
                />
              </div>

              {/* Dynamic fields */}
              {splitMethod !== "EQUAL" && (
                <div className="space-y-4">
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Enter {splitMethod === "PERCENT"
                      ? "percentage (%)"
                      : splitMethod === "AMOUNT"
                      ? "amount"
                      : "shares"}{" "}
                    for each participant.
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
                            placeholder={
                              splitMethod === "PERCENT"
                                ? "0 - 100"
                                : splitMethod === "AMOUNT"
                                ? "0"
                                : "1"
                            }
                            value={
                              splitValues[p.id] !== undefined
                                ? String(splitValues[p.id])
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
                Create Expense
              </button>

              <button
                type="button"
                onClick={() => {
                  setTitle("");
                  setAmount("");
                  setCurrency("AUD");
                  setDate(today);
                  setCategory("FOOD");
                  setPaidBy(members[0]?.id ?? "");
                  setParticipants(members.map((m) => m.id));
                  setSplitMethod("EQUAL");
                  setSplitValues({});
                  setNote("");
                }}
                className="inline-flex items-center justify-center rounded-lg border border-gray-200 bg-white px-5 py-2.5 text-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-200 dark:hover:bg-white/[0.03]"
              >
                Reset
              </button>
            </div>
          </ComponentCard>
        </div>
      </div>
    </div>
  );
}
