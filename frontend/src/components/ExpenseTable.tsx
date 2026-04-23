import { Expense } from '../types';

interface ExpenseTableProps {
  expenses: Expense[];
  isLoading: boolean;
}

export function ExpenseTable({ expenses, isLoading }: ExpenseTableProps) {
  if (isLoading) {
    return <div className="text-center py-8 text-gray-500 animate-pulse">Loading expenses...</div>;
  }

  if (expenses.length === 0) {
    return <div className="text-center py-8 text-gray-500 bg-white rounded-lg border border-gray-100">No expenses found.</div>;
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 border-b border-gray-100 text-gray-600">
            <tr>
              <th className="px-4 py-3 font-medium">Date</th>
              <th className="px-4 py-3 font-medium">Description</th>
              <th className="px-4 py-3 font-medium">Category</th>
              <th className="px-4 py-3 font-medium text-right">Amount</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {expenses.map((expense) => (
              <tr key={expense.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3 whitespace-nowrap text-gray-500">{expense.date}</td>
                <td className="px-4 py-3 text-gray-900">{expense.description}</td>
                <td className="px-4 py-3">
                  <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-primary-100 text-primary-700">
                    {expense.category}
                  </span>
                </td>
                <td className="px-4 py-3 text-right font-medium text-gray-900">
                  ${parseFloat(expense.amount).toFixed(2)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
