interface TotalBarProps {
  total: string;
}

export function TotalBar({ total }: TotalBarProps) {
  return (
    <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex justify-between items-center">
      <span className="text-gray-600 font-medium">Total Amount</span>
      <span className="text-2xl font-bold text-gray-900">
        ${parseFloat(total || '0').toFixed(2)}
      </span>
    </div>
  );
}
