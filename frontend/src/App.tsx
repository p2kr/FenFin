import { useState, useEffect, useMemo } from 'react';
import { api } from './api';
import { CreateExpenseRequest, Expense } from './types';
import { ExpenseForm } from './components/ExpenseForm';
import { ExpenseTable } from './components/ExpenseTable';
import { CategoryFilter } from './components/CategoryFilter';
import { TotalBar } from './components/TotalBar';

function App() {
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [total, setTotal] = useState('0.00');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [selectedCategory, setSelectedCategory] = useState('');
  const [sort, setSort] = useState('date_desc');

  const fetchExpenses = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await api.getExpenses(selectedCategory, sort);
      setExpenses(data.items);
      setTotal(data.total);
    } catch (err) {
      setError('Failed to load expenses. Please try again.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchExpenses();
  }, [selectedCategory, sort]);

  const handleCreateExpense = async (data: CreateExpenseRequest, idempotencyKey: string) => {
    setIsSubmitting(true);
    setError(null);
    try {
      await api.createExpense(idempotencyKey, data);
      await fetchExpenses();
    } catch (err: any) {
      setError(err.message || 'Failed to create expense');
      throw err; // rethrow so form knows not to reset
    } finally {
      setIsSubmitting(false);
    }
  };

  const categories = useMemo(() => {
    const cats = new Set(expenses.map(e => e.category));
    return Array.from(cats).sort();
  }, [expenses]);

  return (
    <div className="min-h-screen max-w-4xl mx-auto p-4 md:p-8">
      <header className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Fenmo Finance</h1>
        <p className="text-gray-500 mt-1">Track your personal expenses</p>
      </header>

      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded-md shadow-sm">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1">
          <ExpenseForm onSubmit={handleCreateExpense} isSubmitting={isSubmitting} />
        </div>
        
        <div className="lg:col-span-2 space-y-6">
          <TotalBar total={total} />
          
          <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 space-y-4">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <CategoryFilter 
                categories={categories} 
                selectedCategory={selectedCategory} 
                onSelectCategory={setSelectedCategory} 
              />
              <div className="flex items-center space-x-2">
                <span className="text-sm font-medium text-gray-700">Sort:</span>
                <select 
                  value={sort}
                  onChange={(e) => setSort(e.target.value)}
                  className="text-sm border-gray-300 rounded-md focus:ring-primary-500 focus:border-primary-500"
                >
                  <option value="date_desc">Newest First</option>
                  <option value="date_asc">Oldest First</option>
                </select>
              </div>
            </div>
            
            <ExpenseTable expenses={expenses} isLoading={isLoading} />
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
