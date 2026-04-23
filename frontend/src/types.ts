export interface Expense {
  id: string;
  amount: string;
  category: string;
  description: string;
  date: string;
  createdAt: string;
}

export interface CreateExpenseRequest {
  amount: string;
  category: string;
  description: string;
  date: string;
}

export interface ExpensesListResponse {
  items: Expense[];
  total: string;
}
