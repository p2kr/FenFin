import { CreateExpenseRequest, Expense, ExpensesListResponse } from './types';

const API_BASE = '/api/expenses';

// Simple UUID v4 generator for idempotency keys
export function generateIdempotencyKey(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0,
      v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

// Wrapper to handle retries and JSON parsing
async function fetchWithRetry(url: string, options: RequestInit, retries = 3): Promise<Response> {
  let attempt = 0;
  while (attempt < retries) {
    try {
      const response = await fetch(url, options);
      // Don't retry on 4xx errors
      if (response.ok || (response.status >= 400 && response.status < 500)) {
        return response;
      }
    } catch (error) {
      if (attempt === retries - 1) throw error;
    }
    attempt++;
    // Exponential backoff
    await new Promise((resolve) => setTimeout(resolve, 500 * Math.pow(2, attempt)));
  }
  throw new Error('Max retries reached');
}

export const api = {
  getExpenses: async (category?: string, sort = 'date_desc'): Promise<ExpensesListResponse> => {
    const params = new URLSearchParams();
    if (category) params.append('category', category);
    params.append('sort', sort);

    const res = await fetchWithRetry(`${API_BASE}?${params.toString()}`, {
      method: 'GET',
    });
    
    if (!res.ok) {
        throw new Error('Failed to fetch expenses');
    }
    
    return res.json();
  },

  createExpense: async (key: string, data: CreateExpenseRequest): Promise<Expense> => {
    const res = await fetchWithRetry(API_BASE, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': key,
      },
      body: JSON.stringify(data),
    });

    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw new Error(errBody.error || errBody.amount || 'Failed to create expense');
    }

    return res.json();
  },
};
