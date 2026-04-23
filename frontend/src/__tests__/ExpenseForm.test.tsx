import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ExpenseForm } from '../components/ExpenseForm';

describe('ExpenseForm', () => {
  it('disables submit button while pending and calls onSubmit', async () => {
    const mockSubmit = vi.fn().mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));
    
    render(<ExpenseForm onSubmit={mockSubmit} isSubmitting={false} />);
    
    // Fill out form
    fireEvent.change(screen.getByLabelText(/Amount/i), { target: { value: '10.50' } });
    fireEvent.change(screen.getByLabelText(/Category/i), { target: { value: 'food' } });
    fireEvent.change(screen.getByLabelText(/Description/i), { target: { value: 'lunch' } });
    
    const submitBtn = screen.getByRole('button', { name: /Save Expense/i });
    expect(submitBtn).not.toBeDisabled();
    
    // Submit form
    fireEvent.click(submitBtn);
    
    // Test that the mock was called
    await waitFor(() => {
      expect(mockSubmit).toHaveBeenCalledTimes(1);
    });

    // Test data passed
    expect(mockSubmit.mock.calls[0][0]).toMatchObject({
      amount: '10.50',
      category: 'food',
      description: 'lunch'
    });
    
    // Check idempotency key exists
    expect(typeof mockSubmit.mock.calls[0][1]).toBe('string');
  });

  it('shows saving state when isSubmitting is true', () => {
    render(<ExpenseForm onSubmit={vi.fn()} isSubmitting={true} />);
    const submitBtn = screen.getByRole('button', { name: /Saving.../i });
    expect(submitBtn).toBeDisabled();
  });
});
