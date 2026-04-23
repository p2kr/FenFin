interface CategoryFilterProps {
  categories: string[];
  selectedCategory: string;
  onSelectCategory: (category: string) => void;
}

export function CategoryFilter({ categories, selectedCategory, onSelectCategory }: CategoryFilterProps) {
  return (
    <div className="flex items-center space-x-2 overflow-x-auto pb-2">
      <span className="text-sm font-medium text-gray-700 whitespace-nowrap">Filter by:</span>
      <button
        onClick={() => onSelectCategory('')}
        className={`px-3 py-1 rounded-full text-sm whitespace-nowrap transition-colors ${
          selectedCategory === ''
            ? 'bg-gray-800 text-white'
            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
        }`}
      >
        All
      </button>
      {categories.map((cat) => (
        <button
          key={cat}
          onClick={() => onSelectCategory(cat)}
          className={`px-3 py-1 rounded-full text-sm whitespace-nowrap transition-colors ${
            selectedCategory === cat
              ? 'bg-primary-600 text-white'
              : 'bg-primary-50 text-primary-700 hover:bg-primary-100'
          }`}
        >
          {cat}
        </button>
      ))}
    </div>
  );
}
