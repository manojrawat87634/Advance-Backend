import React, { useContext, useEffect, useState } from "react";
import { DataContext } from "../../context";
import { Search, ShoppingBag, CheckCircle, Tag, ArrowUpDown } from "lucide-react";

const Product = () => {
  const { apiGet } = useContext(DataContext);
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filter & Sort State
  const [searchTerm, setSearchTerm] = useState("");
  const [sortBy, setSortBy] = useState("default");

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        // Replace with your actual endpoint path
        const response = await apiGet("/api/v1/items", {}, setData); 
        
        // Handle direct array or wrapped response structure
        const result = response?.data || response;
        setData(Array.isArray(result) ? result : []);
      } catch (err) {
        console.error("Failed to fetch products:", err);
        setError("Failed to load products. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [apiGet]);

  // Handle Loading state
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="flex items-center gap-3">
          <div className="w-6 h-6 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
          <h1 className="text-lg font-medium text-slate-700">Loading Products...</h1>
        </div>
      </div>
    );
  }

  // Handle Error state
  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="text-center p-6 bg-white rounded-xl border border-red-200 shadow-sm">
          <p className="text-red-600 font-medium">{error}</p>
        </div>
      </div>
    );
  }

  // Filter and Sort Logic
  const filteredProducts = data
    .filter((item) =>
      item.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.description?.toLowerCase().includes(searchTerm.toLowerCase())
    )
    .sort((a, b) => {
      if (sortBy === "low-to-high") return a.priceInRupees - b.priceInRupees;
      if (sortBy === "high-to-low") return b.priceInRupees - a.priceInRupees;
      return 0;
    });

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 sm:px-6 lg:px-8 font-sans">
      <div className="max-w-7xl mx-auto">
        
        {/* Header Section */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between border-b border-slate-200 pb-6 mb-8 gap-4">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
              <ShoppingBag className="w-8 h-8 text-indigo-600" />
              Product Catalog
            </h1>
            <p className="text-slate-500 mt-1 text-sm">
              Showing {filteredProducts.length} active products.
            </p>
          </div>

          {/* Search & Sort Controls */}
          <div className="flex flex-col sm:flex-row gap-3">
            {/* Search Bar */}
            <div className="relative flex-1 sm:w-64">
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                placeholder="Search products..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-9 pr-4 py-2 bg-white border border-slate-300 rounded-lg text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all shadow-sm"
              />
            </div>

            {/* Price Sorting */}
            <div className="relative sm:w-48">
              <ArrowUpDown className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="w-full pl-9 pr-8 py-2 bg-white border border-slate-300 rounded-lg text-sm text-slate-800 appearance-none focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all shadow-sm cursor-pointer"
              >
                <option value="default">Sort by: Default</option>
                <option value="low-to-high">Price: Low to High</option>
                <option value="high-to-low">Price: High to Low</option>
              </select>
            </div>
          </div>
        </div>

        {/* Grid List */}
        {filteredProducts.length === 0 ? (
          <div className="text-center py-16 bg-white rounded-xl border border-dashed border-slate-300">
            <p className="text-slate-500 text-lg">No products found</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filteredProducts.map((item) => (
              <div
                key={item.id}
                className="bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow duration-200 flex flex-col justify-between overflow-hidden group"
              >
                <div className="p-5">
                  <div className="flex items-center justify-between mb-3">
                    <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                      <CheckCircle className="w-3.5 h-3.5" />
                      In Stock
                    </span>
                    <span className="text-xs font-mono text-slate-400">#{item.id}</span>
                  </div>

                  <h3 className="text-lg font-semibold text-slate-900 group-hover:text-indigo-600 transition-colors line-clamp-1">
                    {item.name}
                  </h3>

                  <p className="text-sm text-slate-600 mt-2 line-clamp-2 leading-relaxed">
                    {item.description || "No description provided."}
                  </p>
                </div>

                <div className="bg-slate-50 px-5 py-4 border-t border-slate-100 flex items-baseline justify-between">
                  <div>
                    <span className="text-xs text-slate-400 block font-medium">PRICE</span>
                    <span className="text-2xl font-bold text-slate-900">
                      ₹{item.priceInRupees?.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                    </span>
                  </div>

                  <span className="text-xs font-mono text-slate-400 flex items-center gap-1">
                    <Tag className="w-3 h-3" />
                    {item.priceInPaise} paise
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}

      </div>
    </div>
  );
};

export default Product;